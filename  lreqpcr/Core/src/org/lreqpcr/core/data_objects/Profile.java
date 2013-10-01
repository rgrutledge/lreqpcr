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
 *
 * @author Bob Rutledge
 */
public abstract class Profile extends LreObject {

    //Profile creation parameters
    private Run run;//The Run that generated this profile
    private Date runDate;
    private String wellLabel;//Plate well label i.e. A1-A12, A1-H1
    private int wellNumber;//Plate well number 1-96, used as an alternative to well label
    private double[] rawFcReadings;//raw Fc dataset, no background subtraction
    private String sampleName;//Could be used to recover information about the sample from a database
    private String ampliconName;//Used to recover amplicon size from the amplicon database
    private int ampliconSize = 0;
    private TargetStrandedness targetStrandedness = TargetStrandedness.DOUBLESTRANDED;
    //Profile processing parameters
    private double ampTm;//The amplicon Tm
    private double ct, ft;//Threshold cycle and fluorescence threshold
    // TODO fcReadings should be stored in a HashMap in order to preserve cycle number
    private double[] fcReadings; //Processed Fc readings corrected for fluorescence background and baseline slope
    private double fb;//Average fluorescence background derived directly from the raw Fc readings
    private boolean hasAnLreWindowBeenFound;//Is this a flat profile or has any abberrancy that dissallows an LRE window to be located
    private int strCycleInt; //LRE window start cycle
    private int lreWinSize; //LRE window size
    private double eMax, deltaE, r2;//Linear regression values for the LRE window
    private double avFo, avFoCV;//Fo values and CV calculated from the LRE window Fo values
    private double midC;//C1/2
    private boolean excluded;//Allows profiles to be excluded from the analysis
    private String whyExcluded;//Text describing the reason why the profile was excluded
    //Nonlinear regression-derived LRE parameters
    private double nrFb = 0;
    private double nrFbSlope = 0;
    private double nrEmax=  0;
    private double nrFmax = 0;
    private double nrFo = 0;
    //Standard deviations derived from repeatative nonlinear regression analysis
    private double nrFbSD, nrFbSlopeSD, nrEmaxSD, nrFmaxSD, nrFoSD;

    /**
     *
     * @param run the Run from which the Profile was generated. Note that Run
     * date is retrieved from the provided Run.
     */
    public Profile() {
    }

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
     * @return the observed amplicon melting temperature (Tm)
     */
    public double getAmpTm() {
        return ampTm;
    }

    /**
     * @param the observed amplicon melting temperature (Tm)
     */
    public void setAmpTm(double ampTm) {
        this.ampTm = ampTm;
    }

    /**
     * This was included for legacy issues. It must be noted however, that
     * dependent on the fluorescence threshold introduce vagaries that make Ct
     * highly unreliable.
     *
     * @return the observed threshold cycle
     */
    public double getCt() {
        return ct;
    }

    /**
     * This was included for legacy issues. It must be noted however, that
     * dependent on the fluorescence threshold introduce vagaries that make Ct
     * highly unreliable.
     *
     * @param ct
     */
    public void setCt(double ct) {
        this.ct = ct;
    }

    /**
     *
     * @return the fluorescence threshold used to generate the profile Ct
     */
    public double getFt() {
        return ft;
    }

    /**
     *
     * @param ftthe fluorescence threshold used to generate the profile Ct
     */
    public void setFt(double ft) {
        this.ft = ft;
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
     * @param averageFoEmax100 the average of the Fo values calculated using
     * Emax fixed to 100%
     */
    public void setAvFo(double averageFo) {
        this.avFo = averageFo;
    }

    /**
     *
     * @return the coefficient of variation for the average Fo
     */
    public double getAvFoCV() {
        return avFoCV;
    }

    /**
     *
     * @param foCV the coefficient of variation for the average Fo
     */
    public void setAvFoCV(double foCV) {
        this.avFoCV = foCV;
    }

    public double getDeltaE() {
        return deltaE;
    }

    public void setDeltaE(double deltaE) {
        this.deltaE = deltaE;
    }

    public double getEmax() {
        return eMax;
    }

    public void setEmax(double eMax) {
        this.eMax = eMax;
    }

    public double getFmax() {
        if (!isExcluded() && hasAnLreWindowBeenFound) {
            return eMax / -(deltaE);
        }
        return 0;
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
     *
     * @param processedFcReadings
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

    public double getMidC() {
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

    public int getStrCycleInt() {
        return strCycleInt;
    }

    public void setStrCycleInt(int strCycleInt) {
        this.strCycleInt = strCycleInt;
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

    /**
     * @param runDate
     */
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
     * @param hasAnLreWindowFound indicates whether an LRE window has been found
     */
    public void setHasAnLreWindowBeenFound(boolean hasAnLreWindowFound) {
        this.hasAnLreWindowBeenFound = hasAnLreWindowFound;
    }

    /**
     * 
     * @return nonlinear regression-derived baseline fluorescence
     */
    public double getNrFb() {
        return nrFb;
    }

    /**
     *
     * @param nrFb nonlinear regression-derived baseline fluorescence
     */
    public void setNrFb(double nrFb) {
        this.nrFb = nrFb;
    }

    /**
     * 
     * @return nonlinear regression-derived baseline slope
     */
    public double getNrFbSlope() {
        return nrFbSlope;
    }

    /**
     * 
     * @param nrFbSlope nonlinear regression-derived baseline slope
     */
    public void setNrFbSlope(double nrFbSlope) {
        this.nrFbSlope = nrFbSlope;
    }

    /**
     * 
     * @return nonlinear regression-derived Emax
     */
    public double getNrEmax() {
        return nrEmax;
    }

    /**
     * 
     * @param nrEmax nonlinear regression-derived Emax
     */
    public void setNrEmax(double nrEmax) {
        this.nrEmax = nrEmax;
    }

    /**
     * 
     * @return nonlinear regression-derived Fmax
     */
    public double getNrFmax() {
        return nrFmax;
    }

    /**
     * 
     * @param nrFmax nonlinear regression-derived Fmax
     */
    public void setNrFmax(double nrFmax) {
        this.nrFmax = nrFmax;
    }

    /**
     * 
     * @return nonlinear regression-derived Fo
     */
    public double getNrFo() {
        return nrFo;
    }

    /**
     * 
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

    public double getNrEmaxSD() {
        return nrEmaxSD;
    }

    public void setNrEmaxSD(double nrEmaxSD) {
        this.nrEmaxSD = nrEmaxSD;
    }

    public double getNrFmaxSD() {
        return nrFmaxSD;
    }

    public void setNrFmaxSD(double nrFmaxSD) {
        this.nrFmaxSD = nrFmaxSD;
    }

    public double getNrFoSD() {
        return nrFoSD;
    }

    public void setNrFoSD(double nrFoSD) {
        this.nrFoSD = nrFoSD;
    }
    
    public void setNrVariablesToZero(){
        nrEmax = 0;
        nrEmaxSD = 0;
        nrFb = 0;
        nrFbSD = 0;
        nrFbSlope = 0;
        nrFbSlopeSD = 0;
        nrFmax = 0;
        nrFmaxSD = 0;
        nrFo = 0;
        nrFoSD = 0;
    }

    @Override
    public String toString() {
        return super.getName();
    }
}
