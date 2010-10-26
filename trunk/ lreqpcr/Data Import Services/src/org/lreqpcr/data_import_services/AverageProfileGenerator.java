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
import org.lreqpcr.core.data_objects.ReactionSetup;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Generates AverageProfiles
 * @author Bob Rutledge
 */
public class AverageProfileGenerator {

    /**
     *
     * @param profileList a list of the Profiles to be processed
     * @param parentRun the Run from which this Profile dataset was derived
     * @param ocf the average OCF
     * @param parameters the LRE window parameters
     * @return a list containing generated AverageSampleProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<AverageSampleProfile> averageSampleProfileConstruction(
            List<Profile> profileList,
            Run parentRun,
            double ocf,
            LreWindowSelectionParameters parameters) {
        ArrayList<Profile> profileArray = new ArrayList<Profile>(profileList);
        //Generate new ReplicatSampleProfiles for each replicate profile set within the profile list
        ArrayList<AverageSampleProfile> averageProfileList = new ArrayList<AverageSampleProfile>();
        //Parse out the replicate Profiles based on identical sample and amlipcon names
        //Prevent changes to the passed Profile list
        ArrayList<Profile> listCopy = (ArrayList<Profile>) profileArray.clone();
        while (!listCopy.isEmpty()) {
            Profile profile = listCopy.get(0);
            AverageSampleProfile avSampleProfile = new AverageSampleProfile();
            avSampleProfile.setTargetStrandedness(profile.getTargetStrandedness());
            ArrayList<Profile> replicateProfileList = new ArrayList<Profile>();
            for (Profile prf : listCopy) {
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
            for (Profile prf : replicateProfileList) {
                listCopy.remove(prf);
            }
            avSampleProfile.setParent(parentRun);
            avSampleProfile.setOCF(ocf);
            avSampleProfile.updateProfile();
            avSampleProfile.setReplicateProfileList(replicateProfileList);
            avSampleProfile.setRawFcReadings(generateAverageFcDataset(replicateProfileList));
            intializeAverageProfile(avSampleProfile, parameters);
            averageProfileList.add(avSampleProfile);
        }
        return averageProfileList;
    }

    /**
     * Generates a list of AverageCalibrationProfiles from the provided list of CalibrationProfiles. 
     * Replicate profiles are identified by having identical sample and amplicon names.
     * 
     * @param profileList the list of CalibrationProfiles 
     * @param rxnSetup the ReactionSetup object for this calibration
     * @param parameters the LRE window parameters
     * @return a list of AverageCalibrationProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public static List<? extends Profile> averageCalbrationProfileConstruction(
            List<Profile> profileList,
            ReactionSetup rxnSetup,
            LreWindowSelectionParameters parameters) {
        ArrayList<Profile> profileArray = new ArrayList(profileList);
        //Generate new AverageCalibrationProfile for each replicate profile set within the profile list
        ArrayList<AverageCalibrationProfile> averageCalbnProfileList =
                new ArrayList<AverageCalibrationProfile>();
        //Parse out the replicate Profiles based on identical sample and amlipcon names
        //Prevent changes to the passed Profile list
        ArrayList<Profile> listCopy = (ArrayList<Profile>) profileArray.clone();
        while (!listCopy.isEmpty()) {
            Profile profile = listCopy.get(0);
            ArrayList<Profile> calibrationProfileList = new ArrayList<Profile>();
            for (Profile prf : listCopy) {
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
            for (Profile prf : calibrationProfileList) {
                listCopy.remove(prf);
            }
            //Average the replicates Profile raw Fc datasets
            AverageCalibrationProfile avCalbnProfile = new AverageCalibrationProfile();
            avCalbnProfile.setParent(rxnSetup);
            CalibrationProfile firstSampleProfile = (CalibrationProfile) calibrationProfileList.get(0);
            avCalbnProfile.setLambdaMass(firstSampleProfile.getLambdaMass() * 1000000);
            avCalbnProfile.setName(firstSampleProfile.getName());
            avCalbnProfile.setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
            avCalbnProfile.setReplicateProfileList(calibrationProfileList);
            avCalbnProfile.setRawFcReadings(generateAverageFcDataset(calibrationProfileList));
            if (avCalbnProfile.getRawFcReadings().length != 0) {
                //0 indicates that sll replicate profiles must be excluded
                intializeAverageProfile(avCalbnProfile, parameters);
                avCalbnProfile.updateProfile();
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
                fcSum += fcMap.get(j)[i];
            }
            averageFcDataset[i] = fcSum / fcMap.size();
        }
        return averageFcDataset;
    }

    @SuppressWarnings(value = "unchecked")
    private static void intializeAverageProfile(AverageProfile averageProfile, LreWindowSelectionParameters parameters) {
        LreAnalysisService profileIntialization =
                Lookup.getDefault().lookup(LreAnalysisService.class);
        ArrayList<Profile> replicateList =
                (ArrayList<Profile>) averageProfile.getReplicateProfileList();
        Profile avProfile = (Profile) averageProfile;
        Profile firstRepProfile = replicateList.get(0);
        avProfile.setRunDate(firstRepProfile.getRunDate());
        avProfile.setAmpliconName(firstRepProfile.getAmpliconName());
        avProfile.setAmpliconSize(firstRepProfile.getAmpliconSize());
        avProfile.setSampleName(firstRepProfile.getSampleName());
        avProfile.setAmpTm(0);
        avProfile.setName(avProfile.getAmpliconName() + " @ " + avProfile.getSampleName());
        if (avProfile.getRawFcReadings().length == 0) {
            //All of the replicate profiles are excluded
            avProfile.setLongDescription("AN LRE WINDOW COULD NOT BE FOUND");
            avProfile.setShortDescription("An LRE window could not be found");
            avProfile.setExcluded(true);
        } else {
            profileIntialization.initializeProfile(avProfile, parameters);
        }
        for (Profile profile : replicateList) {
            profile.setParent(avProfile);
        }
    }
}