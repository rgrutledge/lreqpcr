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

package org.lreqpcr.data_import_services;

import java.util.Date;
import java.util.List;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.SampleProfile;

/**
 * Used to import run data based on supplying lists of
 * Sample and Calibration Profiles. The Run initialization service is
 * then used to complete data import by initializing the profiles, along with
 * generated average profiles and saving the data to the appropriate databases.
 *
 * @author Bob Rutledge
 */
public class RunImportData {

    private List<SampleProfile> sampleProfileList;
    private List<CalibrationProfile> calibrationProfileList;
    private DataImportType importType;
    private Date runDate;
    private String runName;

    /**
     * Interface for Run data import.
     * 
     * @param importType the type of import, which cannot be null
     * @param runDate the date the Run was conducted
     * @param runName an optional name of the Run
     */
    public RunImportData(DataImportType importType, Date runDate, String runName) {
        this.importType = importType;
        this.runDate = runDate;
        this.runName = runName;
    }

    public List<CalibrationProfile> getCalibrationProfileList() {
        return calibrationProfileList;
    }

    /**
     * Provides the calibration profiles conducted during this run.
     * The Calibration Profiles must contain the Amplicon name
     * and quantity of Lambda gDNA in picograms,
     * along with the raw Fc dataset (i.e. not baseline subtracted). Well number
     * and label (e.g. A1) should also be included.      
     * Target strandedness is set to double stranded during run initialization.
     * 
     * @param calibrationProfileList
     */
    public void setCalibrationProfileList(List<CalibrationProfile> calibrationProfileList) {
        this.calibrationProfileList = calibrationProfileList;
    }

    public List<SampleProfile> getSampleProfileList() {
        return sampleProfileList;
    }

    /**
     * Provides the sample profiles conducted during this run.
     * The SampleProfiles must contain the sample and Amplicon names, target strandedness.
     * along with the raw Fc dataset (i.e. not baseline subtracted). Well number
     * and well label (e.g. A1) should also be included.
     * Run date is retrieved from the Run.
     *
     * @param sampleProfileList the list of sample profiles
     */
    public void setSampleProfileList(List<SampleProfile> sampleProfileList) {
        this.sampleProfileList = sampleProfileList;
    }

    public DataImportType getImportType() {
        return importType;
    }

    public Date getRunDate() {
        return runDate;
    }

    public String getRunName() {
        return runName;
    }
    
}
