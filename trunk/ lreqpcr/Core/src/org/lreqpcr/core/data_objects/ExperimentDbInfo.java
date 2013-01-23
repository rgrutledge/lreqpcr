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

/**
 * Stores experiment database information
 * @author Bob Rutledge
 */
public class ExperimentDbInfo extends LreObject {

    private double ocf = 0;//The average OCF derived from the corresponding Reaction Setup
    private boolean isTargetQuantityNormalizedToFax;
    private boolean isEmaxFixTo100Percent;

    /**
     * 
     * @return the average OCF derived from the corresponding Reaction Setup
     */
    public double getOcf() {
        return ocf;
    }

    /**
     * 
     * @param ocf the average OCF derived from the corresponding Reaction Setup
     */
    public void setOcf(double ocf) {
        this.ocf = ocf;
    }

    /**
     * This allows Emax to be fixed to 100% for an entire experiment database, rather than 
     * allowing individual profiles or runs to change this parameter. 
     * @return whether Fmax is fixed to 100%
     */
    public boolean isEmaxFixTo100Percent() {
        return isEmaxFixTo100Percent;
    }

    /**
     * This allows Emax to be fixed to 100% for an entire experiment database, rather than 
     * allowing individual profiles or runs to change this parameter. 
     * 
     */
    public void setIsEmaxFixTo100Percent(boolean isEmaxFixTo100Percent) {
        this.isEmaxFixTo100Percent = isEmaxFixTo100Percent;
    }

    public boolean isTargetQuantityNormalizedToFax() {
        return isTargetQuantityNormalizedToFax;
    }

    public void setIsTargetQuantityNormalizedToFax(boolean isTargetQuantityNormalizedToFax) {
        this.isTargetQuantityNormalizedToFax = isTargetQuantityNormalizedToFax;
    }
}
