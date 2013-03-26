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

import java.util.ArrayList;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * A Run derivative holding Calibration Profiles which is dedicated to optically
 * calibrating the originating Run. A calibration profiles derived quantity is fluorescence units per
 * ng of lambda gDNA, referred to as the optical calibration factor (OCF). An
 * OCF is used to convert Fo values generated by Sample Profiles, into the
 * number of target molecules.
 *
 * @author Bob Rutledge
 */
public class CalibrationRun extends Run {

    private double avOCF = 0;//Only applies to Runs containing CalibrationProfiles
    private double avOcfCV = 0;

    /**
     * Only applies to Runs containing CalibrationProfiles.
     *
     * @return the average OCF generated by all CalibrationProfiles within the
     * Run
     */
    public double getAvOCF() {
        return avOCF;
    }

    /**
     * Only applies to Runs containing CalibrationProfiles.
     *
     * @param avOCF the average OCF generated by all CalibrationProfiles within
     * the Run
     */
    public void setAvOCF(double avOCF) {
        this.avOCF = avOCF;
    }

    public double getAvOcfCV() {
        return avOcfCV;
    }

    public void setAvOcfCV(double avOcfCV) {
        this.avOcfCV = avOcfCV;
    }

    /**
     * This only applies to Runs containing CalibrationProfiles. Indeed, this
     * reflects the poor class structure that has evolved due to the pressure to
     * maintain back compatability
     *
     */
    public void calculateAverageOCF() {
        if (getAverageProfileList() == null || getAverageProfileList().isEmpty()) {
            return;
        }
        if (getAverageProfileList().get(0) instanceof CalibrationProfile) {
            //Base the average OCF on AverageCalibrationProfiles only
            double sum = 0;
            int count = 0;
            ArrayList<Double> ocfList = new ArrayList<Double>();//Used to determine the SD
            for (AverageProfile prf : getAverageProfileList()) {
                AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) prf;
                sum += avCalPrf.getOCF();
                count++;
                ocfList.add(avCalPrf.getOCF());
            }
            if (count >= 1 && sum > 0) {
                avOCF = sum / count;
                if (ocfList.size() > 1) {
                    avOcfCV = MathFunctions.calcStDev(ocfList) / avOCF;
                } else {
                    avOcfCV = 0;
                }
            } else {
                avOCF = 0;
            }
        }
    }//End of calculate average OCF

    /**
     * INACTIVATED in order to preserve the average Fmax derived from all of the 
     * profiles from the full Run from which this Run was derived. This initial value 
     * was calculated during Run initialization and included both sample and calibration 
     * profiles within the Run.
     */
    @Override
    public void calculateAverageFmax() {
        //Inactivated 
    }
    
    /**
     * During data import, the average Fmax is calculated from both Sample and 
     * Calibration profiles, with the intent that this will not be changed. 
     * Referred to as the complete Run average Fmax, the intent is to generate an 
     * accurate estimate of Fmax, that can then be used to normalize the OCF so 
     * that well-to-well variance in fluorescence intensity can be corrected. 
     * 
     * @param totalAvFmax the average Fmax determined from all profiles within the originating Run
     */
    public void setCompleteRunAvFmax(double totalAvFmax){
        averageFmax = totalAvFmax;
    }
    
    /**
     * See setCompleteRunAvFmax
     * 
     * @param totalAvFmaxCV 
     */
    public void setCompleteRunAvFmaxCV(double totalAvFmaxCV){
        avFmaxCV = totalAvFmaxCV;
    }
}
