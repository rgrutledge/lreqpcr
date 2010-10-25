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
package org.lreqpcr.core.database_services;

import java.io.File;

/**
 * Storage and retrieval of various program settings,
 * primarily locations of the last opened databases. 
 *
 * @author Bob Rutledge
 */
public interface SettingsServices {

    public abstract String getLastExperimentDatabaseDirectory();
    public abstract void setLastExperimentDatabaseDirectory(String directory);

    public abstract File getLastExperimentDatabaseFile();
    public abstract void setLastExperimentDatabaseFile(File experimentDbFile);

    public abstract File getLastAmpliconDatabaseFile();
    public abstract void setLastAmpliconDatabaseFile(File lastAmpliconDbFile);

    public abstract String getLastAmpliconDatabaseDirectory();
    public abstract void setLastAmpliconDatabaseDirectory(String directory);

    public abstract void setLastCalibrationDatabaseDirectory(String directory);
    public abstract String getLastCalibrationDatabaseDirectory();

    public abstract void setLastCalibrationDatabaseFile(File file);
    public abstract File getLastCalibrationDatabaseFile();

    public abstract void setLastDataImportDirectory(String directory);
    public abstract String getLastDataImportDirectory();

    public abstract void setLastCyclerDataImportDirectory(String directory);
    public abstract String getLastCyclerDataImportDirectory();
}
