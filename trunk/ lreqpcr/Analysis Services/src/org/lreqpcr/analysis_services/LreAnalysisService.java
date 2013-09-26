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
     * Provides all the functions necessary for automated LRE window selection 
     * for initializing new Profiles.
     * <p>
     * This involves baseline subtraction and automated 
     * LRE window selection using the LreWindowSelectionParameters, along with 
     * setting values within the Profile for all of the parameters associated with LRE analysis. 
     * This should include nonlinear regression analysis if this function if available.
     * ,<p>Note that
     * the caller must take responsibility for saving changes to the Profile.
     *
     * @param profile the Profile to initialize
     * @param parameters the LRE window parameters which cannot be null
     * @return returns true if an LRE window was found or false if automated LRE window selection failed
     */
    public abstract boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters);

    /**
     * Conducts nonlinear regression (AKA curve fitting) on the raw Fc dataset,  
     * constructs a new Fc working dataset, followed by updating the LRE analysis
     * within the Profile.
     * <p>
     * Nonlinear regression analysis determines values 
     * for 5 LRE parameters: Fb (fluorescence background), Fb-slope 
     * (baseline slope), Emax, Fmax and Fo. 
     * <p>
     * The primary objective is to derive 
     * values for Fb and Fb-slope that are then used to generate an optimized 
     * working Fc dataset for LRE analysis. Note that to maintain accurate 
     * curving fitting, the raw Fc dataset must be trimmed to exclude aberrant 
     * early cycles (generally cycles 1-3), in addition to cycles within the plateau phase 
     * in order to minimize the impact of aberrant amplification kinetics. 
     * <p>
     * As such, the regression-derived Emax, Fmax and Fo should only used to determine 
     * the level of convergence with LRE analysis (based on linear regression 
     * analysis of an Ec vs Fc plot), followed by averaging the cycle Fo values 
     * within the LRE window for determining target quantity.
     * <p>
     * For high quality profiles the convergence of nonlinear regression and 
     * LRE analysis have been found to be high. 
     * 
     * @param profile
     * @return indicates whether the curve fitting was successful
     */
    public abstract boolean conductNonlinearRegressionAnalysis(Profile profile);


}
