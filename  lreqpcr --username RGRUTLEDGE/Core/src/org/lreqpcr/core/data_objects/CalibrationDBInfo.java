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
 * For storage and retrieval of information about
 * the Calibration database
 * 
 * @author Bob Rutledge
 */
public class CalibrationDBInfo extends LreObject {
    
    private Double cuutoff = 0.06;//Upper cutoff for the LRE window (% difference from the Av. Fo); 6% default
    private Double minFc;//The lower cutoff for the smallest Fc value in the LRE window
    private int maxLreWindowSize;//The maximum size of the LRE window

    public Double getCuutoff() {
        return cuutoff;
    }

    public void setCuutoff(Double cuutoff) {
        this.cuutoff = cuutoff;
    }

    public Double getMinFc() {
        return minFc;
    }

    public void setMinFc(Double minFc) {
        this.minFc = minFc;
    }
    
}
