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
package org.lrepcr.emjl_curve_fitting_provider;

import org.ejml.data.DenseMatrix64F;
import org.lrepcr.curve_fitting_services.CurveFittingServices;
import org.lreqpcr.core.data_objects.Profile;
import org.openide.util.lookup.ServiceProvider;

/**
 * Curve Fitting Provider based on EJML 
 * <br>
 * Note that a new Fc dataset is generated that includes both CF-derived Fb
 * subtraction and baseline slope correction. Note also that the caller is
 * responsible for conducting a new LRE analysis in that Fc dataset will be modified.
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = CurveFittingServices.class)
public class EjmlCurveFittingProvider implements CurveFittingServices {

    @Override
    public void curveFit(Profile profile) {
        //Need to trim the profile in order to avoid aberrancies at low Fc and in the plateau phase
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
        paramArray[0] = profile.getEmax();
        paramArray[1] = profile.getFb();//Fb
        paramArray[2] = profile.getAvFo();
        paramArray[3] = profile.getEmax() / (-1 * profile.getDeltaE());//Fmax
        paramArray[4] = 0.0;//Baseline slope
        DenseMatrix64F initialParam = new DenseMatrix64F(5, 1);
        initialParam.setData(paramArray);
        //Initiate curve fitting
        //Create the function
        Lre5Param func = new Lre5Param();
        //Instantiate the LM curver fitter
        LevenbergMarquardt fitter = new LevenbergMarquardt(func);
        // TODO this should include determining if curve fitting was successful 
        // and if not, determine what to do...
        boolean successful = fitter.optimize(initialParam, x, y);
        //Retrieve the optimized parameters
        double[] optParam = fitter.getOptimizedParameters().getData();
        profile.setCfEmax(optParam[0]);
        profile.setCfFb(optParam[1]);
        profile.setCfFo(optParam[2]);
        profile.setCfFmax(optParam[3]);
        profile.setCfFbSlope(optParam[4]);
        conductBaselineCorrection(profile);
//        normalizeToAvRunFmax(profile);//*********THIS DOES NOT WORK**************
        //This is for testing
        double iniCost = fitter.getInitialCost();
        double finalCost = fitter.getFinalCost();
        //Used for testing curve fitting
//        int difference = (int) (iniCost - finalCost);
//        double[] pFc = func.computePredictedFc(optParam, x);
//        double r2 = LREmath.calcNonLinearR2(trimmedFcArray, pFc);
        int stop = 1;
    }

    /**
     * This conducts both CF-derived Fb subtraction and baseline slope
     * correction. Note that the caller is responsible for conducting LRE
     * analysis on the updated Fc dataset.
     *
     * @param profile
     */
    @Override
    public void conductBaselineCorrection(Profile profile) {
        //Start with correcting for the CF-derived Fb slope
        double[] rawFcReadings = profile.getRawFcReadings();
        double[] fc = new double[rawFcReadings.length];//The new Fc dataset
        //This assumes that curve fitting has been conducted AND it was successfully completed
        double cfFb = profile.getCfFb();//The curve fitting derived Fb
        double cfFbSlope = profile.getCfFbSlope();//The curve fitting-derived Fb slope
        for (int i = 0; i < fc.length; i++) {
            fc[i] = rawFcReadings[i] - cfFb - (cfFbSlope * (i + 1));
        }
        profile.setFcReadings(fc);
        //Should this include repeating LRE analysis...YES but by the caller!!
    }

    //Does not function correctly
    private void normalizeToAvRunFmax(Profile profile) {
        double runAvFmax = profile.getRun().getAverageFmax();
        double nrmFactor = profile.getFmax() / runAvFmax;//Run Fmax average is based on the LRE-derived Fmax
        double[] rawFc = profile.getRawFcReadings();
        for (int i = 0; i < rawFc.length; i++) {
            rawFc[i] = rawFc[i] / nrmFactor;
        }
    }
}
