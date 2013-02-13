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
package org.lreqpcr.data_import_services;

//import org.lreqpcr.analysis_services.*;
//import org.lreqpcr.analysis_services.*;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.AverageProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Static methods for generating initialized AverageProfiles.
 * @author Bob Rutledge
 */
public class AverageProfileGenerator {

    /**
     * Generates a list of AverageSampleProfiles from the provided list of SampleProfiles.
     * Replicate profiles are identified by having identical sample and amplicon names.
     * Note that the average profiles will also be initialized.
     *
     * @param profileList a list of the Profiles to be processed
     * @param parentRun the Run from which this Profile dataset was derived
     * @param ocf the Experiment database OCF
     * @param parameters the LRE window selection parameters
     * @return a list containing generated AverageSampleProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<AverageSampleProfile> averageSampleProfileConstruction(
            List<SampleProfile> profileList,
            Run parentRun,
            double ocf,
            LreWindowSelectionParameters parameters) {
        ArrayList<SampleProfile> profileArray = new ArrayList<SampleProfile>(profileList);
        //Generate new ReplicatSampleProfiles for each replicate profile set within the profile list
        ArrayList<AverageSampleProfile> averageProfileList = new ArrayList<AverageSampleProfile>();
        //Parse out the replicate Profiles based on identical sample and amlipcon names
        //Prevent changes to the passed Profile list
        ArrayList<SampleProfile> listCopy = (ArrayList<SampleProfile>) profileArray.clone();
        while (!listCopy.isEmpty()) {
            SampleProfile profile = listCopy.get(0);
            AverageSampleProfile avSampleProfile = new AverageSampleProfile(parentRun);
            avSampleProfile.setProfileToVer0_8_0(true);
            avSampleProfile.setTargetStrandedness(profile.getTargetStrandedness());
            ArrayList<SampleProfile> replicateProfileList = new ArrayList<SampleProfile>();
            for (SampleProfile prf : listCopy) {
                try {
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
            intializeAverageProfile(avSampleProfile, parameters);
            averageProfileList.add(avSampleProfile);
        }
        return averageProfileList;
    }

    /**
     * Generates a list of AverageCalibrationProfiles from the provided list of CalibrationProfiles. 
     * Replicate profiles are identified by having identical sample and amplicon names.
     * Note that the average profiles will also be initialized. 
     * 
     * @param profileList the list of CalibrationProfiles 
     * @param rxnSetup the ReactionSetup object for this calibration
     * @param parameters the LRE window parameters
     * @param run the Run that generated the Profiles
     * @return a list of AverageCalibrationProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<AverageCalibrationProfile> averageCalbrationProfileConstruction(
            List<CalibrationProfile> profileList,
            LreWindowSelectionParameters parameters,
            Run run) {
        ArrayList<CalibrationProfile> profileArray = new ArrayList<CalibrationProfile>(profileList);
        //Generate new AverageCalibrationProfile for each replicate profile set within the profile list
        ArrayList<AverageCalibrationProfile> averageCalbnProfileList =
                new ArrayList<AverageCalibrationProfile>();
        //Parse out the replicate Profiles based on identical sample and amlipcon names
        //Prevent changes to the passed Profile list
        ArrayList<CalibrationProfile> listCopy = (ArrayList<CalibrationProfile>) profileArray.clone();
        while (!listCopy.isEmpty()) {
            Profile profile = listCopy.get(0);
            ArrayList<CalibrationProfile> calibrationProfileList = new ArrayList<CalibrationProfile>();
            for (CalibrationProfile prf : listCopy) {
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
            AverageCalibrationProfile avCalbnProfile = new AverageCalibrationProfile(run);
            avCalbnProfile.setProfileToVer0_8_0(true);
            CalibrationProfile firstCalibrationProfile = calibrationProfileList.get(0);
            //Amplicon size is required for initializing mo
            avCalbnProfile.setAmpliconSize(firstCalibrationProfile.getAmpliconSize());
            //This initializes mo
            avCalbnProfile.setLambdaMass(firstCalibrationProfile.getLambdaMass());
            avCalbnProfile.setName(firstCalibrationProfile.getName());
            avCalbnProfile.setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
            avCalbnProfile.setReplicateProfileList(calibrationProfileList);
            avCalbnProfile.setRawFcReadings(generateAverageFcDataset(calibrationProfileList));
            if (avCalbnProfile.getRawFcReadings().length != 0) {
                //0 indicates that sll replicate profiles must be excluded
                intializeAverageProfile(avCalbnProfile, parameters);
            }
            averageCalbnProfileList.add(avCalbnProfile);
        }
        return averageCalbnProfileList;
    }

    private static double[] generateAverageFcDataset(ArrayList<? extends Profile> replicateProfiles) {
        int numberOfCycles = replicateProfiles.get(0).getRawFcReadings().length;
        int numberOfProfiles = replicateProfiles.size();
        HashMap<Integer, double[]> fcMap = new HashMap<Integer, double[]>();
        int key = 0;
        for (int i = 0; i < numberOfProfiles; i++) {
            if (!replicateProfiles.get(i).isExcluded()) {
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
//                    throw new Exception();
//                    return null;
                }
                averageFcDataset[i] = fcSum / fcMap.size();
            }
        }
        return averageFcDataset;
    }

    /**
     * A clumsy attempt to avoid duplicate code for initializing Sample and Calibration AverageProfiles
     * @param averageProfile
     * @param parameters
     */
    @SuppressWarnings(value = "unchecked")
    private static void intializeAverageProfile(AverageProfile averageProfile, LreWindowSelectionParameters parameters) {
        LreAnalysisService profileIntialization =
                Lookup.getDefault().lookup(LreAnalysisService.class);
        //This is necessary because AverageProfile is an interface
        Profile fooProfile = (Profile) averageProfile;
        Profile firstRepProfile = averageProfile.getReplicateProfileList().get(0);
        fooProfile.setAmpliconName(firstRepProfile.getAmpliconName());
        fooProfile.setAmpliconSize(firstRepProfile.getAmpliconSize());
        fooProfile.setSampleName(firstRepProfile.getSampleName());
        fooProfile.setAmpTm(0);
        fooProfile.setName(fooProfile.getAmpliconName() + "@" + fooProfile.getSampleName());
        if (fooProfile.getRawFcReadings().length != 0) {
            //If the replicate No average is <10 it cannot be initialized
            AverageProfile avProfile = (AverageProfile) fooProfile;
            if (!avProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                //Note the the replicate sample profiles have already been initialized
                profileIntialization.conductAutomatedLreWindowSelection(fooProfile, parameters);
            }
        }
        for (Profile profile : averageProfile.getReplicateProfileList()) {
            profile.setParent(fooProfile);
        }
    }
}
