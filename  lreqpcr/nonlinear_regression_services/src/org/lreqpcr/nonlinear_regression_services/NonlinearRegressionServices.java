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
package org.lreqpcr.nonlinear_regression_services;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Bob Rutledge
 */
public abstract class NonlinearRegressionServices {
    
    /**
     * Conducts nonlinear regression analysis based on the LRE 
     * sigmoidal model as defined by the parameters declared in the 
     * LreParameters class. 
     * <p>
     * The primary objective is to derive 
     * values for baseline fluourescence (Fb) and baseline slope (Fb-slope)
     * that are then used to generate an optimized 
     * working Fc dataset required for LRE analysis. 
     * <p>
     * As such, the regression-derived Emax, Fmax and Fo should only used to determine 
     * the level of convergence with LRE analysis (based on linear regression 
     * analysis of an Ec vs Fc plot), followed by averaging the cycle Fo values 
     * within the LRE window for determining target quantity (average Fo). 
     * For high quality profiles the convergence of nonlinear regression and 
     * LRE analysis have been found to be high. 
     * <p>
     * Note also that the profile to be analyzed should be trimmed to remove early 
     * cycles (typically cycles 1-3) that often generate aberrant fluorescence 
     * readings, and plateau cycles (typically cycles above the LRE window) that 
     * are often distorted due to aberrant amplification kinetics. Indeed, fixing the upper limit  
     * to the top of the LRE window has been found to be broadly effective for 
     * analysis of a variety of profiles found to generate aberrant amplification 
     * kinetics. 
     * 
     * @param iniParam initial values for the parameters used in the nonlinear regression analysis
     * @param cycleFc the observed cycle-fluorescence readings of the profile to be analyzed that must be ordered by cycle number
     * @return the optimized parameters derived from the nonlinear regression analysis
     */
    public abstract LreParameters conductLreNRAnalysis(LreParameters iniParam, TreeMap<Integer, Double> cycleFc);
    
}
