/*
 * Copyright (C) 2013  Bob Rutledge
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
package org.lreqpcr.calibration_ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationRun;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.openide.util.Lookup;

/**
 * Static methods for assessing version compatability.
 *
 * @author Bob Rutledge
 */
public class UpdateCalbrationDatabase {

    /**
     *
     *
     * @param avProfileList
     */
    /**
     * Updates calibration databases from 085 and deletes any
     * AverageSampleProfiles found in the database.
     *
     * @param db the calibration database
     * @param avProfileList list of the average profiles to be processed
     */
    @SuppressWarnings("unchecked")
    public static void updateCalibrationProfiles(DatabaseServices calbnDB) {
        List<Run> runList1 = (List<Run>) calbnDB.getAllObjects(Run.class);
        //Some of these runs lack an average calibration profile array but also
        //include runs that have average sample profile array and must be deleted
        for (Run run : runList1) {
            if (run.getAverageProfileList() != null && run.getAverageProfileList().get(0) instanceof AverageSampleProfile) {
                calbnDB.deleteObject(run);
            }
        }
        //Now need to transfer the average calibration profiles into new CalibrationRuns
        //initialized from the ImplRun held within the average profiles
        List<AverageCalibrationProfile> avCalPrfList = (List<AverageCalibrationProfile>) calbnDB.getAllObjects(AverageCalibrationProfile.class);
        //Set the AverageProfile list in each Run and have Run calculate average Fmax
        //Need to sort out all the avProfiles associated with each Run, e.g. CAL1 + CAL2 = 2 avProfiles in one run
        //This is required in order to put the avProfiles into the the correct Run
        HashMap<Run, ArrayList<AverageProfile>> runMap = new HashMap<>();
        for (AverageProfile avPrf : avCalPrfList) {
            AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) avPrf;
            if (!runMap.containsKey(avCalPrf.getRun())) {
                ArrayList<AverageProfile> newAvPrfList = new ArrayList<>();
                newAvPrfList.add(avCalPrf);
                runMap.put(avCalPrf.getRun(), newAvPrfList);
            } else {
                ArrayList<AverageProfile> retrievedRun = runMap.get(avCalPrf.getRun());
                retrievedRun.add(avCalPrf);
            }
        }//End of avPrf loop
        //Now, go through each run and intialize a Calibration run for each
        for (Run run : runMap.keySet()) {
            //Move the AverageCalibrationProfiles into corresponding old Runs
            //in order to calculate the average Fmax
            run.setAverageProfileList(runMap.get(run));
            run.calculateAverageFmax();
            //Intialize a Calibration run
            CalibrationRun calRun = new CalibrationRun();
            calRun.setName(" ");//BMC cal databases had no run and thus the run name is the import file name
            calRun.setRunDate(run.getRunDate());
            calRun.setAverageProfileList(run.getAverageProfileList());
            calRun.setCompleteRunAvFmax(run.getAverageFmax());
            calRun.setAverageProfileList(runMap.get(run));
            calRun.calculateAverageOCF();
            calbnDB.saveObject(calRun);
            //Delete the outdated run but first remove the average profile array
            run.setAverageProfileList(null);
            calbnDB.deleteObject(run);
        }
        //Testing indicates that ImplRun remain, so delete them
        List<Run> runList = (List<Run>) calbnDB.getAllObjects(Run.class);
        for (Run run : runList) {
            if (run instanceof Run) {
                calbnDB.deleteObject(run);
            }
        }
        //Set the CalbrationRun in all profiles
        List<Run> runFinalList = (List<Run>) calbnDB.getAllObjects(Run.class);
        for (Run run : runFinalList) {
            for (AverageProfile avPrf : run.getAverageProfileList()) {
                AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) avPrf;
                for (Profile profile : avCalPrf.getReplicateProfileList()) {
                    profile.setRun(run);
                    calbnDB.saveObject(profile);
                }
                avCalPrf.setRun(run);
                calbnDB.saveObject(avCalPrf);
            }
        }
        calbnDB.commitChanges();
    }

    /**
     * Applies nonlinear regression analysis to pre Version 0.9 database files
     *
     * @param calDB the Calibration database service maintaining the database to
     * be processed
     */
    public static void nonlinearRegressionUpdate(DatabaseServices calDB) {
        if (!calDB.isDatabaseOpen()) {
            return;
        }
        LreAnalysisService lreAnalysisService =
                Lookup.getDefault().lookup(LreAnalysisService.class);
        List<AverageProfile> profileList;
        profileList = calDB.getAllObjects(AverageCalibrationProfile.class);
        if (profileList.isEmpty()) {
            return;
        }
        LreWindowSelectionParameters lreWindowSelectionParameters =
                (LreWindowSelectionParameters) calDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
        for (AverageProfile avProfile : profileList) {
            //Need to update the replicate profiles first in order to test if <10N
            for (Profile profile : avProfile.getReplicateProfileList()) {
     //Odd error in the microarray cal DB in which the profile parent is a Run
                if (!(profile.getParent() instanceof AverageCalibrationProfile)) {
                    LreObject obj = (LreObject) avProfile;
                    profile.setParent(obj);
                }
                ProfileSummary prfSum = new ProfileSummary(profile, calDB);
                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, lreWindowSelectionParameters);

            }
            Profile prf = (Profile) avProfile;
            ProfileSummary prfSum = new ProfileSummary(prf, calDB);
            lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, lreWindowSelectionParameters);
        }
        calDB.commitChanges();
    }
}
