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
import org.lreqpcr.core.data_objects.Profile;

/**
 * Conducts LRE analysis on a supplied Profile.
 *
 * @author Bob Rutledge
 */
public abstract interface LreAnalysisService {

    /**
     * Provides all the functions necessary for automated LRE window selection.
     * <p>
     * This involves baseline subtraction and automated 
     * LRE window selection using the LreWindowSelectionParameters, along with 
     * setting values within the Profile for all of the parameters associated with the associated LRE analysis. 
     * This should include nonlinear regression analysis if this function if available.
     * ,<p>
     * Note that the caller must take responsibility for saving changes to the Profile.
     *
     * @param profile the Profile to initialize
     * @param parameters the LRE window parameters which cannot be null
     * @return returns true if an LRE window was found or false if automated LRE window selection failed
     */
    public abstract boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters);
    
    /**
     * Updates the supplied Profile following changes to the LRE window. This 
     * should include nonlinear regression analysis if this function is available. 
     * 
     * @param profile the profile to be updated 
     */ 
    public abstract void updateProfile(Profile profile);
}
