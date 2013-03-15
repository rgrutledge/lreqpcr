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

/**
 * Abstract class representing a thermocycle that allows an amplification
 * profile to be constructed as a linked list. This in turn allows data retrieval
 * by transversing the profile linked list.
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
    private double foEmax100;//Fo calculated with Emax fixed to 100%
    private double pFc; //Predicted Fc based on the current LRE window settings
    private double oFfracFoAv;//The fractional difference between Fo and the average Fo
    private double[] cycLREparam;//Linear regression values [slope, intercept, r2]

    /**
     * 
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

    /**
     * Returns the cycle efficiency, Ec.
     * @return the cycle efficiency
     */
    public double getEc() {
        return ec;
    }

    /**
     * Set the cycle efficiency Ec.
     * @param ec the cycle efficiency
     */
    public void setEc(double ec) {
        this.ec = ec;
    }

    /**
     * Returns the cycle fluorescence (Fc).
     * @return the cycle fluorescence
     */
    public double getFc() {
        return fc;
    }

    /**
     * Sets the cycle fluorescence (Fc).
     * @param fc the cycle fluorescence
     */
    public void setFc(double fc) {
        this.fc = fc;
    }

    /**
     * Returns the cycle number. 
     * @return the cycle number
     */
    public int getCycNum() {
        return cycNum;
    }

    /**
     * Sets the cycle number 
     * @param cycNum the cycle number
     */
    public void setCycNum(int cycNum) {
        this.cycNum = cycNum;
    }

    /**
     * Return the next cycle object, or null if none exists.
     * @return the next Cycle object
     */
    public Cycle getNextCycle() {
        return nextCycle;
    }

    /**
     * Sets the next cycle object.
     * @param nextCycle the next Cycle object
     */
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

    public double getFoEmax100() {
        return foEmax100;
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

    public void setFoEmax100(double foEmax100) {
        this.foEmax100 = foEmax100;
    }

    /**
     * Returns the predicted cycle fluorescence (pFc).
     * @return the predicted Fc for this cycle
     */
    public double getPredFc() {
        return pFc;
    }

    /**
     * Sets the predicted cycle fluorescence (pFc).
     * @param pFc the predicted Fc for this cycle
     */
    public void setPredFc(double pFc) {
        this.pFc = pFc;
    }

    /**
     * Returns the previous Cycle object
     * @return the previous cycle object
     */
    public Cycle getPrevCycle() {
        return prevCycle;
    }

    /**
     * Sets the previous Cycle object.
     * @param prevCycle the previous cycle object
     */
    public void setPrevCycle(Cycle prevCycle) {
        this.prevCycle = prevCycle;
    }

    /**
     * Returns the fractional difference between in this cycle's Fo and the average Fo 
     * derived from the LRE window.
     * @return the fractional Fo difference i
     */
    public double getFoFracFoAv() {
        return oFfracFoAv;
    }

    /**
     * Sets the fractional difference between in this cycle's Fo and the average Fo 
     * derived from the LRE window.
     * @param oFfracFoAv the fractional Fo difference 
     */
    public void setFoFracFoAv(double oFfracFoAv) {
        this.oFfracFoAv = oFfracFoAv;
    }

    /**
     * Returns the linear regression parameters for this cycle [slope, intercept, r2].
     * @return the linear regression parameters for this cycle [slope, intercept, r2]
     */
    public double[] getCycLREparam() {
        return cycLREparam;
    }

    /**
     * Sets the cycLREparam the linear regression parameters for this cycle [slope, intercept, r2].
     * @param cycLREparam the linear regression parameters for this cycle [slope, intercept, r2]
     */
    public void setCycLREparam(double[] cycLREparam) {
        this.cycLREparam = cycLREparam;
    }

}
