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
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationRun;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.RunImpl;
import org.lreqpcr.core.database_services.DatabaseServices;

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
     * Updates calibration databases from 085 and deletes any AverageSampleProfiles found in the 
     * database.
     * @param db the calibration database
     * @param avProfileList list of the average profiles to be processed
     */
    public static void updateCalibrationProfiles(DatabaseServices calbnDB) {
        List<Run> runList1 = (List<Run>) calbnDB.getAllObjects(Run.class);
        for(Run run : runList1){
            if (run.getAverageProfileList() != null && run.getAverageProfileList().get(0) instanceof AverageSampleProfile){
                calbnDB.deleteObject(run);
            }
        }
//        List<Run> runList2 = (List<Run>) calbnDB.getAllObjects(Run.class);
//        List<AverageSampleProfile> avSampleList2 = (List<AverageSampleProfile>) calbnDB.getAllObjects(AverageSampleProfile.class);
        List<AverageCalibrationProfile> avCalPrfList = (List<AverageCalibrationProfile>) calbnDB.getAllObjects(AverageCalibrationProfile.class);
        //Set the AverageProfile list in each Run and have Run calculate average Fmax
        //Need to sort out all the avProfiles associated with each Run, e.g. CAL1 + CAL2 = 2 avProfiles in one run
        //This is required in order to put the avProfiles into the the correct Run
        HashMap<Run, ArrayList<AverageProfile>> runMap = new HashMap<Run, ArrayList<AverageProfile>>();
        for (AverageProfile avPrf : avCalPrfList) {
            AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) avPrf;
            if (!runMap.containsKey(avCalPrf.getRun())) {
                ArrayList<AverageProfile> newAvPrfList = new ArrayList<AverageProfile>();
                newAvPrfList.add(avCalPrf);
                runMap.put(avCalPrf.getRun(), newAvPrfList);
            } else {
                ArrayList<AverageProfile> retrievedRun = runMap.get(avCalPrf.getRun());
                retrievedRun.add(avCalPrf);
            }
        }//End of avPrf loop
        for (Run run : runMap.keySet()) {
            //Move the AverageCalibrationProfiles into new CalibrationRuns and delete the now outdated Runs
            calbnDB.saveObject(runMap.get(run));
            CalibrationRun calRun = new CalibrationRun();
            calRun.setRunDate(run.getRunDate());
            run.calculateAverageFmax();
            //This is obvious incomplete but should suffice
            calRun.setCompleteRunAvFmax(run.getAverageFmax());
            calRun.setAverageProfileList(runMap.get(run));
            calRun.calculateAverageOCF();
            calbnDB.saveObject(calRun);
            run.setAverageProfileList(null);
            calbnDB.deleteObject(run);
        }
        //Testing indicates that duplicate runs with no average profile list are generated, so delete them
        List<Run> runList = (List<Run>) calbnDB.getAllObjects(Run.class);
        for (Run run : runList){
            if (run instanceof RunImpl){
                calbnDB.deleteObject(run);
            }
        }
        calbnDB.commitChanges();
    }
}
