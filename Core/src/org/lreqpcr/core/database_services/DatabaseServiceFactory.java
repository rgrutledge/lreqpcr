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

/**
 * A factory for database provider of a specified type. Not
 * only does this allow the generation of database providers for multiple
 * types of databases, but also allows opening of multiple database files
 * for any single type.
 */
public interface DatabaseServiceFactory {

    /**
     * Provides a customized DatabaseService based on the Database type
     *
     * @param dbType the type of database
     * @return database service generated for the specified type
     */
    DatabaseServices createDatabaseService(DatabaseType dbType);

}
