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

package org.lreqpcr.core.data_objects;

import java.util.List;

/**
 * An average profile is generated by averaging, for each cycle, the Fc datasets
 * produced by technical replicates.
 * This increases Fc read precision which can be essential for reliable analysis 
 * Note that AverageProiles must correctly override updateProfile() in order to
 * deal with target quantities below 10 molecules.
 * <p>
 * It is recognized that this could have been better implemented. 
 *
 * @author Bob Rutledge
 */
public interface AverageProfile {

    /**
     * List of the replicate profiles from which this average profile was constructed.
     * 
     * @return List of the replicate profiles
     */
    public List<? extends Profile> getReplicateProfileList();

    /**
     * Sets the list of the replicate profiles from which this average profile ia constructed.
     * 
     * @param replicateProfileList List of replicate profiles
     */
    public void setReplicateProfileList(List<? extends Profile> replicateProfileList);
    
    /**
     * Returns the average melting temperature (Tm) of the Replicate Sample Profile
     * amplicons. Note that this will return -1 if a Tm is not available.
     *
     * @return the average SampleProfile Tm or -1 if none is available
     */
    public double calculateAvAmpTm();

    public int getTheNumberOfActiveReplicateProfiles();
    
    /**
     * Determines if the AverageProfile is valid, based on whether the 
     * number of target molecules (No) in the sample is greater than 10. If the number 
     * of target molecules is less than 10, the resulting average profile becomes 
     * distorted due to by Poison Distribution, generating extensive profile scattering. 
     * In this case, the AverageProfile profile becomes invalid and cannot be used 
     * for analysis. As such AverageProfile inherits the average target quantity 
     * derived from the replicate profiles.
     *
     * @return whether the average replicate profile No is less than 10 molecules
     */
    public boolean isTheReplicateAverageNoLessThan10Molecules();

    /**
     * Closely related to the less than 10 molecule problem, if the replicate 
     * profiles are not sufficiently clustered, the resulting average profile 
     * becomes distorted, which in turn generates an unreliable reliable target 
     * quantity. 
     * 
     * @return whether the replicate profiles can be used to generate an average Profile
     */
    public boolean areTheRepProfilesSufficientlyClustered();
}
