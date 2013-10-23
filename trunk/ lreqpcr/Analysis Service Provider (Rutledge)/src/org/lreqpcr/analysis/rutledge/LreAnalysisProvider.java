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
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.nonlinear_regression_services.LreParameters;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionServices;
import org.openide.util.Lookup;
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

    private NonlinearRegressionServices nrService = Lookup.getDefault().lookup(NonlinearRegressionServices.class);
    private Profile profile;
    private ProfileSummary prfSum;
    private Cycle runner;
    private LreWindowSelectionParameters parameters;

    public LreAnalysisProvider() {
    }

    /**
     * Note that the primary function of this method is to select an LRE window
     * starting with a working Fc dataset that is based on a Fb determined from
     * the average of cycles 4-9. Nonlinear regression analysis is then applied,
     * providing Fb and Fb-slope estimates which are used to generate the
     * working Fc dataset followed by another round of LRE window selection.
     *
     * @param profile
     * @param parameters LRE window selection parameters
     * @return whether a LRE window was found
     */
    public boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters) {
        this.profile = profile;
        this.parameters = parameters;
        //Remove any previous nonlinear regression and LRE-derived parameters
        profile.setLreVariablesToZero();
        //Start with a crude Fb subtraction
        substractBackground();
        //Try to find an LRE window, which requires a ProfileSummary
        prfSum = new ProfileSummaryImp(profile);
        LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is irrelevant
            return false;
        }
//Use this preliminary LRE window for nonlinear regression analysis 
//This generates an optimized working Fc dataset and updates the LRE parameters
        conductNonlinearRegressionAnalysisX10();
        if (!testIfRegressionWasSuccessful()) {
            return profile.hasAnLreWindowBeenFound();
        }
        //Repeat optimization of the LRE window but with nonlinear regression 
        optmzLreWinWithNR();
        return true;
    }

    /**
     * This code comes from the LREWindowSelector but includes nonlinear
     * regression after each increase in the LRE window size. LRE window size is
     * reset to three in order to avoid aberrations generated from lack of NR.
     *
     * Expands the upper LRE window boundary based upon the difference between
     * the average LRE window Fo and the Fo value derived from the first cycle
     * above the LRE window. If this difference is smaller than the Fo
     * threshold, this next cycle is added to the LRE window and the analysis
     * repeated. Note however, that the upper limit of this expansion is limited
     * to the cycle Fc less than 95% of Fmax, eliminating the possibility of
     * including plateau phase cycles into the LRE window.
     *
     */
    private void optmzLreWinWithNR() {
        //Go to the first cycle of the LRE window
        runner = prfSum.getLreWindowStartCycle();
        //Start with a 3 cycle window
        if (runner.getNextCycle().getNextCycle() == null) {
            return;//Have reached the last cycle of the profile so the window cannot be expanded
        }
        profile.setLreWinSize(3);
        //Run up two cycles so that the LRE window size starts at 3 cycles
        runner = runner.getNextCycle().getNextCycle();
        //Try to expand the upper region of the window based on the Fo threshold
        //This also limits the top of the LRE window to 95% of Fmax
        double fmaxThreshold = profile.getFmax() * 0.95;
        double fcNext = runner.getNextCycle().getFc();
        double foFrac = runner.getNextCycle().getFoFracFoAv();
        int cycle = runner.getNextCycle().getCycNum();
        while (Math.abs(runner.getNextCycle().getFoFracFoAv()) < parameters.getFoThreshold()
                && runner.getNextCycle().getFc() < fmaxThreshold) {
            //Increase and set the LRE window size by 1 cycle
            profile.setLreWinSize(profile.getLreWinSize() + 1);
            //Need to conduct nonlinear regression analysis
            //First update the LRE parameters, which also generates a new Cycle linked list
            prfSum.updateProfileSummary();
            //Conduct NR which also updates the LRE parameters and instantiates a new Cycle list
            conductNonlinearRegressionAnalysisX10();
            //Must now set the runner to the new last Cycle in the LRE window
            runner = prfSum.getLreWindowEndCycle();
            if (runner.getNextCycle() == null) {
                return;//Odd situation in which the end of the profile is reached
            }
            runner = runner.getNextCycle();
            fcNext = runner.getNextCycle().getFc();
            foFrac = runner.getNextCycle().getFoFracFoAv();
            cycle = runner.getNextCycle().getCycNum();
            int stop = 1;
        }
    }

    /**
     * Because updating the profile requires construction of a new runner linked
     * list, LRE window selection requires that this new runner is set to the
     * last cycle of the LRE window.
     */
//    private void setRunnerToTheEndOfTheLreWindow() {**************************************************************************************
//        runner = prfSum.getLreWindowStartCycle();
//        int lreWindowSize = profile.getLreWinSize();
//        for (int i = runner.getCycNum(); i < lreWindowSize; i++) {
//            runner = runner.getNextCycle();
//        }
//    }

    /**
     * Updates a Profile with a new or modified LRE window, which includes
     * nonlinear regression analysis to determine Fb and Fb-slope. This in turn
     * triggers reprocessing of the raw Fc dataset to generate a new working Fc
     * dataset corrected for Fb-slope, which is followed by LRE analysis using
     * this optimized working Fc dataset, and updating of the LRE parameters
     * using this new Fc dataset.
     *
     * @param profile the Profile to be updated
     */
    public boolean updateProfile(Profile profile) {
        this.profile = profile;
//Construct a new ProfileSummary which updates that LRE parameters within the Profile
        prfSum = new ProfileSummaryImp(profile);
//Apply nonlinear regression analysis to optimize the LRE analysis based on the new LRE window
        //This also updates both the ProfileSummary and Profile
        conductNonlinearRegressionAnalysisX10();
        if (!testIfRegressionWasSuccessful()) {
            return profile.hasAnLreWindowBeenFound();
        }
        return true;
    }

    private boolean testIfRegressionWasSuccessful() {
//Test if the regression analysis was successful based on the LRE line R2
        Double d = profile.getR2();
        if (d < 0.8 || d.isNaN()) {
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
            substractBackground();
            //Try to find an LRE window
            prfSum.updateProfileSummary();
            LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
            return false;
        }
        profile.setWasNonlinearRegressionSuccessful(true);
        return true;
    }

    /**
     * Conducts nonlinear regression reiterated 10 times. Note that this
     * instantiates a new Cycle linked list, so any calling function utilizing 
     * a runner must reset it.
     *
     * Nonlinear regression analysis based on Peter Abeles’s EJML API
     * (http://code.google.com/p/efficient-java-matrix-library/wiki/LevenbergMarquardtExample)
     * <p>
     * Note that this requires that an LRE window has previously been identified
     * and that the LRE window will not be modified by this function. Note also
     * that the LRE parameters are updated within the Profile, so the calling
     * function must take responsibility for saving the modified Profile and for
     * constructing a new Profile Summary.
     *
     */
    private void conductNonlinearRegressionAnalysisX10() {
//This ensures a clean regression analysis that is not influenced from previous NR analyses 
//        profile.setLreVariablesToZero();
//Need to trim the profile in order to avoid aberrancies within early cycles and within the plateau phase
        //Exclude the first three cycles
        int firstCycle = 4;//Start at cycle 4        
//Use the top of the LRE window as the last cycle included in the regression analysis
        double[] fcArray = profile.getRawFcReadings();
//        int lastCycle = profile.getStrCycleInt() + profile.getLreWinSize() - 1;
        int lastCycle = prfSum.getLreWindowEndCycle().getCycNum();//Needs testing**************************************************************
        //LRE windows at the end of the profile can generate an incorrect last cycle
        if (lastCycle > fcArray.length) {
            lastCycle = fcArray.length;
        }
        int numberOfCycles = lastCycle - firstCycle + 1;
        //Construct the trimmed Fc dataset TreeMap<cycle number, Fc reading>
        TreeMap<Integer, Double> profileMap = new TreeMap<Integer, Double>();
        for (int i = 0; i < numberOfCycles; i++) {
            profileMap.put(firstCycle + i, fcArray[firstCycle - 1 + i]);
        }
        //Run NR once to grossly stablize the LRE-derived parameters
        LreParameters lreDerivedParam = getLreParameters();
        LreParameters optParam = nrService.conductNonlinearRegression(lreDerivedParam, profileMap);
        //Reinitialize the LRE-derived parameters
        //First Reset nonlinear regression-derived Fb and Fb-slope within the profile
        profile.setNrFb(optParam.getFb());
        profile.setNrFbSlope(optParam.getFbSlope());
//Generate a new optimized Fc dataset based on NR-derived Fb and Fb-slope
        conductBaselineCorrection(profile);
//Updating the ProfileSummary updates the LRE-derived parameters within the Profile with no change to the LRE window
        //However, this assumes that the NR was successful
        prfSum.updateProfileSummary();
        //Reset the LRE-derived paramaters
        lreDerivedParam = getLreParameters();
        //Run the regression analysis 10 times to determine the average and SD
        int numberOfIterations = 10;
        ArrayList<Double> emaxArray = new ArrayList<Double>();
        ArrayList<Double> fbArray = new ArrayList<Double>();
        ArrayList<Double> foArray = new ArrayList<Double>();
        ArrayList<Double> fmaxArray = new ArrayList<Double>();
        ArrayList<Double> fbSlopeArray = new ArrayList<Double>();
        double emaxSum = 0;
        double fbSum = 0;
        double foSum = 0;
        double fmaxSum = 0;
        double fbSlopeSum = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            optParam = nrService.conductNonlinearRegression(lreDerivedParam, profileMap);
            emaxArray.add(optParam.getEmax());
            emaxSum += optParam.getEmax();
            fbArray.add(optParam.getFb());
            fbSum += optParam.getFb();
            foArray.add(optParam.getFo());
            foSum += optParam.getFo();
            fmaxArray.add(optParam.getFmax());
            fmaxSum += optParam.getFmax();
            fbSlopeArray.add(optParam.getFbSlope());
            fbSlopeSum += optParam.getFbSlope();
            //Reinitialize the LRE-derived parameters
            //First reset nonlinear regression-derived Fb and Fb-slope within the profile
            profile.setNrFb(optParam.getFb());
            profile.setNrFbSlope(optParam.getFbSlope());
            //Generate a new optimized Fc dataset
            conductBaselineCorrection(profile);
            //Update the LRE-derived parameters within the Profile
            prfSum.updateProfileSummary();
            //Retrieve the new LRE parameters
            lreDerivedParam = getLreParameters();
        }
//Set the average for each parameter into the Profile 
//This allows the final LRE analysis to be based on the average Fb and Fb-slope
        profile.setNrEmax(emaxSum / numberOfIterations);
        profile.setNrFb(fbSum / numberOfIterations);
        profile.setNrFo(foSum / numberOfIterations);
        profile.setNrFmax(fmaxSum / numberOfIterations);
        profile.setNrFbSlope(fbSlopeSum / numberOfIterations);
        //Determine and set the parameter SD
        profile.setNrEmaxSD(MathFunctions.calcStDev(emaxArray));
        profile.setNrFbSD(MathFunctions.calcStDev(fbArray));
        profile.setNrFoSD(MathFunctions.calcStDev(foArray));
        profile.setNrFmaxSD(MathFunctions.calcStDev(fmaxArray));
        profile.setNrFbSlopeSD(MathFunctions.calcStDev(fbSlopeArray));
//Recaculate the optimized Fc dataset using the average NR-derived Fb and Fb-slope
        conductBaselineCorrection(profile);
        //Update the LRE parameters
        prfSum.updateProfileSummary();
    }

    /**
     * Retrieves the current LRE parameters from the Profile
     *
     * @param profile
     * @return
     */
    private LreParameters getLreParameters() {
        //Setup the initial parameters 
        LreParameters lreDerivedParam = new LreParameters();
//Testing indicates that if Fb=0 the NR fails
        if (profile.getNrFb() == 0) {
            lreDerivedParam.setFb(profile.getFb());//This Fb is derived from the average of cycles 4-9
        } else {
            lreDerivedParam.setFb(profile.getNrFb());
        }
        lreDerivedParam.setEmax(profile.getEmax());//Current LRE-derived Emax
        lreDerivedParam.setFmax(profile.getFmax());//Current LRE-derived Fmax
        lreDerivedParam.setFo(profile.getAvFo());//Current LRE-derived average Fo
        lreDerivedParam.setFbSlope(profile.getNrFbSlope());//Current Fb slope
        return lreDerivedParam;
    }

    /**
     * This conducts both nonlinear regression-derived Fb subtraction and
     * baseline slope correction.
     *
     * @param profile
     */
    private void conductBaselineCorrection(Profile profile) {
        //Start with correcting for the NR-derived Fb slope
        double[] rawFcReadings = profile.getRawFcReadings();
        double[] processedFcDataset = new double[rawFcReadings.length];//The new optimized Fc dataset
        //This assumes that nonlinear regression has been conducted AND it was successfully completed
        double nrFb = profile.getNrFb();//The regression derived Fb
        double nrFbSlope = profile.getNrFbSlope();//The regression-derived Fb slope
        for (int i = 0; i < processedFcDataset.length; i++) {
            processedFcDataset[i] = rawFcReadings[i] - nrFb - (nrFbSlope * (i + 1));
        }
        profile.setFcReadings(processedFcDataset);
    }

    /**
     * This is a very crude method based on averaging cycles 4-9 to determine
     * the fluorescence background (Fb). Starting at cycle 4 avoids aberrant
     * fluorescence readings that are is commonly observed for cycles 1-3.
     *
     * @param profile the Profile to be processed
     */
    private void substractBackground() {
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
