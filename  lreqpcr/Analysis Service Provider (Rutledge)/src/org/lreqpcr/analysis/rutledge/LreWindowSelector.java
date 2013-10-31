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

import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.LREmath;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Static functions used for automated LRE window selection
 *
 * @author Bob Rutledge
 */
public class LreWindowSelector {

    /**
     * Automated selection of the LRE window Start Cycle within an uninitialized
     * Profile where a valid LRE window has not been identified.
     * <p>
     * The start cycle is set to the first cycle in which the LRE plot r2 value
     * derived from a window encompassing the two preceeding and two following
     * Cycles (5 cycles total), is above the r2 tolerance of 0.95. The start
     * cycle is the adjusted to two cycles below half of Fmax (~=C1/2),
     * generating an LRE window in the lower region of the Profle.
     * <p>
     * The LRE window size is then set to 3 cycles.
     * <p>
     *
     * As of version 0.8.5 inclusion of an Emax threshold, which must be >40%,
     * was found to greatly increase the accuracy of LRE window selection by
     * prevent false identification of the start of the profile. <p>
     *
     * @param prfSum the ProfileSummary encapsulating the Profile to be
     * processed
     */
    public static void selectLreStartCycleViaScanning(ProfileSummary prfSum) {
        double r2Tolerance = 0.95; //The tolerance of the LRE window r2 to determine the start of the profile
        double emaxThreshold = 0.4;//Emax > 40%
        double foThreshold = 0.06;//Default threshold
        int defaultLREwinSize = 3;//The default window size used for initiating Profiles
        Profile profile = prfSum.getProfile();
        if (profile instanceof AverageProfile) {
            AverageProfile avPrf = (AverageProfile) profile;
            if (!avPrf.areTheRepProfilesSufficientlyClustered()
                    || avPrf.isTheReplicateAverageNoLessThan10Molecules()) {
                //This is an invalid AverageProfile
                return;
            }
        }
        //Start at cycle 2
        Cycle runner = prfSum.getZeroCycle().getNextCycle().getNextCycle();
        //Find the start of the profile by running a 5 cycle window up
        //the profile until a LRE linear region is found based on the LRE plot r2
        //Calculate and set the LRE parameters across all Cycles within the profile
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
            double[] regressionValues;
            runner.setCycLREparam(MathFunctions.linearRegressionAnalysis(fcEcArray));
            int cycNum = runner.getCycNum();
            double fc = runner.getFc();
            regressionValues = runner.getCycLREparam();
            //Calculate Fo
            runner.setFo(LREmath.calcFo(
                    cycNum,
                    fc,
                    regressionValues[0],
                    regressionValues[1]));
            runner = runner.getNextCycle();
        }
        /*-----Finds start cycle based on the Cycle LRE r2-----*/
        //Reinitialize the runner
        runner = prfSum.getZeroCycle().getNextCycle().getNextCycle().getNextCycle(); //Start at cycle 3
        //Limit the analysis to 3 cycles before the end of the profile
        while (runner.getNextCycle().getNextCycle().getNextCycle() != null) {
//Test for the minimum r2 >r2 tolerance across 1 cycle before and after the target cycle
//Testing Emax was found to greatly increase the accuracy of the analysis in ver 0.8.5
            //[slope, intercept, r2]
            if (runner.getPrevCycle().getCycLREparam()[2] > r2Tolerance
                    && runner.getCycLREparam()[2] > r2Tolerance
                    && runner.getNextCycle().getCycLREparam()[2] > r2Tolerance
                    && runner.getCycLREparam()[1] > emaxThreshold) {
                profile.setStrCycleInt(runner.getCycNum()); //Sets the integer start cycle
                profile.setLreWinSize(defaultLREwinSize);
                profile.setHasAnLreWindowBeenFound(true);
//The prfSum MUST BE UPDATED because the LRE window has been modified
//Note that this reinstantiates the Cycle linked-list, invaliding the current runner
                prfSum.update();
                break;
            }
//If the end of the profile has been reached, LRE window selection has failed
            //Need at least two additional cycles for a window to be established
            if (runner.getNextCycle() == null || runner.getNextCycle().getNextCycle() == null) {
                processFailedProfile(profile);
                return;
            }
            runner = runner.getNextCycle(); //Advances to the next cycle
        }//End of while statement
/*-------Note that an LRE window must have been found in order to reach this point--------*/
        //However, an AverageProfile could be invalid due to profile scattering or <10N but still reach here...
        //Need an estimate of 1/2 Fmax
        optimizeLreWindow(prfSum, foThreshold);
        double halfFmax = (profile.getEmax() / -profile.getDeltaE()) / 2;
        if (Double.isNaN(halfFmax) || Double.isInfinite(halfFmax) || halfFmax <= 0) {
            processFailedProfile(profile);
            return;
        }
        //Reinstantiate the runner, needed in part because the prfSum has been updated
        runner = prfSum.getZeroCycle();
        //Run to the cycle with an Fc just > 1/2 Fmax
        while (halfFmax > runner.getFc()) {
            runner = runner.getNextCycle();
        }
        //Go back two cycles below 1/2 Fmax, which should be two cycles below C1/2
        runner = runner.getPrevCycle().getPrevCycle();
        profile.setStrCycleInt(runner.getCycNum());
        prfSum.update();
    }

    /**
     * This method selects the LRE window Start Cycle as the first cycle with an
     * Fc reading above the minimum fluorescence (minFc). Once a start cycle has
     * been found, a default 3 cycle LRE window is then set.
     *
     * @param prfSum the ProfileSummary to be processed
     * @param minFc the minimum fluorescence for setting the Start Cycle which
     * must be >0
     */
    public static void selectLreStartCycleUsingMinFc(ProfileSummary prfSum, double minFc) {
        if (minFc <= 0) {//Zero signifies that no minimum Fc has been set
            return;
        }
        Profile profile = prfSum.getProfile();
        //Must determine if a LRE window exsists
//This is needed to avoid artifacts generated by late partial profiles which reach min Fc     
        if (!profile.hasAnLreWindowBeenFound()) {
            selectLreStartCycleViaScanning(prfSum);
            if (!profile.hasAnLreWindowBeenFound()) {
//LRE window not found so abort attempting to use a min Fc
                processFailedProfile(profile);
                return;
            }
        }
        //Select the LRE window using the min Fc
        Cycle cycZero = prfSum.getZeroCycle();
        //Run to the first cycle above the minFc and set it as the start cycle
        Cycle runner = cycZero.getNextCycle().getNextCycle(); //Go to cycle 2
        while (runner.getFc() < minFc) {
            //Need at least 2 additional cycles to set window size to 3
            if (runner.getNextCycle() == null
                    || runner.getNextCycle().getNextCycle() == null) {
                //Reached the end of the profile...
                //This should never happen unless the min Fc is set too high
// TODO Present a warning that minFc is likely too large
                profile.setHasAnLreWindowBeenFound(false);
                processFailedProfile(profile);
                return;
            }
            //Move up one cycle
            runner = runner.getNextCycle();
        }
//The start cycle is set to the next cycle, because minFc should be applied to the denominator of Ec
        profile.setStrCycleInt(runner.getNextCycle().getCycNum());
        profile.setLreWinSize(3);
        prfSum.update();
    }

    /**
     * Expands the upper boundary of the LRE window. This is based upon the
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
     * Note also that this does not include nonlinear regression analysis, and
     * thus the Fc working dataset remains unmodified, and that the modified
     * Profile is saved via ProfileSummary.update().
     *
     * @param prfSum the ProfileSummary to be processed
     * @param foThreshold the Fo threshold used to determine whether the next
     * cycle should be included into the LRE window
     * @return returns true if a LRE window was optimized or false if an
     * optimized LRE window selection failed
     */
    public static boolean optimizeLreWindow(ProfileSummary prfSum, Double foThreshold) {
        Profile profile = prfSum.getProfile();
        //Go to the first cycle of the LRE window
        Cycle runner = prfSum.getLreWindowEndCycle();
        //Run to the last cycle of the LRE window
        if (runner == null || runner.getNextCycle() == null) {
//Failed Profile or the window is at the end of the profile and thus cannot be expanded
            return false;
        }
        //Set the upper boundary of the LRE window based on the Fo threshold
        //This also limits it to 95% of Fmax
        double fmaxThreshold = profile.getFmax() * 0.95;
        while (Math.abs(runner.getNextCycle().getFoFracFoAv()) < foThreshold
                && runner.getNextCycle().getFc() < fmaxThreshold) {
            //Increase and set the LRE window size by 1 cycle
            profile.setLreWinSize(profile.getLreWinSize() + 1);
            prfSum.update();//This instantiates a new Cycle list and saves the Profile
            runner = prfSum.getLreWindowEndCycle();//This resinstantiates the runner and moves it to the next cycle
            //It is unnecessary to move to the next cycle
            if (runner.getNextCycle() == null) {
                return true;//The end of the amplification profile has been reached
            }
        }
        return true;
    }

    /**
     * Sets the LRE window to 3 cycles and attempts to expand the upper boundary
     * of the LRE window using nonlinear regression to progressively optimize
     * the working Fc dataset.
     * <p>
     * Window expansion is based upon the difference between the average Fo
     * determined from the cycles within the LRE window and the Fo value derived
     * from the first cycle immediately above the LRE window. If this difference
     * is smaller than the Fo threshold, this next cycle is added to the LRE
     * window, and the analysis repeated.
     * <p>
     * Note that the upper limit of this expansion is limited to the cycle Fc
     * less than 95% of Fmax, eliminating the possibility of including plateau
     * phase cycles into the LRE window.
     * <p>
     * Note also that the Profile must have a valid LRE window and that the
     * modified Profile is saved via ProfileSummary.update().
     *
     * @param prfSum the ProfileSummary to be processed
     * @param foThreshold the Fo threshold used to determine whether the next
     * cycle should be included into the LRE window
     * @return returns true if a LRE window was optimized or false if an
     * optimized LRE window selection failed
     */
    public static boolean optimizeLreWindowUsingNR(ProfileSummary prfSum, Double foThreshold) {
        Profile profile = prfSum.getProfile();
        //A vaild LRE window must be present
        if (!profile.hasAnLreWindowBeenFound()) {
            return false;
        }
        //Reset the window size to 3 cycle in order to ensure optimized LRE parameters
        profile.setLreWinSize(3);
        prfSum.update();
        NonlinearRegressionImplementation nrAnalysis = new NonlinearRegressionImplementation();
        //Conduct a preliminary NR to stabilize the LRE analysis
        nrAnalysis.conductNonlinearRegressionOptimization(prfSum);
        //Place a runner at the last cycle of the window
        Cycle runner = prfSum.getLreWindowEndCycle();
        //Try to expand the upper region of the window based on the Fo threshold
        //This also limits the top of the LRE window to 95% of Fmax
        double fmaxThreshold = profile.getFmax() * 0.95;
        while (Math.abs(runner.getNextCycle().getFoFracFoAv()) < foThreshold
                && runner.getNextCycle().getFc() < fmaxThreshold) {
            //Increase and set the LRE window size by 1 cycle
            profile.setLreWinSize(profile.getLreWinSize() + 1);
            //Need to conduct nonlinear regression analysis
            //First update the LRE parameters, which also generates a new Cycle linked list
            prfSum.update();
            //Conduct NR which also updates the LRE parameters and instantiates a new Cycle list
            nrAnalysis.conductNonlinearRegressionOptimization(prfSum);
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

    /**
     * Process a Profile for which an LRE window could not be found by resetting
     * all LRE parameters to zero.
     *
     * @param failedProfile
     */
    private static void processFailedProfile(Profile failedProfile) {
        failedProfile.setLreVariablesToZero();
    }

    /**
     * This is a very crude method based on averaging cycles 4-9 to determine
     * the fluorescence background (Fb). Starting at cycle 4 avoids aberrant
     * fluorescence readings that are is commonly observed for cycles 1-3.
     *
     * @param profile the Profile to be processed
     */
    public static void substractBackgroundUsingAvFc(Profile profile) {
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
