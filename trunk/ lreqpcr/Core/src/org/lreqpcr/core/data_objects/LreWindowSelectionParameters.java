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
 *
 * @author Bob Rutledge
 */
public class LreWindowSelectionParameters {

    private Double minFc = 0d;
    private Double foThreshold = 0.06;//6% default

    public Double getFoThreshold() {
        return foThreshold;
    }

    public void setFoThreshold(Double foThreshold) {
        this.foThreshold = foThreshold;
    }

    public Double getMinFc() {
        return minFc;
    }

    public void setMinFc(Double minFc) {
        this.minFc = minFc;
    }

}