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
    private Cycle runner;
    private LreWindowSelectionParameters parameters;
    private NonlinearRegressionImplementation nrAnalysis = new NonlinearRegressionImplementation();

    public LreAnalysisProvider() {
    }

    /**
     * Note that the primary function of this method is to select an LRE window
     * starting with a working Fc dataset that is based on a Fb determined from
     * the average of cycles 4-9. 
     *
     * @param profile
     * @param parameters LRE window selection parameters
     * @return whether a LRE window was found
     */
    public boolean lreWindowSelection(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        this.prfSum = prfSum;
        this.parameters = parameters;
        this.profile = prfSum.getProfile();
        //Start with a crude Fb subtraction
        substractBackgroundUsingAvFc();
        prfSum.update();
        if (parameters.getMinFc() == 0){
            //No user selected minimum Fc, so need to scan the profile for a  LRE window
            LreWindowSelector.selectLreStartCycleViaScanning(prfSum);
        }else{//A user-selected minFc has be set, so use it
            LreWindowSelector.selectLreStartCycleUsingMinFc(prfSum, parameters.getMinFc());
        }
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is irrelevant
            return false;
        }
        //Attempt to expand the upper limit of the LRE window
        LreWindowSelector.optimizeLreWin(prfSum, parameters.getFoThreshold());
////Use this preliminary LRE window for nonlinear regression analysis 
////This generates an optimized working Fc dataset and updates the LRE parameters
////        conductNonlinearRegressionAnalysisX10();
//        optmzLreWinWithNR();//********************************Not appropriate here************************************
//        if (!testIfRegressionWasSuccessful()) {
//            return profile.hasAnLreWindowBeenFound();
//        }
//        //Repeat optimization of the LRE window but with nonlinear regression 
        
        return true;
    }

    /**
     Expands the upper boundary of the LRE window. This is based upon the
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
     * Note also that nonlinear regression is used to determine Fb and Fb-slope, from which 
     * a new working Fc dataset is derived, a process that is repeated following 
     * each increase in the LRE window size. 
     *
     * 
     *
     */
    public boolean lreWindowSelectionUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        profile = prfSum.getProfile();
        this.parameters = parameters;
        //First need to identify a LRE 
        if (!profile.hasAnLreWindowBeenFound()){
            //Need to first find a start cycle
            if (parameters.getMinFc() <= 0){
                //Need to scan
                LreWindowSelector.selectLreStartCycleViaScanning(prfSum);
            }else{
                LreWindowSelector.selectLreStartCycleUsingMinFc(prfSum, parameters.getMinFc());
            }
            if (!profile.hasAnLreWindowBeenFound()){
                return false;
            }
        }
        //A LRE window has been found
        runner = prfSum.getLreWindowStartCycle();
        if (runner.getNextCycle() == null) {
            return false;//Have reached the last cycle of the profile so the window cannot be expanded
        }
        //Start with a 3 cycle window
        profile.setLreWinSize(3);
        prfSum.update();
        //Start at the end of the window
        runner = prfSum.getLreWindowEndCycle();
        //Try to expand the upper region of the window based on the Fo threshold
        //This also limits the top of the LRE window to 95% of Fmax
        double fmaxThreshold = profile.getFmax() * 0.95;
        while (Math.abs(runner.getNextCycle().getFoFracFoAv()) < parameters.getFoThreshold()
                && runner.getNextCycle().getFc() < fmaxThreshold) {
            //Increase and set the LRE window size by 1 cycle
            profile.setLreWinSize(profile.getLreWinSize() + 1);
            //Need to conduct nonlinear regression analysis
            //First update the LRE parameters, which also generates a new Cycle linked list
            prfSum.update();
            //Conduct NR which also updates the LRE parameters and instantiates a new Cycle list
            nrAnalysis.conductNonlinearRegressionAnalysisX10(prfSum);
            //Must now set the runner to the new last Cycle in the LRE window
            runner = prfSum.getLreWindowEndCycle();
            //This also makes it unecessary to move to the next cycle as this is already the end of the LRE window
            if (runner.getNextCycle() == null) {
  //Reached the end of the profile, so the window cannot be expanded any further
                break;//Odd situation in which the end of the profile is reached
            }
        }
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
            substractBackgroundUsingAvFc();
            //Try to find an LRE window
            prfSum.update();
            lreWindowSelection(prfSum, parameters);
            return false;
        }
        profile.setWasNonlinearRegressionSuccessful(true);
        return true;
    }

    /**
     * This is a very crude method based on averaging cycles 4-9 to determine
     * the fluorescence background (Fb). Starting at cycle 4 avoids aberrant
     * fluorescence readings that are is commonly observed for cycles 1-3.
     *
     * @param profile the Profile to be processed
     */
    private void substractBackgroundUsingAvFc() {
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
        profile.setFb(fb);
        //Subtract this initial Fb from the raw Fc readings
        double[] fc = new double[rawFc.length];//The background subtracted Fc dataset
        for (int i = 0; i < fc.length; i++) {
            fc[i] = rawFc[i] - fb;
        }
        profile.setFcReadings(fc);
    }
}
