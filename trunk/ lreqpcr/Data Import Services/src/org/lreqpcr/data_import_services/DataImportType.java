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
 *
 * @author Bob Rutledge
 */
public enum DataImportType {

    /**
     * Standard data import that potentially requires an Experiment database (for storing SampleProfiles), 
     * a Calibration database (for storing CalibrationProfiles) and Amplicon database (for retrieving Amplicon sizes).
     */
    STANDARD,
    
    /**
     * Manual SampleProfile import via an Excel template which only requires
     * an Experiment database in that as Amplicon size is supplied in the template and
     * contain only SampleProfiles.
     */
    MANUAL_SAMPLE_PROFILE,
    
    /**
     * Manual CalibrationProfile import via an Excel template which only requires
     * a Calibration database in that as Amplicon size is supplied in the template and
     * contain only CalibrationProfiles.
     */
    MANUAL_CALIBRATION_PROFILE
}
