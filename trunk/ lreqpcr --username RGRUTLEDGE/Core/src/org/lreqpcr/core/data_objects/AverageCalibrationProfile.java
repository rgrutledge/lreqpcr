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

import java.util.ArrayList;

/**
 *
 * @author Bob Rutledge
 */
public class AverageCalibrationProfile extends CalibrationProfile implements AverageProfile {

    private ArrayList<CalibrationProfile> lambdaProfileList;

    public AverageCalibrationProfile() {
        setChildClass(CalibrationProfile.class);
    }

    /**
     *
     * @return list of replicate Calibration Profiles
     */
    public ArrayList<CalibrationProfile> getReplicateProfileList() {
        return lambdaProfileList;
    }

    /**
     * Will throw an illegal cast exception if the list does not contain
     * Calibration Profiles.
     *
     * @param replicateProfileList list of replicate Calibration profiles
     */
    @SuppressWarnings(value = "unchecked")
    public void setReplicateProfileList(ArrayList<? extends Profile> replicateProfileList) {
        lambdaProfileList = (ArrayList<CalibrationProfile>) replicateProfileList;
    }

    /**
     * Sort by run date
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        CalibrationProfile prf = (AverageCalibrationProfile) o;
        if (getRunDate() != null) {
            if (prf.getRunDate() != null) {
                return getRunDate().compareTo(prf.getRunDate());
            }
        }
        return 0;
    }
}
