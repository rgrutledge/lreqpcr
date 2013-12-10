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
package org.lreqpcr.analysis.rutledge;

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rutledge implementation of LRE window selection and optimization.
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = LreAnalysisService.class)
public class LreAnalysisProvider implements LreAnalysisService {

    private Profile profile;
    private ProfileSummary prfSum;
    private LreWindowSelectionParameters parameters;
    private NonlinearRegressionImplementation nrAnalysis = new NonlinearRegressionImplementation();

    public LreAnalysisProvider() {
    }

    /**
     * Provides automated LRE window selection 
     * using nonlinear regression-derived Fb and Fb-slope to generate an optimized working Fc dataset. 
     * If a LRE window has not been found, the Profile is reinitialized before 
     * attempting nonlinear regression analysis.
     * <p>
     * LRE window selection is based on the LreWindowSelectionParameters. 
     * Note also that this function saves the modified Profile via ProfileSummary.update().
     *
     * @param profile
     * @param parameters LRE window selection parameters
     * @return true if an LRE window was found or false if window selection failed
     */
    public boolean lreWindowInitialization(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        this.prfSum = prfSum;
        this.parameters = parameters;
        this.profile = prfSum.getProfile();
        //Reset the Profile to remove any previous LRE or NR analysis derived values
        profile.setLreVariablesToZero();
        //Be sure that the working Fc dataset is derived using an average Fb 
        LreWindowSelector.substractBackgroundUsingAvFc(profile);
        prfSum.update();
        //Selecting a start cycle also removes any previously determined LRE parameters
        if (parameters.getMinFc() == 0) {
            //No user selected minimum Fc, so need to scan the profile for a  LRE window
            LreWindowSelector.selectLreStartCycleViaScanning(prfSum);
        } else {//A user-selected minFc has be set, so use it
            LreWindowSelector.selectLreStartCycleUsingMinFc(prfSum, parameters.getMinFc());
        }
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is irrelevant
            return false;
        }
        //Attempt to expand the upper limit of the LRE window
        LreWindowSelector.expandLreWindowWithoutNR(prfSum, parameters.getFoThreshold());
        return true;
    }

    /**
     * Expands the upper boundary of the LRE window. This is based upon the
     * difference between the average Fo determined from the cycles within the
     * LRE window and the Fo value derived from the first cycle immediately
     * above the LRE window. If this difference is smaller than the Fo
     * threshold, this next cycle is added to the LRE window, and the analysis
     * repeated.
     * <p>
     * Note that the upper limit of this expansion is limited to the cycle Fc
     * less than 95% of Fmax, eliminating the possibility of including plateau
     * phase cycles into the LRE window.
     * <p>
     * Note also that nonlinear regression is used to determine Fb and Fb-slope,
     * from which a new working Fc dataset is derived, a process that is
     * repeated following each increase in the LRE window size.
     *
     *
     *
     */
    public boolean optimizeLreWindowUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        profile = prfSum.getProfile();
        this.parameters = parameters;
        //Determine if a valid LRE window has been established
        if (!profile.hasAnLreWindowBeenFound()) {
            //Try to reinitialize the LRE window
            lreWindowInitialization(prfSum, parameters);
            if (!profile.hasAnLreWindowBeenFound()) {
                //If a window has not been found, abort
                return false;
            }
        }
        //Attempt to optimize the window using nonlinear regression
        LreWindowSelector.optimizeLreWindowUsingNR(prfSum, parameters.getFoThreshold());
        return prfSum.getProfile().didNonlinearRegressionSucceed();
    }

    /**
     * Note that the working Fc dataset must not be modified. 
     * 
     * @param prfSum
     * @param parameters
     * @return 
     */
    public boolean lreWindowSelectionUpdate(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        //A valid LRE window must be present
        if (!prfSum.getProfile().hasAnLreWindowBeenFound()) {
            return false;
        }
        //Determine if NR has been applied for back compatability
        
        //Determine if a new start cycle is required, else use the exsisting default start cycle
        if (parameters.getMinFc() != 0) {
 //Resets the window start cycle based on minFc and also resets the window size to 3 cycles
            LreWindowSelector.selectLreStartCycleUsingMinFc(prfSum, parameters.getMinFc());
        }//Else, it is assumed that the exsisting start cycle will be used
//Attempt to expand the LRE window using the exsisting working Fc dataset and without NR
        LreWindowSelector.expandLreWindowWithoutNR(prfSum, parameters.getFoThreshold());
        return prfSum.getProfile().hasAnLreWindowBeenFound();
    }
    
    public boolean lreWindowUpdateUsingNR(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        nrAnalysis.lreWindowUpdateUsingNR(prfSum);
        return prfSum.getProfile().didNonlinearRegressionSucceed();
    }
}
