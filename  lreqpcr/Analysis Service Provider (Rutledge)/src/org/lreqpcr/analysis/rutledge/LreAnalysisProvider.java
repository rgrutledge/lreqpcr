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
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.nonlinear_regression_services.LreParameters;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionServices;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rutledge implementation of LRE window selection and optimization.
 * <p>
 * As of Sept13 this includes nonlinear regression to generate an estimation of
 * baseline fluorescence that replaces the average Fc of cycles 4-9, in addition
 * to an estimation of baseline slope, both of which are used to generate an
 * optimized Fc dataset, which is followed by LRE analysis.
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = LreAnalysisService.class)
public class LreAnalysisProvider implements LreAnalysisService {

    private NonlinearRegressionServices nrService = Lookup.getDefault().lookup(NonlinearRegressionServices.class);
    private ProfileSummary prfSum;
    private Profile profile;

    public LreAnalysisProvider() {
    }

    /**
     * Note that this includes nonlinear regression analysis using the LRE
     * parameters as starting values, in addition to using the top of the LRE
     * window as the upper cut off. This is used to generate a Fc dataset based
     * on both Fb and Fb-slope correction.
     *
     * @param profile
     * @param parameters LRE window selection parameters
     * @return whether a LRE window was found
     */
    public boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters) {
        this.profile = profile;
        
        profile.setHasAnLreWindowBeenFound(false);
        profile.setFcReadings(null);//Reset the processed Fc dataset
        substractBackground();
        //Try to find an LRE window
        prfSum = ProfileInitializer.constructProfileSummary(profile);
        LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is irrelevant
            return false;
        }
//Conduct nonlinear regression analysis for deriving estimates of Fb and Fb slope
//from which an optimized working Fc dataset is generated, followed by
//updating the LRE parameters within the Profile
        conductNonlinearRegressionAnalysis();
        return true;
    }

    public void updateProfile(Profile profile) {
        this.profile = profile;
//Initialize the ProfileSummary which updates that LRE parameters within the Profile
        prfSum = ProfileInitializer.constructProfileSummary(profile);
        //Apply nonlinear regression analysis to optimize the LRE analysis based on the new LRE window
        conductNonlinearRegressionAnalysis();
    }

    /**
     * Nonlinear regression analysis based on Peter Abeles’s EJML API
     * (http://code.google.com/p/efficient-java-matrix-library/wiki/LevenbergMarquardtExample)
     * <p>
     * Note that this requires that an LRE window has previously been identified
     * and that the LRE window will not be modified by this function. Note also
     * that the LRE parameters are updated within the Profile.
     *
     */
    private void conductNonlinearRegressionAnalysis() {
        profile.setNrVariablesToZero();
        //Need to trim the profile in order to avoid aberrancies within early cycles and within the plateau phase
        //Exclude the first three cycles
        int firstCycle = 4;//Start at cycle 4        
        //This top of the LRE window is the last cycle used in the regression analysis
        int lastCycle = profile.getStrCycleInt() + profile.getLreWinSize() - 1;
        int numberOfCycles = lastCycle - firstCycle + 1;
        double[] fcArray = profile.getRawFcReadings();
        //Construct the trimmed Fc dataset TreeMap<cycle number, Fc reading>
        TreeMap<Integer, Double> profileMap = new TreeMap<Integer, Double>();
        for (int i = 0; i < numberOfCycles; i++) {
            profileMap.put(firstCycle + i, fcArray[firstCycle - 1 + i]);
        }
        //Run NR once to grossly stablize the LRE-derived parameters
        LreParameters lreDerivedParam = getLreParameters();
        for (int i = 0; i < 1; i++) {
            //This assumes that the regression analysis will be successfull*******************************************
            LreParameters optParam = nrService.conductNonlinearRegression(lreDerivedParam, profileMap);
            //Reinitialize the LRE-derived parameters
            //First Reset nonlinear regression-derived Fb and Fb-slope within the profile
            profile.setNrFb(optParam.getFb());
            profile.setNrFbSlope(optParam.getFbSlope());
            //Generate a new optimized Fc dataset
            conductBaselineCorrection(profile);
            //Updating the ProfileSummary updates the LRE-derived parameters within the Profile
            ProfileInitializer.updateProfileSummary(prfSum);
        }
        //Run the analysis 10 times to determine the average and SD
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
            //This assumes that the regression analysis will be successfull*******************************************
            LreParameters optParam = nrService.conductNonlinearRegression(lreDerivedParam, profileMap);
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
            //First Reset nonlinear regression-derived Fb and Fb-slope within the profile
            profile.setNrFb(optParam.getFb());
            profile.setNrFbSlope(optParam.getFbSlope());
            //Generate a new optimized Fc dataset
            conductBaselineCorrection(profile);
            //Updating the ProfileSummary updates the LRE-derived parameters within the Profile
            ProfileInitializer.updateProfileSummary(prfSum);
            //Retrieve the new LRE-derived parameters
            lreDerivedParam = getLreParameters();
        }
        //Set the average for each parameter into the Profile
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
        //Recaculate the optimized Fc dataset using the average nonlinear regression values
        conductBaselineCorrection(profile);
        //Update the LRE parameters
        ProfileInitializer.updateProfileSummary(prfSum);
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
        //Testing indicates that this is necessary for the NR to be successful
        if (profile.getNrFb() == 0) {
            lreDerivedParam.setFb(profile.getFb());
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
        double nrFb = profile.getNrFb();//The regression -erived Fb
        double nrFbSlope = profile.getNrFbSlope();//The regression-derived Fb slope
        for (int i = 0; i < processedFcDataset.length; i++) {
            processedFcDataset[i] = rawFcReadings[i] - nrFb - (nrFbSlope * (i + 1));
        }
        profile.setFcReadings(processedFcDataset);
    }

    /**
     * This is a very crude method based on cycles 4-9. Starting at cycle 4
     * avoids changes in background fluorescence that is commonly observed for
     * cycles 1-3. This is should only used for new Profiles or when a curve
     * fitting Fb is not available.
     * <p>
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
