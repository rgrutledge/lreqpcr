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
import java.util.List;
import org.lreqpcr.core.data_processing.BaselineSubtraction;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.ExperimentDbInfo;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;

/**
 *
 *
 * @author Bob Rutledge
 */
public class LreAnalysisProvider extends LreAnalysisService {

    public LreAnalysisProvider() {
    }

    @Override
    public boolean conductAutomatedLreWindowSelection(Profile profile, LreWindowSelectionParameters parameters) {
        profile.setHasAnLreWindowBeenFound(false);
        //Construct a ProfileSummary which is used for automated LRE window selection 
        //Subtract background fluorescence
        if (profile.getFcReadings() == null) {
            //A new profile that requires baseline substraction
            BaselineSubtraction.baselineSubtraction(profile);
        }
        ProfileSummaryImp prfSum = new ProfileSummaryImp(profile);
        prfSum.setZeroCycle(ProfileInitializer.makeCycleList(profile.getFcReadings()));
        //Try to find an LRE window
        LreWindowSelector.selectLreWindowUsingMinFc(prfSum, parameters);
        if (!profile.hasAnLreWindowBeenFound()) {
//Failed to find a window, thus return as updating the LRE parameters is not relevant
            return false;
        }
        ProfileInitializer.calcLreParameters(prfSum);
        return true;
    }

    public ProfileSummary initializeProfileSummary(Profile profile, LreWindowSelectionParameters parameters) {
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
            //Calculate the cycle parameters for Cycle list
            ProfileInitializer.calcAllFo(prfSum);
            ProfileInitializer.calcAverageFo(prfSum);
            ProfileInitializer.calcAllpFc(prfSum);
        }
        return prfSum;
    }

    public boolean updateLreWindow(ProfileSummary prfSum) {
        //All that is needed is to update the LRE parameters
        ProfileInitializer.calcLreParameters(prfSum);
        return true;
    }

    /**
     * Converts a database to version 0.8.0 which includes saving the modified
     * profiles and comitting the changes to disk.
     * 
     * @param db the database to be converted to version 0.8.0
     */
    @Override
    public void convertDatabaseToNewVersion(DatabaseServices db) {

        if (db.getDatabaseType() == DatabaseType.EXPERIMENT) {
            //Need to retrieve the OCF
            ExperimentDbInfo dbInfo = (ExperimentDbInfo) db.getAllObjects(ExperimentDbInfo.class).get(0);
            LreWindowSelectionParameters parameters = (LreWindowSelectionParameters) db.getAllObjects(LreWindowSelectionParameters.class).get(0);
            double ocf;
            List runList = db.getAllObjects(Run.class);
            //This is necessary to apply Run-specific OCFs
            for (Object o : runList) {
                Run run = (Run) o;
                if (run.getRunOCF() != 0) {
                    ocf = run.getRunOCF();
                } else {
                    ocf = dbInfo.getOcf();
                }
                ArrayList<AverageSampleProfile> avPrfList = (ArrayList<AverageSampleProfile>) run.getAverageProfileList();
                for (AverageSampleProfile avProfile : avPrfList) {
                    avProfile.setProfileToVer0_8_0(true);
                    //Convert all of the replicate profiles to version 0.8.0
                    for (SampleProfile repSamplePrf : avProfile.getReplicateProfileList()) {
                        repSamplePrf.setProfileToVer0_8_0(true);
                        //See if an LRE window is present
                        if (repSamplePrf.getLreWinSize() > 2) {
                            //This should preserve any user modifications to the LRE window
                            repSamplePrf.setHasAnLreWindowBeenFound(true);
                            //This will initiate all of the Emax100 fields
                            initializeProfileSummary(repSamplePrf, parameters);
                        } else {
                            repSamplePrf.setHasAnLreWindowBeenFound(false);
                        }
                        //This will trigger an auto update of the SampleProfile
                        repSamplePrf.setOCF(ocf);
                        db.saveObject(repSamplePrf);
                    }
                    if (avProfile.getLreWinSize() > 2) {
                        //This should preserve any user modifications to the LRE window
                        avProfile.setHasAnLreWindowBeenFound(true);
                        //This will initiate all of the Emax100 fields
                        initializeProfileSummary(avProfile, parameters);
                    } else {
                        avProfile.setHasAnLreWindowBeenFound(false);
                    }
                    //This will trigger an auto update of the AverageSampleProfile
                    avProfile.setOCF(ocf);
                    avProfile.isTheReplicateAverageNoLessThan10Molecules();
                    db.saveObject(avProfile);
                }
            }
            db.commitChanges();
        }

        if (db.getDatabaseType() == DatabaseType.CALIBRATION) {
            LreWindowSelectionParameters parameters = (LreWindowSelectionParameters) db.getAllObjects(LreWindowSelectionParameters.class).get(0);
            List avProfileList = db.getAllObjects(AverageCalibrationProfile.class);
            for (Object o : avProfileList) {
                AverageCalibrationProfile avProfile = (AverageCalibrationProfile) o;
                avProfile.setProfileToVer0_8_0(true);
                //Convert all of the replicate profiles to version 0.8.0
                for (CalibrationProfile repPrf : avProfile.getReplicateProfileList()) {
                    repPrf.setProfileToVer0_8_0(true);
                    //See if an LRE window is present
                    if (repPrf.getLreWinSize() > 2) {
                        //This should preserve any user modifications to the LRE window
                        repPrf.setHasAnLreWindowBeenFound(true);
                        //This is necessary to initialize mo
                        repPrf.setLambdaMass(repPrf.getLambdaMass());
                        //This will initiate all of the Emax100 fields
                        initializeProfileSummary(repPrf, parameters);
                    } else {
                        repPrf.setHasAnLreWindowBeenFound(false);
                    }
                    db.saveObject(repPrf);
                }
                if (avProfile.getLreWinSize() > 2) {
                    //This should preserve any user modifications to the LRE window
                    avProfile.setHasAnLreWindowBeenFound(true);
                    //This is necessary to initialize mo
                    avProfile.setLambdaMass(avProfile.getLambdaMass());
                    //This will initiate all of the Emax100 fields
                    initializeProfileSummary(avProfile, parameters);
                } else {
                    avProfile.setHasAnLreWindowBeenFound(false);
                }
                db.saveObject(avProfile);
            }
            db.commitChanges();
        }
    }
}
