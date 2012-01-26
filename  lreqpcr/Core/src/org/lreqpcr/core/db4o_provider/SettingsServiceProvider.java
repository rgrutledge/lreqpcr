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
package org.lreqpcr.core.db4o_provider;

import org.lreqpcr.core.data_objects.Settings;
import java.io.File;
import java.util.List;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;

/**
 * A single Settings database file is used, which is located in the default LRE
 * directory.
 *
 * @author Bob Rutledge
 */
public class SettingsServiceProvider extends Db4oDatabaseServices implements SettingsServices {

    private Settings settings;//The object holding the settings

    @SuppressWarnings(value = "unchecked")
    public SettingsServiceProvider() {
        openDatabaseFile(new File("Settings.lre"));
        List<Settings> list = (List<Settings>) getAllObjects(Settings.class);
        if (list.isEmpty()) {
            settings = new Settings();
            saveObject(settings);
            commitChanges();
        } else {
            settings = list.get(0);
        }
    }

    public String getLastExperimentDatabaseDirectory() {
        return settings.getLastExperimentDatabaseDirectory();
    }

    public void setLastExperimentDatabaseDirectory(String lastDirectory) {
        settings.setLastExperimentDatabaseDirectory(lastDirectory);
        saveObject(settings);
        commitChanges();
    }

    public File getLastExperimentDatabaseFile() {
        return settings.getLastExperimentDatabaseFile();
    }

    public void setLastExperimentDatabaseFile(File settingsDatabaseFile) {
        settings.setLastExperimentDatabaseFile(settingsDatabaseFile);
        saveObject(settings);
        commitChanges();
    }

    public File getLastAmpliconDatabaseFile() {
        return settings.getLastAmpliconDatabaseFile();
    }

    public String getLastAmpliconDatabaseDirectory() {
        return settings.getLastAmpliconDatabaseDirectory();
    }

    public void setLastAmpliconDatabaseFile(File lastAmpliconDbFile) {
        settings.setLastAmpliconDatabaseFile(lastAmpliconDbFile);
        saveObject(settings);
        commitChanges();
    }

    public void setLastAmpliconDatabaseDirectory(String directory) {
        settings.setLastAmpliconDatabaseDirectory(directory);
        saveObject(settings);
        commitChanges();
    }

    public void setLastCalibrationDatabaseDirectory(String directory) {
        settings.setLastCalibrationDatabaseDirectory(directory);
        saveObject(settings);
        commitChanges();
    }

    public String getLastCalibrationDatabaseDirectory() {
        return settings.getLastCalibrationDatabaseDirectory();
    }

    @Override
    public void setLastCalibrationDatabaseFile(File file) {
        if (file != null) {
            settings.setLastCalibrationDatabaseFile(file);
            saveObject(settings);
            commitChanges();
        }
    }

    public File getLastCalibrationDatabaseFile() {
        return settings.getLastCalibrationDatabaseFile();
    }

    public void setLastDataImportDirectory(String directory) {
        settings.setLastDataImportDirectory(directory);
        saveObject(settings);
        commitChanges();
    }

    public String getLastDataImportDirectory() {
        return settings.getLastDataImportDirectory();
    }

    public void setLastCyclerDataImportDirectory(String directory) {
        settings.setLastCyclerDataImportDirectory(directory);
        saveObject(settings);
        commitChanges();
    }

    public String getLastCyclerDataImportDirectory() {
        return settings.getLastCyclerDataImportDirectory();
    }

    /**
     * Not implement.. throws an UnsupportedOperationException event if called
     * @return
     */
    public boolean createNewDatabaseFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return Settings database type
     */
    public DatabaseType getDatabaseType() {
        return DatabaseType.SETTINGS;
    }

    /**
     * Not implement.. throws an UnsupportedOperationException event if called
     *
     */
    public boolean openUserSelectDatabaseFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Not implement.. throws an UnsupportedOperationException event if called
     *
     */
    public boolean openLastDatabaseFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean closeDatabase() {
        return closeDb4oDatabase();
    }
}
