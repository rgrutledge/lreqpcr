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
package org.lreqpcr.analysis.rutledge;

import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Unimplemented methods for analyzing the Profile baseline fluorescence
 * for detection of baseline drift.
 * 
 * @author Bob Rutledge
 */
public class RutledgeBaselineAnalysis {

    /**
     * Estimate Fb drift based on the slope of the Fb window. Note
     * that the Fc dataset must be background subtracted and that the
     * Fb window must be >2 cycles.
     *
     * @param profile the profile to test
     */
    public static void calcFbDrift(Profile profile) {
        if (profile.getFbWindow() < 3) {
            return;
        }
        //Build the double[][] required for linear regression
        //[0][x] [1][y]
        double[][] xy = new double[2][profile.getFbWindow()];
        int fbStart = profile.getFbStart();//The start of the Fb window
        for (int i = 0; i < profile.getFbWindow(); i++) {
            xy[0][i] = i + fbStart;//Cycle number
            xy[1][i] = profile.getFcReadings()[i + fbStart];//Fc reading
        }
        double[] fbRegressionParameters = MathFunctions.linearRegressionAnalysis(xy);
        profile.setFbSlope(fbRegressionParameters[0]);
        profile.setFbIntercept(fbRegressionParameters[1]);
        profile.setFbR2(fbRegressionParameters[2]);
    }
}
