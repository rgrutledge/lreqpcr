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
    private String wellLabel;//Well label i.e. A1-A12, A1-H1
    private int wellNumber;//1-96 etc, also allows ordering of profiles
    private Date runDate;
    private double[] rawFcReadings;//raw Fc dataset, no background subtraction
    private Sample sample;//Not implemented
    private String sampleName;
    private Amplicon amplicon;//Not implemented
    private String ampliconName;
    private ReactionSetup reactionSetup;//Not fully implemented
    private String reactionSetupName;//This is likely redundant to reactionSetup
    private int ampliconSize = 0;
    private TargetStrandedness targetStrandedness;
    
    //Profile processing parameters
    private double ampTm;//The amplicon Tm
    private double ct, ft;//Threshold cycle and fluorescence threshold
    private int fbStart, fbWindow;//Start and size of the Fb window for determining background fluorescence
    private double[] fcReadings; //Fluorescence dataset (Background subtracted)
    private double fb;//Average fluorescence backgroung
    private int strCycleInt; //LRE window start cycle
    private int lreWinSize; //LRE window size
    private double eMax, deltaE, r2;//Linear regression values for the LRE window
    private double avFo, avFoCV;//Target quantity and CV derived from the LRE window Fo values
    private double adjustedAvFo;//Target quantity adjusted to 100% Emax
    private double nonR2;//Nonlinear r2 of predicted Fc to actual Fc within the LRE window... not very useful
    private double midC;//C1/2
    private double fbSlope, fbIntercept, fbR2;//Baseline linear regression used to test for baseline drift... under development
    private boolean excluded = false;//Allows profiles to be excluded from the analysis
    private String whyExcluded;//Text describing why the profile was excluded... not implemented
    
    //Target molecule (No) determination via optical calibration
    private double ocf;//The optical calibration factor used to calculate the number of target molecules
    //ocf is derived from the corresponding Reaction Setup which is currently manually implemented
    //because the full functionality of ReactionSetup is not yet implemented
    private double runOCF;//Run specific OCF
    private double no;//Number of targets molecules
    private double adjustedNo;//Number of targets molecules adjusted to 100% Emax

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
     * ReactionSetup is not yet implemented
     * @return
     */
    public ReactionSetup getReactionSetup() {
        return reactionSetup;
    }

    /**
     * ReactionSetup is not yet implemented
     * @param reactionSetup
     */
    public void setReactionSetup(ReactionSetup reactionSetup) {
        this.reactionSetup = reactionSetup;
    }

    public String getReactionSetupName() {
        return reactionSetupName;
    }

    public void setReactionSetupName(String reactionSetupName) {
        this.reactionSetupName = reactionSetupName;
    }

    /**
     * 
     * @return the Amplicon used for PCR amplification.
     */
    public Amplicon getAmplicon() {
        return amplicon;
    }

    /**
     * 
     * @param amplicon the Amplicon used for PCR amplification.
     */
    public void setAmplicon(Amplicon amplicon) {
        this.amplicon = amplicon;
    }

    /**
     * 
     * @return the Sample used in the PCR amplification.
     */
    public Sample getSample() {
        return sample;
    }

    /**
     * 
     * @param sample the Sample used in the PCR amplification.
     */
    public void setSample(Sample sample) {
        this.sample = sample;
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
     * It must be noted however, that the vagaries
     * of positional analysis (e.g. Ct is dependent on the fluorescence
     * threshold) makes Ct unreliable.
     * 
     * @return the observed threshold cycle (AKA Cq)
     */
    public double getCt() {
        return ct;
    }

    /**
     * This was included for legacy issues. 
     * It must be noted however, that the vagaries
     * of positional analysis (e.g. Ct is dependent on the fluorescence
     * threshold) makes Ct unreliable.
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

    public double getRunOCF() {
        return runOCF;
    }

    public void setRunOCF(double runOCF) {
        this.runOCF = runOCF;
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
     * 
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
     *
     * @return average Fo adjusted to 100% Emax
     */
    public double getAdjustedAvFo() {
        return adjustedAvFo;
    }

    /**
     *
     * @param adjustedAvFo average Fo adjusted to 100% Emax
     */
    public void setAdjustedAvFo(double adjustedAvFo) {
        this.adjustedAvFo = adjustedAvFo;
        updateProfile();
    }

    /**
     *
     * @return No adjusted to 100% Emax
     */
    public double getAdjustedNo() {
        return adjustedNo;
    }

    /**
     *
     * @param adjustedNo No adjusted to 100% Emax
     */
    public void setAdjustedNo(double adjustedNo) {
        this.adjustedNo = adjustedNo;
    }

    @Override
    public String toString() {
        return super.getName();
    }
}
