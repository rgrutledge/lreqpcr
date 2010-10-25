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

package org.lreqpcr.core.data_processing;

/**
 * Representation of a thermocycle that allows an amplification
 * profile to be constructed as a linked list. This in turn allows data retrieval
 * by transversing the profile, cycle by cycle. 
 * 
 * @author Bob Rutledge
 */
public abstract class Cycle {

    private int cycNum; //The cycle number
    private Cycle nextCycle;
    private Cycle prevCycle;
    private double fc; //The fluorescence reading for this Cycle
    private double ec; //Cycle efficiency derived by dividing the Fc by the previous cycle Fc
    private double fo; //Fc to Fo conversion based on the current LRE window settings
    private double adjustedFo;//Fo adjusted to 100%
    private double pFc; //Predicted Fc based on the current LRE window settings
    private double oFfracFoAv;//The fractional difference between Fo and the average Fo
    private double[] cycLREparam;//Linear regression values [slope, intercept, r2]

    /**
     * Cycle constructor
     *@param cycleNumber the cycle number of this Cycle
     *@param fluorReading the fluorescence reading from this Cycle (Fc)
     *@param previousCycle the previous Cycle
     */
    public Cycle(int cycleNumber, double fluorReading, Cycle previousCycle) {
        cycNum = cycleNumber; //The cycle number
        fc = fluorReading; //The Fc reading
        prevCycle = previousCycle; //Pointer to the previous cycle{
        //Skip cycle 0 and 1
        //First cycle has no Fc-1; Ec is left at zero (default value)
        //Calculate and sets the cycle efficiency Ec
        if(cycNum != 0 && cycleNumber !=1){ec = (fc/prevCycle.fc)-1;}
    }

    public double getEc() {
        return ec;
    }

    public void setEc(double ec) {
        this.ec = ec;
    }

    public double getFc() {
        return fc;
    }

    public void setFc(double fc) {
        this.fc = fc;
    }

    public int getCycNum() {
        return cycNum;
    }

    public void setCycNum(int cycNum) {
        this.cycNum = cycNum;
    }

    public Cycle getNextCycle() {
        return nextCycle;
    }

    public void setNextCycle(Cycle nextCycle) {
        this.nextCycle = nextCycle;
    }

    /**
     * Target quantity in fluorescence units (Fo) is calculated using
     * the deltaE, Emax and average Fo.
     *
     * @return he predicted Fo derived from this cycle
     */
    public double getFo() {
        return fo;
    }

    /**
     * Target quantity in fluorescence units (Fo) is calculated using 
     * the deltaE, Emax and average Fo.
     * 
     * @param fo the Fo predicted for this cycle
     */
    public void setFo(double fo) {
        this.fo = fo;
    }

    /**
     *
     * @return Fo adjusted to 100% Emax
     */
    public double getAdjustedFo() {
        return adjustedFo;
    }

    /**
     *
     * @param adjustedFo Fo adjusted to 100% Emax
     */
    public void setAdjustedFo(double adjustedFo) {
        this.adjustedFo = adjustedFo;
    }

    /**
     * 
     * @return the predicted Fc for this cycle
     */
    public double getPredFc() {
        return pFc;
    }

    /**
     * 
     * @param pFc the predicted Fc for this cycle
     */
    public void setPredFc(double pFc) {
        this.pFc = pFc;
    }

    public Cycle getPrevCycle() {
        return prevCycle;
    }

    public void setPrevCycle(Cycle prevCycle) {
        this.prevCycle = prevCycle;
    }

    /**
     * 
     * @return the fractional difference in this cycle's Fo and the average Fo derived
     * from the LRE window.
     */
    public double getFoFracFoAv() {
        return oFfracFoAv;
    }

    /**
     * 
     * @param oFfracFoAv the fractional difference in this cycle's Fo and the average Fo derived
     * from the LRE window.
     */
    public void setFoFracFoAv(double oFfracFoAv) {
        this.oFfracFoAv = oFfracFoAv;
    }

    /**
     * 
     * @return the linear regression parameters for this cycle [slope, intercept, r2]
     */
    public double[] getCycLREparam() {
        return cycLREparam;
    }

    /**
     * 
     * @param cycLREparam the linear regression parameters for this cycle [slope, intercept, r2]
     */
    public void setCycLREparam(double[] cycLREparam) {
        this.cycLREparam = cycLREparam;
    }
}
