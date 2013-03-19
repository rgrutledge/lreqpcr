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
public class VersionVerification {

    /**
     * Determines if Run average Fmax needs to be calculated, which for earlier 
     * versions was not set during Run import.
     * @param avProfileList 
     */
    public static void updateCalibrationProfiles(DatabaseServices db, List<AverageCalibrationProfile> avProfileList){
        for (AverageCalibrationProfile avCalPrf : avProfileList){
            Run run = avCalPrf.getRun();
            if (run.getAverageFmax() == 0){
                if (avCalPrf.getRun().getAverageProfileList() == null){
                    //Get all the profile within this run
                    ArrayList<AverageProfile> array = new ArrayList<AverageProfile>();
                    array.add(avCalPrf);
                    db.saveObject(array);
                    run.setAverageProfileList(array);
                }
                avCalPrf.getRun().calculateAverageFmax();
                db.saveObject(run);
            }
            if (avCalPrf.getAvAmpTm() == 0){
                avCalPrf.calculateAvTm();
            }
        }
    }
}
