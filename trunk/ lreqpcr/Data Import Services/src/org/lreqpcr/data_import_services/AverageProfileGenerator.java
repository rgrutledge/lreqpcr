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
// TODO this should be moved into core utilities
package org.lreqpcr.data_import_services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Static methods for generating initialized AverageProfiles.
 *
 * @author Bob Rutledge
 */
public class AverageProfileGenerator {

    /**
     * Generates a list of AverageSampleProfiles from the provided list of
     * SampleProfiles. Replicate profiles are identified by having identical
     * sample and amplicon names. Note that the average profiles will also be
     * initialized.
     *
     * @param profileList a list of the Profiles to be processed
     * @param parentRun the Run from which this Profile dataset was derived
     * @param ocf the Experiment database OCF
     * @param parameters the LRE window selection parameters
     * @return a list containing generated AverageSampleProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<AverageProfile> averageSampleProfileConstruction(
            List<SampleProfile> profileList,
            Run parentRun,
            double ocf,
            LreWindowSelectionParameters parameters,
            DatabaseServices db) {
        ArrayList<SampleProfile> profileArray = new ArrayList<SampleProfile>(profileList);
        //Generate new ReplicatSampleProfiles for each replicate profile set within the profile list
        ArrayList<AverageProfile> averageProfileList = new ArrayList<AverageProfile>();
        //Parse out the replicate Profiles based on identical sample and amlipcon names
        //Prevent changes to the passed Profile list
        ArrayList<SampleProfile> listCopy = (ArrayList<SampleProfile>) profileArray.clone();
        while (!listCopy.isEmpty()) {
            SampleProfile profile = listCopy.get(0);
            AverageSampleProfile avSampleProfile = new AverageSampleProfile();
            avSampleProfile.setRunDate(parentRun.getRunDate());
            avSampleProfile.setRun(parentRun);//Also sets the run date and sets the parent to this Runt
            avSampleProfile.setTargetStrandedness(profile.getTargetStrandedness());
            ArrayList<SampleProfile> replicateProfileList = new ArrayList<SampleProfile>();
            //This orders the sample profiles into the correct AverageProfile list using the sample and amplicon name.
            //This assumes that profiles with the same sample and name are replicate profiles
            for (SampleProfile prf : listCopy) {
                try {
                    //This is needed because in version 0.8.6 the Run is not available during profile creation 
                    //Run objects are now created by the Run initializer
                    if (prf.getRun() == null) {
                        prf.setRun(parentRun);
                    }
                    if (profile.getAmpliconName().equals(prf.getAmpliconName()) && profile.getSampleName().equals(prf.getSampleName())) {
                        replicateProfileList.add(prf);
                    }
                } catch (Exception e) {
                    String well = "Unknown";
                    if (prf.getAmpliconName() == null || prf.getSampleName() == null) {
                        well = prf.getWellLabel();
                    }
                    String msg = "Either the Amplicon or Sample\n name is missing for well: "
                            + well + "\nData import will be aborted";
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                            msg,
                            "Missing Amplicon or Sample",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            //Remove the replicate Profiles from the list copy
            for (SampleProfile prf : replicateProfileList) {
                listCopy.remove(prf);
            }
            avSampleProfile.setReplicateProfileList(replicateProfileList);
            avSampleProfile.setRawFcReadings(generateAverageFcDataset(replicateProfileList));
            avSampleProfile.setOCF(ocf);
            intializeAverageProfile(avSampleProfile, parameters, db);
            averageProfileList.add(avSampleProfile);
        }
        return averageProfileList;
    }

    /**
     * Generates a list of AverageCalibrationProfiles from the provided list of
     * CalibrationProfiles. Replicate profiles are identified by having
     * identical sample and amplicon names. Note that the average profiles will
     * also be initialized.
     *
     * @param profileList the list of CalibrationProfiles
     * @param rxnSetup the ReactionSetup object for this calibration
     * @param parameters the LRE window parameters
     * @param parentRun the Run that generated the Profiles
     * @return a list of AverageCalibrationProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<AverageProfile> averageCalbrationProfileConstruction(
            List<CalibrationProfile> profileList,
            LreWindowSelectionParameters parameters,
            Run parentRun,
            DatabaseServices db) {
        ArrayList<CalibrationProfile> profileArray = new ArrayList<CalibrationProfile>(profileList);
        //Generate new AverageCalibrationProfile for each replicate profile set within the profile list
        ArrayList<AverageProfile> averageCalbnProfileList =
                new ArrayList<AverageProfile>();
        //Parse out the replicate Profiles based on identical sample and amlipcon names
        //Prevent changes to the passed Profile list
        ArrayList<CalibrationProfile> listCopy = (ArrayList<CalibrationProfile>) profileArray.clone();
        while (!listCopy.isEmpty()) {
            Profile profile = listCopy.get(0);
            ArrayList<CalibrationProfile> calibrationProfileList = new ArrayList<CalibrationProfile>();
            for (CalibrationProfile prf : listCopy) {
                //This is needed because in version 0.8.6 the Run is not available during profile creation 
                //Run objects are now created by the Run initializer
                if (prf.getRun() == null) {
                    prf.setRun(parentRun);
                }
                try {
                    if (profile.getAmpliconName().equals(prf.getAmpliconName()) && profile.getSampleName().equals(prf.getSampleName())) {
                        calibrationProfileList.add(prf);
                    }
                } catch (Exception e) {
                    String well = "Unknown";
                    if (prf.getAmpliconName() == null || prf.getSampleName() == null) {
                        well = prf.getWellLabel();
                    }
                    String msg = "Either the Calibration Amplicon or Sample\n name is missing for well: "
                            + well + "\n Calibration data import will be aborted";
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                            msg,
                            "Missing Calibration Amplicon or Sample",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            //Remove the replicate Profiles from the list copy
            for (CalibrationProfile prf : calibrationProfileList) {
                listCopy.remove(prf);
            }
            //Average the replicates Profile raw Fc datasets
            AverageCalibrationProfile avCalbnProfile = new AverageCalibrationProfile();
            avCalbnProfile.setRun(parentRun);//This also sets the Run data and the parent to this run
            avCalbnProfile.setRunDate(parentRun.getRunDate());
            CalibrationProfile firstCalibrationProfile = calibrationProfileList.get(0);
//LamdaMass is required for initializing mo and must be converted back to fg as it is stored as ng in CalibrationProfile
            avCalbnProfile.setLambdaMass(firstCalibrationProfile.getLambdaMass() * 1000000);
            //This initializes mo and OCF
            avCalbnProfile.setAmpliconSize(firstCalibrationProfile.getAmpliconSize());
            avCalbnProfile.setName(firstCalibrationProfile.getName());
            avCalbnProfile.setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
            avCalbnProfile.setReplicateProfileList(calibrationProfileList);
            avCalbnProfile.setRawFcReadings(generateAverageFcDataset(calibrationProfileList));
            if (avCalbnProfile.getRawFcReadings().length != 0) {
                //0 indicates that all replicate profiles must be excluded
                intializeAverageProfile(avCalbnProfile, parameters, db);
            }
            averageCalbnProfileList.add(avCalbnProfile);
        }
        return averageCalbnProfileList;
    }
// TODO this is redundant to org.lreqpcr.core.utilities 
//GenerateAverageFcDataset.public static double[] generateAverageFcDataset(List<? extends Profile> replicates)

    private static double[] generateAverageFcDataset(ArrayList<? extends Profile> replicateProfiles) {
        int numberOfCycles = replicateProfiles.get(0).getRawFcReadings().length;
        int numberOfProfiles = replicateProfiles.size();
        HashMap<Integer, double[]> fcMap = new HashMap<Integer, double[]>();
        int key = 0;
        for (int i = 0; i < numberOfProfiles; i++) {
            if (!replicateProfiles.get(i).isExcluded()
                    && replicateProfiles.get(i).hasAnLreWindowBeenFound()) {
                fcMap.put(key, replicateProfiles.get(i).getRawFcReadings());
                key++;
            }
        }
        if (fcMap.isEmpty()) {
            //All of the replicate profiles are excluded
            //return an empty double[]
            return new double[0];
        }
        //Calculate average Fc for each cycle and put it into a new double[]
        double[] averageFcDataset = new double[numberOfCycles];
        for (int i = 0; i < numberOfCycles; i++) {
            double fcSum = 0;
            for (int j = 0; j < fcMap.size(); j++) {
                try {
                    fcSum += fcMap.get(j)[i];
                } catch (Exception e) {
                    String msg = "An error has occurred, likely due replicate profiles containing "
                            + "\ndifferent number of cycles. Be sure the import file only contains "
                            + "\nprofiles containing the same number of cycles.";
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                            msg,
                            "Data Emport Error",
                            JOptionPane.ERROR_MESSAGE);
                    // TODO Do something with this...low priority
//                    throw new Exception();
//                    return null;
                }
                averageFcDataset[i] = fcSum / fcMap.size();
            }
        }
        return averageFcDataset;
    }

    /**
     * A clumsy attempt to avoid duplicate code for initializing Sample and
     * Calibration AverageProfiles
     *
     * @param averageProfile
     * @param parameters
     */
    @SuppressWarnings(value = "unchecked")
    private static void intializeAverageProfile(AverageProfile averageProfile, LreWindowSelectionParameters parameters, DatabaseServices db) {
        LreAnalysisService lreAnalysisService =
                Lookup.getDefault().lookup(LreAnalysisService.class);
        //This is necessary because AverageProfile is an interface
        Profile fooProfile = (Profile) averageProfile;
        Profile firstRepProfile = averageProfile.getReplicateProfileList().get(0);
        fooProfile.setAmpliconName(firstRepProfile.getAmpliconName());
        fooProfile.setAmpliconSize(firstRepProfile.getAmpliconSize());
        fooProfile.setSampleName(firstRepProfile.getSampleName());
        fooProfile.setName(fooProfile.getAmpliconName() + "@" + fooProfile.getSampleName());
        //If the replicate No average is <10 it cannot be initialized
        AverageProfile avProfile = (AverageProfile) fooProfile;
        if (!avProfile.isTheReplicateAverageNoLessThan10Molecules() && avProfile.areTheRepProfilesSufficientlyClustered()) {//
            //Note the the replicate sample profiles have already been initialized
            ProfileSummary prfSum = new ProfileSummaryImp(fooProfile, db);
            lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, parameters);
        }
        for (Profile profile : averageProfile.getReplicateProfileList()) {
            profile.setParent(fooProfile);
        }
    }
}
