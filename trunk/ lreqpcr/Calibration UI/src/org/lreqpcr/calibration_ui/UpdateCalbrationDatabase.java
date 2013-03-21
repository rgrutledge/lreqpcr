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
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;

/**
 * Static methods for assessing version compatability.
 *
 * @author Bob Rutledge
 */
public class UpdateCalbrationDatabase {

    /**
     * Determines if average Run Fmax needs to be calculated, which for earlier
     * versions, was not set during Run import. This includes generating an
     * AverageProfile list in the Run.
     *
     * @param avProfileList
     */
    public static void updateCalibrationProfiles(DatabaseServices db, List<AverageCalibrationProfile> avProfileList) {
        //Set the AverageProfile list in each Run and have Run calculate average Fmax
        //Need to sort out all the avProfiles associated with each Run, e.g. CAL1 + CAL2 = 2 avProfiles in one run
        //This is required in order to put the avProfiles into the the correct Run
        HashMap<Run, ArrayList<AverageProfile>> runMap = new HashMap<Run, ArrayList<AverageProfile>>();
        for (AverageCalibrationProfile avCalPrf : avProfileList) {
            if (!runMap.containsKey(avCalPrf.getRun())) {
                ArrayList<AverageProfile> array = new ArrayList<AverageProfile>();
                array.add(avCalPrf);
                runMap.put(avCalPrf.getRun(), array);
            } else {
                ArrayList<AverageProfile> gottedArray = runMap.get(avCalPrf.getRun());
                gottedArray.add(avCalPrf);
            }
            for (Run run : runMap.keySet()) {
                db.saveObject(runMap.get(run));
                run.setAverageProfileList(runMap.get(run));
                run.calculateAverageFmax();
                run.calculateAverageOCF();
                db.saveObject(run);
            }
            db.commitChanges();
        }
    }
}
