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
 * Amplicon base abstract class
 * @author Bob Rutledge
 */
public abstract class Amplicon extends LreObject {

    private int ampliconSize;
    private String upPrimer, downPrimer;
    private double emaxAverage;
    private double emaxCV;

    public int getAmpliconSize() {
        return ampliconSize;
    }

    public void setAmpliconSize(int ampliconSize) {
        this.ampliconSize = ampliconSize;
    }

    public String getDownPrimer() {
        return downPrimer;
    }

    public void setDownPrimer(String downPrimer) {
        this.downPrimer = downPrimer;
    }

    public String getUpPrimer() {
        return upPrimer;
    }

    public void setUpPrimer(String upPrimer) {
        this.upPrimer = upPrimer;
    }
    public double getEmaxAverage() {
        return emaxAverage;
    }

    public void setEmaxAverage(double emaxAverage) {
        this.emaxAverage = emaxAverage;
    }

    public double getEmaxCV() {
        return emaxCV;
    }

    public void setEmaxCV(double emaxCV) {
        this.emaxCV = emaxCV;
    }

    @Override
    public String toString(){
        return getName();
    }
}
