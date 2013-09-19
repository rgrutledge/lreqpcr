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

import org.lrepcr.curve_fitting_services.CurveFittingServices;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = LreAnalysisService.class)
public class LreAnalysisProvider extends LreAnalysisService {

    private CurveFittingServices cfServices = Lookup.getDefault().lookup(CurveFittingServices.class);

    public LreAnalysisProvider() {
    }

    /**
     * Note that curve fitting analysis is also conducted based on the initial 
     * LRE parameters, which generates a new 
     * processed Fc dataset that includes both Fb and Fb slope correction.
     *
     * @param profile
     * @param parameters
     * @return whether a LRE window was found
     */
    @Override
    public boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters) {
        //This will force a new LRE window to be found so that complete reinitialized will be conducted
        profile.setHasAnLreWindowBeenFound(false);
        //Construct a ProfileSummary which is used for automated LRE window selection 
        //Subtract background fluorescence if needed
        if (profile.getFcReadings() == null) {
            //This also signifies that the Fc dataset has been modified and needs a new Fb determination
            //Note also that this is solely based on the average of cycles 4-6 and NOT curve fitting
            substractBackground(profile);
        }
        //Need to instantiate a ProfileSummary in order to search for an LRE window
        ProfileSummary prfSum = new ProfileSummaryImp(profile);
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        //Try to find an LRE window
        LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is irrelevant
            return false;
        }
        //This also includes recalculating the cycle Fo, avFo and pFc
        ProfileInitializer.calcLreParameters(prfSum);
        //Conduct nonlinear curve fitting analysis based on the current LRE parameters
        //Note that this also includes reprocessing the Fc dataset
        //Note also that imperical testing has shown repeating the process is required
        //for convergence. 
        for (int i = 0; i < 5; i++) {
            cfServices.curveFit(profile);
            //Need to repeat the Profile initialization using the new Fc dataset
            //Reinitialize the prfSum
            prfSum = initializeProfileSummary(profile, null);
            //Update the LRE parametersx
            ProfileInitializer.calcLreParameters(prfSum);
        }
        return true;
    }

    /**
     * Generates a ProfileSummary for display purposes using the supplied
     * Profile. Note that this does not include any updating.      *
     * @param profile the profile to be processed
     * @param parameters LRE window selection parameters
     * @return the initialized ProfileSummary
     */
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
            //Both LRE-derived Emax and Emax =100% Fo values are calculated
            ProfileInitializer.calcAverageFo(prfSum);
            ProfileInitializer.calcAllpFc(prfSum);
        }
        return prfSum;
    }

    /**
     * This is a very crude method based on cycles 4-9. Starting at cycle 4
     * avoids changes in background fluorescence that is commonly observed for
     * cycles 1-3. This is should only used when a curve fitting Fb is not
     * available.
     * <p>
     *
     * @param profile the Profile to be processed
     */
    private void substractBackground(Profile profile) {
        double[] rawFc = profile.getRawFcReadings();
        double fb = 0;
        int start = 4;
        int end = 9;
        int fbWindow = (end - start) + 1;
        //Calculate the average for cycle 4-9
        for (int i = start; i < end + 1; i++) {
            fb = fb + rawFc[i - 1];//List starts at 0
        }
        fb = fb / fbWindow;
        // TODO this is redundant when a CF Fb is determined, so this will need to be eliminated once CF is fully implemented
        profile.setFbStart(start);
        profile.setFbWindow(end - start + 1);
        profile.setFb(fb);
        //Subtract this initial Fb from the raw Fc readings
        double[] fc = new double[rawFc.length];//The background subtracted Fc dataset
        for (int i = 0; i < fc.length; i++) {
            fc[i] = rawFc[i] - fb;
        }
        profile.setFcReadings(fc);
    }
}
