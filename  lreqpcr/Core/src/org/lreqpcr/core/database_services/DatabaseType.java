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
 * Used to specify the type of database.
 * @author Bob Rutledge
 */
public enum DatabaseType {

    /**
     * An experiment database that holds a group of related Runs
     */
    EXPERIMENT,

    /**
     * A calibration database which holds calibration profiles
     */
    CALIBRATION,

    /**
     * A amplicon database that holds amplicon information
     */
    AMPLICON,

    /**
     * A settings database that is used to store various types of program information,
     * such as the last accessed directory or database file.
     */
    SETTINGS
}
