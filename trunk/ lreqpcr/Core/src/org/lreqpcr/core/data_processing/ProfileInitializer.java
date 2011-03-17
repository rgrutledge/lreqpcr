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

package org.lreqpcr.core.data_processing;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.lreqpcr.core.utilities.LREmath;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Provides static functions for basic LRE initialization, excluding automated LRE
 * window selection. This approach facilitates modification
 * to the algorithms used to select the LRE window.
 *
 * @author Bob Rutledge
 */
public class ProfileInitializer {

    /**
     * Constructs a Cycle linked list for a Profile.
     * Note that no additional analysis is conducted other than to generate 
     * the link list.
     *
     * @param fc the array containing the Fc dataset
     * @return the header (cycle zero) of the Cycle linked list
     */
    public static Cycle makeCycleList(double[] fc) {
        if(fc == null){
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
     * Initializes a LRE window based upon a the supplied LRE start cycle
     *
     *<p>Generates a new LRE window using the supplied start cycle. This is 
     * used for initializing a new Profile, or for adjusting
     * the LRE window in a previously initialized Profile 
     *
     *@param prfSum the ProfileSummary holding the Profile to be processed
     *
     **/
    public static void calcLreParameters(ProfileSummary prfSum) {
        //Reset the StartCycle in prfSum
        Profile profile = prfSum.getProfile();
        Cycle runner = prfSum.getZeroCycle();
        //Run to the start cycle
        for (int i = 0; i < profile.getStrCycleInt(); i++) {
            runner = runner.getNextCycle();
        }
        prfSum.setStrCycle(runner);
        int winSize = profile.getLreWinSize();
        if (profile.getLreWinSize() == 0) {//New Profile
            winSize = 3;
            profile.setLreWinSize(winSize);
        }
        if(winSize <1) return;
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
        if(profile.getDeltaE() >0){//Start cycle selection is corrupted by Fb drift
            profile.setStrCycleInt(profile.getStrCycleInt() +1);
            calcLreParameters(prfSum);
        }
        //Update the linked cycle list
        calcAllFo(prfSum);
        calcAverageFo(prfSum);
        calcAllpFc(prfSum);
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
        }
        
        profile.setNonR2(LREmath.calcNonLinearR2(winFcpFc));
        profile.setMidC(LREmath.getMidC(profile.getDeltaE(), profile.getEmax(), profile.getAvFo()));
        profile.updateProfile();
    }

    /**
     * Calculates and assigns Fo values across the entire cycle profile
     * using the LRE parameters are supplied by the current LRE window from
     * the Profile. An average Fo value is also calculated across 
     * the LRE window, and the fractional difference of each cycle Fo value 
     * vs. the average Fo from the LRE window is set for each Cycle.
     * 
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private static void calcAllFo(ProfileSummary prfSum) {
        //The Linked Cycle List is traversed & Fo values assigned to each cycle
        //The Cycle#-Fo Point2D.Double is also initialized 
        Cycle runner = prfSum.getZeroCycle().getNextCycle();//Start at cycle #1
        Profile profile = prfSum.getProfile();
        do { //This should provide Fo values to all Cycles
            runner.setFo(LREmath.calcFo(runner.getCycNum(), runner.getFc(),
                    profile.getDeltaE(), profile.getEmax()));
            runner.setAdjustedFo(LREmath.calcAdjustedFo(runner.getCycNum(), runner.getFc(),
                    profile.getDeltaE(), profile.getEmax()));
            runner = runner.getNextCycle();
        } while (runner != null);
    }

      /**
     * Calculates the average Fo across the LRE Window using the Cycle 
     *linked list generated from the Profile.
       * 
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private static void calcAverageFo(ProfileSummary prfSum) {
        Profile profile = prfSum.getProfile();
        //The current LRE window is traversed and the average Fo calculated
        double sumFo = 0;
        double adjustedFoSum = 0;
        ArrayList<Double> oFlist = Lists.newArrayList();//Used to calculate average Fo CV
        ArrayList<Double> adjustedFoList = Lists.newArrayList();
        //The Fo from the cycle previous to the start cycle must be included
        Cycle runner = prfSum.getStrCycle().getPrevCycle(); //First cycle to be included in the average
        try {
            for (int i = 0; i < profile.getLreWinSize() + 1; i++) { //Calculates the sum of the LRE window Fo values
            oFlist.add(runner.getFo());
            sumFo += runner.getFo();
            adjustedFoList.add(runner.getAdjustedFo());
            adjustedFoSum += runner.getAdjustedFo();
            runner = runner.getNextCycle();
        }
        } catch (Exception e) {
            // TODO present an error dialog...is this even necessary ??
        }
        
        profile.setAvFo(sumFo / (profile.getLreWinSize() + 1)); //Sets the LRE window average Fo value
        profile.setAvFoCV(MathFunctions.calcStDev(oFlist) / profile.getAvFo()); //Sets the average Fo CV
        profile.setAdjustedAvFo((adjustedFoSum / (profile.getLreWinSize() + 1))); //Sets the LRE window average Fo value
        //Goto to cycle 1
        runner = prfSum.getZeroCycle().getNextCycle();
        //Sets the fractional difference between Fo and the averageFo across the entire profile
        while (runner.getNextCycle() != null) {
            runner.setFoFracFoAv(1 - (runner.getFo() / profile.getAvFo()));
            runner = runner.getNextCycle();
        }
    }

    /**
     * Calculates the predicted Fc across the entire cycle profile
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
        }while (runner != null);
    }
}