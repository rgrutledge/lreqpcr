/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lreqpcr.analysis.rutledge;

import org.ejml.data.DenseMatrix64F;

/**
 *
 * Computes predicted fluorescence readings (Fc) using the LRE sigmoid model
 * using 5 parameters: Fb, Fb-slope, Fmax, Emax and Fo
 *
 * @author Bob Rutledge
 */
public class Lre5Param implements LevenbergMarquardt.Function {

    @Override
    public void compute(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F y) {
        double[] pFcArray = new double[x.getNumRows()];//Predicted Fc
        double[] cycles = x.data;
        double emax = param.get(0);//Emax
        double fb = param.get(1);//Fluorescence baseline Fb
        double fo = param.get(2);//Fo
        double fmax = param.get(3);//Fmax
        double slope = param.get(4);

        for (int i = 0; i < x.numRows; i++) {
            pFcArray[i] = (fmax / (1 + ((((fmax / fo) - 1) * Math.pow(emax + 1, -cycles[i]))))) + fb + (slope * cycles[i]);
        }
        y.setData(pFcArray);
    }
    
    /**
     * Calculate the predicted fluorescence readings (pFc)
     * @param param the 5 LRE parameters 
     * @param x the cycles to be included
     * @return the predicted fluorescence readings
     */
    private double[] computePredictedFc(DenseMatrix64F param, DenseMatrix64F x) {
        double[] pFcArray = new double[x.getNumRows()];//Predicted Fc
        double[] cycles = x.data;
        double emax = param.get(0);//Emax
        double fb = param.get(1);//Fluorescence baseline Fb
        double fo = param.get(2);//Fo
        double fmax = param.get(3);//Fmax
        double slope = param.get(4);

        for (int i = 0; i < x.numRows; i++) {
            pFcArray[i] = (fmax / (1 + ((((fmax / fo) - 1) * Math.pow(emax + 1, -cycles[i]))))) + fb + (slope * cycles[i]);
        }
        return pFcArray;
    }
}
