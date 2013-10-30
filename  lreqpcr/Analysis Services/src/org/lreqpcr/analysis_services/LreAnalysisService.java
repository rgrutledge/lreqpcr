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

package org.lreqpcr.analysis_services;

import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 * Conducts LRE analysis on a supplied Profile.
 *
 * @author Bob Rutledge
 */
public abstract interface LreAnalysisService {

    /**
     * Provides all the functions necessary for automated LRE window selection.
     * <p>
     * This involves baseline subtraction using the average Fc of early cycles,
     * followed by automated LRE window selection using the 
     * LreWindowSelectionParameters, or default values if none is provided. 
     * ,<p>
     * Note that this function also saves the modified to Profile to the 
     * database from which it is derived via ProfileSummary.update.
     *
     * @param prfSum the ProfileSummary encapsulating the Profile
     * @param parameters the LRE window selection parameters
     * @return 
     */
    public abstract boolean lreWindowSelection(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
    
    /**
     * Provides all the functions necessary for automated LRE window selection 
     * using using nonlinear regression-derived Fb and Fb-slope. 
     * LRE window selection using the LreWindowSelectionParameters, or default 
     * values if none is provided. 
     * <p>
     * Note that this function also saves the modified to Profile to the 
     * database from which it is derived via ProfileSummary.update.
     * 
     * @param prfSum the ProfileSummary encapsulating the Profile to be initialized
     * @param parameters the LRE window selection parameters or null if default values are to be used
     * @return returns true if an LRE window was found or false if window selection failed
     */
    public abstract boolean lreWindowSelectionUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
}
