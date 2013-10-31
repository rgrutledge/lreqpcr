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

import java.awt.Toolkit;
import javax.swing.JOptionPane;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Rutledge implementation of LRE window selection and optimization.
 * <p>
 * As of Sept13 this includes nonlinear regression to generate an estimation of
 * baseline fluorescence that replaces the average Fc of cycles 4-9, in addition
 * to an estimation of baseline slope, both of which are used to generate a
 * working Fc dataset used for LRE analysis.
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = LreAnalysisService.class)
public class LreAnalysisProvider implements LreAnalysisService {

    private Profile profile;
    private ProfileSummary prfSum;
//    private Cycle runner;
    private LreWindowSelectionParameters parameters;
    private NonlinearRegressionImplementation nrAnalysis = new NonlinearRegressionImplementation();

    public LreAnalysisProvider() {
    }

    /**
     * This method selects a LRE window using a working Fc dataset derived from 
     * a Fb determined from the average of cycles 4-9. All previous LRE parameters 
     * are also reset to zero. 
     *
     * @param profile
     * @param parameters LRE window selection parameters
     * @return whether a LRE window was found
     */
    public boolean lreWindowInitialization(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        this.prfSum = prfSum;
        this.parameters = parameters;
        this.profile = prfSum.getProfile();
        //Reset the Profile to remove any previous LRE or NR analysis derived values
        profile.setLreVariablesToZero();//*************** TODO review this function and its purpose
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
        LreWindowSelector.optimizeLreWindow(prfSum, parameters.getFoThreshold());
////Use this preliminary LRE window for nonlinear regression analysis 
////This generates an optimized working Fc dataset and updates the LRE parameters
////        conductNonlinearRegressionOptimization();
//        optmzLreWinWithNR();//********************************Not appropriate here************************************
//        if (!testIfRegressionWasSuccessful()) {
//            return profile.hasAnLreWindowBeenFound();
//        }
//        //Repeat optimization of the LRE window but with nonlinear regression 

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
    public boolean lreWindowOptimizationUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        profile = prfSum.getProfile();
        this.parameters = parameters;
        //Determine if a valid LRE window has been established
        if (!profile.hasAnLreWindowBeenFound()) {
            //Need to reinitialize the LRE window
            lreWindowInitialization(prfSum, parameters);
            if (!profile.hasAnLreWindowBeenFound()) {
                //If a window has not been found, abort
                return false;
            }
        }
        //Attempt to optimize the window using nonlinear regression
        LreWindowSelector.optimizeLreWindowUsingNR(prfSum, parameters.getFoThreshold());
        return true;
    }

    /**
     * Note that the LRE window must not be modified. 
     * 
     * @param prfSum
     * @param parameters
     * @return 
     */
    public boolean lreWindowSelectionUpdate(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        //What would invalid a profile? No window is key
        //A valid LRE window must be present
        if (!prfSum.getProfile().hasAnLreWindowBeenFound()) {
            return false;
        }
        //Determine if a new start cycle is required, else use the exsisting default start cycle
        if (parameters.getMinFc() != 0) {
            //Resets the window start cycle based on minFc and also resets the window size to 3 cycles
            LreWindowSelector.selectLreStartCycleUsingMinFc(prfSum, parameters.getMinFc());
        }
//Attempt to expand the LRE window without nonlinear regression, that is, use the exsist working Fc dataset
        LreWindowSelector.optimizeLreWindowUsingNR(prfSum, parameters.getFoThreshold());
        return true;
    }
    
    public boolean lreWindowUpdate(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        nrAnalysis.conductNonlinearRegressionOptimization(prfSum);
        return true;
    }

    private boolean testIfRegressionWasSuccessful() {//********************* TODO Design an effective scheme to test for NR success
//Test if the regression analysis was successful based on the LRE line R2
        Double r2 = profile.getR2();
//        Double emax = profile
        if (r2 < 0.8 || r2.isNaN()) {
            //Error dialog
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    "The nonlinear regression analysis has failed for well '"
                    + profile.getWellLabel() + "'.\n\n"
                    + "LRE analysis will be conducted without baseline-slope correction",
                    "Failed to Apply Nonlinear Regression.",
                    JOptionPane.ERROR_MESSAGE);
            //Reset the LRE window using Fb
            profile.setWasNonlinearRegressionSuccessful(false);
            profile.setNrFb(0);
            profile.setNrFbSlope(0);
            LreWindowSelector.substractBackgroundUsingAvFc(profile);
            //Try to find an LRE window
            prfSum.update();
            lreWindowInitialization(prfSum, parameters);
            return false;
        }
        profile.setWasNonlinearRegressionSuccessful(true);
        return true;
    }
}
