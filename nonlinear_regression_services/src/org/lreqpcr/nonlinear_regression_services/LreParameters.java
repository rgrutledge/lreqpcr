/**
 * Copyright (C) 2013 Bob Rutledge
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 * and open the template in the editor.
 */
package org.lreqpcr.nonlinear_regression_services;

/**
 * Holds values for the 5 parameters used by the LRE sigmoidal model: 
 * Emax, Fmax, Fo, Fb and Fb-slope.
 * 
 * @author Bob Rutledge
 */
public class LreParameters {
    
    private double emax, fmax, fo, fb, fbSlope;

    public double getEmax() {
        return emax;
    }

    public void setEmax(double emax) {
        this.emax = emax;
    }

    public double getFmax() {
        return fmax;
    }

    public void setFmax(double fmax) {
        this.fmax = fmax;
    }

    public double getFo() {
        return fo;
    }

    public void setFo(double fo) {
        this.fo = fo;
    }

    public double getFb() {
        return fb;
    }

    public void setFb(double fb) {
        this.fb = fb;
    }

    public double getFbSlope() {
        return fbSlope;
    }

    public void setFbSlope(double fbSlope) {
        this.fbSlope = fbSlope;
    }
    
}
