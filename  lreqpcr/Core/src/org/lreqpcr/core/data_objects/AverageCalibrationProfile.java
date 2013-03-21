/*
 * Copyright (C) 2013   Bob Rutledge
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
 * Average calibration profile generated by averaging of the Fc datasets from
 * replicate calibration profiles. Note that it is implicitly assumed that that
 * average ReplicateCalibrationProfile No will never be less than 10N and thus
 * that the AverageCalibrationProfile will always be valid.
 *
 * @author Bob Rutledge
 */
public class AverageCalibrationProfile extends CalibrationProfile implements AverageProfile {

    private List<CalibrationProfile> lambdaProfileList;
    private double avTm = 0;
    private boolean isTragetQuantityNormalizedToFmax = false;

    /**
     * An average calibration file constructed from the replicate calibration
     * profiles
     */
    public AverageCalibrationProfile() {
        setChildClass(CalibrationProfile.class);
    }

    /**
     * List of the replicate profiles
     *
     * @return list of replicate Calibration Profiles
     */
    public List<CalibrationProfile> getReplicateProfileList() {
        return lambdaProfileList;
    }

    public boolean isTargetQuantityNormalizedToFmax() {
        return isTragetQuantityNormalizedToFmax;
    }

    public void setIsTargetQuantityNormalizedToFmax(boolean normalizeToFmax) {
        this.isTragetQuantityNormalizedToFmax = normalizeToFmax;
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

    public int getTheNumberOfActiveReplicateProfiles() {
        int numberOfActiveReplicateProfiles = 0;
        for (Profile profile : lambdaProfileList) {
            if (!profile.isExcluded()) {
                numberOfActiveReplicateProfiles++;
            }
        }
        return numberOfActiveReplicateProfiles;
    }

    /**
     * Sorted by date. 
     * 
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Object o) {
        AverageCalibrationProfile profile = (AverageCalibrationProfile) o;
        return profile.getRunDate().compareTo(getRunDate());
    }

    /**
     * Based on the assumption that the average ReplicateCalibrationProfile No
     * will never have less than 10 molecules, this method has not been
     * implemented and will return false regardless of the number of target
     * molecules.
     *
     * @return always returns false.
     */
    public boolean isTheReplicateAverageNoLessThan10Molecules() {
        return false;
    }

    /**
     * Returns the average melting temperature (Tm) of the SampleProfile(s)
     * amplicons. Note that this will return -1 if a Tm is not available.
     *
     * @return the average SampleProfile Tm or -1 if none is available
     */
    public double getAvAmpTm() {
        if (avTm == 0) {
            calculateAvAmpTm();
        }
        return avTm;
    }

    public double calculateAvAmpTm() {
        double tmSum = 0;
        int counter = 0;
        for (CalibrationProfile calProfile : lambdaProfileList) {
            if (!calProfile.isExcluded()
                    && calProfile.hasAnLreWindowBeenFound()
                    && calProfile.getAmpTm() != 0) {
                tmSum += calProfile.getAmpTm();
                counter++;
            }
            if (counter != 0) {
                avTm = tmSum / counter;
            } else {
                avTm = -1;
            }
        }
        return avTm;
    }
}
