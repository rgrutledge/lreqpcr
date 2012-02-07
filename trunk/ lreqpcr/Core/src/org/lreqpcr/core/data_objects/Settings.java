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
package org.lreqpcr.core.data_objects;

import java.io.File;
import javax.swing.JOptionPane;
import org.openide.windows.WindowManager;

/**
 * Stores program settings such as last database file.
 *
 * @author Bob Rutledge
 */
public class Settings {

    private String lastExperimentDirectory;
    private String lastExperimentDbFile;
    private String lastAmpliconDbFile;
    private String lastAmpliconDirectory;
    private String lastCalbnDirectory;
    private String lastCalbnDbFile;
    private String lastDataImportDirectory;
    private String lastCyclerDataImportDirectory;
    private String lastCyclerDataFile;

    public Settings() {
    }

    private void fileDoesNotExsist(File file) {
        String msg = "The database file \"" + file.getName()
                + "\" could not be opened. \n This is likely due to fact that it has been deleted or moved.";
        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "Unable to open database file",
                JOptionPane.ERROR_MESSAGE);
    }

    public String getLastExperimentDatabaseDirectory() {
        return lastExperimentDirectory;
    }

    public void setLastExperimentDatabaseDirectory(String lastDirectory) {
        if (lastDirectory != null) {
            this.lastExperimentDirectory = lastDirectory;
        }
    }

    public File getLastExperimentDatabaseFile() {
        if (lastExperimentDbFile != null) {
            File file = new File(lastExperimentDbFile);
            if (!file.exists()) {
                fileDoesNotExsist(file);
                return null;
            } else {
                return file;
            }
        }
        return null;
    }

    public void setLastExperimentDatabaseFile(File lastDatabaseFile) {
        if (lastDatabaseFile != null) {
            this.lastExperimentDbFile = lastDatabaseFile.getAbsolutePath();
        }
    }

    public File getLastAmpliconDatabaseFile() {
        if (lastAmpliconDbFile != null) {
            File file = new File(lastAmpliconDbFile);
            if (!file.exists()) {
                fileDoesNotExsist(file);
                return null;
            } else {
                return file;
            }
        }
        return null;
    }

    public void setLastAmpliconDatabaseFile(File lastAmpliconDbFile) {
        if (lastAmpliconDbFile != null) {
            this.lastAmpliconDbFile = lastAmpliconDbFile.getAbsolutePath();
        }
    }

    public String getLastAmpliconDatabaseDirectory() {
        return lastAmpliconDirectory;
    }

    public void setLastAmpliconDatabaseDirectory(String lastAmpliconDirectory) {
        if (lastAmpliconDirectory != null) {
            this.lastAmpliconDirectory = lastAmpliconDirectory;
        }
    }

    public void setLastCalibrationDatabaseDirectory(String directory) {
        if (directory != null) {
            lastCalbnDirectory = directory;
        }
    }

    public String getLastCalibrationDatabaseDirectory() {
        return lastCalbnDirectory;
    }

    public void setLastCalibrationDatabaseFile(File file) {
        if (file != null) {
            lastCalbnDbFile = file.getAbsolutePath();
        }
    }

    public File getLastCalibrationDatabaseFile() {
        if (lastCalbnDbFile != null) {
            File file = new File(lastCalbnDbFile);
            if (!file.exists()) {
                fileDoesNotExsist(file);
                return null;
            } else {
                return file;
            }
        }
        return null;
    }

    public String getLastDataImportDirectory() {
        return lastDataImportDirectory;
    }

    public void setLastDataImportDirectory(String directory) {
        if (directory != null) {
            lastDataImportDirectory = directory;
        }
    }

    public File getLastCyclerDataFile() {
        if (lastCyclerDataFile != null) {
            File file = new File(lastCyclerDataFile);
            if (!file.exists()) {
                fileDoesNotExsist(file);
                return null;
            } else {
                return file;
            }
        }
        return null;
    }

    public void setLastCyclerDataFile(File file) {
        if (file != null) {
            this.lastCyclerDataFile = file.getAbsolutePath();
        }
    }

    public String getLastCyclerDataImportDirectory() {
        return lastCyclerDataImportDirectory;
    }

    public void setLastCyclerDataImportDirectory(String lastCyclerDataImportDirectory) {
        if (lastCyclerDataImportDirectory != null) {
            this.lastCyclerDataImportDirectory = lastCyclerDataImportDirectory;
        }
    }
}
