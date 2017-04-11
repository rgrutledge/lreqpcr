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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.nonlinear_regression_services.LreParameters;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionServices;
import org.openide.util.Lookup;

/**
 *
 * @author Bob Rutledge
 */
public class NonlinearRegressionImplementation {

    private ProfileSummary prfSum;
    private Profile profile;
    private NonlinearRegressionServices nrService = Lookup.getDefault().lookup(NonlinearRegressionServices.class);

    /**
     * Generates an optimized working Fc dataset via nonlinear regression
     * to derived Fb and Fb-slope using the current LRE window.
     * <p>
     * Nonlinear regression is conducted using Emax, Fmax and Fo derived from
     * the current LRE window, from WHICH values for Fb and Fb-slope are determined.
     * These are then used to calculate a new working Fc dataset, followed by
     * recalculation of the LRE parameters. This process repeated 3 times from which
     * an average Fb and Fb-slope are then determined and a final
     * optimized working Fc dataset generated. This is followed by a final
     * recalculation of the LRE parameters to determine final values for Emax, Fmax and Fo.
     * THIS DOES THIS INCLUDE ANY MODIFICATION TO THE LRE WINDOW.
     * <p>
     * Note also that the LRE parameters are updated and the modified Profile is
     * saved, which includes initialization of a new Cycle linked list, so any calling
     * function must reset its runner.
     *
     * @param prfSum the ProfileSummary encapsulating the Profile
     * @return true if nonlinear regression analysis was successful or false if it failed
     */
    public boolean generateOptimizedFcDatasetUsingNonliearRegression(ProfileSummary prfSum) {
        this.prfSum = prfSum;
        profile = prfSum.getProfile();

        //The profile must have a valid LRE window
        if (!profile.hasAnLreWindowBeenFound()) {
            return false;
        }
//Need to trim the profile in order to avoid aberrancies within early cycles and within the plateau phase
        //Exclude the first three cycles
        int firstCycle = 4;//Start at cycle 4
//Use the top of the LRE window as the last cycle included in the regression analysis******THIS IS VERY IMPORTANT
        double[] fcArray = profile.getRawFcReadings();
        int lastCycle = 0;
        if (prfSum.getLreWindowEndCycle() != null){
            lastCycle = prfSum.getLreWindowEndCycle().getCycleNumber();
        }else{
            return false;
        }
        int numberOfCycles = lastCycle - firstCycle + 1;
        //Construct the trimmed Fc dataset TreeMap<cycle number, Fc reading>
        TreeMap<Integer, Double> profileMap = new TreeMap<>();
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
        conductBaselineCorrection();
//Updating the ProfileSummary updates the LRE-derived parameters within the Profile with no change to the LRE window
        //However, this assumes that the NR was successful
        prfSum.update();
        //Reset the LRE-derived paramaters
        lreDerivedParam = getLreParameters();
        //Run the regression analysis 10 times to determine the average and SD
//This is necessary due to the poor performance of Peter Abeles’s EJML implementation
        int numberOfIterations = 3;
        ArrayList<Double> emaxArray = new ArrayList<>();
        ArrayList<Double> fbArray = new ArrayList<>();
        ArrayList<Double> foArray = new ArrayList<>();
        ArrayList<Double> fmaxArray = new ArrayList<>();
        ArrayList<Double> fbSlopeArray = new ArrayList<>();
        double emaxSum = 0;
        double fbSum = 0;
        double foSum = 0;
        double fmaxSum = 0;
        double fbSlopeSum = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            optParam = nrService.conductNonlinearRegression(lreDerivedParam, profileMap);
            emaxArray.add(optParam.getMaxEfficiency());
            emaxSum += optParam.getMaxEfficiency();
            fbArray.add(optParam.getFb());
            fbSum += optParam.getFb();
            foArray.add(optParam.getTargetFluorescence());
            foSum += optParam.getTargetFluorescence();
            fmaxArray.add(optParam.getMaxFluorescence());
            fmaxSum += optParam.getMaxFluorescence();
            fbSlopeArray.add(optParam.getFbSlope());
            fbSlopeSum += optParam.getFbSlope();
            //Reinitialize the LRE-derived parameters
            //First reset nonlinear regression-derived Fb and Fb-slope within the profile
            profile.setNrFb(optParam.getFb());
            profile.setNrFbSlope(optParam.getFbSlope());
            //Generate a new optimized Fc dataset
            conductBaselineCorrection();
            //Update the LRE-derived parameters within the Profile
            prfSum.update();
            //Retrieve the new LRE parameters
            lreDerivedParam = getLreParameters();
        }
        //Set the average for each parameter into the Profile
//This allows the final recalculation of the LRE parameters based on the average Fb and Fb-slope
        profile.setNonlinearMaxEfficiency(emaxSum / numberOfIterations);
        profile.setNrFb(fbSum / numberOfIterations);
        profile.setNrFo(foSum / numberOfIterations);
        profile.setNonlinearMaxFluorescence(fmaxSum / numberOfIterations);
        profile.setNrFbSlope(fbSlopeSum / numberOfIterations);
        //Determine and set the parameter SD
        profile.setNonlinearMaxEfficiencyStandardDeviation(MathFunctions.calcStDev(emaxArray));
        profile.setNrFbSD(MathFunctions.calcStDev(fbArray));
        profile.setNrFoSD(MathFunctions.calcStDev(foArray));
        profile.setNonlinearMaxFluorescenceStandardDeviation(MathFunctions.calcStDev(fmaxArray));
        profile.setNrFbSlopeSD(MathFunctions.calcStDev(fbSlopeArray));
//Recaculate the optimized Fc dataset using the average NR-derived Fb and Fb-slope
        conductBaselineCorrection();
        //Update the LRE parameters
        prfSum.update();
        return testIfRegressionWasSuccessful();
    }

    /**
     * This conducts both nonlinear regression-derived Fb subtraction and
     * baseline slope correction.
     *
     * @param profile
     */
    private void conductBaselineCorrection() {
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
        lreDerivedParam.setMaxEfficiency(profile.getMaxEfficiency());//Current LRE-derived Emax
        lreDerivedParam.setMaxFluorescence(profile.getMaxFluorescence());//Current LRE-derived Fmax
        lreDerivedParam.setTargetFluorescence(profile.getAvFo());//Current LRE-derived average Fo
        lreDerivedParam.setFbSlope(profile.getNrFbSlope());//Current Fb slope
        return lreDerivedParam;
    }

    private boolean testIfRegressionWasSuccessful() {//**** TODO Design a more effective scheme to test for NR success
//Test if the regression analysis was successful based on the LRE line R2
        Double r2 = profile.getR2();
        Double emaxNR = profile.getNonlinearMaxEfficiency();
        //Very, very crude approach
        if (r2 < 0.8 || r2.isNaN() || emaxNR < 0.3) {
            //Error dialog DISABLED as it is extremely irrating
//            Toolkit.getDefaultToolkit().beep();
//            SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
//            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
//                    "The nonlinear regression analysis has failed for well '"
//                    + sdf.format(profile.getRunDate()) + ": "
//                    + profile.getWellLabel() + "'.\n"
//                    + "LRE analysis will be conducted without baseline-slope correction",
//                    "Failed to Apply Nonlinear Regression.",
//                    JOptionPane.ERROR_MESSAGE);
            //Reset the LRE window using Fb
            profile.setWasNonlinearRegressionSuccessful(false);
            profile.setNrFb(0);
            profile.setNrFbSlope(0);
            //Return to average Fb background subtraction
            LreWindowSelector.substractBackgroundUsingAvFc(profile);
            //Recalculate the LRE parameters
            prfSum.update();
            //Reinitialize the profile
            List<LreWindowSelectionParameters> l = prfSum.getDatabase().getAllObjects(LreWindowSelectionParameters.class);
            LreWindowSelectionParameters parameters = l.get(0);
            LreAnalysisService lreAnalService = Lookup.getDefault().lookup(LreAnalysisService.class);
            lreAnalService.lreWindowInitialization(prfSum, parameters);
            return false;
        }
        profile.setWasNonlinearRegressionSuccessful(true);
        return true;
    }
}
