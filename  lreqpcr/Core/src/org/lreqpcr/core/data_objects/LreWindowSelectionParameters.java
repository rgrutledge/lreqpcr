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

/**
 * Holds user defined parameters for automated LRE window selection.
 * This includes the minimal fluorescence used for start cycle selection
 * and the Fo threshold, which is the maximum fractional difference
 * in a cycle's Fo vs. the average Fo derived from the cycles within
 * the LRE window, to determine if this next cycle is to be included into the LRE window.
 * @author Bob Rutledge
 */
public class LreWindowSelectionParameters {

    private Double minFc = 0d;
    private Double foThreshold = 0.06;//6% default

    /**
     * Returns he Fo threshold (fraction of the cycle Fo vs the average Fo) beyond which the
     * the next upper cycle will not be added to the LRE window.
     * @return the Fo threshold
     */
    public Double getFoThreshold() {
        return foThreshold;
    }

    /**
     * Sets the Fo threshold (fraction of the cycle Fo vs the average Fo) beyond which the
     * the next upper cycle will not be added to the LRE window.
     * @param foThreshold the Fo threshold
     */
    public void setFoThreshold(Double foThreshold) {
        this.foThreshold = foThreshold;
    }

    /**
     * Returns the minimum Fc reading used to set the start cycle of the LRE window.
     * @return the minimum Fc reading
     */
    public Double getMinFc() {
        return minFc;
    }

    /**
     * Sets the minimum Fc reading used to set the start cycle of the LRE window.
     * @param minFc the minimum Fc reading
     */
    public void setMinFc(Double minFc) {
        this.minFc = minFc;
    }   
}