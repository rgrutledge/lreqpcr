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
 * Data object represent a Run. Note that Profile is linked to
 * all of the reaction-specific components via Reaction Setup,
 * thus separating the Run from the details as to how the amplification
 * is conducted. This includes the machine, optical gain
 * and cycling regime (etc.), which is documented within the
 * Reaction Setup, although full functionality of ReactionSetup has not yet
 * been implemented. Note also that Run is not linked to CalibrationProfiles,
 * which are instead linked to a ReactionSetup
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
    private String cyclerDatafileName;
    private byte[] machineDataFile; //The machine data file; e.g. a MXP file
    private String importDataFileName;
    private byte[] importDataFile; //The import data file; e.g. XLS
    
    public Run() {
        setChildClass(AverageProfile.class);
    }

    /**
     *
     * @return the persons conducting the Run
     */
    public List<String> getOperators() {
        return operators;
    }

    /**
     *
     * @param operator the person conducting the Run
     */
    public void addOperator(String operator) {
        this.operators.add(operator);
    }

    public byte[] getMachineDataFile() {
        return machineDataFile;
    }

    public void setMachineDataFile(byte[] machineDataFile) {
        this.machineDataFile = machineDataFile;
    }

    public List<AverageSampleProfile> getAverageProfileList() {
        return averageProfileList;
    }

    public void setAverageProfileList(List<AverageSampleProfile> averageProfileList) {
        this.averageProfileList = averageProfileList;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(runDate);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public byte[] getRunDataFile() {
        return machineDataFile;
    }

    public void setRunDataFile(byte[] runDataFile) {
        this.machineDataFile = runDataFile;
    }

    public String getCyclerDatafileName() {
        return cyclerDatafileName;
    }

    public void setCyclerDatafileName(String cyclerDatafileName) {
        this.cyclerDatafileName = cyclerDatafileName;
    }

    public byte[] getImportDataFile() {
        return importDataFile;
    }

    public void setImportDataFile(byte[] importDataFile) {
        this.importDataFile = importDataFile;
    }

    public String getImportDataFileName() {
        return importDataFileName;
    }

    public void setImportDataFileName(String importDataFileName) {
        this.importDataFileName = importDataFileName;
    }

    public double getRunOCF() {
        return runOCF;
    }

    public void setRunOCF(double runOCF) {
        this.runOCF = runOCF;
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
            return runDate.compareTo(run.getRunDate());
        }
    }
}
