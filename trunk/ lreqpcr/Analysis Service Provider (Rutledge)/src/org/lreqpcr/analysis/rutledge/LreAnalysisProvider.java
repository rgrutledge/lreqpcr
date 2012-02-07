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
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 *
 *
 * @author Bob Rutledge
 */
public class LreAnalysisProvider extends LreAnalysisService {

    public LreAnalysisProvider() {
    }

    @Override
    public boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters) {
        profile.setHasAnLreWindowBeenFound(false);
        //Construct a ProfileSummary which is used for automated LRE window selection 
        //Subtract background fluorescence
        if (profile.getFcReadings() == null){
            //A new profile that requires baseline substraction
            BaselineSubtraction.baselineSubtraction(profile);
        }
        ProfileSummaryImp prfSum = new ProfileSummaryImp(profile);
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        //Try to find an LRE window
        LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is not relevant
            return false;
        }
        ProfileInitializer.calcLreParameters(prfSum);
        return true;
    }

    public ProfileSummary initializeProfileSummary(Profile profile, LreWindowSelectionParameters parameters) {
        ProfileSummaryImp prfSum = new ProfileSummaryImp(profile);
        //Initialize the linked Cycle list
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        //Set the start cycle within the linked Cycle list
        Cycle runner = prfSum.getZeroCycle();
        //Without an LRE window, nothing can be calculated
        if (profile.hasAnLreWindowBeenFound()) {
            //Run to the start cycle
            for (int i = 0; i < profile.getStrCycleInt(); i++) {
                runner = runner.getNextCycle();
            }
            prfSum.setStrCycle(runner);
            //Calculate the cycle parameters for Cycle list
            ProfileInitializer.calcAllFo(prfSum);
            ProfileInitializer.calcAverageFo(prfSum);
            ProfileInitializer.calcAllpFc(prfSum);
        }
        return prfSum;
    }

    public boolean updateLreWindow(ProfileSummary prfSum) {
        //All that is needed is to update the LRE parameters
        ProfileInitializer.calcLreParameters(prfSum);
        return true;
    }

    /**
     * Converts the profile to version 0.8.0. Note it is the responsibility
     * of the calling function to save the  Profiles into the corresponding database.
     * 
     * @param profile
     * @param parameters
     * @return
     */
    @Override
    public boolean convertProfileToNewVersion(Profile profile) {
        profile.isProfileVer0_8_0(true);
        //See if an LRE window is present
        if (profile.getLreWinSize() > 2) {
            //This should preserve any user modifications to the LRE window
            profile.setHasAnLreWindowBeenFound(true);
        }
//For AverageSampleProfiles it must determined if replicate average No <10
        if (profile instanceof AverageSampleProfile) {
            AverageSampleProfile avProfile = (AverageSampleProfile) profile;
            avProfile.updateProfile();//This is all that is needed
        }
        return true;
    }
}
