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
package org.lreqpcr.core.database_services;

import java.io.File;

/**
 * Storage and retrieval of various program settings,
 * primarily locations of the last opened database files and directories.
 */
public interface SettingsServices {

    String getLastExperimentDatabaseDirectory();

    void setLastExperimentDatabaseDirectory(String directory);

    File getLastExperimentDatabaseFile();

    void setLastExperimentDatabaseFile(File experimentDbFile);

    File getLastAmpliconDatabaseFile();

    void setLastAmpliconDatabaseFile(File lastAmpliconDbFile);

    String getLastAmpliconDatabaseDirectory();

    void setLastAmpliconDatabaseDirectory(String directory);

    void setLastCalibrationDatabaseDirectory(String directory);

    String getLastCalibrationDatabaseDirectory();

    void setLastCalibrationDatabaseFile(File file);

    File getLastCalibrationDatabaseFile();

    void setLastDataImportDirectory(String directory);

    String getLastDataImportDirectory();

    void setLastCyclerDataImportDirectory(String directory);

    String getLastCyclerDataImportDirectory();
}
