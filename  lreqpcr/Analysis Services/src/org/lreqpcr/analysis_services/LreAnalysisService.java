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
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 * Conducts LRE analysis on a supplied Profile
 * ProfileSummary that encapsulates it.
 *
 * @author Bob Rutledge
 */
public abstract interface LreAnalysisService {

    /**
     * Provides all the functions necessary for automated LRE window selection.
     * This involves baseline subtraction for new Profiles and
     * LRE window selection using the LreWindowSelectionParameters, along with 
     * setting values for all of the parameters associated with LRE analysis. Note that
     * the caller must take responsibility for saving changes to the Profile.
     *
     * @param profile the Profile to initialize
     * @param parameters the LRE window parameters which cannot be null
     * @return returns true if an LRE window was found or false if automated LRE window selection failed
     */
    public abstract boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters);

    /**
     * Conducts nonlinear regression (AKA curve fitting) on the raw Fc dataset to determine values 
     * for 5 parameters: Fb (fluorescence background), Fb-slope 
     * (baseline slope), Emax, Fmax and Fo. The primary objective is to derive 
     * values for Fb and Fb-slope that are then used to generate an optimized 
     * working Fc dataset for LRE analysis. Note that to maintain accurate 
     * curving fitting, the raw Fc dataset must be trimmed to exclude aberrant 
     * early cycles (generally cycles 1-3) and cycles within the plateau phase 
     * in order to minimize the impact of aberrant amplification kinetics. 
     * As such, the regression-derived Emax, Fmax and Fo are only used to determine 
     * the level of convergence with LRE analysis. 
     * 
     * @param profile
     * @return indicates whether the curve fitting was successful
     */
    public abstract boolean conductNonlinearRegressionAnalysis(Profile profile);


}
