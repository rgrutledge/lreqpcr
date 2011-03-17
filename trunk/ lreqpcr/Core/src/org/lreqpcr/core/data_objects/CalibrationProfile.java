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
 * A lambda gDNA derived profile generated by a single amplification reaction.
 * This is used to conduct optical calibration based on amplification of a known quantity of
 * lambda gDNA, from which an optical calibration factors (OCF) is generated.
 * Calculating OCF requires that the mass of lambda used in the amplification be provided.
 *
 * @author Bob Rutledge
 */
public class CalibrationProfile extends Profile {

    private double lambdaMass;//The mass of the lamdba gDNA in nanograms
    private double adjustedOCF;//OCF adjusted to 100% Emax
    private double mo;//Mo calculated from the lambdaMass

    /**
     *  Target strandedness is set to double stranded by default
     */
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
        mo = (lambdaMass * getAmpliconSize()) / 48502;//Mo for lambda gDNA
        setOCF(getAvFo() / mo);
        setAdjustedOCF(getAdjustedAvFo() / mo);
        setNo(((getAvFo() / getOCF()) * 910000000000d) / getAmpliconSize());
    }

    /**
     * For profiles that generate Emax values >100%, an adjusted OCF is determined in
     * which Emax is set to 100% (Emax normalization).
     * @return OCF adjusted to 100% Emax
     */
    public double getAdjustedOCF() {
        return adjustedOCF;
    }

    /**
     * For profiles that generate Emax values >100%, an adjusted OCF is determined in
     * which Emax is set to 100% (Emax normalization).
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
        if (!profile.isExcluded()) {
            if (isExcluded()) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }
}