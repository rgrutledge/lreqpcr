/*
 * Copyright (C) 2013   Bob Rutledge
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
import org.lreqpcr.core.data_objects.ExptDbInfo;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Bob Rutledge
 */
public class ExperimentDb4oDatabaseServiceProvider extends Db4oDatabaseServices {

    private SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);

    public ExperimentDb4oDatabaseServiceProvider() {
    }

    public boolean createNewDatabaseFile() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Experiment database files", "exp");
        fc.setFileFilter(filter);
        fc.setDialogTitle("New Experiment Database");
        File directory;
        if (settingsDB.getLastExperimentDatabaseDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastExperimentDatabaseDirectory());
            } catch (NullPointerException ev) {
                directory = null;
            }
            fc.setCurrentDirectory(directory);
        }
        int returnVal = fc.showDialog(WindowManager.getDefault().getMainWindow(), "New Expt DB");
        while (returnVal == JFileChooser.APPROVE_OPTION) {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File previousDatabaseFile = getDatabaseFile();
                settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
                File selectedFile = fc.getSelectedFile();
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
                        return createNewDatabaseFile();//Start again
                    } else {
                        return false;//Abort new file creation
                    }
                }
                if (openDatabaseFile(selectedFile)) {
                    saveObject(new LreWindowSelectionParameters());
                    saveObject(new ExptDbInfo());
                    commitChanges();
                    settingsDB.setLastExperimentDatabaseFile(previousDatabaseFile);
                    return true;
                }
            }
        }
        return false;//Default if true is not returned...
    }

    /**
     * Records the current database file as the last open database and 
     * then closes the database file.
     */
    @Override
    public boolean closeDatabase() {
        File previousDatabaseFile = getDatabaseFile();
        if (closeDb4oDatabase()) {
            settingsDB.setLastExperimentDatabaseFile(previousDatabaseFile);
            return true;
        }
        return false;
    }

    /**
     * Opens an Experiment database file as chosen by the user. Once a valid file
     * is selected, the current database file is closed and this new file is opened. 
     * If opened successfully, the old file is stored as the last database to be retrieved
     * 
     * @return true if a new database file is successfully opened, false if not
     */
    public boolean openUserSelectDatabaseFile() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Experiment database files", "exp");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open Experiment Database");
        //This allows the last directory to be retrieved
        File directory;
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
            //Close the current database file
            closeDatabase();//This stores the last exp database file location in the Settings database
            //Store the last exp database directory in settings
            settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
            return openDatabaseFile(fc.getSelectedFile());//Open the DB4O file via Db4oDatabaseServices;
        } else {
            return false;
        }
    }

    @Override
    public boolean openLastDatabaseFile() {
        //Opening a file resets the database file so it needs to be recorded
        File currentDatabaseFile = getDatabaseFile();
        if (openDatabaseFile(settingsDB.getLastExperimentDatabaseFile())) {
            settingsDB.setLastExperimentDatabaseFile(currentDatabaseFile);
            return true;
        }
        return false;
    }

    public DatabaseType getDatabaseType() {
        return DatabaseType.EXPERIMENT;
    }
}
