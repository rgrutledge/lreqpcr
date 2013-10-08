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
package org.lreqpcr.nonlinear_regression_provider;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.ejml.data.DenseMatrix64F;
import org.lreqpcr.nonlinear_regression_services.LreParameters;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionServices;
import org.openide.util.lookup.ServiceProvider;

/**
 * Nonlinear regression analysis utilizing Peter Abeles’s EJML API
 * (http://code.google.com/p/efficient-java-matrix-library/wiki/LevenbergMarquardtExample). 
 * <p>
 * The analysis uses the 5 parametric LRE model that include baseline fluorescence 
 * and baseline slope correction, as declared within the LreParameters class. 
 * 
 * @author Bob Rutledge
 */
@ServiceProvider(service = NonlinearRegressionServices.class)
public class NonlinearRegressionProvider extends NonlinearRegressionServices {
    
    private Lre5Param func = new Lre5Param();
    private LevenbergMarquardt fitter = new LevenbergMarquardt(func);
    private DenseMatrix64F initialParam = new DenseMatrix64F(5, 1);

    @Override
    public LreParameters conductNonlinearRegression(LreParameters iniParam, TreeMap<Integer, Double> cycleFc) {
        List<Integer> cycleArray = new ArrayList<Integer>(cycleFc.keySet());
        double[] cycles = new double[cycleArray.size()];
        double[] fcReadings = new double[cycleArray.size()];
        //Construct the double arrays
        for (int i = 0; i < cycles.length; i++) {
            cycles[i] = cycleArray.get(i);
            fcReadings[i] = cycleFc.get(cycleArray.get(i));
        }
        //DenseMatrix require double[]
        DenseMatrix64F cycle = new DenseMatrix64F(cycleArray.size(), 1);
        cycle.setData(cycles);
        DenseMatrix64F fc = new DenseMatrix64F(cycleArray.size(), 1);
        fc.setData(fcReadings);
        //Contruct the initial paramater array
        double[] paramArray = new double[5];
        paramArray[0] = iniParam.getEmax();//LRE-derived Emax
        paramArray[1] = iniParam.getFb();//Fb derived from Fc average (e.g. cycles 4-9)
        paramArray[2] = iniParam.getFo();//LRE-derived Fo
        paramArray[3] = iniParam.getFmax();//LRE-derived Fmax
        paramArray[4] = iniParam.getFbSlope();//Baseline slope 
        initialParam.setData(paramArray);
        //Returns true even when the NR fails!!!
        fitter.optimize(initialParam, cycle, fc);
        //Retrieve and set the optimized parameters
        double[] optParamArray = fitter.getOptimizedParameters().getData();
        LreParameters optzParam = new LreParameters();
        optzParam.setEmax(optParamArray[0]);
        optzParam.setFb(optParamArray[1]);
        optzParam.setFo(optParamArray[2]);
        optzParam.setFmax(optParamArray[3]);
        optzParam.setFbSlope(optParamArray[4]);
        return optzParam;
    }
}
