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
 * Implementation of the AverageProfile interface.
 *
 * This is used to analyze and display technical replicates,
 * which are identified by the
 * fact that all have the same amplicon and sample names.
 *
 * @author Bob Rutledge
 */
public class AverageSampleProfile extends SampleProfile implements AverageProfile {

    private List<SampleProfile> sampleProfileList;

    /**
     * An average sample profile constructed from its sample replicate profiles.
     */
    public AverageSampleProfile() {
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
     * Calibration Profiles.
     *
     * @param replicateProfileList
     */
    @SuppressWarnings(value = "unchecked")
    public void setReplicateProfileList(List<? extends Profile> replicateProfileList) {
        this.sampleProfileList = (List<SampleProfile>) replicateProfileList;
    }
//    public void setReplicateProfileList(List<SampleProfile> replicateProfileList) {
//        this.sampleProfileList = replicateProfileList;
//    }

    @Override
    public void updateProfile() {
        super.updateProfile();
    }

    @Override
    public int compareTo(Object o) {
        SampleProfile prf = (AverageSampleProfile) o;
        //Sort by name
        return getName().compareToIgnoreCase(prf.getName());
    }
}
