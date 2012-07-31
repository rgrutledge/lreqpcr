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
package org.lreqpcr.core.data_objects;

import java.util.List;

/**
 * An average sample profile generated by averaging the Fc datasets
 * produced by  replicates sample profiles.
 * This increases Fc read precision which in turn generates a more reliable analysis.
 *
 * @author Bob Rutledge
 */
public class AverageSampleProfile extends SampleProfile implements AverageProfile {

    private List<SampleProfile> sampleProfileList;
    private boolean isTheAverageReplicateNoLessThan10Molecules;
//    private int numberOfActiveReplicateProfiles;

    /**
     * An average sample profile constructed from its sample replicate profiles.
     */
    public AverageSampleProfile(Run run) {
        super(run);
        setChildClass(SampleProfile.class);
    }

    /**
     *
     * @return list of replicate sample profiles
     */
    public List<SampleProfile> getReplicateProfileList() {
        return sampleProfileList;
    }

    /**
     * Will throw an illegal cast exception if the list does not contain
     * Sample Profiles.
     *
     * @param replicateProfileList
     */
    @SuppressWarnings(value = "unchecked")
    public void setReplicateProfileList(List<? extends Profile> replicateProfileList) {
        this.sampleProfileList = (List<SampleProfile>) replicateProfileList;
    }

    @Override
    public void updateProfile() {
        //Without an OCF, No values cannot be calculated
        if (getOCF() > 0) {
            determineIfTheAverageReplicateNoIsLessThan10Molecules();
        } 
        if(!isTheAverageReplicateNoLessThan10Molecules){
            super.updateProfile();//An LRE window must have already been found or the No will be set to zero
        }
    }

    /**
     * Sort By Amplicon Name, then Sample name
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        SampleProfile profile = (AverageSampleProfile) o;
        //Sort by name
        if (getAmpliconName().compareTo(profile.getAmpliconName()) == 0) {
            //They have the same Sample name
            //Sort by Amplicon name
            return getSampleName().compareTo(profile.getSampleName());
        }
        //They have different Sample names, to use Sample name to sort
        return getAmpliconName().compareTo(profile.getAmpliconName());
    }

    /**
     * Determines if the AverageProfile is valid, due to the fact that if
     * the number of target molecules is less than 10, the AverageProfile
     * can be distorted due to profile scattering caused by Poison Distribution.
     * If so, the
     * average No of the AverageProfile is set to the average of the ReplicateProfile
     * No values. 
     * 
     * @return whether the average No is less than 10 molecules
     */
    public boolean isReplicateAverageNoLessThan10Molecules() {
        return isTheAverageReplicateNoLessThan10Molecules;
    }

    /**
     * If the average No is less than 10 molecules, that AverageProfile can be
     * distorted, and thus it must be invalidated. If so,
     * the average No for the AverageProfile is determined by
     * averaging the No values from each of the ReplicateProfiles. 
     * Note that this will be aborted if the profile is excluded. Note also that 
     * if found to be below 10 molecules, the profile will be set to "No LRE 
     * window has been found".
     */
    public boolean determineIfTheAverageReplicateNoIsLessThan10Molecules() {
        //Not sure how well this will work
        if (isExcluded()) {
            return true;
        }
        if (sampleProfileList.size() == 1) {
//This allows the AverageProfile to function as if No >10 when there is a single replicate
//This allows single replicates to be viewed and edited from the AverageSampleReplicate node
//This is valid because the average Profile Fc is derived from a single Profile and thus is not impacted by profile scattering    
            isTheAverageReplicateNoLessThan10Molecules = false;
            return false;
        }
        double sum = 0;
        int counter = 0;
        for (Profile repPrf : getReplicateProfileList()) {
            //It is important not to include excluded profiles
            if (!repPrf.isExcluded()) {
                if (!repPrf.hasAnLreWindowBeenFound()) {
//Without an LRE Window, a valid LRE-derived avNo is not available
//However, such profiles (i.e. flat profiles) default to zero molecules and thus must be counted
                    counter++;
                } else {
                    sum = sum + repPrf.getNo();
                    counter++;
                }
            }
        }
        if (sum == 0) {
            //None of the replicate profiles have an LRE window
            isTheAverageReplicateNoLessThan10Molecules = true;
            setHasAnLreWindowBeenFound(false);
            setNo(0);
            return false;
        }
        double averageReplicateNo = sum / counter;
        if (averageReplicateNo < 10) {
            isTheAverageReplicateNoLessThan10Molecules = true;
            setHasAnLreWindowBeenFound(false);
            setNo(averageReplicateNo);
            return true;
        } else {
            isTheAverageReplicateNoLessThan10Molecules = false;
            return false;
        }
    }

    public int numberOfActiveReplicateProfiles() {
        int numberOfActiveReplicateProfiles = 0;
        for (Profile profile : sampleProfileList) {
            if (!profile.isExcluded()) {
                numberOfActiveReplicateProfiles++;
            }
        }
        return numberOfActiveReplicateProfiles;
    }
}
