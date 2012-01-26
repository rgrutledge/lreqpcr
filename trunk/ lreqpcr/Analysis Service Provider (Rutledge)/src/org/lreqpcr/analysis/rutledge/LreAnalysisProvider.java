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
package org.lreqpcr.analysis.rutledge;

import org.lreqpcr.core.data_processing.BaselineSubtraction;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 *
 *
 * @author Bob Rutledge
 */
public class LreAnalysisProvider extends LreAnalysisService {

    public LreAnalysisProvider() {
    }

    /**
     * Initializes the supplied Profile and constructs a ProfileSummary 
     * which is used to display and edit the Profile.  
     * 
     * @param profile the profile to process
     * @param parameters the LRE window selection parameters
     * @return a ProfileSummary holding the profile
     */
    public ProfileSummary initializeProfile(Profile profile, LreWindowSelectionParameters parameters) {
        if (profile.getFcReadings() == null) {//Background fluorescence has NOT been subtracted
            BaselineSubtraction.baselineSubtraction(profile);
        }
        ProfileSummaryImp prfSum = new ProfileSummaryImp(profile);
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        //Set the startCycle
        if (!profile.hasAnLreWindowBeenFound()) {//Signifies new window is needed
            LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
            if (!profile.hasAnLreWindowBeenFound()) {//Is a bad profile, e.g. is flat
                return prfSum;
            }
        }
        ProfileInitializer.calcLreParameters(prfSum);
        return prfSum;
    }

    /**
     * Updates the supplied Profile based on a manual adjustment to the LRE window.
     *
     * @param prfSum the ProfileSummary holding the Profile
     * @return true for a full reanalyze or false for a simple update
     */
    public boolean updateLreWindow(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        if (!prfSum.getProfile().hasAnLreWindowBeenFound()) {
            //Full reanalysis requested
            LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
            return true;
        } //Only an update required
            ProfileInitializer.calcLreParameters(prfSum);
            return false;
    }
}
