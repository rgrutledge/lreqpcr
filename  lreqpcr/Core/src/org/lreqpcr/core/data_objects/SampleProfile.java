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
 * A SampleProfile is generated by amplification of a sample. The quantitative
 * output is the number of target molecules (No) that is derived from the average
 * Fo using the optical calibration factor derived from the same assay setup used
 * to conduct the amplification.
 * 
 * @author Bob Rutledge
 */
public class SampleProfile extends Profile {

    //Target molecule (No) determination via optical calibration
    private double ocf;//The optical calibration factor used to calculate the number of target molecules
    private double no;//Number of targets molecules
    private double noEmax100;//Number of target molecules when Emax is fixed to 100%

    public SampleProfile(Run run) {
        super(run);
    }
//Override all setters for all paramaters that change No values in order to recalculate
//the No values via calling updateSampleProfile()

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
     * Calculate the number of target molecules (No) 
     * based on the profile average Fo, OCF, amplicon size and target strandedness.
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

    /**
     * Returns the optical calibration factor that wa used to calculate the
     * number of target molecules (No).
     * @return the optical calibration factor
     */
    public double getOCF() {
        return ocf;
    }

    /**
     * Sets the optical calibration factor to be used to calculate the number of
     * target molecules (No). Note that this will initiate an auto update to
     * recalculate the number of target molecules (No).
     * @param ocf the optical calibration factor
     */
    public void setOCF(double ocf) {
        this.ocf = ocf;
        updateSampleProfile();
    }

    /**
     * Target quantity should only be modified by implementations of SampleProfile. 
     * @param no target quantity expressed in molecules calculated using the LRE-derived Emax.
     * @param noEmax100 target quantity expressed in molecules calculated using Emax fixed to 100%
     */
    public void setNoValues(double no, double noEmax100){
        this.no = no;
        this.noEmax100 = noEmax100;
    }
    /**
     * The primary product of a SampleProfile is determination of the number
     * of target molecules (No). 
     * @return No, the number of target molecules
     */
    public double getNo() {
        if (isEmaxFixedTo100()) {
            return noEmax100;
        } else {
            return no;
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
