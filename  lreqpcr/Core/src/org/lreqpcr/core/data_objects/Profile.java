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
 * Abstract class for the basic object representing an
 * amplification profile. Stores all basic parameters of a Profile in which target quantity is expressed
 * in fluorescence units. Note that this is designed to only be a data storage 
 * object such that it contains only setter and getter methods. 
 * Note also that target quantity is only expressed in fluorescence units. 
 *
 * @author Bob Rutledge
 */
public abstract class Profile extends LreObject {

    //Profile creation parameters
    private Run run;//The run that generated this profile
    private Date runDate;
    private String wellLabel;//Plate well label i.e. A1-A12, A1-H1
    private int wellNumber;//Plate well number 1-96, used as an alternative to well label
    private double[] rawFcReadings;//raw Fc dataset, no background subtraction
    private String sampleName;//Could be used to recover information about the sample from a database
    private String ampliconName;//Used to recover amplicon size from the amplicon database
    private int ampliconSize = 0;
    private TargetStrandedness targetStrandedness = TargetStrandedness.DOUBLESTRANDED;
    //This is a very clumsy method for maintaiing back compatability...a better versioning method is clearly needed
    private boolean isProfileVer0_8_0 = false;//This must be set to true during data import!!!!!
        
    //Profile processing parameters
    private double ampTm;//The amplicon Tm
    private double ct, ft;//Threshold cycle and fluorescence threshold
    private int fbStart, fbWindow;//Start and size of the Fb window for determining background fluorescence
    // TODO fcReadings should be stored in a HashTable in order to preserve cycle number
    private double[] fcReadings; //Fluorescence dataset (Background subtracted)
    private double fb;//Average fluorescence backgroung used for background substraction
    private boolean hasAnLreWindowBeenFound;//Is this a flat profile or has any abberrancy that dissallows an LRE window to be located
    private int strCycleInt; //LRE window start cycle
    private int lreWinSize; //LRE window size
    private double eMax, deltaE, r2;//Linear regression values for the LRE window
    private boolean isEmaxFixedTo100;//Overides LRE analysis-derived Emax by setting it to 100%
    private double avFo, avFoCV;//Fo values and CV calculated from the LRE window Fo values
    private double avFoEmax100;//Fo values and CV calculated with Emax fixed to 100%
    private double nonR2;//Nonlinear r2 of predicted Fc to actual Fc within the LRE window... not very useful
    private double midC;//C1/2
    private double fbSlope, fbIntercept, fbR2;//Baseline linear regression used to test for baseline drift... under development
    private boolean excluded;//Allows profiles to be excluded from the analysis
    private String whyExcluded;//Text describing why the profile was excluded

    /**
     * 
     * @param run the Run from which the Profile was generated. Note that Run date is retrieved from the provided Run.
     */
    public Profile(Run run) {
        this.run = run;
        setParent(run);
        this.runDate = run.getRunDate();
    }

    public Run getRun() {
        return run;
    }

    /**
     * For defining well position using a numbering system. 
     * @return well position expressed as a number.
     */
    public int getWellNumber() {
        return wellNumber;
    }

    /**
     * Sets the well position for this profile using a numbering system. 
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
     * This was included for legacy issues. 
     * It must be noted however, that dependent on the fluorescence
     * threshold introduce vagaries that make Ct highly unreliable.
     * 
     * @return the observed threshold cycle
     */
    public double getCt() {
        return ct;
    }

    /**
     * This was included for legacy issues. 
     * It must be noted however, that dependent on the fluorescence
     * threshold introduce vagaries that make Ct highly unreliable.
     * 
     * @param ct
     */
    public void setCt(double ct) {
        this.ct = ct;
    }

    /**
     * 
     * @return the fluorescence threshold used to
     * generate the profile Ct
     */
    public double getFt() {
        return ft;
    }

    /**
     * 
     * @param ftthe fluorescence threshold used to
     * generate the profile Ct
     */
    public void setFt(double ft) {
        this.ft = ft;
    }

    /**
     * The average Fo is calculated using the LRE-derived Emax
     * @return the average of the Fo values generated 
     * by the Fc readings within the LRE window.
     */
    public double getAvFo() {
        return avFo;
    }

    /**
     * The average Fo is by the Fc readings within the LRE window the primary quantitative unit for a profile. 
     * @param averageFo the average of the Fo values calculated using the LRE derived Emax 
     * @param averageFoEmax100 the average of the Fo values calculated using Emax fixed to 100%
     */
    public void setAvFoValues(double averageFo, double averageFoEmax100) {
        this.avFo = averageFo;
        this.avFoEmax100 = averageFoEmax100;
    }

    /**
     * The average Fo calculated using Emax fixed to 100%
     * @return the average Fo calculated using Emax fixed to 100%
     */
    public double getAvFoEmax100() {
        return avFoEmax100;
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
    
    public double getFmax(){
        if(!isExcluded() && hasAnLreWindowBeenFound){
            return eMax/-(deltaE);
        }
        return 0;
    }

    public double getFb() {
        return fb;
    }

    public void setFb(double fb) {
        this.fb = fb;
    }

    public int getFbStart() {
        return fbStart;
    }

    public void setFbStart(int fbStart) {
        this.fbStart = fbStart;
    }

    public int getFbWindow() {
        return fbWindow;
    }

    public void setFbWindow(int fbWindow) {
        this.fbWindow = fbWindow;
    }

    public double[] getFcReadings() {
        return fcReadings;
    }

    public void setFcReadings(double[] fcReadings) {
        this.fcReadings = fcReadings;
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
     * nonR2 is the nonlinear correlation coefficient based on correlation
     * of the predicted Fc and actual Fc within the LRE window
     *
     * @return the nonlinear R2 derived the LRE window
     */
    public double getNonR2() {
        return nonR2;
    }

    /**
     * nonR2 is the nonlinear correlation coefficient based on correlation
     * of the predicted Fc and actual Fc within the LRE window
     *
     * @param nonR2 the nonlinear R2 derived the LRE window
     */
    public void setNonR2(double nonR2) {
        this.nonR2 = nonR2;
    }

    public double[] getRawFcReadings() {
        return rawFcReadings;
    }

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

    /**
     * Note that 
     * @param ampliconSize
     */
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
     * Note that Run data is set via the constructor so that setting the Run date
     * is redundant. It is provided solely for future options that could allow
     * the Run date to be changed.
     * @param runDate
     */
    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    public double getFbIntercept() {
        return fbIntercept;
    }

    public void setFbIntercept(double fbIntercept) {
        this.fbIntercept = fbIntercept;
    }

    public double getFbR2() {
        return fbR2;
    }

    public void setFbR2(double fbR2) {
        this.fbR2 = fbR2;
    }

    /**
     * This is part of an unimplemented function to determine the level of baseline 
     * drifting and thus has no current relevance.
     * @return the slope of the baseline
     */
    public double getFbSlope() {
        return fbSlope;
    }

    /**
     * This is part of an unimplemented function to determine the level of baseline 
     * drifting and thus has no current relevance.
     * @param fbSlope 
     */
    public void setFbSlope(double fbSlope) {
        this.fbSlope = fbSlope;
    }

    public boolean isExcluded() {
        return excluded;
    }

    /**
     * Exclude/include this profile. Note that this also updates the Run average Fmax.
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
     * Indicates whether the LRE-derived Emax has been fixed to 100% 
     * @return whether Emax is fixed to 100%
     */
    public boolean isEmaxFixedTo100() {
        return isEmaxFixedTo100;
    }

    /**
     * Allows the LRE-derived Emax to be fixed/unfixed to 100%.
     * Note that Emax fixed to 100% is only applied during conversion of Fo to No, and
     * thus does not impact LRE analysis used to generate Fo values, as this 
     * would generate aberrant biases. 
     * Note also that this updates the Run's average Fmax. 
     * @param isEmaxFixedTo100
     */
    public void setIsEmaxFixedTo100(boolean isEmaxFixedTo100) {
        this.isEmaxFixedTo100 = isEmaxFixedTo100;
        run.calculateAverageFmax();
    }

    /**
     * Indicates whether an LRE window has been found for this Profile.
     * 
     * @return whether an LRE window has been found
     */
    public boolean hasAnLreWindowBeenFound() {
        return hasAnLreWindowBeenFound;
    }

    /**
     * Indicates whether a this is a valid Profile. If an LRE window has 
     * not been found the Profile is not displayable. 
     * 
     * @param hasAnLreWindowFound sets whether an LRE window has been found
     */
    public void setHasAnLreWindowBeenFound(boolean hasAnLreWindowFound) {
        this.hasAnLreWindowBeenFound = hasAnLreWindowFound;
    }

    /**
     * Implemented in version 0.8.0 in order to maintain
     * back compatability, which requires that pre 0.8.0 profiles be reinitialized.
     * @return whether this Profile is version 0.8.0 or later
     */
    public boolean isProfileVer0_8_0() {
        return isProfileVer0_8_0;
    }

    /**
     * Used for back compatability, it is necessary to set to true for all 
     * new Profiles during Profile creation.
     * 
     * @param setProfileToVer0_8_0 true for all new Profiles
     */
    public void setProfileToVer0_8_0(boolean isProfileVer0_8_0) {
        this.isProfileVer0_8_0 = isProfileVer0_8_0;
    }

    @Override
    public String toString() {
        return super.getName();
    }
}
