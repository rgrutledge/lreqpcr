/*
 * Copyright (C) 2010  Bob Rutledge
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

import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.LREmath;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Automated LRE window selection
 * 
 * @author Bob Rutledge
 */
public class LreWindowSelector {

    public LreWindowSelector() {
    }

    /**
     * Automated selection of the LRE window start cycle. This is initiated by
     * identifying the first cycle in which the LRE r2 value derived from LRE analysis
     * of a window that encompassing the two preceeding and two following Cycles
     * (5 cycles total) above the "r2 tolerance". To prevent setting
     * the start cycle within the baseline region, the cycle Fc must be 1.5X
     * greater that the fluorescence background (Fb).
     *
     * The LRE window is then set to 3 cycles and the StartCycle adjusted
     * to the first cycle below C1/2, generating an LRE window in the
     * center to the profle. The upper limit of the LRE window is then 
     * expanded based on a default Fo tolerance of 6%.
     *
     * @param prfSum the ProfileSummary to process
     */
    public static void selectLreWindow(ProfileSummary prfSum) {
        if (prfSum.getZeroCycle() == null) {
            //There is no Fc data in this profile
            return;
        }
        Profile profile = prfSum.getProfile();
        //Find the start of the profile by running a 5 cycle window up
        //the profile until an LRE linear region is found based on the r2
        Cycle cycZero = prfSum.getZeroCycle();
        Cycle runner = cycZero.getNextCycle().getNextCycle(); //Go to cycle 2
        //Calculate the LRE parameters for all the Cycles in the profile
        while (runner.getNextCycle().getNextCycle() != null) {
            double[][] fcEcArray = new double[2][5];
            fcEcArray[0][0] = runner.getPrevCycle().getPrevCycle().getFc();
            fcEcArray[1][0] = runner.getPrevCycle().getPrevCycle().getEc();
            fcEcArray[0][1] = runner.getPrevCycle().getFc();
            fcEcArray[1][1] = runner.getPrevCycle().getEc();
            fcEcArray[0][2] = runner.getFc();
            fcEcArray[1][2] = runner.getEc();
            fcEcArray[0][3] = runner.getNextCycle().getFc();
            fcEcArray[1][3] = runner.getNextCycle().getEc();
            fcEcArray[0][4] = runner.getNextCycle().getNextCycle().getFc();
            fcEcArray[1][4] = runner.getNextCycle().getNextCycle().getEc();

            //Calc cycle LRE paramaters [dE, Emax, r2]
            double[] regressionValues = null;
            runner.setCycLREparam(MathFunctions.linearRegressionAnalysis(fcEcArray));
            int cycNum = runner.getCycNum();
            double fc = runner.getFc();
            regressionValues = runner.getCycLREparam();
            runner.setFo(LREmath.calcFo(cycNum, fc,
                    regressionValues[0], regressionValues[1]));
            runner = runner.getNextCycle();
        }

        double r2Tol = 0.95; //The tolerance of the LRE r2 to determine the start of the profile
        double fb = profile.getFb();
        /*-----Finds start cycle based on the cycle LRE r2-----*/
        Cycle strCycle = null;
        runner = cycZero.getNextCycle().getNextCycle().getNextCycle(); //Start at cycle 3
        boolean foundStrCyc = false; //Used to test if the profile is flat
        while (runner.getNextCycle().getNextCycle().getNextCycle() != null) {

            /*Tests for the minimum r2 >r2 tolerance across 3 cycles*/
            if (runner.getPrevCycle().getCycLREparam()[2] > r2Tol && runner.getCycLREparam()[2] > r2Tol && runner.getNextCycle().getCycLREparam()[2] > r2Tol) {
                double fbRatio = runner.getFc() / fb;
                if (fbRatio < 0) {
                    fbRatio = fbRatio * -1;
                }
//Tests for the minimum Fc relative to Fb in an attempt to avoid setting the StartCycle in the baseline
//Note that small Fmax vs. large Fb such as for MXP profiles can can fail
//this step, resulting in a "AN LRE WINDOW COULD NOT BE FOUND" error
                if (fbRatio > 0.25) {
                    strCycle = runner;
                    profile.setStrCycleInt(strCycle.getCycNum()); //Sets the integer start cycle
                    prfSum.setStrCycle(strCycle);
                    foundStrCyc = true;
                }
            }
            if (foundStrCyc) {
                break;
            }
            if (runner.getNextCycle() == null) {
                break;
            }
            runner = runner.getNextCycle(); //Advances to the next cycle
        }
        if (!foundStrCyc) {
            profile.setLongDescription("AN LRE WINDOW COULD NOT BE FOUND");
            profile.setShortDescription("An LRE window could not be found");
            profile.setExcluded(true);
            return;
        }
        //Set the initiate LRE window size to a default size
        int defaultLREwinSize = 3;
        profile.setLreWinSize(defaultLREwinSize);
        //Need a preliminary C1/2 value
        ProfileInitializer.calcLreParameters(prfSum);
        if (profile.getWellLabel() != null) {
        }

        optimizeLreWin(prfSum, 0.06);//6% default threshold cutoff
//Use C1/2 as a reference point to adjust the initial start cycle
        double midC = (int) profile.getMidC();
        runner = prfSum.getZeroCycle();
        //Move the strCyclestart cycle to 1 cycle below C1/2
        while (midC > runner.getCycNum()) {
            runner = runner.getNextCycle();
        }
        //This moves the run to one cycle below C1/2, which is what is wanted
        profile.setStrCycleInt(runner.getCycNum());
        prfSum.setStrCycle(runner);
        //Reoptimize the LRE window
        profile.setLreWinSize(3);
        //Reoptimize the LRE window which tries to add cycles to the top of the LRE window
        ProfileInitializer.calcLreParameters(prfSum);
        optimizeLreWin(prfSum, 0.06);//6% default threshold cutoff
    }

    /**
     * This method sets the Start Cycle at the first cycle which has an Fc reading 
     * above the minimum fluorescence value provided. A default 3 cycle LRE window
     * is then applied, and the upper limit of the LRE window expanded until the 
     * Fo threshold is exceeded. 
     *
     * @param prfSum the ProfileSummary to be processed
     * @param minFc the minimum fluorescence for setting the Start Cycle
     * @param threshold maximum fractional difference (%) between cycle Fo and the average Fo
     */
    public static void selectLreWindow(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        if (parameters.getMinFc() == null) {
            selectLreWindow(prfSum);
            return;
        }
        Profile profile = prfSum.getProfile();
        Cycle cycZero = prfSum.getZeroCycle();
        if (cycZero == null) {
            //Profile initialization failed
            profile.setLongDescription("AN LRE WINDOW COULD NOT BE FOUND");
            profile.setShortDescription("An LRE window could not be found");
            profile.setExcluded(true);
            return;
        }
        double minFc = parameters.getMinFc();
        double foThreshold = parameters.getFoThreshold();

        if (minFc <= 0) {//Zero signifies that no minimum Fc has been set
            selectLreWindow(prfSum);
            return;
        }
        if (foThreshold <= 0) {
            return;//Reject negative numbers
        }
        //Run to the first cycle above the minFc and set it as the start cycle
        Cycle runner = cycZero.getNextCycle().getNextCycle(); //Go to cycle 2
        while (runner.getFc() < minFc) {
            if (runner.getNextCycle() == null) {
                //Reached the end of the profile but the Fc > minFc
                profile.setLongDescription("AN LRE WINDOW COULD NOT BE FOUND");
                profile.setShortDescription("An LRE window could not be found");
                profile.setExcluded(true);
                return;
            }
            if (runner.getNextCycle().getNextCycle() == null) {
                //Need at least three cycle in order to set the LRE window
                profile.setLongDescription("AN LRE WINDOW COULD NOT BE FOUND");
                profile.setShortDescription("An LRE window could not be found");
                profile.setExcluded(true);
                return;
            }
            //Move up one cycle
            runner = runner.getNextCycle();
        }
//The start cycle is set to the next cycle, because minFc should be applied to the denominator of Ec
        prfSum.setStrCycle(runner.getNextCycle());
        profile.setStrCycleInt(runner.getNextCycle().getCycNum());
        profile.setLreWinSize(3);
        optimizeLreWin(prfSum, foThreshold);
    }

    /**
     * Expands the upper LRE window boundary based upon the 
     * difference between the average LRE window Fo and 
     * the Fo value derived from the first cycle above
     * the LRE window. If this difference is smaller than the Fo threshold,
     * this next cycle is added to the LRE window and the analysis repeated.
     * 
     * @param prfSum the ProfileSummary to be processed
     * @param foThreshold the Fo threshold
     */
    private static void optimizeLreWin(ProfileSummary prfSum, Double foThreshold) {
        ProfileInitializer.calcLreParameters(prfSum);//To be sure that the LRE parameters are upto date
        Profile profile = prfSum.getProfile();
        //Go to the first cycle of the LRE window
        Cycle runner = prfSum.getStrCycle();
        //Run to the last cycle of the LRE window
        for (int i = 1; i < profile.getLreWinSize(); i++) {
            runner = runner.getNextCycle();
            if (runner == null) {
                return;//Runner is at the end of the Profile
            }
        }
        //Set the initial upper boundary of the LRE window based on the Fo threshold
        if (runner.getNextCycle() == null) {
            return;
        }
        while (Math.abs(runner.getNextCycle().getFoFracFoAv()) < foThreshold) {
            //Increase and set the LRE window size by 1 cycle
            //PrfSum will reinitialize the linked Cycle list with this next call
            profile.setLreWinSize(profile.getLreWinSize() + 1);
            ProfileInitializer.calcLreParameters(prfSum);
            if (runner.getNextCycle().getNextCycle() == null) {
                return;
            }
            runner = runner.getNextCycle();
        }
    }
}
