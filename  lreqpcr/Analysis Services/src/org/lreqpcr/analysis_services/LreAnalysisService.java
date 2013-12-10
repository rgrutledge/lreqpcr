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
 * Identifies and optimizes LRE window selection for the Profile encapsulated within 
 * the supplied ProfileSummary. 
 * <p>
 * 
 *
 * @author Bob Rutledge
 */
public abstract interface LreAnalysisService {

    /**
     * LRE window selection and expansion using a working Fc dataset generated 
     * from an average Fb derived from the Fc readings within early cycles. 
     * 
     * Note that this entails removing any 
     * previous LRE analysis, so that the Profile can be reinitialized 
     * and automated LRE window selection conducted using the 
     * LreWindowSelectionParameters. 
     * ,<p>
     * Note also that this function must also save the modified to Profile to the 
     * corresponding database, e.g. via ProfileSummary.update().
     *
     * @param prfSum the ProfileSummary encapsulating the Profile
     * @param parameters the LRE window selection parameters or null if default values are to be used
     * @return true if an LRE window was found or false if window selection failed
     */
    public abstract boolean lreWindowInitialization(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
    
    /**
     * Provides automated LRE window by set its size to a default 3 cycles and 
     * expanding it by attempting to add cycles to the top of the window. This involves  
     * using nonlinear regression-derived Fb and Fb-slope to generate an 
     * optimized working Fc dataset after each cycle is added to the top of the window. 
     * If a LRE window has not been found, the LRE window is reinitialized before 
     * attempting nonlinear regression analysis; otherwise the existing start cycle is used. 
     * <p>
     * LRE window expansion adds cycles to the top of the window, based on the 
     * LreWindowSelectionParameters. Note also that this function must save the 
     * modified Profile to the corresponding database, e.g. via ProfileSummary.update().
     * 
     * @param prfSum the ProfileSummary encapsulating the Profile to be initialized
     * @param parameters the LRE window selection parameters or null if default values are to be used
     * @return true if an LRE window was found or false if window selection failed
     */
    public abstract boolean optimizeLreWindowUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
    
    
    
    //********************************This is invalid*********************************************
    /** 
     * Updates the LRE window based on the assumption that a valid LRE window 
     * has been identified and that modifying the working Fc dataset is unnecessary. 
     * The primary intent is to process changes to the window selection parameters.
     * <p>
     * Note that this function saves the modified to Profile to the 
     * database from which it is derived via ProfileSummary.update().
     * 
     * @param prfSum the ProfileSummary encapsulating the Profile to be updated
     * @param parameters the LRE window selection parameters
     * @return if the update was successful
     */
    public abstract boolean lreWindowSelectionUpdate(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
    
    //*********************************** TOD review if without NR that this is redundant to prfSum.update()******************************
    /**
     * Updates the LRE analysis within the supplied Profile without 
     * modifying the LRE window. The primary intent is to process changes to 
     * the LRE window. If available, nonlinear regression should be applied. 
     * <p>
     * Note that this function must save the modified to Profile to the 
     * corresponding database, e.g. via ProfileSummary.update().
     * 
     * @param prfSum the ProfileSummary encapsulating the Profile to be updated
     * @param parameters the LRE window selection parameters
     * @return whether the LRE window was updated successfully
     */
    public abstract boolean lreWindowUpdateUsingNR(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
}
