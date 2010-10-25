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
 * A DB4O-centric implementation 
 * representing a lambda gDNA-based calibration Profile
 * which has the primary function is to provide
 * an optical calibration factor (OCF). Calculating OCF requires
 * that the mass of lambda (ng) be provided. Note also that target
 * strandedness is set to double stranded.
 *
 * @author Bob Rutledge
 */
public class CalibrationProfile extends Profile {

    private double lambdaMass;//The mass of the lamdba gDNA in nanograms
    private double adjustedOCF;//OCF adjusted to 100% Emax

    public CalibrationProfile() {
        setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
    }

    /**
     * Retrieves the quantity of the lambda standard that was amplified
     * 
     * @return the mass of lambda gDNA in picograms
     */
    public double getLambdaMass() {
        return lambdaMass;
    }

    /**
     * Sets the quantity of the lambda standard that was amplified
     * 
     * @param lambdaMass the mass of lambda gDNA in PICOGRAMS (pg)
     */
    public void setLambdaMass(double lambdaMass) {
        this.lambdaMass = lambdaMass / 1000000;//convert to nanograms
    }

    @Override
    /**
     * Calculates the optical calibration factor (OCF)
     * based on the amount of lambda gDNA (Mo) and the average Fo. 
     */
    public void updateProfile() {
        double mo = (lambdaMass * getAmpliconSize()) / 48502;//Mo for lambda gDNA
        setRunOCF(getAvFo() / mo);
        setAdjustedOCF(getAdjustedAvFo() / mo);
        setNo(((getAvFo() / getRunOCF()) * 910000000000d) / getAmpliconSize());
    }

    /**
     *
     * @return OCF adjusted to 100% Emax
     */
    public double getAdjustedOCF() {
        return adjustedOCF;
    }

    /**
     *
     * @param adjustedOCF OCF adjusted to 100% Emax
     */
    public void setAdjustedOCF(double adjustedOCF) {
        this.adjustedOCF = adjustedOCF;
    }

    /**
     * Sort to put excluded profiles at the bottom of the list
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
//        Profile profile = (Profile) o;
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
