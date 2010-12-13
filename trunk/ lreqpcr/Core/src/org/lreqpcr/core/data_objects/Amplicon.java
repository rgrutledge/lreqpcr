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

    /**
     * 
     * @return the amplicon size in base pairs, zero if not set
     */
    public int getAmpliconSize() {
        return ampliconSize;
    }

    /**
     * Set the amplicon size in base pairs.
     * @param ampliconSize the amplicon size in base pairs
     */
    public void setAmpliconSize(int ampliconSize) {
        this.ampliconSize = ampliconSize;
    }

    /**
     *
     * @return sequence of the 3' primer
     */
    public String getDownPrimer() {
        return downPrimer;
    }

    /**
     *
     * @param downPrimer sequence of the 3' primer
     */
    public void setDownPrimer(String downPrimer) {
        this.downPrimer = downPrimer;
    }

    /**
     *
     * @return sequence of the 5' primer
     */
    public String getUpPrimer() {
        return upPrimer;
    }

    /**
     *
     * @param upPrimer sequence of the 5' primer
     */
    public void setUpPrimer(String upPrimer) {
        this.upPrimer = upPrimer;
    }
    /**
     * Not implemented
     * @return the average Emax
     */
    public double getEmaxAverage() {
        return emaxAverage;
    }

    /**
     * Not implemented
     * @param emaxAverage the average Emax
     */
    public void setEmaxAverage(double emaxAverage) {
        this.emaxAverage = emaxAverage;
    }

    /**
     * Not implemented
     * @return the CV of the average Emax
     */
    public double getEmaxCV() {
        return emaxCV;
    }

    /**
     * Not implemented
     * @param emaxCV the CV of the average Emax
     */
    public void setEmaxCV(double emaxCV) {
        this.emaxCV = emaxCV;
    }

    @Override
    public String toString(){
        return getName();
    }
}
