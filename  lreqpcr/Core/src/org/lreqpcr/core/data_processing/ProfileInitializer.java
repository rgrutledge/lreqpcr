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
package org.lreqpcr.core.data_processing;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.utilities.LREmath;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Provides static functions for basic LRE initialization, except for automated
 * LRE window selection, which is provided as a separate service. This
 * facilitates modification to the algorithms used to select the LRE window.
 *
 * @author Bob Rutledge
 */
public class ProfileInitializer {

    /**
     * Encapsulates the supplied Profile in a ProfileSummary interface that is
     * used to edit and display the Profile.
     *
     * @param profile the Profile to be encapsulated
     * @return an initialized ProfileSummary
     */
    public static ProfileSummary constructProfileSummary(Profile profile) {
        ProfileSummaryImp prfSum = new ProfileSummaryImp(profile);
        //Initialize the linked Cycle list
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        //Set the start cycle within the linked Cycle list
        Cycle runner = prfSum.getZeroCycle();
        //Without an LRE window, nothing can be calculated
        if (profile.hasAnLreWindowBeenFound()) {
            //Run to the start cycle
            for (int i = 0; i < profile.getStrCycleInt(); i++) {
                runner = runner.getNextCycle();
            }
            prfSum.setStrCycle(runner);
//Calculate the cycle parameters for Cycle list which is necessary for initializing the runner
            updateProfileSummary(prfSum);
        }
        return prfSum;
    }

    /**
     * Updates the supplied ProfileSummary, which is necessary whenever the
     * encapsulated Profile is modified. This primarily involves either changes 
     * to the LRE window or changes to the Fb and/or Fb-slope that in turn modifies
     * the working Fc dataset
     *
     * @param prfSum
     */
    public static void updateProfileSummary(ProfileSummary prfSum) {
//Assume that the working Fc dataset has been modified and thus the Cycle list needs updating
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(prfSum.profile.getFcReadings()));
        //Update the LRE parameters within the profile
        calcLreParameters(prfSum);
        //Update the cycle Fo
        calcAllFo(prfSum);
        //Update the average Fo within the Profile
        calcAverageFo(prfSum);
        //Update the cycle predicted Fc
        calcAllpFc(prfSum);
        //Update C1/2
        Profile profile = prfSum.getProfile();
        profile.setMidC(LREmath.getMidC(profile.getDeltaE(), profile.getEmax(), profile.getAvFo()));
    }

    /**
     * Constructs a Cycle linked list for a Profile that is used for display and
     * editing of the associated Profile. Note that no additional analysis is
     * conducted other than to generate the link list.
     *
     * @param fc the array containing the background subtracted Fc dataset
     * @return the header (cycle zero) of the Cycle linked list
     */
    private static Cycle makeCycleList(double[] fc) {
        if (fc == null) {
            return null;
        }
        Cycle cycZero = new CycleImp(0, 0, null); //Zero cycle does not have a previous cycle
        Cycle prevCycle = cycZero; //Prepares for initialization of the link list construction
        for (int i = 0; i < fc.length; i++) { //Link list construction
            Cycle cycle = new CycleImp(i + 1, fc[i], prevCycle); //Sets the previous cycle pointer in Cycle constructor
            prevCycle.setNextCycle(cycle); //Sets the nextCycle pointer within the previous cycle object
            prevCycle = cycle; //Move to the next Cycle
        }
        return cycZero; //Returns the head pointer to the linked Cycle List
    }

    /**
     * Calculates and updates the LRE parameters within encapsulated Profile.
     * Note that the calling function is responsible for saving the modified
     * Profile. The ProfileSummary is also updated to allow display of the
     * modified Profile.
     *
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private static void calcLreParameters(ProfileSummary prfSum) {
        Profile profile = prfSum.getProfile();
        Cycle runner = prfSum.getZeroCycle();
        //Assume that the start cycle has been changed
        //Run to the start cycle and reset the ProfileSummary Start Cycle
        for (int i = 0; i < profile.getStrCycleInt(); i++) {
            runner = runner.getNextCycle();
        }
        prfSum.setStrCycle(runner);
        //Gather Fc and Ec from the LRE window
        int winSize = profile.getLreWinSize();
        double[][] lreWinPts = new double[2][winSize];
        for (int i = 0; i < winSize; i++) {
            try {
                lreWinPts[0][i] = runner.getFc();
            } catch (Exception e) {
                return;
            }
            lreWinPts[1][i] = runner.getEc();
            runner = runner.getNextCycle();
        }
        //Calculates and transfers the LRE parameters to the Profile
        double[] regressionValues = MathFunctions.linearRegressionAnalysis(lreWinPts);
        profile.setDeltaE(regressionValues[0]);
        profile.setEmax(regressionValues[1]);
        profile.setR2(regressionValues[2]);
        //Gather the LRE window Fc and predicted Fc
        //Start at the cycle immediately preceeding the start cycle
        runner = prfSum.getStrCycle().getPrevCycle();
        double[][] winFcpFc = new double[2][winSize + 1];
        //Transverse the LRE window Fc values to construct the Fc-pFc list
        try {
            for (int i = 0; i < winSize + 1; i++) {
                winFcpFc[0][i] = runner.getFc();
                winFcpFc[1][i] = runner.getPredFc();
                runner = runner.getNextCycle();
            }
        } catch (Exception e) {
            //Not sure if this is necessary
        }
    }

    /**
     * Calculates and assigns Fo values across the entire cycle profile using
     * the LRE parameters supplied by the current LRE window within the
     * encapsulated Profile. An average Fo value is also calculated across the
     * LRE window, and the fractional difference of each cycle Fo value vs. the
     * average Fo from the LRE window is set for each Cycle. Note that the
     * Profile is not modified.
     *
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private static void calcAllFo(ProfileSummary prfSum) {
        //The Linked Cycle List is traversed & Fo values assigned to each cycle
        //The Cycle#-Fo Point2D.Double is also initialized 
        Cycle runner = prfSum.getZeroCycle().getNextCycle();//Start at cycle #1
        Profile profile = prfSum.getProfile();
        do { //This should provide Fo and FoEmax100 values for all Cycles
            runner.setFo(LREmath.calcFo(runner.getCycNum(), runner.getFc(),
                    profile.getDeltaE(), profile.getEmax()));
            runner = runner.getNextCycle();
        } while (runner != null);
    }

    /**
     * Calculates the average Fo and Fo CV derived from the cycles within the
     * LRE window, using the Cycle linked-list within the ProfileSummary. Note
     * the encapsulated Profile is also updated and that the calling function is
     * responsibility to save the modified profile.
     *
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private static void calcAverageFo(ProfileSummary prfSum) {
        Profile profile = prfSum.getProfile();
        //The current LRE window is traversed and the average Fo calculated
        double sumFo = 0;
        ArrayList<Double> oFlist = Lists.newArrayList();//Used to calculate average Fo CV
        //The Fo from the cycle previous to the start cycle must be included
        Cycle runner = prfSum.getStrCycle().getPrevCycle(); //First cycle to be included in the average
        try {
            for (int i = 0; i < profile.getLreWinSize() + 1; i++) { //Calculates the sum of the LRE window Fo values
                oFlist.add(runner.getFo());
                sumFo += runner.getFo();
                runner = runner.getNextCycle();
            }
        } catch (Exception e) {
            // TODO present an error dialog...is this even necessary ??
        }

        //Calculate the LRE window average Fo value using the LRE-derived Emax
        double averageFo = (sumFo / (profile.getLreWinSize() + 1));
        //Sets the average Fo CV
        profile.setAvFoCV(MathFunctions.calcStDev(oFlist) / profile.getAvFo());
        //Sets the LRE window average Fo value calculated with Emax fixed to 100%
        //Setting the average Fo values will initiate an auto update within both Sample and Calibration Profiles
        profile.setAvFo(averageFo);
        //Goto to cycle 1
        runner = prfSum.getZeroCycle().getNextCycle();
//Sets the fractional difference between Fo and the averageFo across the entire profile using the LRE derived Emax
        while (runner.getNextCycle() != null) {
            runner.setFoFracFoAv(1 - (runner.getFo() / profile.getAvFo()));
            runner = runner.getNextCycle();
        }
    }

    /**
     * Calculates the predicted cycle fluorescence (Fc) across the entire cycle
     * linked-list using the LRE-derived parameters. Note that the encapsulated
     * Profile is not modified.
     *
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private static void calcAllpFc(ProfileSummary prfSum) {
        Profile profile = prfSum.getProfile();
        //The cycle linked-list is traversed & predicted Fc values assigned to each cycle
        Cycle runner = prfSum.getZeroCycle().getNextCycle();//Goto cycle #1
        do {
            runner.setPredFc(LREmath.calcPrdFc(runner.getCycNum(), profile.getDeltaE(),
                    profile.getEmax(), profile.getAvFo()));
            runner = runner.getNextCycle();
        } while (runner != null);
    }

    private static class ProfileSummaryImp extends ProfileSummary {

        public ProfileSummaryImp(Profile profile) {
            this.profile = profile;
        }
    }
    
    private static class CycleImp extends Cycle{

        public CycleImp(int cycleNumber, double fluorReading, Cycle previousCycle) {
            super(cycleNumber, fluorReading, previousCycle);
        }
    }
}
