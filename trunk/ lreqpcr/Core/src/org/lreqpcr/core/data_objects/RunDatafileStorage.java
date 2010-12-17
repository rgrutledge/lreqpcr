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
 * A class for storing instrument and import data files associated with a Run 
 * NOT YET IMPLEMEMTED.
 * The primary intent is to provide a central location 
 * that would allow retrieval of data files generated by a run.
 * @author Bob Rutledge
 */
public class RunDatafileStorage {

    private Run run;
    private String instrumentDatafileName;
    private byte[] instrumentDataFile; //The machine data file; e.g. a MXP file
    private String importDataFileName;
    private byte[] importDataFile; //The import data file; e.g. XLS

    /**
     * Provides access to data files associated with the specified Run.
     * @param run the from which the data files were created
     */
    public RunDatafileStorage(Run run) {
        this.run = run;
    }

    public byte[] getInstrumentDataFile() {
        return instrumentDataFile;
    }

    public void setInstrumentDataFile(byte[] instrumentDataFile) {
        this.instrumentDataFile = instrumentDataFile;
    }

    public String getInstrumentDatafileName() {
        return instrumentDatafileName;
    }

    public void setInstrumentDatafileName(String instrumentDatafileName) {
        this.instrumentDatafileName = instrumentDatafileName;
    }

    public Run getRun() {
        return run;
    }
    
    public byte[] getMachineDataFile() {
        return instrumentDataFile;
    }

    public void setMachineDataFile(byte[] machineDataFile) {
        this.instrumentDataFile = machineDataFile;
    }
    public byte[] getRunDataFile() {
        return instrumentDataFile;
    }

    public void setRunDataFile(byte[] runDataFile) {
        this.instrumentDataFile = runDataFile;
    }

    public String getCyclerDatafileName() {
        return instrumentDatafileName;
    }

    public void setCyclerDatafileName(String cyclerDatafileName) {
        this.instrumentDatafileName = cyclerDatafileName;
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

}
