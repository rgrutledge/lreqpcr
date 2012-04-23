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

import java.util.Date;

/**
 * Abstract class for the basic data object representing an
 * amplification profile. Parameters (fields) are separated into
 * Profile creation, Profile processing and No determination.
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
    private TargetStrandedness targetStrandedness;
    //This is a very clumsy method for maintaiing back compatability...a better versioning method is clearly needed
    private boolean isProfileVer0_8_0 = false;//This must be set to true during data import!!!!!
        
    //Profile processing parameters
    private double ampTm;//The amplicon Tm
    private double ct, ft;//Threshold cycle and fluorescence threshold
    private int fbStart, fbWindow;//Start and size of the Fb window for determining background fluorescence
    private double[] fcReadings; //Fluorescence dataset (Background subtracted)
    private double fb;//Average fluorescence backgroung
    private boolean hasAnLreWindowBeenFound;
    private int strCycleInt; //LRE window start cycle
    private int lreWinSize; //LRE window size
    private double eMax, deltaE, r2;//Linear regression values for the LRE window
    private boolean isEmaxOverridden;//Overides LRE analysis-derived Emax
    private double overriddenEmaxValue;//Allows fixing Emax to this value, which can then be used to calculate Fo
    private double avFo, avFoCV;//Target quantity and CV derived from the LRE window Fo values
    private double nonR2;//Nonlinear r2 of predicted Fc to actual Fc within the LRE window... not very useful
    private double midC;//C1/2
    private double fbSlope, fbIntercept, fbR2;//Baseline linear regression used to test for baseline drift... under development
    private boolean excluded;//Allows profiles to be excluded from the analysis
    private String whyExcluded;//Text describing why the profile was excluded
    
    //Target molecule (No) determination via optical calibration
    private double ocf;//The optical calibration factor used to calculate the number of target molecules
    private double no;//Number of targets molecules

    /**
     * Holds all basic parameters of a Profile
     * @param run the Run from which the Profile was generated. Note that Run date is retrieved from the provide Run.
     */
    public Profile(Run run) {
        this.run = run;
        setParent(run);
        this.runDate = run.getRunDate();
    }

    public Run getRun() {
        return run;
    }

    public int getWellNumber() {
        return wellNumber;
    }

    public void setWellNumber(int wellNumber) {
        this.wellNumber = wellNumber;
    }

    /**
     * Primarily for updating following a change
     * to the LRE window, or any other parameter
     * such as the OCF
     * 
     */
    public abstract void updateProfile();

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

    public double getOCF() {
        return ocf;
    }

    public void setOCF(double averageOCF) {
        this.ocf = averageOCF;
    }

    /**
     * 
     * @return the average of the Fo values generated 
     * by the Fc readings within the LRE window.
     */
    public double getAvFo() {
        return avFo;
    }

    /**
     * The average Fo is the primary quantitative unit for a profile. 
     * @param Fo the average of the Fo values generated 
     * by the Fc readings within the LRE window.
     */
    public void setAvFo(double Fo) {
        this.avFo = Fo;
        updateProfile();
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
        //Some nearly flat profiles generate a negative Emax
        if(eMax < 0 || eMax == Double.NaN || deltaE > 0){
            setNo(0);
        }
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

    public double getFbSlope() {
        return fbSlope;
    }

    public void setFbSlope(double fbSlope) {
        this.fbSlope = fbSlope;
    }

    public void setNo(double no) {
        this.no = no;
    }

    /**
     * The number target molecules as determined by LRE analysis in .
     * in combination with the optical calibration factor (OCF). 
     * 
     * return number of target molecules
     */
    public double getNo() {
        return no;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public String getWhyExcluded() {
        return whyExcluded;
    }

    public void setWhyExcluded(String whyExcluded) {
        this.whyExcluded = whyExcluded;
    }

    /**
     * Indicates whether the LRE-derived Emax has been replaced by a fixed value 
     * as specified by the overriddenEmaxValue parameter.
     * @return
     */
    public boolean isEmaxOverridden() {
        return isEmaxOverridden;
    }

    /**
     * Allows the LRE-derived Emax to be overridden as specified by the overriddenEmaxValue parameter.
     * Note that an overridden Emax must only applied during conversion of Fo to No, and
     * thus does not impact LRE analysis used to generate Fo values.
     * @param isEmaxOverriden
     */
    public void setIsEmaxOverridden(boolean isEmaxOverriden) {
        this.isEmaxOverridden = isEmaxOverriden;
    }
    
    /**
     * This allows the LRE-derived Emax to be overridden in order to circumvent
     * aberrant Emax values generated by kinetic anomalies. Note that the option
     * to use this alternative Emax value for calculating the profile's average Fo
     * is the responsibility of the data processing service provider.
     *
     * @param overrideenEmaxValue the Emax value that overrides the LRE-derived Emax
     */
    public void setOverridentEmaxValue(double overrideenEmaxValue){
        this.overriddenEmaxValue = overrideenEmaxValue;
    }

    /**
     * 
     * @return the Emax value used to calculate Fo if the LRE-derived Emax is overridden
     * as indicated by the isEmaxFixed parameter.
     */
    public double getOverriddendEmaxValue(){
        return overriddenEmaxValue;
    }

    /**
     * If an LRE window has not been found the Profile is not displayable.
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
     * @param hasAnLreWindowFound determines whether an LRE window has been found
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
