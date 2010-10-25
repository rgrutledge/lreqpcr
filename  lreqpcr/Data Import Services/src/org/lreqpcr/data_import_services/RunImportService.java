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

package org.lreqpcr.data_import_services;

import org.openide.util.Lookup;

/**
 * A service provider needs only to generate an ImportData instance
 * via implementing the importRunData method.
 * Run initialization and data storage is conducted via the 
 * Run Initialization service which is called within the constructor.
 *
 * @author Bob Rutledge
 */
public abstract class RunImportService {

    public RunImportService() {
        // TODO fix this terrible methodology
        importRun();
    }

    /**
     * 
     */
    private void importRun(){
        initializeRun(importRunData());
    }
    
    /**
     * 
     * 
     * @return the Run data ready for initialization
     */
    public abstract ImportData importRunData();

     /**
     * Use the RunInitializationService to initialize the Run data. 
     *
     * @param importData the import data
     */
    private void initializeRun(ImportData importData){
        RunInitializationService initRunService = Lookup.getDefault().lookup(RunInitializationService.class);
        initRunService.intializeRun(importData);
    }

   /**
    * Provides the name to be used to identify the type of import service
    * @return the name of the import service
    */ public abstract String getRunImportServiceName();
}
