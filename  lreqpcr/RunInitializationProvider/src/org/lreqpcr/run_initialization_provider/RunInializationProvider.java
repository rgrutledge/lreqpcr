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

import java.util.ArrayList;
import java.util.List;
import org.lreqpcr.data_import_services.AverageProfileGenerator;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.ExperimentDbInfo;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.ReactionSetupImpl;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.data_import_services.ImportData;
import org.lreqpcr.data_import_services.RunInitializationService;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.util.Lookup;

/**
 * 
 *
 * @author Bob Rutledge
 */
public class RunInializationProvider implements RunInitializationService {

    @SuppressWarnings(value = "unchecked")
    public void intializeRun(ImportData importData) {
        if (importData == null || importData.getRun() == null) {
            return;
        }
        Run run = importData.getRun();
        List<? extends Profile> sampleProfileList = importData.getSampleProfileList();
        List<? extends Profile> calibnProfileList = importData.getCalibrationProfileList();
        //Try to retrieve the three database files which are in alphabetic order
        DatabaseServices[] dbArray = RunImportUtilities.getDatabases();
        if(dbArray == null){
            //The run import has been aborted
            return;
        }
        DatabaseServices ampliconDB = dbArray[0];
        DatabaseServices calbnDB = dbArray[1];
        DatabaseServices experimentDB = dbArray[2];
        LreAnalysisService prfIntlz = Lookup.getDefault().lookup(LreAnalysisService.class);

//Process the SampleProfiles if an experiment database is open
        if (!sampleProfileList.isEmpty()) {
            if (experimentDB.isDatabaseOpen()) {
                LreWindowSelectionParameters winParameters = (LreWindowSelectionParameters)
                        experimentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                ExperimentDbInfo dbInfo = (ExperimentDbInfo) experimentDB.getAllObjects(ExperimentDbInfo.class).get(0);
                double averageOCF = dbInfo.getOcf();
                for (Profile profile : sampleProfileList) {
                    profile.setParent(run);
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
                        AverageProfileGenerator.averageSampleProfileConstruction((List<Profile>) sampleProfileList,
                        run,
                        averageOCF,
                        winParameters);
                if(averageSampleProfileList == null){
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
                LreWindowSelectionParameters calbnParameters = (LreWindowSelectionParameters)
                        calbnDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                //Process the CalibnProfiles
                List<? extends LreObject> rxnSetupList = (List<? extends LreObject>) calbnDB.getAllObjects(ReactionSetupImpl.class);
                //Each calibration db has one reaction setup object, created when the database was created
                ReactionSetupImpl rxnSetup = (ReactionSetupImpl) rxnSetupList.get(0);
                for (Profile profile : calibnProfileList) {
                    profile.setRunDate(run.getRunDate());
                    profile.setReactionSetup(rxnSetup);
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
                        (List<Profile>) calibnProfileList,
                        rxnSetup,
                        calbnParameters);
                calbnDB.saveObject(averageCalbnProfileList);
                calbnDB.commitChanges();
                //Broadcast that the calibration panels must be updated
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
            }
        }
    }
}
