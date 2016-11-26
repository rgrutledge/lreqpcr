/*
 * Copyright (C) 2013  Bob Rutledge
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

import java.util.Date;

/**
 * Abstract class representing an amplification profile. Stores all basic
 * parameters of a Profile in which target quantity is expressed in fluorescence
 * units. Note that this is designed to primarily be a data storage object
 * containing only setter and getter methods.
 */
public class Profile extends LreObject {

    //Profile creation parameters
    /**
     * The Run that generated this profile
     */
    private Run run;
    private Date runDate;
    /**
     * Plate well label i.e. A1-A12, A1-H1
     */
    private String wellLabel;
    /**
     * Plate well number 1-96, used as an alternative to well label
     */
    private int wellNumber;
    /**
     * raw Fc dataset, no background subtraction
     */
    private double[] rawFcReadings;
    /**
     * Could be used to recover information about the sample from a database
     */
    private String sampleName;
    /**
     * Used to recover amplicon size from the amplicon database
     */
    private String ampliconName;
    private int ampliconSize = 0;
    private TargetStrandedness targetStrandedness = TargetStrandedness.DOUBLESTRANDED;

    //Profile processing parameters
    private double ampliconTm;
    private double cycleThreshold;
    private double fluorescenceThreshold;
    // TODO fcReadings should be stored in a HashMap in order to preserve cycle number
    /**
     * Processed Fc readings corrected for fluorescence background and baseline slope
     */
    private double[] fcReadings;
    /**
     * Average fluorescence background derived directly from the raw Fc readings
     */
    private double fb;
    /**
     * Is this a flat profile or has any aberrancy that disallows an LRE window to be located
     */
    private boolean hasAnLreWindowBeenFound = false;
    private boolean didNonlinearRegressionSucceed;
    /**
     * LRE window start cycle
     */
    private int startingCycleIndex;
    /**
     * LRE window size
     */
    private int lreWinSize;
    /**
     * Linear regression values for the LRE window
     */
    private double maxEfficiency, changeInEfficiency, r2;
    /**
     * Fo values and CV calculated from the LRE window Fo values
     */
    private double avFo, avFoCV;
    /**
     * C1/2
     */
    private double midC;
    /**
     * Allows profiles to be excluded from the analysis
     */
    private boolean excluded;
    /**
     * Text describing the reason why the profile was excluded
     */
    private String whyExcluded;

    // Nonlinear regression-derived LRE parameters
    private double nrFb = 0;
    private double nrFbSlope = 0;
    private double nonlinearMaxEfficiency = 0;
    private double nonlinearMaxFluorescence = 0;
    private double nrFo = 0;

    // Standard deviations derived from repetative nonlinear regression analysis
    private double nrFbSD;
    private double nrFbSlopeSD;
    private double nonlinearMaxEfficiencyStandardDeviation;
    private double nonlinearMaxFluorescenceStandardDeviation;
    private double nrFoSD;

    /**
     * Also sets the parent to the Run and the run date retrieved from the Run.
     *
     * @param run
     */
    public void setRun(Run run) {
        this.run = run;
        runDate = run.getRunDate();
        setParent(run);
    }

    public Run getRun() {
        return run;
    }

    /**
     * For defining well position using a numbering system.
     *
     * @return well position expressed as a number.
     */
    public int getWellNumber() {
        return wellNumber;
    }

    /**
     * Sets the well position for this profile using a numbering system.
     *
     * @param wellNumber
     */
    public void setWellNumber(int wellNumber) {
        this.wellNumber = wellNumber;
    }

    /**
     *
     * @return the observed amplicon melting temperature (Tm) or -1 if it is not available
     */
    public double getAmpliconTm() {
        return ampliconTm;
    }

    /**
     * @param ampliconTm the observed amplicon melting temperature (Tm)
     */
    public void setAmpliconTm(double ampliconTm) {
        this.ampliconTm = ampliconTm;
    }

    /**
     * This was included for legacy issues. It must be noted however, that
     * dependent on the fluorescence threshold introduce vagaries that make Ct
     * highly unreliable.
     *
     * @return the observed threshold cycle
     */
    public double getCycleThreshold() {
        return cycleThreshold;
    }

    /**
     * This was included for legacy issues. It must be noted however, that
     * dependent on the fluorescence threshold introduce vagaries that make Ct
     * highly unreliable.
     *
     * @param cycleThreshold
     */
    public void setCycleThreshold(double cycleThreshold) {
        this.cycleThreshold = cycleThreshold;
    }

    /**
     * @return the fluorescence threshold used to generate the profile Ct
     */
    public double getFluorescenceThreshold() {
        return fluorescenceThreshold;
    }

    /**
     * @param fluorescenceThreshold fluorescence threshold used to generate the profile Ct
     */
    public void setFluorescenceThreshold(double fluorescenceThreshold) {
        this.fluorescenceThreshold = fluorescenceThreshold;
    }

    /**
     * The average Fo is calculated using the LRE-derived Emax
     *
     * @return the average of the Fo values generated by the Fc readings within
     * the LRE window.
     */
    public double getAvFo() {
        return avFo;
    }

    /**
     * The average Fo is by the Fc readings within the LRE window the primary
     * quantitative unit for a profile.
     *
     * @param averageFo the average of the Fo values calculated using the LRE
     * derived Emax
     */
    public void setAvFo(double averageFo) {
        this.avFo = averageFo;
    }

    /**
     * @return the coefficient of variation for the average Fo
     */
    public double getAvFoCV() {
        return avFoCV;
    }

    /**
     * @param foCV the coefficient of variation for the average Fo
     */
    public void setAvFoCV(double foCV) {
        this.avFoCV = foCV;
    }

    public double getChangeInEfficiency() {
        return changeInEfficiency;
    }

    public void setChangeInEfficiency(double changeInEfficiency) {
        this.changeInEfficiency = changeInEfficiency;
    }

    /**
     * @return LRE-derived Emax
     */
    public double getMaxEfficiency() {
        return maxEfficiency;
    }

    public void setMaxEfficiency(double eMax) {
        this.maxEfficiency = eMax;
    }

    /**
     * @return LRE-derived Fmax or -1 if the profile is invalid
     */
    public double getMaxFluorescence() {
        if (isExcluded()){
            return -1;
        }
        if (hasAnLreWindowBeenFound) {
            return maxEfficiency / -(changeInEfficiency);
        }
        return -1;
    }

    public double getFb() {
        return fb;
    }

    public void setFb(double fb) {
        this.fb = fb;
    }

    /**
     * Returns the processed Fc dataset
     *
     * @return the processed Fc dataset
     */
    public double[] getFcReadings() {
        return fcReadings;
    }

    /**
     * Sets the processed Fc dataset
     */
    public void setFcReadings(double[] processedFcReadings) {
        this.fcReadings = processedFcReadings;
    }

    public int getLreWinSize() {
        return lreWinSize;
    }

    public void setLreWinSize(int lreWinSize) {
        this.lreWinSize = lreWinSize;
    }

    /**
     *
     * @return the LRE-derived C1/2 or -1 if the profile is invalid
     */
    public double getMidC() {
        if (isExcluded()){
            return -1;
        }
        return midC;
    }

    public void setMidC(double midC) {
        this.midC = midC;
    }

    public double getR2() {
        return r2;
    }

    public void setR2(double r2) {
        this.r2 = r2;
    }

    /**
     * Retrieves the raw fluorescence readings (i.e. with no background
     * subtraction) in an ordered array that starts with Cycle 1 and is
     * continuous to the last cycle.
     *
     * @return the array containing the raw fluorescence readings for each cycle
     */
    public double[] getRawFcReadings() {
        return rawFcReadings;
    }

    /**
     * Sets the raw fluorescence readings (no background subtraction) in an
     * ordered array that must start with Cycle 1 and must be continuous upto
     * the last cycle. Cycle number is derived from the array index and thus
     * must contain fluorescence readings from every cycle. Note also that Cycle
     * number is based on the array index and thus index[0] MUST BE CYCLE 1.
     *
     * @param rawFcReadings raw fluorescence readings starting with Cycle 1
     */
    public void setRawFcReadings(double[] rawFcReadings) {
        this.rawFcReadings = rawFcReadings;
    }

    public int getStartingCycleIndex() {
        return startingCycleIndex;
    }

    public void setStartingCycleIndex(int startingCycleIndex) {
        this.startingCycleIndex = startingCycleIndex;
    }

    public String getWellLabel() {
        return wellLabel;
    }

    public void setWellLabel(String wellName) {
        this.wellLabel = wellName;
    }

    public String getAmpliconName() {
        return ampliconName;
    }

    public void setAmpliconName(String ampliconName) {
        this.ampliconName = ampliconName;
    }

    public int getAmpliconSize() {
        return ampliconSize;
    }

    public void setAmpliconSize(int ampliconSize) {
        this.ampliconSize = ampliconSize;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public TargetStrandedness getTargetStrandedness() {
        return targetStrandedness;
    }

    public void setTargetStrandedness(TargetStrandedness targetStrandedness) {
        this.targetStrandedness = targetStrandedness;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    /**
     * Flag indicating whether a profile has been excluded, primarily due to
     * failed amplification or anomalous amplification kinetics.
     *
     * @return whether the profile been excluded from analysis
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * Exclude/include this profile. Note that this also updates the Run average
     * Fmax.
     *
     * @param excluded whether this profile is excluded
     */
    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
        run.calculateAverageFmax();
    }

    public String getWhyExcluded() {
        return whyExcluded;
    }

    public void setWhyExcluded(String whyExcluded) {
        this.whyExcluded = whyExcluded;
    }

    /**
     * Indicates whether an LRE window has been found for this Profile. Lack of
     * a LRE window is primarily indicative of a flat profile.
     *
     * @return whether an LRE window has been found
     */
    public boolean hasAnLreWindowBeenFound() {
        return hasAnLreWindowBeenFound;
    }

    /**
     * Indicates whether a this is a valid Profile.
     *
     * @param hasAnLreWindowBeenFound indicates whether an LRE window has been found
     */
    public void setHasAnLreWindowBeenFound(boolean hasAnLreWindowBeenFound) {
        this.hasAnLreWindowBeenFound = hasAnLreWindowBeenFound;
    }

    /**
     * Indicates whether nonlinear regression analysis was successful
     * @return false if nonlinear regression has not been applied
     */
    public boolean didNonlinearRegressionSucceed() {
        return didNonlinearRegressionSucceed;
    }

    public void setWasNonlinearRegressionSuccessful(boolean didNonlinearRegressionSucceed) {
        this.didNonlinearRegressionSucceed = didNonlinearRegressionSucceed;
    }

    /**
     * @return nonlinear regression-derived baseline fluorescence
     */
    public double getNrFb() {
        return nrFb;
    }

    /**
     * @param nrFb nonlinear regression-derived baseline fluorescence
     */
    public void setNrFb(double nrFb) {
        this.nrFb = nrFb;
    }

    /**
     * @return nonlinear regression-derived baseline slope
     */
    public double getNrFbSlope() {
        return nrFbSlope;
    }

    /**
     * @param nrFbSlope nonlinear regression-derived baseline slope
     */
    public void setNrFbSlope(double nrFbSlope) {
        this.nrFbSlope = nrFbSlope;
    }

    /**
     * @return nonlinear regression-derived Emax
     */
    public double getNonlinearMaxEfficiency() {
        return nonlinearMaxEfficiency;
    }

    /**
     * @param nonlinearMaxEfficiency nonlinear regression-derived Emax
     */
    public void setNonlinearMaxEfficiency(double nonlinearMaxEfficiency) {
        this.nonlinearMaxEfficiency = nonlinearMaxEfficiency;
    }

    /**
     * @return nonlinear regression-derived Fmax
     */
    public double getNonlinearMaxFluorescence() {
        return nonlinearMaxFluorescence;
    }

    /**
     * @param nonlinearMaxFluorescence nonlinear regression-derived Fmax
     */
    public void setNonlinearMaxFluorescence(double nonlinearMaxFluorescence) {
        this.nonlinearMaxFluorescence = nonlinearMaxFluorescence;
    }

    /**
     * @return nonlinear regression-derived Fo
     */
    public double getNrFo() {
        return nrFo;
    }

    /**
     * @param nrFo nonlinear regression-derived Fo
     */
    public void setNrFo(double nrFo) {
        this.nrFo = nrFo;
    }

    public double getNrFbSD() {
        return nrFbSD;
    }

    public void setNrFbSD(double nrFbSD) {
        this.nrFbSD = nrFbSD;
    }

    public double getNrFbSlopeSD() {
        return nrFbSlopeSD;
    }

    public void setNrFbSlopeSD(double nrFbSlopeSD) {
        this.nrFbSlopeSD = nrFbSlopeSD;
    }

    public double getNonlinearMaxEfficiencyStandardDeviation() {
        return nonlinearMaxEfficiencyStandardDeviation;
    }

    public void setNonlinearMaxEfficiencyStandardDeviation(double nonlinearMaxEfficiencyStandardDeviation) {
        this.nonlinearMaxEfficiencyStandardDeviation = nonlinearMaxEfficiencyStandardDeviation;
    }

    public double getNonlinearMaxFluorescenceStandardDeviation() {
        return nonlinearMaxFluorescenceStandardDeviation;
    }

    public void setNonlinearMaxFluorescenceStandardDeviation(double nonlinearMaxFluorescenceStandardDeviation) {
        this.nonlinearMaxFluorescenceStandardDeviation = nonlinearMaxFluorescenceStandardDeviation;
    }

    public double getNrFoSD() {
        return nrFoSD;
    }

    public void setNrFoSD(double nrFoSD) {
        this.nrFoSD = nrFoSD;
    }

    /**
     * Resets this Profile to an uninitialized status, removing all previous
     * determined LRE parameters, including eliminating the LRE window.
     */
    public void setLreVariablesToZero(){
        nonlinearMaxEfficiency = 0;
        nonlinearMaxEfficiencyStandardDeviation = 0;
        nrFb = 0;
        nrFbSD = 0;
        nrFbSlope = 0;
        nrFbSlopeSD = 0;
        nonlinearMaxFluorescence = 0;
        nonlinearMaxFluorescenceStandardDeviation = 0;
        nrFo = 0;
        nrFoSD = 0;
        maxEfficiency = 0;
        changeInEfficiency = 0;
        r2 = 0;
        avFo = 0;
        avFoCV = 0;
        midC = 0;
        startingCycleIndex = 0;
        lreWinSize = 0;
        hasAnLreWindowBeenFound = false;
    }

    @Override
    public String toString() {
        return super.getName();
    }
}
