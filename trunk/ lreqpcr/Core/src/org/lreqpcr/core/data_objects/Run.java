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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Data object representing a Run loosely based on the RDML 1.0 specification.
 * 
 * @author Bob Rutledge
 */
public abstract class Run extends LreObject {
    
    private Date runDate;
    private List<String> operators;//The person(s) conducting the Run
    private List<AverageSampleProfile> averageProfileList;
    private int year;
    private int month;
    private double runOCF = 0;//Run-specific OCF
    private double averageFmax = 0;//Average Fmax of all replicate profiles
    
    /**
     * The child class is set to AverageProfile.
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

    public List<AverageSampleProfile> getAverageProfileList() {
        return averageProfileList;
    }

    public void setAverageProfileList(List<AverageSampleProfile> averageProfileList) {
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
     * Sets the date on which the run was conducted.
     * @param runDate the run date
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

    public double getAverageFmax() {
        if(averageFmax == 0){
            calculateAverageFmax();            
        }
        return averageFmax;
    }

    /**
     * Returns the run-specific OCF.
     * @return run-specific OCF
     */
    public double getRunOCF() {
        return runOCF;
    }

    /**
     * Sets the run-specific OCF.
     * @param runOCF the run-specific OCF
     */
    public void setRunOCF(double runOCF) {
        this.runOCF = runOCF;
    }
    
    /**
     * Calculates the average Fmax from the replicate profiles. Note that the
     * AverageProfiles are ignored, as are excluded SampleProfiles, along with  
     * SampleProfiles for which an LRE window has not been found.
     */
    public void calculateAverageFmax(){
        double fmaxSum = 0;
        int profileCount = 0;
        if (averageProfileList == null){
            return;
        }
        for (AverageSampleProfile avProfile: averageProfileList){
            for(SampleProfile profile: avProfile.getReplicateProfileList()){
                if(profile.hasAnLreWindowBeenFound() && !profile.isExcluded()){
                    fmaxSum += profile.getFmax();
                    profileCount++;
                }
            }
        }
        if (profileCount > 1 && fmaxSum > 0){
            averageFmax = fmaxSum/profileCount;
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
