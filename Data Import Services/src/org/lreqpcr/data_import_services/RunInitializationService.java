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

/**
 * Uses a RunImportData object to initialize the the Profiles within the run
 * and stores the data to the appropriate databases.
 * This is provided as a Service in order to allow changes to how the databases
 * are handled (e.g. having multiple database files open at one time).
 *
 * @author Bob Rutledge
 */
public interface RunInitializationService {

    /**
     * Initialize a run using the data provided in the RunImportData object.
     * @param importData the RunImportData object containing the run data
     */
    public void intializeRun(RunImportData importData);

}
