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

package org.lreqpcr.core.data_objects;

/**
 * NOT YET IMPLEMENTED Represents a sample.
 *
 * Sample is a complex element that in e.g. gene expression analysis 
 * is composed of RNA derived from a sample, followed by cDNA derived by RT. 
 * The sample type (unknown, standard, NTC etc) should also be
 * set by the Sample. Note also, that is possible that targets could 
 * be a mixture of both single and double stranded Target molecules
 * 
 * @author Bob Rutledge
 */
public abstract class Sample extends LreObject {

    private double emaxAverage;//Average Emax
    private double emaxCV;//CV of the average Emax
//Note that it is possible the the sample is a mixture of single and 
//double stranded targets, so this designation is likely not correct. 
//It may be best to specify strandedness in Profile instead. 
    private TargetStrandedness targetStandedness;
    //Strandedness should be an enum...
    private boolean isTargetSingleStranded = true;

    public TargetStrandedness getTargetStandedness() {
        return targetStandedness;
    }

    public void setTargetStandedness(TargetStrandedness targetStandedness) {
        this.targetStandedness = targetStandedness;
    }
    //Sample type should also be an enum...

    public double getEmaxAverage() {
        return emaxAverage;
    }

    public void setEmaxAverage(double emaxAverage) {
        this.emaxAverage = emaxAverage;
    }

    public double getEmaxCV() {
        return emaxCV;
    }

    public void setEmaxCV(double emaxCV) {
        this.emaxCV = emaxCV;
    }

    @Override
    public String toString() {
        return getName();
    }

}
