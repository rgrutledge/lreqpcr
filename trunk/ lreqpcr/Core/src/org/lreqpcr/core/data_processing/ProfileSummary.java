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

import org.lreqpcr.core.data_objects.Profile;

/**
 * Provides functions required for editing and display of a Profile, which primarily involves 
 * processing changes to the LRE window and updating the associated LRE parameters 
 * within the Profile. A central aspect of these functions is utilization 
 * of a linked-list of Cycle objects that represents the cycles within the Profile.  
 *
 * @author Bob Rutledge
 */
public abstract class ProfileSummary {

    protected Profile profile;
    private Cycle zeroCycle, strCycle;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Cycle getStrCycle() {
        return strCycle;
    }

    public void setStrCycle(Cycle strCycle) {
        this.strCycle = strCycle;
    }

    public Cycle getZeroCycle() {
        return zeroCycle;
    }

    public void setZeroCycle(Cycle zeroCycle) {
        this.zeroCycle = zeroCycle;
    }
}
