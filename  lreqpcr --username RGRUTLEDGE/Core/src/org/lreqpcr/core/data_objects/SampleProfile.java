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

/**
 * A DB4O-centric implementation in which
 * the primary function is determining
 * the number of target molecules, which requires
 * that requires an amplicon size and
 * Optical Calibratio Factor (OCF),
 * which is provided by Lambda calibration Profile(s)
 *
 * @author Bob Rutledge
 */
public class SampleProfile extends Profile {

    public SampleProfile() {
    }

    @Override
    /**
     * Calculate the number of target molecules (No) 
     * based on the OCF and amplicon size.
     * If the run OCF not = 0, use the runOCF, else use the average OCF.
     * If both are = 0, simply return
     */
    public void updateProfile() {
        double currentOCF = 0;
        if (getRunOCF() != 0) {
            currentOCF = getRunOCF();
        }else {
            if (getOCF() != 0){
                currentOCF = getOCF();
            }else {
                setNo(0);
                setAdjustedNo(0);
                return;
            }
        }
        if (getTargetStrandedness() == TargetStrandedness.SINGLESTRANDED) {
            setNo(2 * ((getAvFo() / currentOCF) * 910000000000d) / getAmpliconSize());
            setAdjustedNo(2 * ((getAdjustedAvFo() / currentOCF) * 910000000000d) / getAmpliconSize());
        } else {
            if(getTargetStrandedness() == TargetStrandedness.DOUBLESTRANDED){
                setNo(((getAvFo() / currentOCF) * 910000000000d) / getAmpliconSize());
                setAdjustedNo(((getAdjustedAvFo() / currentOCF) * 910000000000d) / getAmpliconSize());
            }
        }
    }

    /**
     * Sort based on the date of the Run if this is an average profile, or
     * sort on well number if this is a replicate reaction profile
     * @param o the Profile to compare to
     * @return the comparator integer
     */
    @Override
    public int compareTo(Object o) {
        Profile profile = (Profile) o;
        if(!profile.isExcluded()){
            if(isExcluded()){
                return 1;
            }else {
                return -1;
            }
        }
        return 0;

//        if(isExcluded()){
//            if(profile.isExcluded()){
//                return 0;
//            }else{
//                return -1;
//            }
//        }
//        int runDateComparison = getRunDate().compareTo(profile.getRunDate());
//        //Must identify if this is an average profile, which does not contain
//        //a well number
//        if (getWellNumber() == 0) {
//            return runDateComparison;
//        }
//        if (runDateComparison == 0) {
//            if (getWellNumber() > profile.getWellNumber()) {
//                return 1;
//            }
//        }
//        return runDateComparison;
    }
}
