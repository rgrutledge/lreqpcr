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

import org.ejml.data.DenseMatrix64F;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rutledge implementation of LRE window selection and optimization. As of
 * Sept13 This includes implementation of nonlinear regression using Peter
 * Abeles’s EJML API
 * (http://code.google.com/p/efficient-java-matrix-library/wiki/LevenbergMarquardtExample)
 * that provides both fluorescence background and baseline slope determination
 * that are used to optimize LRE analysis
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = LreAnalysisService.class)
public class LreAnalysisProvider implements LreAnalysisService {

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
//This will force a new LRE window to be found so that complete reinitialized will be conducted
        profile.setHasAnLreWindowBeenFound(false);
        //Construct a ProfileSummary which is needed for automated LRE window selection 
        //Subtract background fluorescence if needed
        if (profile.getFcReadings() == null) {//First time this Profile has been analyzed so need a Fb
//Note  that this is based on the average of cycles 4-9 and thus is as crude estimate
            substractBackground(profile);
        }
        //Need to instantiate a ProfileSummary in order to conduct the LRE window search
        ProfileSummary prfSum = ProfileInitializer.constructProfileSummary(profile);
        //Try to find an LRE window
        LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is irrelevant
            return false;
        }
        //Conduct curve fitting which generates a new corrected Fc dataset
        conductNonlinearRegressionAnalysis(profile);
        return true;
    }

    /**
     * Nonlinear regression analysis based on Peter Abeles’s EJML API
     * (http://code.google.com/p/efficient-java-matrix-library/wiki/LevenbergMarquardtExample)
     *
     * @param profile
     * @return whether the regression analysis was successful
     */
    public boolean conductNonlinearRegressionAnalysis(Profile profile) {
        //Need to trim the profile in order to avoid aberrancies within early cycles and within the plateau phase
        //Exclude the first three cycles
        int startIndex = 3;//Cycle 4        
        //This uses the top of the LRE window as a reference point
        //Cut off = endIndex, set to the top of the LRE window (+1)
        int endIndex = profile.getStrCycleInt() + profile.getLreWinSize() - startIndex + 1;
        int numberOfCycles = endIndex - startIndex + 1;
        double[] fcArray = profile.getRawFcReadings();
        //Construct the trimmed Fc dataset
        double[] trimmedFcArray = new double[numberOfCycles];
        double[] cycleArray = new double[numberOfCycles];
        for (int i = 0; i < numberOfCycles; i++) {
            cycleArray[i] = i + 4;
            trimmedFcArray[i] = fcArray[i + 3];
        }
        DenseMatrix64F y = new DenseMatrix64F(numberOfCycles, 1);
        y.setData(trimmedFcArray);
        DenseMatrix64F x = new DenseMatrix64F(numberOfCycles, 1);
        x.setData(cycleArray);
        double[] paramArray = new double[5];
        paramArray[0] = profile.getEmax();//LRE-derived Emax
        paramArray[1] = profile.getFb();//Fb derived from Fc average (e.g. cycles 4-9)
        paramArray[2] = profile.getAvFo();//LRE-derived Fo
        paramArray[3] = profile.getEmax() / (-1 * profile.getDeltaE());//LRE-derived Fmax
        paramArray[4] = 0.0;//Baseline slope assumed to be zero as a starting parameter
        DenseMatrix64F initialParam = new DenseMatrix64F(5, 1);
        initialParam.setData(paramArray);
        //Initiate nonlinear regression
        //Create the function
        Lre5Param func = new Lre5Param();
        //Instantiate the LM nonlinear regression function
        LevenbergMarquardt fitter = new LevenbergMarquardt(func);
        boolean successful = fitter.optimize(initialParam, x, y);
        //Retrieve the optimized parameters
        double[] optParam = fitter.getOptimizedParameters().getData();
        profile.setCfEmax(optParam[0]);
        profile.setCfFb(optParam[1]);
        profile.setCfFo(optParam[2]);
        profile.setCfFmax(optParam[3]);
        profile.setCfFbSlope(optParam[4]);
        conductBaselineCorrection(profile);
        //Need to update the Profiles LRE parameters, which requires a ProfileSummary
        //Creating a ProfileSummary includes updating the LRE parameters within the Profile
        ProfileInitializer.constructProfileSummary(profile);
        return successful;
    }

    /**
     * This conducts both CF-derived Fb subtraction and baseline slope
     * correction. Note that the caller is responsible for conducting LRE
     * analysis on the updated Fc dataset and for saving the modified Profile.
     *
     * @param profile
     */
    private void conductBaselineCorrection(Profile profile) {
        //Start with correcting for the NR-derived Fb slope
        double[] rawFcReadings = profile.getRawFcReadings();
        double[] processedFcDataset = new double[rawFcReadings.length];//The new optimized Fc dataset
        //This assumes that nonlinear regression has been conducted AND it was successfully completed
        double cfFb = profile.getCfFb();//The regression derived Fb
        double cfFbSlope = profile.getCfFbSlope();//The regression-derived Fb slope
        for (int i = 0; i < processedFcDataset.length; i++) {
            processedFcDataset[i] = rawFcReadings[i] - cfFb - (cfFbSlope * (i + 1));
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
        profile.setFb(fb);
        //Subtract this initial Fb from the raw Fc readings
        double[] fc = new double[rawFc.length];//The background subtracted Fc dataset
        for (int i = 0; i < fc.length; i++) {
            fc[i] = rawFc[i] - fb;
        }
        profile.setFcReadings(fc);
    }
}
