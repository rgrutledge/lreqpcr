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
 * Represents the setup used to conduct an amplification NOTE YET FULLY IMPLEMENTED.
 *
 * @author Bob Rutledge
 */
public abstract class ReactionSetup extends LreObject {

    private String machineName, pmtGain;//MXP uses a gain setting
    private String cyclingRegime;
    private String enzymeFormulation, enzymeManufacturer;
    private double primerConcentration;//Concentration in micoMolar (uM)
    private double reactionVolume;
    private String reactionVessel;
    private String reactionClosure;//The type of vessel closure (e.g. caps)
    private double averageOCF;
    private double ocfCV;

    public ReactionSetup() {
        setChildClass(AverageCalibrationProfile.class);
    }

    public String getCyclingRegime() {
        return cyclingRegime;
    }

    public void setCyclingRegime(String cyclingRegime) {
        this.cyclingRegime = cyclingRegime;
    }

    public String getEnzymeFormulation() {
        return enzymeFormulation;
    }

    public void setEnzymeFormulation(String enzymeFormulation) {
        this.enzymeFormulation = enzymeFormulation;
    }

    public String getEnzymeManufacturer() {
        return enzymeManufacturer;
    }

    public void setEnzymeManufacturer(String enzymeManufacturer) {
        this.enzymeManufacturer = enzymeManufacturer;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getPmtGain() {
        return pmtGain;
    }

    public void setPmtGain(String pmtGain) {
        this.pmtGain = pmtGain;
    }

    public double getReactionVolume() {
        return reactionVolume;
    }

    public void setReactionVolume(double reactionVolume) {
        this.reactionVolume = reactionVolume;
    }

    /**
     *
     * @return the primer concentration in micromolar (uM)
     */
    public double getPrimerConcentration() {
        return primerConcentration;
    }

    /**
     *
     * @param primerConcentration the primer concentration in micromolar (uM)
     */
    public void setPrimerConcentration(double primerConcentration) {
        this.primerConcentration = primerConcentration;
    }

    public String getReactionClosure() {
        return reactionClosure;
    }

    public void setReactionClosure(String reactionClosure) {
        this.reactionClosure = reactionClosure;
    }

    public String getReactionVessel() {
        return reactionVessel;
    }

    public void setReactionVessel(String reactionVessel) {
        this.reactionVessel = reactionVessel;
    }
    //Could incorporate a Calibrator preparation date,
    //possibly via a Calibrator class (PrepDate, Concentration, etc.)

    public void setAverageOCF(double averageOCF) {
        this.averageOCF = averageOCF;
    }

    public double getAverageOCF() {
        return averageOCF;
    }

    public double getOcfCV() {
        return ocfCV;
    }

    public void setOcfCV(double ocfCV) {
        this.ocfCV = ocfCV;
    }
}
