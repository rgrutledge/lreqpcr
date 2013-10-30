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
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.LREmath;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Provides functions required for initializing, editing and display of a Profile, which
 * primarily involves selection of, or processing changes to, the LRE window and updating the
 * associated LRE parameters within the Profile. A central aspect of these
 * functions is utilization of a linked-list of Cycle objects that represents
 * the cycles of the amplification profile within the Profile object.
 *
 * @author Bob Rutledge
 */
public abstract class ProfileSummary {

    private DatabaseServices db;
    private Profile profile;
    private Cycle zeroCycle;

    /**
     * 
     * @param profile the Profile to encapsulate
     * @param db the database holding the Profile
     */
    public ProfileSummary(Profile profile, DatabaseServices db) {
        this.profile = profile;
        this.db = db;
        initiateProfileSummary();
    }

    private void initiateProfileSummary() {
        update();
    }

    /**
     * Reinitializes this ProfileSummary's Cycle linked list, which is necessary
     * whenever the encapsulated Profile is modified. This involves either
     * changes to the LRE window or changes to the working Fc dataset. 
     * <p>
     * Note that the Profile is also saved to the database from which it was derived. 
     *
     */
    public void update() {
        //Create a new Cycle 
        makeCycleList();
        //If an window is absent nothing else can be done
        if (profile.hasAnLreWindowBeenFound()) {
//If this is an AverageProfile who's replicates are not clustered or it is <10N, it is invalid
            if(profile instanceof AverageProfile){
                AverageProfile avPrf = (AverageProfile) profile;
                if (!avPrf.areTheRepProfilesSufficientlyClustered() 
                        || avPrf.isTheReplicateAverageNoLessThan10Molecules()){
                    return;
                }
            }
            //This is a valid profile with a LRE window, so complete processing
            //Update the LRE parameters within the profile
            calcLreParameters();
            //Update the cycle Fo
            calcAllFo();
            //Update the average Fo within the Profile
            calcAverageFo();
            //Update the cycle predicted Fc
            calcPredictedFc();
            //Update C1/2
            profile.setMidC(LREmath.getMidC(profile.getDeltaE(), profile.getEmax(), profile.getAvFo()));
            db.saveObject(profile);
        }
    }

    /**
     * 
     * @return the encapsulated Profile
     */
    public Profile getProfile() {
        return profile;
    }
    
    /**
     * 
     * @return the database holding the encapsulated Profile
     */
    public DatabaseServices getDatabase(){
        return db;
    }
    
    /**
     * Saves the encapsulated Profile to the database from which is was derived
     */
    public void saveProfile(){
        db.saveObject(profile);
    }

    /**
     * Constructs a Cycle linked-list for a Profile that is used for display and
     * editing of the cycles within the amplification profile. Note that no
     * additional analysis is conducted other than to generate the link list in
     * which the Fc readings are set for each cycle.
     *
     */
    private void makeCycleList() {
        if (profile.getFcReadings() == null) {
            zeroCycle = null;
            return;
        }
        double[] fc = profile.getFcReadings();
        zeroCycle = new CycleImp(0, 0, null); //Zero cycle does not have a previous cycle
        Cycle prevCycle = zeroCycle; //Prepares for initialization of the link list construction
        for (int i = 0; i < fc.length; i++) { //Link list construction
            Cycle cycle = new CycleImp(i + 1, fc[i], prevCycle); //Sets the previous cycle pointer in Cycle constructor
            prevCycle.setNextCycle(cycle); //Sets the nextCycle pointer within the previous cycle object
            prevCycle = cycle; //Move to the next Cycle
        }
    }

    /**
     * Calculates and updates the LRE parameters within encapsulated Profile.
     * Note that the calling function is responsible for saving the modified
     * Profile. The ProfileSummary is also updated to allow display of the
     * modified Profile.
     *
     * @param prfSum the ProfileSummary holding the Profile to be processed
     */
    private void calcLreParameters() {
        if (profile.isExcluded() || !profile.hasAnLreWindowBeenFound()) {//Invalid profile
            return;//Invalid profile
        }
        Cycle runner = getLreWindowStartCycle();
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
        runner = getLreWindowStartCycle().getPrevCycle();
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
    private void calcAllFo() {
        //The Linked Cycle List is traversed & Fo values assigned to each cycle
        //The Cycle#-Fo Point2D.Double is also initialized 
        Cycle runner = zeroCycle.getNextCycle();//Start at cycle #1
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
    private void calcAverageFo() {
        //The current LRE window is traversed and the average Fo calculated
        double sumFo = 0;
        ArrayList<Double> foList = Lists.newArrayList();//Used to calculate average Fo CV
        //The Fo from the cycle previous to the start cycle must be included
        Cycle runner = getLreWindowStartCycle().getPrevCycle(); //First cycle to be included in the average
        for (int i = 0; i < profile.getLreWinSize() + 1; i++) { //Calculates the sum of the LRE window Fo values
            try {
                foList.add(runner.getFo());
            sumFo += runner.getFo();
            runner = runner.getNextCycle();
            } catch (Exception e) {
                int stop =0;
            }
            
        }
        //Calculate the LRE window average Fo value using the LRE-derived Emax
        double averageFo = (sumFo / (profile.getLreWinSize() + 1));
        //Sets the average Fo CV
        profile.setAvFoCV(MathFunctions.calcStDev(foList) / profile.getAvFo());
        //Sets the LRE window average Fo value calculated with Emax fixed to 100%
        //Setting the average Fo values will initiate an auto update within both Sample and Calibration Profiles
        profile.setAvFo(averageFo);
        //Goto to cycle 1
        runner = zeroCycle.getNextCycle();
//Sets the fractional difference between Fo and the averageFo across the entire profile using the LRE derived Emax
        while (runner.getNextCycle() != null) {
            runner.setFoFracFoAv(1 - (runner.getFo() / profile.getAvFo()));
            runner = runner.getNextCycle();
        }
    }

    /**
     * Calculates the predicted cycle fluorescence (Fc) across the entire Cycle
     * linked-list using the LRE-derived parameters. Note that the encapsulated
     * Profile is not modified.
     */
    private void calcPredictedFc() {
//The cycle linked-list is traversed & predicted Fc values assigned to each cycle
        Cycle runner = getZeroCycle().getNextCycle();//Goto cycle #1
        do {
            runner.setPredFc(LREmath.calcPrdFc(runner.getCycNum(), profile.getDeltaE(),
                    profile.getEmax(), profile.getAvFo()));
            runner = runner.getNextCycle();
        } while (runner != null);
    }

    /**
     *
     *
     * @return the zero cycle, that is, the header of the Cycle linked list,
     * from which individual cycles within the amplification profile can be
     * accessed.
     */
    public Cycle getZeroCycle() {
        return zeroCycle;
    }

    /**
     * Traverses the Cycle linked-list to the first cycle of the LRE window and
     * returns the corresponding Cycle object.
     *
     * @return the first Cycle of the LRE window or null if a LRE window has not
     * been found
     */
    public Cycle getLreWindowStartCycle() {
        if (!profile.hasAnLreWindowBeenFound()) {
            return null;
        }
        Cycle runner = zeroCycle;
        for (int i = 0; i < profile.getStrCycleInt(); i++) {
            runner = runner.getNextCycle();
        }
        return runner;
    }

    /**
     * Returns the Cycle object corresponding to the last cycle in the LRE window
     *
     * @return the Cycle corresponding to the last cycle in the LRE window or
     * null if a LRE window has not been found
     */
    public Cycle getLreWindowEndCycle() {
        if (!profile.hasAnLreWindowBeenFound()) {
            return null;
        }
        Cycle runner = zeroCycle;
        for (int i = 0; i < profile.getStrCycleInt() + profile.getLreWinSize() - 1; i++) {
            runner = runner.getNextCycle();
        }
        return runner;
    }
}
