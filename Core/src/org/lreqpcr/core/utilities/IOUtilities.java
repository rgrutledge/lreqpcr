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
package org.lreqpcr.core.utilities;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.lreqpcr.core.database_services.SettingsServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Static utilities for data input/output
 *
 * @author Bob Rutledge
 */
public class IOUtilities {

    /**
     * Presents a file chooser dialog for selection of a generic file.
     *
     * @return the selected file, or null if selection is canceled.
     */
    public static File selectFile() {
        return selectFile(null);
    }

    /**
     * Presents a file chooser dialog for selection of a generic file.
     *
     * @param directory the directory to be opened.
     * @return the selected file, or null if selection is canceled.
     */
    public static File selectFile(File directory) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(directory);
        int returnVal = fc.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }

    /**
     * Converts the selected file into a byte array
     *
     * @param file the file to be read
     * @return the byte array, or null if it could not be read
     */
    public static byte[] dataFileImport(File file) {
        byte[] byteFile;
        FileInputStream in;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The datafile could not be opened...";
            JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Error reading datafile",
                    JOptionPane.OK_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        byteFile = new byte[(int) file.length()];
        int c;
        int i = 0;
        try {
            while ((c = in.read()) != -1) {
                byteFile[i] = (byte) c;
                i++;
            }

        } catch (IOException ex) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The datafile could not be read...";
            JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Error reading datafile",
                    JOptionPane.OK_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return byteFile;
    }

    /**
     * Presents a file chooser dialog for creating a new xls file
     *
     * @return the new xls file
     */
    public static File newExcelFile() {
        SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);
        File directory;
        try {
            directory = new File(settingsDB.getLastExperimentDatabaseDirectory());
        } catch (NullPointerException ev) {
            directory = null;
        }
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(directory);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Excel file", "xls");
        fc.setFileFilter(filter);
        int returnVal = fc.showDialog(
                WindowManager.getDefault().getMainWindow(),
                "New Excel File");
        File selectedFile = null;
        while (returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            String fileName = selectedFile.getAbsolutePath();
            if (!fileName.endsWith(".xls")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
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
                Toolkit.getDefaultToolkit().beep();
                String msg = "The file '" + selectedFile.getName()
                        + "' already exists. Do you want to replace the existing file?";
                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                        msg, "Overwrite File?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (n != JOptionPane.YES_OPTION) {
                    returnVal = fc.showDialog(null, "New");
                    continue;
                }
            }
            break;
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
            return selectedFile;
        } else {
            return null;
        }
    }

    /**
     * Returns a user selected xls file.
     *
     * @param title the file chooser title
     * @return the selected xls file, or null if it does not exist
     */
    public static File openImportExcelFile(String title) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Excel workbook", "xls", "xlsx");
        return createFileChooser(title, filter);
    }
    
     /**
     * Returns a user selected XML file. This is used for importing RDML
     * data (www.rdml.org).
     *
     * @param title the file chooser title
     * @return the selected xml file, or null if it does not exist
     */
    public static File openXmlFile(String title){
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "RDML file", "xml");
        return createFileChooser(title, filter);
    }
    
    /**
     * Presents a file chooser dialog to the user for selecting a preexisting file 
     * based on the FileNameExtensionFilter. If
     * the file does not exist null is returned
     * @param title
     * @param filter
     * @return 
     */
    private static File createFileChooser(String title, FileNameExtensionFilter filter){
        SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);
        JFileChooser fc = new JFileChooser();
        if (title != null) {
            fc.setDialogTitle(title);
        }
        fc.setFileFilter(filter);
        File directory;
        if (settingsDB.getLastDataImportDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastDataImportDirectory());
            } catch (NullPointerException ev) {
                directory = null;
            }
            fc.setCurrentDirectory(directory);
        }
        int returnVal = fc.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            settingsDB.setLastDataImportDirectory(fc.getCurrentDirectory().getAbsolutePath());
            //Check to see if the files exists. If not return null
            //This prevents creation of a new xls file
            if (!fc.getSelectedFile().exists()) {
                Toolkit.getDefaultToolkit().beep();
                String msg = "The datafile could not be read...";
                JOptionPane.showConfirmDialog(
                        WindowManager.getDefault().getMainWindow(),
                        msg,
                        "Error reading datafile",
                        JOptionPane.OK_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }
}
