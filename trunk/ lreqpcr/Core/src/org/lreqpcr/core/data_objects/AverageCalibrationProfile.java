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

import java.util.List;

/**
 *
 * @author Bob Rutledge
 */
public class AverageCalibrationProfile extends CalibrationProfile implements AverageProfile {

    private List<CalibrationProfile> lambdaProfileList;

    /**
     * An average calibration file constructed from the replicate calibration
     * profiles
     */
    public AverageCalibrationProfile() {
        setChildClass(CalibrationProfile.class);
    }

    /**
     * List of the replicate profiles
     * @return list of replicate Calibration Profiles
     */
    public List<CalibrationProfile> getReplicateProfileList() {
        return lambdaProfileList;
    }

    /**
     * Will throw an illegal cast exception if the list does not contain
     * Calibration Profiles.
     *
     * @param replicateProfileList list of replicate Calibration profiles
     */
    @SuppressWarnings(value = "unchecked")
    public void setReplicateProfileList(List<? extends Profile> replicateProfileList) {
        lambdaProfileList = (List<CalibrationProfile>) replicateProfileList;
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
