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
package org.lreqpcr.run_initialization_provider;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import org.lreqpcr.data_import_services.AverageProfileGenerator;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.ExperimentDbInfo;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunInitializationService;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.util.Lookup;

/**
 * Processes RunImportData objects, storing the resulting Run and its profiles into
 * the appropriate database files. 
 *
 * @author Bob Rutledge
 */
public class RunInializationProvider implements RunInitializationService {

    private boolean isThisAManualDataImport;
    private DatabaseServices ampliconDB;
    private DatabaseServices calbnDB;
    private DatabaseServices experimentDB;

    @SuppressWarnings(value = "unchecked")
    public void intializeRun(RunImportData importData) {
        if (importData == null || importData.getRun() == null) {
            return;
        }
        isThisAManualDataImport = importData.isThisAManualDataImport();
        Run run = importData.getRun();
        List<SampleProfile> sampleProfileList = importData.getSampleProfileList();
        List<CalibrationProfile> calibnProfileList = importData.getCalibrationProfileList();
        //Try to retrieve the three database files which are in alphabetic order
        if (!getDatabases()) {//Continue??
            return;//Abort Run import
        }
//        if (dbArray == null) {
//            //The run import has been aborted
//            return;
//        }
//        DatabaseServices ampliconDB = dbArray[0];
//        DatabaseServices calbnDB = dbArray[1];
//        DatabaseServices experimentDB = dbArray[2];
        LreAnalysisService prfIntlz = Lookup.getDefault().lookup(LreAnalysisService.class);

//Process the SampleProfiles if an experiment database is open
        if (!sampleProfileList.isEmpty()) {
            if (experimentDB.isDatabaseOpen()) {
                LreWindowSelectionParameters winParameters = (LreWindowSelectionParameters) experimentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                ExperimentDbInfo dbInfo = (ExperimentDbInfo) experimentDB.getAllObjects(ExperimentDbInfo.class).get(0);
                double averageOCF = dbInfo.getOcf();
                for (Profile profile : sampleProfileList) {
                    profile.isProfileVer0_8_0(true);//Needed for back compatablity 
                    profile.setParent(run);
                    profile.setRun(run);//This is NOT redundant to setParent 
                    profile.setRunDate(run.getRunDate());
                    if (ampliconDB != null) {
                        if (ampliconDB.isDatabaseOpen()
                                && !profile.getAmpliconName().equals("")
                                && profile.getAmpliconSize() == 0) {//Prevents over writting when using manual Run import
                            RunImportUtilities.getAmpliconSize(ampliconDB, profile);
                        }
                    }
                    prfIntlz.initializeProfile(profile, winParameters);
                    if (profile.getStrCycleInt() != 0) {
                        profile.setOCF(averageOCF);
                        profile.updateProfile();
                    }
                    experimentDB.saveObject(profile);
                }
                List<? extends Profile> averageSampleProfileList =
                        AverageProfileGenerator.averageSampleProfileConstruction(sampleProfileList,
                        run,
                        averageOCF,
                        winParameters);
                if (averageSampleProfileList == null) {
                    return;
                }
                experimentDB.saveObject(averageSampleProfileList);
                run.setAverageProfileList((ArrayList<AverageSampleProfile>) averageSampleProfileList);
                //Deactivated due to a bug that can produce long delays during file import
//        RunImportUtilities.importCyclerDatafile(run);
                experimentDB.saveObject(sampleProfileList);
                experimentDB.saveObject(run);
                experimentDB.commitChanges();
                //This allows access to the newly imported Run
                UniversalLookup.getDefault().addSingleton(PanelMessages.NEW_RUN_IMPORTED, run);
                //Broadcast that a new Run has been added to the Experiment database
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.NEW_RUN_IMPORTED);
            }
        }

        //Process the CalibnProfileList
        if (!calibnProfileList.isEmpty()) {
            if (calbnDB.isDatabaseOpen()) {
                LreWindowSelectionParameters calbnParameters = (LreWindowSelectionParameters) calbnDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                //Process the CalibnProfiles
                for (Profile profile : calibnProfileList) {
                    profile.isProfileVer0_8_0(true);
                    profile.setRunDate(run.getRunDate());
                    if (ampliconDB != null) {
                        if (ampliconDB.isDatabaseOpen()
                                && !profile.getAmpliconName().equals("")
                                && profile.getAmpliconSize() == 0) {//Prevents over writting when using manual import
                            RunImportUtilities.getAmpliconSize(ampliconDB, profile);
                        }
                    }
                    prfIntlz.initializeProfile(profile, calbnParameters);
                    if (profile.getStrCycleInt() != 0) {
                        profile.updateProfile();
                    }
                    calbnDB.saveObject(profile);
                }
                //Process the AverageCalibnProfiles
                List<AverageCalibrationProfile> averageCalbnProfileList =
                        (List<AverageCalibrationProfile>) AverageProfileGenerator.averageCalbrationProfileConstruction(
                        calibnProfileList,
                        calbnParameters);
                calbnDB.saveObject(averageCalbnProfileList);
                calbnDB.commitChanges();
                //Broadcast that the calibration panels must be updated
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
            }
        }
    }
    
    /**
     * Retrieves the three LRE databases via universal lookup.
     * This version assumes only one database file is open for each of the database
     * types.
     *
     * @return yes if to continue with the Run import or false to abandon the import
     */
    @SuppressWarnings(value = "unchecked")
    public boolean getDatabases() {
        UniversalLookup uLookup = UniversalLookup.getDefault();
        //This assumes only one database file is open for each database type

        //Check if the necessary database services have active databases
        //This is done via the universal lookup which stores database service instances as a list
        //associated with a key, which here is defined by the enum DatabaseType
        if (uLookup.containsKey(DatabaseType.EXPERIMENT)) {
            //Assumes that only one of each database will be open...this will have to be modified
            //if multiple database files are implemented
            experimentDB = (DatabaseServices) uLookup.getAll(DatabaseType.EXPERIMENT).get(0);
            if (!experimentDB.isDatabaseOpen() && !isThisAManualDataImport) {
                //Provide the ability to continue Run import without a exptDB
                Toolkit.getDefaultToolkit().beep();
                String msg = "Experiment database not available"
                        + "Do you want to continue with the data import?";
                boolean yes = RunImportUtilities.requestYesNoAnswer("Experiment database not available?",
                        msg);
                if (!yes) {
                    return false;
                }
//                
//                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, "Calibration database not available. ",
//                        JOptionPane.YES_NO_OPTION);
//                if (n != JOptionPane.YES_OPTION) {
//                    return null;//Abort the Run import
//                }
//            }else{//No experiment database service is available
//                //This type of error should be handled by the Database Window, not here
            }
        }

        if (uLookup.containsKey(DatabaseType.CALIBRATION)) {
            calbnDB = (DatabaseServices) uLookup.getAll(DatabaseType.CALIBRATION).get(0);
            if (!calbnDB.isDatabaseOpen() && !isThisAManualDataImport) {
                //Provide the ability to continue Run import without a calbnDB
                Toolkit.getDefaultToolkit().beep();
                String msg = "A Calibration database has not been opened. "
                        + "Do you want to continue with the data import?";
                boolean yes = RunImportUtilities.requestYesNoAnswer("Calibration database not available?",
                        msg);
                if (!yes) {
                    return false;
                }
//                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, "Calibration database not available. ",
//                        JOptionPane.YES_NO_OPTION);
//                if (n != JOptionPane.YES_OPTION) {
//                    return null;//Abort the Run import
//                }
            }
        }
        if (uLookup.containsKey(DatabaseType.AMPLICON)) {
            ampliconDB = (DatabaseServices) uLookup.getAll(DatabaseType.AMPLICON).get(0);
            //Provide the ability to continue Run import without a calbnDB
            if (!ampliconDB.isDatabaseOpen() && !isThisAManualDataImport) {
                Toolkit.getDefaultToolkit().beep();
                String msg = "An Amplicon database has not been opened. "
                        + "Do you want to continue with the data import?";
                boolean yes = RunImportUtilities.requestYesNoAnswer("Amplicon database not available?",
                        msg);
                if (!yes) {
                    return false;
                }
//                int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, "Amplicon database not available. ",
//                        JOptionPane.YES_NO_OPTION);
//                if (n != JOptionPane.YES_OPTION) {
//                    return null;//Abort the Run import
//                }
//            }
//        } else {//No amplicon database service is available
//            //This type of error should be handled by the Database Window, not here
            }
        }
        return true;
    }
}
