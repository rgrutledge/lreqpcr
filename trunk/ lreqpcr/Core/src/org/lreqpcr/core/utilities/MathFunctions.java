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

package org.lreqpcr.core.utilities;

import java.util.List;

/**
 * Static methods that provide various mathematical functions
 *
 * @author Bob Rutledge
 */
public class MathFunctions {

    /**
     * Conducts linear regression analysis on the provided double array
     *
     *@param  pointArray  xy double array (x = [0][], y = [1][])
     *@return a regression values as double[slope, intercept, r2]
     */
    public static double[] linearRegressionAnalysis(double[][] pointArray) {
        int arraySize = pointArray[1].length;//
        double sumX = 0;
        double sumY = 0;
        double avX = 0;
        double avY = 0;
        double slope = 0;
        double intercept = 0;
        for (int i=0; i < arraySize; i++) {
            sumX += pointArray[0][i];
            sumY += pointArray[1][i];
        }
        avX = sumX/arraySize;
        avY = sumY/arraySize;
        double numSum = 0, demSum = 0; //numerator and denominator sums
        for (int i=0; i < arraySize; i++) {
            numSum += (avX - pointArray[0][i])*(avY - pointArray[1][i]);
            demSum += Math.pow(avX - pointArray[0][i], 2);
        }
        slope = numSum/demSum;
        intercept = (sumY-(slope*sumX))/arraySize;
        int i = 0;
        numSum = 0;
        double demXsum = 0;
        double demYsum = 0;
        double r2 = 0;
        for (i=0; i < arraySize; i++) {
            numSum += (avX - pointArray[0][i])*(avY - pointArray[1][i]);
            demXsum += Math.pow((avX - pointArray[0][i]), 2);
            demYsum += Math.pow((avY - pointArray[1][i]), 2);
        }
        r2 = Math.pow((numSum/(Math.sqrt(demXsum*demYsum))), 2);
        double[] regressionValues = new double[3];
        regressionValues[0] = slope;
        regressionValues[1] = intercept;
        regressionValues[2] = r2;
        return regressionValues;
    }
    
     /**
     * Calculates standard deviation based on Excel's stdev function
     *
      *@param values the values to process
      * @return stDev the standard deviation
     */
    public static double calcStDev(List<Double> values) {
        double stDev = 0;
        double xSum = 0; //Sum of values
        double x2Sum = 0; //Sum of the values squared
        for(Double v : values){
            xSum += v;
            x2Sum += Math.pow(v, 2);
        }
        int n = values.size();
        stDev = Math.sqrt(((n*x2Sum)-Math.pow(xSum, 2))/(n*(n-1)));
        return stDev;
    }
}
