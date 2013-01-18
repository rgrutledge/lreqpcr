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
import javax.swing.JOptionPane;
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
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.data_import_services.AverageProfileGenerator;
import org.lreqpcr.data_import_services.DataImportType;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.data_import_services.RunInitializationService;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Processes RunImportData objects, storing the resulting Run and its profiles into
 * the appropriate database files. 
 *
 * @author Bob Rutledge
 */
public class RunInializationProvider implements RunInitializationService {

    private UniversalLookup uLookup = UniversalLookup.getDefault();
    private DatabaseServices ampliconDB;
    private DatabaseServices calbnDB;
    private DatabaseServices experimentDB;
    private DataImportType importType;

    public RunInializationProvider() {
        //Retrieve the databases
        //This assumes that only one database file is open for each database type
        //Thus the first entry is the active database for each 
        experimentDB = (DatabaseServices) uLookup.getAll(DatabaseType.EXPERIMENT).get(0);
        calbnDB = (DatabaseServices) uLookup.getAll(DatabaseType.CALIBRATION).get(0);
        ampliconDB = (DatabaseServices) uLookup.getAll(DatabaseType.AMPLICON).get(0);
    }

    /**
     * Initialized the Run using the supplied ImportData object
     * 
     * @param importData the ImportData which cannot be null
     */
    @SuppressWarnings(value = "unchecked")
    public void intializeRun(RunImportData importData) {
        if (importData == null){
            //Run import has been cancelled
            return;
        }
        importType = importData.getImportType();
        //This is obviously inefficient, but it is expected that data import will be limited
        //to very few types, with the manual data import being rare exceptions
        //Check for the necessary databases for each type of import format
        if (importType == DataImportType.STANDARD) {
            //All three databases will likely be needed
            if (!experimentDB.isDatabaseOpen()) {
                if (!experimentDatabaseNotOpen()) {
                    return;
                }
            }
            if (!calbnDB.isDatabaseOpen()) {
                if (!calibrationDatabaseNotOpen()) {
                    return;
                }
            }
            if (!ampliconDB.isDatabaseOpen()) {
                if (!ampliconDatabaseNotOpen()) {
                    return;
                }
            }
        }
        if (importType == DataImportType.MANUAL_SAMPLE_PROFILE) {
            //Absolutely need an active Experiment database
            if (!experimentDB.isDatabaseOpen()) {
                String msg = "An Experiment database is not open. \n"
                        + "Data import will be terminated.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "Experiment database not open",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (importType == DataImportType.MANUAL_CALIBRATION_PROFILE) {
            //Absolutely need an active Experiment database
            if (!calbnDB.isDatabaseOpen()) {
                String msg = "A Calibration database is not open. \n"
                        + "Data import will be terminated.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "No Calibration database is open",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Run run = importData.getRun();
        List<SampleProfile> sampleProfileList = importData.getSampleProfileList();
        List<CalibrationProfile> calibnProfileList = importData.getCalibrationProfileList();

        LreAnalysisService prfIntlz = Lookup.getDefault().lookup(LreAnalysisService.class);

//Process the SampleProfiles if an Experiment database is open
        if (sampleProfileList != null) {
            if (!sampleProfileList.isEmpty()) {//A manual Calibration Profile import type should have an empty SampleProfile list
                if (experimentDB.isDatabaseOpen()) {
                    LreWindowSelectionParameters winSelectionParameters =
                            (LreWindowSelectionParameters) experimentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    ExperimentDbInfo dbInfo = (ExperimentDbInfo) experimentDB.getAllObjects(ExperimentDbInfo.class).get(0);
                    double ocf = dbInfo.getOcf();
                    for (SampleProfile sampleProfile : sampleProfileList) {
                        // TODO This conversion should be moved to a dedicated module 
                        sampleProfile.setProfileToVer0_8_0(true);//Needed for back compatablity
                        if (ampliconDB != null) {
                            if (ampliconDB.isDatabaseOpen()
                                    && !sampleProfile.getAmpliconName().equals("")
                                    && sampleProfile.getAmpliconSize() == 0) {//Prevents over writting when using manual SampleProfile import
                                RunImportUtilities.getAmpliconSize(ampliconDB, sampleProfile);
                            }
                        }
                        //Initialize the new Profile which will conduct an automated LRE window selection
                        prfIntlz.conductAutomatedLreWindowSelection(sampleProfile, winSelectionParameters);
                        sampleProfile.setOCF(ocf);
                        experimentDB.saveObject(sampleProfile);
                    }
                    List<AverageSampleProfile> averageSampleProfileList =
                            AverageProfileGenerator.averageSampleProfileConstruction(
                            sampleProfileList,
                            run,
                            ocf,
                            winSelectionParameters);
                    experimentDB.saveObject(averageSampleProfileList);
                    run.setAverageProfileList((ArrayList<AverageSampleProfile>) averageSampleProfileList);
                    run.determineAverageFmax();
                    //Deactivated due to a bug that can produce long delays during file import
//        RunImportUtilities.importCyclerDatafile(run);
                    experimentDB.saveObject(run);
                    experimentDB.commitChanges();
                    //This allows access to the newly imported Run
                    UniversalLookup.getDefault().addSingleton(PanelMessages.NEW_RUN_IMPORTED, run);
                    //Broadcast that a new Run has been added to the Experiment database
                    UniversalLookup.getDefault().fireChangeEvent(PanelMessages.NEW_RUN_IMPORTED);
                }
            }
        }

        //Process the CalibnProfileList
        if (calibnProfileList != null) {
            if (!calibnProfileList.isEmpty()) {//A manual Sample Profile import should have an empty Calibration Profile list.
                if (calbnDB.isDatabaseOpen()) {
                    LreWindowSelectionParameters lreWindowSelectionParameters = (LreWindowSelectionParameters) calbnDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    //Process the CalibnProfiles
                    for (Profile profile : calibnProfileList) {
                        profile.setProfileToVer0_8_0(true);
                        if (ampliconDB != null) {
                            if (ampliconDB.isDatabaseOpen()
                                    && !profile.getAmpliconName().equals("")
                                    && profile.getAmpliconSize() == 0) {//Prevents over writting when using manual import
                                RunImportUtilities.getAmpliconSize(ampliconDB, profile);
                            }
                        }
                        prfIntlz.conductAutomatedLreWindowSelection(profile, lreWindowSelectionParameters);
                        calbnDB.saveObject(profile);
                    }
                    //Process the AverageCalibnProfiles
                    List<AverageCalibrationProfile> averageCalbnProfileList =
                            (List<AverageCalibrationProfile>) AverageProfileGenerator.averageCalbrationProfileConstruction(
                            calibnProfileList,
                            lreWindowSelectionParameters,
                            run);
                    calbnDB.saveObject(averageCalbnProfileList);
                    calbnDB.commitChanges();
                    //Broadcast that the calibration panels must be updated
                    UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
                }
            }
        }
    }

    /**
     * Generates a yes/no dialog asking the user whether to continue when an Experiment database is not open.
     * @return whether the user wants to continue with data import
     */
    public boolean experimentDatabaseNotOpen() {
        Toolkit.getDefaultToolkit().beep();
        String msg = "An Experiment database has not been opened. \n"
                + "Do you want to continue with the data import?";
        return RunImportUtilities.requestYesNoAnswer("Experiment database not open?",
                msg);
    }

    /**
     * Generates a yes/no dialog asking the user whether to continue when a Calibration database is not open.
     * @return whether the user wants to continue with data import
     */
    public boolean calibrationDatabaseNotOpen() {
        Toolkit.getDefaultToolkit().beep();
        String msg = "A Calibration database has not been opened. \n"
                + "Do you want to continue with the data import?";
        return RunImportUtilities.requestYesNoAnswer("Calibration database not open?", msg);
    }

    public boolean ampliconDatabaseNotOpen() {
        Toolkit.getDefaultToolkit().beep();
        String msg = "An Amplicon database has not been opened. \n"
                + "Do you want to continue with the data import?";
        return RunImportUtilities.requestYesNoAnswer("Amplicon database not open?", msg);
    }
}
