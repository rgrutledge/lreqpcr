/*
 * Copyright (C) 2010  Bob Rutledge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * and open the template in the editor.
 */
package org.lreqpcr.core.db4o_provider;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Bob Rutledge
 */
public class ExperimentDb4oServiceProvider extends Db4oServices {
//    implements DatabaseServices {

    private SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);

    public ExperimentDb4oServiceProvider() {
    }

    public boolean createNewDatabase() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Experiment database files", "exp");
        fc.setFileFilter(filter);
        fc.setDialogTitle("New Experiment Database");
        File directory = null;
        if (settingsDB.getLastExperimentDatabaseDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastExperimentDatabaseDirectory());
            } catch (NullPointerException ev) {
                directory = null;
            }
            fc.setCurrentDirectory(directory);
        }
        int returnVal = fc.showDialog(WindowManager.getDefault().getMainWindow(), "New Expt DB");
        File selectedFile = null;
        while (returnVal == JFileChooser.APPROVE_OPTION) {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
                selectedFile = fc.getSelectedFile();
                String fileName = selectedFile.getAbsolutePath();
                if (!fileName.endsWith(".exp")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".exp");
                }
                //Check to see if another file with the same name exsists
                File[] fileListing = fc.getCurrentDirectory().listFiles();
                boolean isDuplicate = false;
                for (File file : fileListing) {
                    if (file.equals(selectedFile)) {
                        isDuplicate = true;
                    }
                }
                if (isDuplicate) {
                    String msg = "The file '" + selectedFile.getName()
                            + "' already exists. Please enter a unique file name";
                    int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                            msg,
                            "File Exsists",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (n == JOptionPane.OK_OPTION) {
                        returnVal = fc.showDialog(null, "New Expt DB");
                        continue;
                    } else {
                        return false;//Abort new file creation
                    }
                }
            }
            break;
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            //Save the previously open file location to the settings database
            if (getDatabaseFile() != null) {
                settingsDB.setLastExperimentDatabaseFile(getDatabaseFile());
            }
            settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
            openDatabase(selectedFile);
            return true;
        }
        return false;
    }

    public boolean openDatabase() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Experiment database files", "exp");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open Experiment Database");
        //This allows the last directory to be retrieved
        File directory = null;
        if (settingsDB.getLastExperimentDatabaseDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastExperimentDatabaseDirectory());
            } catch (NullPointerException ev) {
                directory = null;
            }
            fc.setCurrentDirectory(directory);
        }
        int returnVal = fc.showDialog(WindowManager.getDefault().getMainWindow(), "Open Expt DB");
        if (returnVal == JFileChooser.APPROVE_OPTION && fc.getSelectedFile().exists()) {
            if (getDatabaseFile() != null) {
                settingsDB.setLastExperimentDatabaseFile(getDatabaseFile());
            }
            settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
            openDatabase(fc.getSelectedFile());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void closeDatabase() {
        //Save the previously open file location to the settings database
        if (getDatabaseFile() != null) {
            settingsDB.setLastExperimentDatabaseFile(getDatabaseFile());
        }
        super.closeDatabase();
    }

    public DatabaseType getDatabaseType() {
        return DatabaseType.EXPERIMENT;
    }
}
