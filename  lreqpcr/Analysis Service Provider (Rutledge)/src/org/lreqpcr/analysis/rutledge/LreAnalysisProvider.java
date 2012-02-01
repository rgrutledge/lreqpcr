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

import java.util.List;
import org.lreqpcr.core.data_processing.BaselineSubtraction;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
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

    public ProfileSummary initializeProfile(Profile profile, LreWindowSelectionParameters parameters) {
        if (profile.getFcReadings() == null) {//Background fluorescence has NOT been subtracted
            BaselineSubtraction.baselineSubtraction(profile);
        }
        ProfileSummaryImp prfSum = new ProfileSummaryImp(profile);
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        if (!profile.hasAnLreWindowBeenFound()) {
            //Try to find an LRE window...this is necessary to cover situations 
            //when the LRE window was lost e.g. by incorrect minFc settings.
            LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
            if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is not relevant
                return prfSum;
            }
        }
        //Recalculate the LRE parameters
        ProfileInitializer.calcLreParameters(prfSum);
        return prfSum;
    }

    public boolean updateLreWindow(ProfileSummary prfSum) {
        //All that is needed is to update the LRE parameters
        ProfileInitializer.calcLreParameters(prfSum);
        return true;
    }

    /**
     * Converts to the profile 0.8.0. Note also that if the Profile is an 
     * AverageSampleProfile, the replicate profiles will also be updated. 
     * However, it is the responsibility of the calling function to save the 
     * Profiles into the corresponding database. 
     * 
     * @param profile
     * @param parameters
     * @return
     */
    @Override
    public boolean convertProfileToNewVersion(Profile profile, LreWindowSelectionParameters parameters) {
        //See if an LRE window is present
        if (profile.getLreWinSize() > 2) {
            //This should preserve any user modifications to the LRE window
            profile.setHasAnLreWindowBeenFound(true);
        }
        //For AverageSampleProfiles it must determined if replicate average No <10
        //This is not necessary for AverageCalibrationProfiles

        //See if an OCF has been applied or of this is a calibration profile
        if (profile.getOCF() <= 0 || profile instanceof CalibrationProfile) {
            //Number of molecules not available so abort
            return true;
        }
        if (profile instanceof AverageSampleProfile) {
            AverageSampleProfile avProfile = (AverageSampleProfile) profile;
            List<? extends Profile> replicateProfileList = avProfile.getReplicateProfileList();
            for (Profile repProfile : replicateProfileList) {
                if(repProfile.getLreWinSize() >2){
                    repProfile.setHasAnLreWindowBeenFound(true);
                }else{
                    repProfile.setNo(0);
                }
            }
            avProfile.updateProfile();
            if (avProfile.isReplicateAverageNoLessThan10Molecules()){
                avProfile.setHasAnLreWindowBeenFound(false);
            }
        }
        return true;
    }
}
