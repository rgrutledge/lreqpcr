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

import org.lreqpcr.core.database_services.DatabaseServiceFactory;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;

/**
 *
 *
 * @author Bob Rutledge
 */
public class DatabaseServiceProvider implements DatabaseServiceFactory {

    DatabaseServices service;

    public DatabaseServices createDatabaseService(DatabaseType dbType) {
        switch (dbType) {
            case EXPERIMENT: service = createExptDbService(); break;
            case CALIBRATION: service = createCalbnDbService(); break;
            case AMPLICON: service = createAmpDbService(); break;
            default: service = null; break;
        }
        return service;
    }

    private DatabaseServices createExptDbService() {
        return new ExperimentDb4oDatabaseServiceProvider();
    }

    private DatabaseServices createCalbnDbService() {
        return new CalibrationDb4oServiceProvider();
    }

    private DatabaseServices createAmpDbService() {
        return new AmpliconDb4oServiceProvider();
    }

}
