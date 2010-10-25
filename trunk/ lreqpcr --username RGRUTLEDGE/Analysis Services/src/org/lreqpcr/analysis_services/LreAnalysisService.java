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

package org.lreqpcr.analysis_services;

import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 * Intialize all fields within the provided Profile and within the
 * ProfileSummary that encapsulates the Profile. 
 *
 * @author Bob Rutledge
 */
public abstract class LreAnalysisService {

    /**
     * Provides all the functions necessary for full initialization of both
     * the provided Profile and the encapsulating ProfileSummary. Central to
     * this capability is automating LRE window selection.
     * 
     * @param profile the Profile to initialize
     * @return fully intialized ProfileSummary
     */
    public abstract ProfileSummary initializeProfile(Profile profile);

    /**
     * This allows manual adjustment of the two parameters used in automated
     * LRE window selection: the minimal fluorescence used for start cycle selection
     * and the Fo threshold, which is the maximum fractional difference (i.e. percent
     * difference) in a cycle's Fo vs. the average Fo derived from the cycles within
     * the LRE window, to allow the cycle to be included into the LRE window.
     *
     * @param profile the Profile to initialize
     * @param parameters the LRE window parameters which cannot be null
     * @return fully intialized ProfileSummary
     */
    public abstract ProfileSummary initializeProfile(Profile profile, LreWindowSelectionParameters parameters);

    /**
     * Provides the necessary functions to update the LRE window,
     * along with recalculation of all of the associated LRE parameters.
     * Note that this method does not save the modified Profile to the
     * database holding the Profile.
     *
     * @param profileSummary the ProfileSummary to update
     * @param parameters LRE window parameters
     * @return returns true if the ProfileSummary has been updated, false if not
     */
    // TODO is it appropriate to have the service save the changes???
    public abstract boolean updateLreWindow(ProfileSummary profileSummary, LreWindowSelectionParameters parameters);
}
