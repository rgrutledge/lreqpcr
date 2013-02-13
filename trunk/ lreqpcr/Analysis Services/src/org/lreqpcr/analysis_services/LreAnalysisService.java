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
import org.lreqpcr.core.database_services.DatabaseServices;

/**
 * Initialize all fields within the provided Profile and the
 * ProfileSummary that encapsulates it.
 *
 * @author Bob Rutledge
 */
public abstract class LreAnalysisService {

    /**
     * Provides all the functions necessary for automated LRE window selection,
     * including the ability to initialize new Profiles.
     * This involves baseline subtraction for new Profiles and
     * LRE window selection using the LreWindowSelectionParameters. Note that
     * the caller must take responsibility for saving the changes to the Profile.
     *
     * @param profile the Profile to initialize
     * @param parameters the LRE window parameters which cannot be null
     * @return returns true if an LRE window was found or false if automated LRE window selection failed
     */
    public abstract boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters);
    
    /**
     * Constructs a ProfileSummary for the supplied Profile. This will also update 
     * all of the quantitative parameters such as the average Fo, so this function 
     * can be used to update the profile, such as after fixing Emax to 100%. Note 
     * that ProfileSummary is also used to view and edit a Profiles.
     *
     * Note also that the averageFo is recalculated and that it is upto the calling
     * function to save the changes to the corresponding database.
     * 
     * @param profile the profile to be viewed
     * @param parameters LRE window selection parameters
     * @return the initialized ProfileSummary
     */
    public abstract ProfileSummary initializeProfileSummary(Profile profile, LreWindowSelectionParameters parameters);

    /**
     * Provides the necessary functions to update the ProfileSummary when the user
     * manually changes the LRE window.
     * The boolean denotes whether the update was successful.
     *
     * @param profileSummary the ProfileSummary to update
     * @param parameters LRE window parameters
     * @return returns true if the ProfileSummary has been updated, false if not
     */
    public abstract boolean updateLreWindow(ProfileSummary profileSummary);

    /**
     * Primarily for back compatability, provides all the needed actions to
     * update a preexisting Profile to a new version.
     *
     * @param db the database to be converted
     * @return true if the update was successful
     */
    public abstract void convertDatabaseToNewVersion(DatabaseServices db);
}
