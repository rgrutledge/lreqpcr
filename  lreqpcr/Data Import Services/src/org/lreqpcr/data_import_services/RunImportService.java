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

import org.openide.util.Lookup;

/**
 * Data import service using data generated from a run. This is divided into
 * two steps: 1. generating a generic RunImportData object that holds all of the
 * run information and 2. initialization of the profiles within the run via
 * the Run Initialization Service. 
 *
 * @author Bob Rutledge
 */
public abstract class RunImportService {

    /**
     * 
     * A service provider needs only to generate an RunImportData instance
     * via implementing the constructRunImportData method.
     * Run initialization and data storage is conducted via the
     * Run Initialization service.
     */
    public RunImportService() {
        importRun();
    }

    /**
     * Called from the constructor to start the data import
     */
    public final void importRun() {
        importRun(constructRunImportData());
    }

    /**
     * Constructs the RunImportData object
     * 
     * @return the Run data ready for importation
     */
    public abstract RunImportData constructRunImportData();

    /**
     * Use the RunInitializationService to initialize the Run data. 
     *
     * @param importData the run import data
     */
    public void importRun(RunImportData importData) {
        RunInitializationService initRunService = Lookup.getDefault().lookup(RunInitializationService.class);
        initRunService.intializeRun(importData);
    }
}
