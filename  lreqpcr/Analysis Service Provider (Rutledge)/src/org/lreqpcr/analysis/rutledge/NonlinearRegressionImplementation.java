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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.nonlinear_regression_services.LreParameters;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class NonlinearRegressionImplementation {

    private ProfileSummary prfSum;
    private Profile profile;
    private NonlinearRegressionServices nrService = Lookup.getDefault().lookup(NonlinearRegressionServices.class);

    /**
     * Generates an optimized working Fc dataset based on nonlinear regression
     * derived Fb and Fb-slope without changes to the LRE window.
     * <p>
     * This is achieved by using LRE analysis to derive values for Emax, Fmax
     * and Fo that are then feed into the nonlinear regression, which in turn
     * derives values for Fb and Fb-slope, These are used to derive a new Fc
     * dataset upon which LRE analysis is applied, and the process repeated 10
     * times. An average Fb and Fb-slope are then determined, from which a final
     * optimized working Fc dataset is generated. This is followed by a final
     * LRE analysis to determine final values for Emax, Fmax and Fo.
     * <p>
     * Note that the Profile must have a valid LRE window and that the LRE
     * window is not modified.
     * <p>
     * Note also that the LRE parameters are updated and the modified Profile is
     * saved, and that a new Cycle linked list is instantiated, so any calling
     * function utilizing a runner must reset it.
     *
     * @param prfSum the ProfileSummary encapsulating the Profile
     */
    public boolean lreWindowUpdateUsingNR(ProfileSummary prfSum) {
        this.prfSum = prfSum;
        profile = prfSum.getProfile();

        //The profile must have a valid LRE window
        if (!profile.hasAnLreWindowBeenFound()) {
            return false;
        }
//Need to trim the profile in order to avoid aberrancies within early cycles and within the plateau phase
        //Exclude the first three cycles
        int firstCycle = 4;//Start at cycle 4        
//Use the top of the LRE window as the last cycle included in the regression analysis**********
        double[] fcArray = profile.getRawFcReadings();
        int lastCycle = prfSum.getLreWindowEndCycle().getCycNum();
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
        conductBaselineCorrection();
//Updating the ProfileSummary updates the LRE-derived parameters within the Profile with no change to the LRE window
        //However, this assumes that the NR was successful
        prfSum.update();
        //Reset the LRE-derived paramaters
        lreDerivedParam = getLreParameters();
        //Run the regression analysis 10 times to determine the average and SD
//This is necessary due to the poor performance of Peter Abeles’s EJML implementation
        int numberOfIterations = 3;
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
            conductBaselineCorrection();
            //Update the LRE-derived parameters within the Profile
            prfSum.update();
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
        lreDerivedParam.setEmax(profile.getEmax());//Current LRE-derived Emax
        lreDerivedParam.setFmax(profile.getFmax());//Current LRE-derived Fmax
        lreDerivedParam.setFo(profile.getAvFo());//Current LRE-derived average Fo
        lreDerivedParam.setFbSlope(profile.getNrFbSlope());//Current Fb slope
        return lreDerivedParam;
    }

    private boolean testIfRegressionWasSuccessful() {//********************* TODO Design an effective scheme to test for NR success
//Test if the regression analysis was successful based on the LRE line R2
        Double r2 = profile.getR2();
        Double emaxNR = profile.getNrEmax();
        //Very, very crude approach
        if (r2 < 0.8 || r2.isNaN() || emaxNR < 0.3) {
            //Error dialog
            Toolkit.getDefaultToolkit().beep();
            SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    "The nonlinear regression analysis has failed for well '"
                    + sdf.format(profile.getRunDate()) + ": "
                    + profile.getWellLabel() + "'.\n"
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
