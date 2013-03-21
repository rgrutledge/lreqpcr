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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Data object representing a Run loosely based on the RDML 1.0 specification.  
 * 
 * @author Bob Rutledge
 */
public abstract class Run extends LreObject {
    
    private Date runDate;
    private List<String> operators;//The person(s) conducting the Run
    private List<AverageProfile> averageProfileList;
    private int year;
    private int month;
    private double runSpecificOCF = 0;//Run-specific OCF that only applies to Runs containing SampleProfiles
    private double averageFmax = 0;//Average Fmax of all replicate profiles
    private double avFmaxCV = 0;//Average Fmax coefficient of variation 
    private double avOCF = 0;//Only applies to Runs containing CalibrationProfiles
    private double avOcfCV = 0;
    private String versionNumber;
    
    /**
     * The child class is set to AverageProfile. This allows average profiles 
     * within a Run to be retrieved via its child objects. However, for performance 
     * reasons, this has been largely abandoned, relying instead on a List of 
     * average profiles. 
     */
    public Run() {
        setChildClass(AverageProfile.class);
    }

    /**
     * Returns a list of persons that conducted the Run.
     * @return the persons conducting the Run
     */
    public List<String> getOperators() {
        return operators;
    }

    /**
     * Add a person to those conducting the Run
     * @param operator the person conducting the Run
     */
    public void addOperator(String operator) {
        this.operators.add(operator);
    }

    /**
     * 
     * 
     * @return list containing all of the average profiles within this Run
     */
    public List<AverageProfile> getAverageProfileList() {
        return averageProfileList;
    }

    /**
     * All average profiles within a Run are stored in a list, allow direct 
     * access without having to conduct a database search. Note that order 
     * is not important. 
     * 
     * @param averageProfileList list containing all of the average profiles within this Run
     */
    public void setAverageProfileList(List<AverageProfile> averageProfileList) {
        this.averageProfileList = averageProfileList;
    }

    /**
     * Returns the date of the run.
     * @return the run date
     */
    public Date getRunDate() {
        return runDate;
    }

    /**
     * Sets the date on which the run was conducted. While this was initially 
     * considered inappropriate as Run date is set during data import. However, 
     * it is provided to allow an incorrect date to be changed without re-importing  
     * the Run.  
     * 
     * @param runDate the new Run date
     */
    public void setRunDate(Date runDate) {
        this.runDate = runDate;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(runDate);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
    }

    /**
     * 
     * @return the month of the run 
     */
    public int getMonth() {
        return month;
    }

    /**
     * 
     * @return the year of the run
     */
    public int getYear() {
        return year;
    }

    /**
     * 
     * @return 
     */
    public double getAverageFmax() {
        if(averageFmax == 0){
            calculateAverageFmax();            
        }
        return averageFmax;
    }

    public double getAvFmaxCV() {
        return avFmaxCV;
    }

    /**
     * Only applies to Runs containing SampleProfile and provides the 
     * ability to apply a run-specific OCF. This is applicable to Runs that differ 
     * in fluorescence intensity in comparison with the standard reaction setup 
     * 
     * @return run-specific OCF
     */
    public double getRunSpecificOCF() {
        return runSpecificOCF;
    }

    /**
     * Only applies to Runs containing SampleProfile and provides the 
     * ability to apply a run-specific OCF. This is applicable to Runs that differ 
     * in fluorescence intensity in comparison with the standard reaction setup 
     * @param runOCF the run-specific OCF
     */
    public void setRunSpecificOCF(double runOCF) {
        this.runSpecificOCF = runOCF;
    }

    /**
     * Only applies to Runs containing CalibrationProfiles. 
     * 
     * @return the average OCF generated by all CalibrationProfiles within the Run
     */
    public double getAvOCF() {
        return avOCF;
    }

    /**
     * Only applies to Runs containing CalibrationProfiles. 
     * 
     * @param avOCF the average OCF generated by all CalibrationProfiles within the Run
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
     * Included in version 0.8 to allow database compatability to be assessed.
     * 
     * @return the version number
     */
    public String getVersionNumber() {
        return versionNumber;
    }

    /**
     * Allows updating to a new version to maintain database compatability.
     * 
     * @param versionNumber the version number that can contain characters
     */
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    /**
     * Calculates the average Fmax and it's CV based only on the replicate profiles
     * within this Run. Note that the
     * AverageProfiles are ignored, as are excluded SampleProfiles, along with  
     * SampleProfiles for which an LRE window has not been found.
     */
    public void calculateAverageFmax(){
        ArrayList<Double> fmaxList = Lists.newArrayList();//Used to determine the SD
        double fmaxSum = 0;
        int profileCount = 0;
        if (averageProfileList == null){
            return;
        }
        for (AverageProfile avProfile: averageProfileList){
            for(Profile profile: avProfile.getReplicateProfileList()){
                if(profile.hasAnLreWindowBeenFound() && !profile.isExcluded()){
                    fmaxSum += profile.getFmax();
                    profileCount++;
                    fmaxList.add(profile.getFmax());
                }
            }
        }
        if (profileCount >= 1 && fmaxSum > 0){
            averageFmax = fmaxSum/profileCount;
            if(fmaxList.size()>1){
                avFmaxCV = MathFunctions.calcStDev(fmaxList)/averageFmax;
            }else{
                avFmaxCV = 0;
            }
        } else {
            averageFmax = 0;
        }
    }

    /**
     * This only applies to Runs containing CalibrationProfiles. 
     * 
     */
    public void calculateAverageOCF(){
        if (averageProfileList == null || averageProfileList.isEmpty()){
            return;
        }
        if (averageProfileList.get(0) instanceof CalibrationProfile) {
            //Base the average OCF on AverageCalibrationProfiles only
            double sum = 0;
            int count = 0;
            ArrayList<Double> ocfList = Lists.newArrayList();//Used to determine the SD
            for (AverageProfile prf : averageProfileList){
                AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) prf;
                sum += avCalPrf.getOCF();
                count++;
                ocfList.add(avCalPrf.getOCF());
            }
            if (count >= 1 && sum >0 ){
                avOCF = sum/count;
                if (ocfList.size() > 1) {
                    avOcfCV = MathFunctions.calcStDev(ocfList)/avOCF;
                }else {
                    avOcfCV =0;
                }
            } else {
                avOCF = 0;
            }
        }
    }
    
    /**
     * Sort Run objects based on the date of the Run
     * @param o the Run to compare to
     * @return the comparator integer
     */
    @Override
    public int compareTo(Object o) {
        Run run = (Run)o;
        if(runDate.compareTo(run.getRunDate()) == 0){
            return getName().compareTo(run.getName());
        }else{
            return run.getRunDate().compareTo(getRunDate());
        }
    }
}
