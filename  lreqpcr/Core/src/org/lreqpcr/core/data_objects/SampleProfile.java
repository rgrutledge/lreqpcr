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
package org.lreqpcr.core.data_objects;

/**
 * A SampleProfile is generated by amplification of a sample. Target quantity 
 * is expressed as the number of molecules that is derived from the
 * average Fo using an optical calibration factor.
 *
 * @author Bob Rutledge
 */
public class SampleProfile extends Profile {

    private double ocf;//The optical calibration factor used to calculate the number of target molecules
    protected double no = -1;//Number of targets molecules; -1 signifies that a value is not avialable
    private double noEmax100;//Number of target molecules when Emax is fixed to 100%
    private boolean isTragetQuantityNormalizedToFmax;//Normalize the target quantity to the Run's average Fmax

    public SampleProfile(Run run) {
        super(run);
    }
    
//Need to override setters for all paramaters that change No values in order to recalculate
//the number of target molecules via calling updateSampleProfile()

    @Override
    public void setEmax(double eMax) {
        super.setEmax(eMax);
        //Some nearly flat profiles generate a negative Emax
        if (eMax < 0 || eMax == Double.NaN || getDeltaE() > 0) {
            setAvFoValues(0, 0);
        }
        updateSampleProfile();
    }

    @Override
    public void setTargetStrandedness(TargetStrandedness targetStrandedness) {
        super.setTargetStrandedness(targetStrandedness);
        updateSampleProfile();
    }

    @Override
    public void setAmpliconSize(int ampliconSize) {
        super.setAmpliconSize(ampliconSize);
        updateSampleProfile();
    }

    @Override
    public void setAvFoValues(double averageFo, double averageFoEmax100) {
        super.setAvFoValues(averageFo, averageFoEmax100);
        updateSampleProfile();
    }

    /**
     * Calculate the number of target molecules (No) based on the profile
     * average Fo, OCF, amplicon size and target strandedness.
     */
    public void updateSampleProfile() {
        if (ocf > 0) {
            if (getTargetStrandedness() == TargetStrandedness.SINGLESTRANDED) {
                no = (2 * ((getAvFo() / ocf) * 910000000000d) / getAmpliconSize());
                noEmax100 = (2 * ((getAvFoEmax100() / ocf) * 910000000000d) / getAmpliconSize());
            } else {
                no = (((getAvFo() / ocf) * 910000000000d) / getAmpliconSize());
                noEmax100 = (((getAvFoEmax100() / ocf) * 910000000000d) / getAmpliconSize());
            }
        } else {
            no = 0;
            noEmax100 = 0;
        }
    }

    public boolean isTargetQuantityNormalizedToFmax() {
        return isTragetQuantityNormalizedToFmax;
    }

    public void setIsTargetQuantityNormalizedToFmax(boolean normalizeToFmax) {
        this.isTragetQuantityNormalizedToFmax = normalizeToFmax;
    }

    /**
     * Returns the optical calibration factor that was used to calculate the
     * number of target molecules (No).
     *
     * @return the optical calibration factor
     */
    public double getOCF() {
        return ocf;
    }

    /**
     * Sets the optical calibration factor to be used to calculate the number of
     * target molecules (No). Note that this will initiate an auto update to
     * recalculate the number of target molecules (No).
     *
     * @param ocf the optical calibration factor
     */
    public void setOCF(double ocf) {
        this.ocf = ocf;
        updateSampleProfile();
    }

    /**
     * The primary product of a SampleProfile is determination of the number of
     * target molecules (No). 
     *
     * @return No, the number of target molecules or -1 if no value is available
     */
    public double getNo() {
        if (no == -1){//Signifies that no value is available
            return -1;
        }
        if (isTragetQuantityNormalizedToFmax){
            return getNoAdjustedToFmax();
        }
        
        if (isEmaxFixedTo100()) {
            return noEmax100;
        } else {
            return no;
        }
    }

    public double getNoAdjustedToFmax() {
//This could degrade response if the database is large, but not sure by how much
//However, avFmax can change, so this seems necessary, and likely takes very little time
        double avFmax = super.getRun().getAverageFmax();
        double fmax = getFmax();
        if (fmax <= 0 || avFmax <= 0) {
            return 0;
        }
        double correctionFactor = avFmax / fmax;
        if (isEmaxFixedTo100()) {
            return noEmax100 * correctionFactor;
        } else {
            return no * correctionFactor;
        }
    }

    /**
     * Sort Profile --> NoLreWindow --> Excluded profiles respectively
     *
     * @param o the Profile to compare to
     * @return the comparator integer
     */
    @Override
    public int compareTo(Object o) {
        Profile profile = (Profile) o;
        if (profile.isExcluded()) {
            if (!isExcluded()) {
                return -1;
            }
        } else {
            if (isExcluded()) {
                return 1;
            }
            if (!profile.hasAnLreWindowBeenFound()) {
                if (hasAnLreWindowBeenFound()) {
                    return -1;
                }
            } else {
                return 1;
            }
        }
        return 0;
    }
}