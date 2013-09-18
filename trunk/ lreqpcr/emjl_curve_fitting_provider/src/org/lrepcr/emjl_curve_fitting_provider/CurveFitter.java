/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lrepcr.emjl_curve_fitting_provider;

import java.util.Collection;
import org.lreqpcr.core.utilities.*;
import org.ejml.data.DenseMatrix64F;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.ui_elements.LreNode;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
//import org.openide.util.Utilities;

/**
 *
 * @author brutledg
 */
public class CurveFitter implements UniversalLookupListener, LookupListener {
//    private UniversalLookup universalLookup;

    private Lookup.Result nodeResult;
    protected LreNode selectedNode;
    private Profile selectedProfile;
//    private LevenbergMarquardt.Function function = new Lre4Param();
//    private LevenbergMarquardt curveFitter = new LevenbergMarquardt(function);

    public CurveFitter() {
//        nodeResult = Utilities.actionsGlobalContext().lookupResult(LreNode.class);
//        nodeResult.allItems();
//        nodeResult.addLookupListener(this);
    }

    @Override
    public void universalLookupChangeEvent(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        //A new node has been selected
//        Lookup.Result r = (Lookup.Result) ev.getSource();
//        Collection<LreNode> c = r.allInstances();
//        if (!c.isEmpty()) {
//            selectedNode = c.iterator().next();
//            //Reject if this is an Amplicon database object
//            DatabaseType type = selectedNode.getDatabaseServices().getDatabaseType();
//            if (type == DatabaseType.AMPLICON) {
//                return;
//            }
//            LreObject member = selectedNode.getLookup().lookup(LreObject.class);
//            if (member instanceof Profile) {//A new profile has been selected
//                selectedProfile = (Profile) member;
//                int startIndex=  3;//Cycle 4
//                //Push the endIndex to the end of the profile
////                int endIndex = 49;
//                int endIndex = selectedProfile.getStrCycleInt() + selectedProfile.getLreWinSize()-startIndex +7;
//                int numberOfCycles = endIndex-startIndex +1;
//                double[] fcArray = selectedProfile.getRawFcReadings();
//                //Construct the trimmed Fc dataset
//                double[] trimmedFcArray = new double[numberOfCycles];
//                double[] cycleArray = new double[numberOfCycles];
//                for (int i = 0; i < numberOfCycles; i++) {
//                    cycleArray[i] = i + 4;
//                    trimmedFcArray[i] = fcArray[i + 3];
//                }
//                DenseMatrix64F y = new DenseMatrix64F(numberOfCycles, 1);
//                y.setData(trimmedFcArray);
//                DenseMatrix64F x = new DenseMatrix64F(numberOfCycles, 1);
//                x.setData(cycleArray);
//                double[] paramArray = new double[5];
//                paramArray[0] = selectedProfile.getEmax();
//                paramArray[1] = selectedProfile.getFb();//Fb
//                paramArray[2] = selectedProfile.getAvFo();
//                paramArray[3] = selectedProfile.getEmax() / (-1 * selectedProfile.getDeltaE());//Fmax
//                paramArray[4] = 0.0;//Baseline slope
//                DenseMatrix64F initialParam = new DenseMatrix64F(5, 1);
//                initialParam.setData(paramArray);
//                //Initiate curve fitting
//                //Create the function
//                Lre5Param func = new Lre5Param();
//                //Instantiate the LM curver fitter
//                LevenbergMarquardt scf = new LevenbergMarquardt(func);
//                boolean b = scf.optimize(initialParam, x, y);
//                DenseMatrix64F optParam = scf.getOptimizedParameters();
//                double iniCost = scf.getInitialCost();
//                double finalCost = scf.getFinalCost();
//                int difference = (int) (iniCost-finalCost);
//                double[] pFc = func.computePredictedFc(optParam, x);
//                double r2 = LREmath.calcNonLinearR2(trimmedFcArray, pFc);
//                int stop = 1;
//            }
//        }
    }
}
