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
 * Class representing the cycles within a Profile via a linked-list that
 * allows analysis and display of the Profile. This in turn allows data retrieval
 * by transversing the Cycle linked-list.
 *
 * @author Bob Rutledge
 */
public class Cycle {

    /**
     * The cycle number
     */
    private int cycleNumber;
    private Cycle nextCycle;
    private Cycle previousCycle;
    /**
     * The fluorescence reading for this Cycle
     */
    private double currentCycleFluorescence;
    /**
     * Cycle efficiency derived by dividing the Fc by the previous cycle Fc
     */
    private double cycleEfficiency;
    /**
     * Fc to Fo conversion based on the current LRE window settings
     */
    private double fo;
    /**
     * Predicted Fc based on the current LRE window settings
     */
    private double predictedCycleFluorescence;
    /**
     * The fractional difference between Fo and the average Fo
     */
    private double oFfracFoAv;
    /**
     * Linear regression values [slope, intercept, r2]
     */
    private double[] cycLREparam;

    public Cycle(int cycleNumber, double fluorescenceReading, Cycle previousCycle) {
        this.cycleNumber = cycleNumber;
        currentCycleFluorescence = fluorescenceReading;
        this.previousCycle = previousCycle;
        // Skip cycle 0 and 1
        // First cycle has no Fc-1; Ec is left at zero (default value)
        // Calculate and sets the cycle efficiency Ec
        if (this.cycleNumber != 0 && cycleNumber != 1) {
            cycleEfficiency = (currentCycleFluorescence / this.previousCycle.currentCycleFluorescence) - 1;
        }
    }

    public double getCycleEfficiency() {
        return cycleEfficiency;
    }

    public void setCycleEfficiency(double cycleEfficiency) {
        this.cycleEfficiency = cycleEfficiency;
    }

    public double getCurrentCycleFluorescence() {
        return currentCycleFluorescence;
    }

    public void setCurrentCycleFluorescence(double currentCycleFluorescence) {
        this.currentCycleFluorescence = currentCycleFluorescence;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(int cycleNumber) {
        this.cycleNumber = cycleNumber;
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
     * @param fo
     *     the Fo predicted for this cycle
     */
    public void setFo(double fo) {
        this.fo = fo;
    }

    public double getPredictedCyclecFluorescence() {
        return predictedCycleFluorescence;
    }

    public void setPredictedFluorescence(double pFc) {
        this.predictedCycleFluorescence = pFc;
    }

    public Cycle getPreviousCycle() {
        return previousCycle;
    }

    public void setPreviousCycle(Cycle previousCycle) {
        this.previousCycle = previousCycle;
    }

    /**
     * Returns the fractional difference between in this cycle's Fo and the average Fo
     * derived from the LRE window.
     *
     * @return the fractional Fo difference i
     */
    public double getFoFracFoAv() {
        return oFfracFoAv;
    }

    /**
     * Sets the fractional difference between in this cycle's Fo and the average Fo
     * derived from the LRE window.
     *
     * @param oFfracFoAv
     *     the fractional Fo difference
     */
    public void setFoFracFoAv(double oFfracFoAv) {
        this.oFfracFoAv = oFfracFoAv;
    }

    /**
     * Returns the linear regression parameters for this cycle [slope, intercept, r2].
     *
     * @return the linear regression parameters for this cycle [slope, intercept, r2]
     */
    public double[] getCycLREparam() {
        return cycLREparam;
    }

    /**
     * Sets the the LRE linear regression parameters for this cycle [slope, intercept, r2].
     *
     * @param cycLREparam
     *     the LRE linear regression parameters for this cycle [slope, intercept, r2]
     */
    public void setCycLREparam(double[] cycLREparam) {
        this.cycLREparam = cycLREparam;
    }
}
