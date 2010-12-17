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
package org.lreqpcr.data_import_services;

import java.awt.Toolkit;
import org.lreqpcr.core.utilities.*;
import org.lreqpcr.core.data_objects.AmpliconImpl;
import org.lreqpcr.core.data_objects.Profile;
import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.swing.JOptionPane;
import jxl.DateCell;
import org.lreqpcr.core.data_objects.RunDatafileStorage;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Static methods that provide various data import utility functions.
 * @author Bob Rutledge
 */
public class RunImportUtilities {

    /**
     * A hack necessary to extract the correct date
     * from a JExcel DateCell. This is a known issue with JExcel.
     *
     * @param dateCell JExcel DateCell
     * @return the date with time set to 0:00
     */
    public static Date importExcelDate(DateCell dateCell) {
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        df.setTimeZone(gmtZone);
        String dateString = df.format(dateCell.getDate());
        Date runDate = df.parse(dateString, new ParsePosition(0));
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(runDate);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * Retrieves the amplicon size from the supplied amplicon database. 
     * Amplicon name is retrieved from the supplied profile, which is
     * is the name of the amplicon.
     * @param ampliconDB the amplicon database holding this amplicon
     * @param profile the profile generated by the amplicon
     */
    public static void getAmpliconSize(DatabaseServices ampliconDB, Profile profile) {
        List ampliconList = ampliconDB.retrieveUsingFieldValue(AmpliconImpl.class, "name",
                profile.getAmpliconName());
        if (ampliconList.size() > 1) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "There is more than one amplicon with the name '" + profile.getAmpliconName()
                    + "'. The amplicon size will be taken from the first amplicon found";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg,
                    "Amplicon name is not unique", JOptionPane.ERROR_MESSAGE);
        }
        if (!ampliconList.isEmpty()) {
            AmpliconImpl amplicon = (AmpliconImpl) ampliconList.get(0);
            profile.setAmpliconSize(amplicon.getAmpliconSize());
        }
    }

    /**
     * Generates a generic yes/no question dialog.
     * @param title dialog title
     * @param question the dialog question
     * @return the response
     */
    public static boolean requestYesNoAnswer(String title, String question) {
        Toolkit.getDefaultToolkit().beep();
        String msg = question;
        int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg,
                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (n == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Stores the import file and its name.
     * @param dataStorageObject the data object holding the files
     * @param importFile the import file
     */
    public static void importExcelImportFile(RunDatafileStorage dataStorageObject, File importFile) {
        byte[] byteFile = IOUtilities.dataFileImport(importFile);
        if (byteFile != null) {         
            dataStorageObject.setImportDataFile(byteFile);
            dataStorageObject.setImportDataFileName(importFile.getName());
        } else {
            // TODO present an error dialog
        }
    }

    /**
     * Retrieves and stores the instrument data file and its name.
     * @param dataStorageObject the data storage object
     */
    public static void importCyclerDatafile(RunDatafileStorage dataStorageObject) {
        //Get the last import directory
        SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);
        File directory = null;
        if (settingsDB.getLastCyclerDataImportDirectory() != null) {
            try {
                directory = new File(settingsDB.getLastCyclerDataImportDirectory());
            } catch (NullPointerException ev) {
                directory = null;
            }
        }
        if (requestYesNoAnswer("Would you like to save a copy of the cycler datafile?",
               "Save a copy of the datafile?" )) {
            File datafile = IOUtilities.selectFile(directory);
            if (datafile != null) {
                settingsDB.setLastCyclerDataImportDirectory(datafile.getParent());
                byte[] byteFile = IOUtilities.dataFileImport(datafile);
                if (byteFile != null) {
                    dataStorageObject.setRunDataFile(byteFile);
                    dataStorageObject.setCyclerDatafileName(datafile.getName());
                }
            }
        }
    }

    /**
     * Used to parse MXP well labels for Amplicon and Sample name.
     * Returns two Strings Amplicon and Sample name if a comma is present in
     * the well label, or a single String if a comma is absent.
     * 
     * @param label the well label
     * @return String[0] = Amplicon name String[1] = Sample name
     */
    public static String[] parseAmpSampleNames(String label) {
        String[] names = new String[2];
        if (!label.contains(",")) {
            //No names specified; consider it just a well label
            names[0] = label.trim();//Generic Sample name, what ever is present
            return names;
        }
        int commaIndex = label.indexOf(",");
        names[0] = label.substring(0, commaIndex).trim();
        names[1] = label.substring(commaIndex + 1).trim();//Jump the comma and trim spaces
        return names;
    }

    /**
     * Retrieves the three LRE databases via universal lookup.
     * This version assumes only one database file is open for each of the database
     * types. 
     * 
     * @return an array contain the database services in alphabetic order
     * (e.g. Amplicon DB, calibration DB, experiment DB)
     */
    @SuppressWarnings(value = "unchecked")
    public static DatabaseServices[] getDatabases() {
        UniversalLookup uLookup = UniversalLookup.getDefault();
        //This assumes only one database file is open for each database type
        DatabaseServices exptDB = null;
        DatabaseServices calbnDB = null;
        DatabaseServices ampliconDB = null;
        DatabaseServices[] dbArray = new DatabaseServices[3];//ampDB, calDB, exptDB (alphabetic order)

        //Check if the necessary database services have active databases
        //This is done via the universal lookup which stores database service intances as a list
        //associated with a key, which here is defined by the enum DatabaseType
        if (uLookup.containsKey(DatabaseType.EXPERIMENT)) {
            //Assumes that only one of each database will be open...this will have to be modified
            //when multiple database files are implemented
            exptDB = (DatabaseServices) uLookup.getAll(DatabaseType.EXPERIMENT).get(0);
            dbArray[2] = exptDB;
            if (!exptDB.isDatabaseOpen()) {
                //Provide the ability to continue Run import without a exptDB
                Toolkit.getDefaultToolkit().beep();
                String msg = "An Experiment database has not been opened. "
                        + "Do you want to continue with the data import?";
                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, "Calibration database not available. ",
                        JOptionPane.YES_NO_OPTION);
                if (n != JOptionPane.YES_OPTION) {
                    return null;//Abort the Run import
                }
            }else{//No experiment database service is available
                //This type of error should be handled by the Database Window, not here
            }
        }

        if (uLookup.containsKey(DatabaseType.CALIBRATION)) {
            calbnDB = (DatabaseServices) uLookup.getAll(DatabaseType.CALIBRATION).get(0);
            dbArray[1] = calbnDB;
            if (!calbnDB.isDatabaseOpen()) {
                //Provide the ability to continue Run import without a calbnDB
                Toolkit.getDefaultToolkit().beep();
                String msg = "A Calibration database has not been opened. "
                        + "Do you want to continue with the data import?";
                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, "Calibration database not available. ",
                        JOptionPane.YES_NO_OPTION);
                if (n != JOptionPane.YES_OPTION) {
                    return null;//Abort the Run import
                }
            }
        } else {//No calibration database service is available
            //This type of error should be handled by the Database Window, not here
        }

        if (uLookup.containsKey(DatabaseType.AMPLICON)) {
            ampliconDB = (DatabaseServices) uLookup.getAll(DatabaseType.AMPLICON).get(0);
            dbArray[0] = ampliconDB;
            //Provide the ability to continue Run import without a calbnDB
            if (!ampliconDB.isDatabaseOpen()) {
                Toolkit.getDefaultToolkit().beep();
                String msg = "An Amplicon database has not been opened. "
                        + "Do you want to continue with the data import?";
                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, "Amplicon database not available. ",
                        JOptionPane.YES_NO_OPTION);
                if (n != JOptionPane.YES_OPTION) {
                    return null;//Abort the Run import
                }
            }
        } else {//No amplicon database service is available
            //This type of error should be handled by the Database Window, not here
        }
        return dbArray;
    }

    /**
     * This is a hack that deals with determining target strandedness.
     * This is necessary due to the
     * fact that current run export methods do not provide target strandedness.
     * The approach taken here is to assume the most if not all targets within a
     * run are either single or double stranded. 
     * @return the target strandedness
     */
    public static TargetStrandedness isTheTargetSingleStranded() {
        Toolkit.getDefaultToolkit().beep();
        String msg = "Are the majority of the targets SINGLE STRANDED "
                + "(excluding the Lambda calibrator)? "
                + "\nNote that if this run contains a mix of ssDNA and dsDNA "
                + "targets, strandedness will "
                + "\nhave to be manually corrected via the Profile viewer";
        if (requestYesNoAnswer("Are the Targets single stranded", msg)){
             return TargetStrandedness.SINGLESTRANDED;
        }
        return TargetStrandedness.DOUBLESTRANDED;
    }
}
