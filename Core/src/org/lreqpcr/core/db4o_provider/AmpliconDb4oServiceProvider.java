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

import org.lreqpcr.core.data_objects.AmpliconDbInfo;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class AmpliconDb4oServiceProvider extends Db4oDatabaseServices implements DatabaseServices {

    private SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);

    public AmpliconDb4oServiceProvider() {
    }

    public boolean createNewDatabaseFile() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Amplicon database files", "amp");
        fc.setFileFilter(filter);
        fc.setDialogTitle("New Amplicon Database");
        File directory;
        if (settingsDB.getLastAmpliconDatabaseDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastAmpliconDatabaseDirectory());
            }
            catch (NullPointerException ev) {
                directory = null;
            }
            fc.setCurrentDirectory(directory);
        }
        int returnVal = fc.showDialog(WindowManager.getDefault().getMainWindow(), "New Amp DB");
        File selectedFile;
        while (returnVal == JFileChooser.APPROVE_OPTION) {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File previousDatabaseFile = getDatabaseFile();
                settingsDB.setLastAmpliconDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
                selectedFile = fc.getSelectedFile();
                String fileName = selectedFile.getAbsolutePath();
                if (!fileName.endsWith(".amp")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".amp");
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
                    int n = JOptionPane.showConfirmDialog(
                        WindowManager.getDefault().getMainWindow(),
                        msg,
                        "File Exsists",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                    if (n == JOptionPane.OK_OPTION) {
                        return createNewDatabaseFile();//Start again
                    }
                    else {
                        return false;//Abort new file creation
                    }
                }
                if (openDatabaseFile(selectedFile)) {
                    saveObject(new AmpliconDbInfo());
                    commitChanges();
                    settingsDB.setLastAmpliconDatabaseFile(previousDatabaseFile);
                    return true;
                }
            }
        }
        return false;//Default if true is not returned...
    }

    @Override
    public boolean closeDatabase() {
        File previousDatabaseFile = getDatabaseFile();
        if (closeDb4oDatabase()) {
            settingsDB.setLastAmpliconDatabaseFile(previousDatabaseFile);
            return true;
        }
        return false;
    }

    public boolean openUserSelectDatabaseFile() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Amplicon database files", "amp");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open Amplicon Database");
        //This allows the last directory to be retrieved
        File directory;
        if (settingsDB.getLastAmpliconDatabaseDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastAmpliconDatabaseDirectory());
            }
            catch (NullPointerException ev) {
                directory = null;
            }
            fc.setCurrentDirectory(directory);
        }
        int returnVal = fc.showDialog(WindowManager.getDefault().getMainWindow(), "Open AmpliconDB");
        if (returnVal == JFileChooser.APPROVE_OPTION && fc.getSelectedFile().exists()) {
            File newDatabaseFile = fc.getSelectedFile();
            closeDatabase();
            settingsDB.setLastAmpliconDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
            return openDatabaseFile(newDatabaseFile);
        }
        else {
            return false;
        }
    }

    @Override
    public boolean openLastDatabaseFile() {
        //At startup, no database file will be open
        if (getDatabaseFile() == null) {
            if (openDatabaseFile(settingsDB.getLastAmpliconDatabaseFile())) {
                return true;
            }
            else {
                return false;
            }
        }
        //Opening a file resets the datafile to this new file
        File currentDatabaseFile = getDatabaseFile();
        if (openDatabaseFile(settingsDB.getLastAmpliconDatabaseFile())) {
            settingsDB.setLastAmpliconDatabaseFile(currentDatabaseFile);
            return true;
        }
        return false;
    }

    public DatabaseType getDatabaseType() {
        return DatabaseType.AMPLICON;
    }
}
