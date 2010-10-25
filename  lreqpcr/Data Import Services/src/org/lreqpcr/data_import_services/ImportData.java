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

package org.lreqpcr.data_import_services;

import java.util.List;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;

/**
 * Used to transfer import data based on supplying a Run and lists of
 * all Sample and Calibration Profiles. The Run initialization service is
 * then used to complete data import by initializing the profiles, along with
 * generated average profiles and saving the data to the appropriate databases.
 *
 * @author Bob Rutledge
 */
public class ImportData {

    private Run run;
    private List<? extends Profile> sampleProfileList;
    private List<? extends Profile> calibrationProfileList;

    public List<? extends Profile> getCalibrationProfileList() {
        return calibrationProfileList;
    }

    /**
     * The Calibration Profiles must contain the Amplicon name
     * and quantity of Lambda gDNA in picograms,
     * along with the raw Fc dataset (i.e. not baseline subtracted). Well number
     * and label (e.g. A1) should also be included. Run date is retrieved from the Run.
     * Target strandedness will be set to double stranded during run initialization.
     * 
     * @param calibrationProfileList
     */
    public void setCalibrationProfileList(List<? extends Profile> calibrationProfileList) {
        this.calibrationProfileList = calibrationProfileList;
    }

    public Run getRun() {
        return run;
    }

    /**
     * The Run must include the Run date and Run name. It can also include copies of the
     * Excel import and machine data files, which are optional.
     *
     * @param run the run which contains run information such as run date
     */
    public void setRun(Run run) {
        this.run = run;
    }

    public List<? extends Profile> getSampleProfileList() {
        return sampleProfileList;
    }

    /**
     * The SampleProfiles must contain the sample and Amplicon names, target strandedness.
     * along with the raw Fc dataset (i.e. not baseline subtracted). Well number
     * and well label (e.g. A1) should also be included. Run date is retrieved from the Run.
     *
     * @param sampleProfileList the list of sample profiles
     */
    public void setSampleProfileList(List<? extends Profile> sampleProfileList) {
        this.sampleProfileList = sampleProfileList;
    }


}
