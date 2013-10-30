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
import java.util.TreeMap;
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
    
    private Profile profile;
    

    /**
     * Conducts nonlinear regression reiterated 10 times. Note that this
     * instantiates a new Cycle linked list, so any calling function utilizing a
     * runner must reset it.
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
    public void conductNonlinearRegressionAnalysisX10(ProfileSummary prfSum) {
        NonlinearRegressionServices nrService = Lookup.getDefault().lookup(NonlinearRegressionServices.class);
         profile = prfSum.getProfile();
//This ensures a clean regression analysis that is not influenced from previous NR analyses 
//        profile.setLreVariablesToZero();
//Need to trim the profile in order to avoid aberrancies within early cycles and within the plateau phase
        //Exclude the first three cycles
        int firstCycle = 4;//Start at cycle 4        
//Use the top of the LRE window as the last cycle included in the regression analysis
        double[] fcArray = profile.getRawFcReadings();
//        int lastCycle = profile.getStrCycleInt() + profile.getLreWinSize() - 1;
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
        int stop = 1;
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
}
