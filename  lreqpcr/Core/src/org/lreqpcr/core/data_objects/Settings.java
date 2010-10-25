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

import java.io.File;

/**
 * Stores program settings
 *
 * @author Bob Rutledge
 */
public class Settings {

    private String lastExperimentDirectory;
    private String lastExperimentDbFile;
    private String currentExperimentDbFile;
    private String lastAmpliconDbFile;
    private String currentAmpliconDbFile;
    private String lastAmpliconDirectory;
    private String lastCalbnDirectory;
    private String lastCalbnDbFile;
    private String currentCalbnDbFile;
    private String lastDataImportDirectory;
    private String lastCyclerDataImportDirectory;
    private String lastCyclerDataFile;

    public Settings() {
    }

    public String getLastExperimentDatabaseDirectory() {
        return lastExperimentDirectory;
    }

    public void setLastExperimentDatabaseDirectory(String lastDirectory) {
        this.lastExperimentDirectory = lastDirectory;
    }

    public File getLastExperimentDatabaseFile() {
        if(lastExperimentDbFile != null){
            return new File(lastExperimentDbFile);
        }else {
            return null;
        }        
    }

    public void setLastExperimentDatabaseFile(File lastDatabaseFile) {
        this.lastExperimentDbFile = lastDatabaseFile.getAbsolutePath();
    }

    public File getLastAmpliconDatabaseFile() {
        if(lastAmpliconDbFile != null){
            return new File(lastAmpliconDbFile);
        }else {
            return null;
        }
    }

    public void setLastAmpliconDatabaseFile(File lastAmpliconDbFile) {
        this.lastAmpliconDbFile = lastAmpliconDbFile.getAbsolutePath();
    }

    public String getLastAmpliconDatabaseDirectory() {
        return lastAmpliconDirectory;
    }

    public void setLastAmpliconDatabaseDirectory(String lastAmpliconDirectory) {
        this.lastAmpliconDirectory = lastAmpliconDirectory;
    }

    public void setLastCalibrationDatabaseDirectory(String directory){
        lastCalbnDirectory = directory;
    }

    public String getLastCalibrationDatabaseDirectory(){
        return lastCalbnDirectory;
    }

    public void setLastCalibrationDatabaseFile(File file){
        lastCalbnDbFile = file.getAbsolutePath();
    }

    public File getLastCalibrationDatabaseFile(){
        if(lastCalbnDbFile != null){
            return new File(lastCalbnDbFile);
        }else {
            return null;
        }
    }

    public String getLastDataImportDirectory() {
        return lastDataImportDirectory;
    }

    public void setLastDataImportDirectory(String directory) {
        lastDataImportDirectory = directory;
    }

    public File getLastCyclerDataFile() {
       if(lastCyclerDataFile != null){
            return new File(lastCyclerDataFile);
        }else {
            return null;
        }
    }

    public void setLastCyclerDataFile(File file) {
        this.lastCyclerDataFile = file.getAbsolutePath();
    }

    public String getLastCyclerDataImportDirectory() {
        return lastCyclerDataImportDirectory;
    }

    public void setLastCyclerDataImportDirectory(String lastCyclerDataImportDirectory) {
        this.lastCyclerDataImportDirectory = lastCyclerDataImportDirectory;
    }

    public File getCurrentAmpliconDbFile() {
        if(currentAmpliconDbFile != null){
            return new File(currentAmpliconDbFile);
        }else {
            return null;
        }
    }

    public void setCurrentAmpliconDbFile(File file) {
        currentAmpliconDbFile = file.getAbsolutePath();
    }

    public File getCurrentCalbnDbFile() {
        if(currentCalbnDbFile != null){
            return new File(currentCalbnDbFile);
        }else {
            return null;
        }
    }

    public void setCurrentCalbnDbFile(File file) {
        currentCalbnDbFile = file.getAbsolutePath();
    }

    public File getCurrentExperimentDbFile() {
        if(currentExperimentDbFile != null){
            return new File(currentExperimentDbFile);
        }else {
            return null;
        }
    }

    public void setCurrentExperimentDbFile(File file) {
        currentExperimentDbFile = file.getAbsolutePath();
    }

}
