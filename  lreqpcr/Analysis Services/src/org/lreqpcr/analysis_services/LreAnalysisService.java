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
 * Initialize all fields within the provided Profile and the
 * ProfileSummary that encapsulates it.
 *
 * @author Bob Rutledge
 */
public abstract class LreAnalysisService {

    /**
     * Provides all the functions necessary initialize both
     * the supplied Profile and to encapsulate within a ProfileSummary.
     * This primarily involves baseline subtraction and automated
     * LRE window selection. Note that these
     * methods do not save the modifications made to the Profile.
     * 
     * @param profile the Profile to initialize
     * @return a fully initialized ProfileSummary
     */
    public abstract ProfileSummary initializeProfile(Profile profile);

    /**
     * Profile initialization using the user defined parameters provided by
     * the LreWindowSelectionParameters object for automated LRE window selection.
     *
     * @param profile the Profile to initialize
     * @param parameters the LRE window parameters which cannot be null
     * @return fully initialized ProfileSummary
     */
    public abstract ProfileSummary initializeProfile(Profile profile, LreWindowSelectionParameters parameters);

    /**
     * Provides the necessary functions to update the LRE window,
     * along with recalculation of all of the associated LRE parameters.
     * The boolean denotes whether the update was successful.
     * @param profileSummary the ProfileSummary to update
     * @param parameters LRE window parameters
     * @return returns true if the ProfileSummary has been updated, false if not
     */
    public abstract boolean updateLreWindow(ProfileSummary profileSummary, LreWindowSelectionParameters parameters);
}
