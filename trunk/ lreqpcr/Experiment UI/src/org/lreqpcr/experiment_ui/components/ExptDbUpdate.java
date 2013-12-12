/*
 * Copyright (C) 2013  Bob Rutledge
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
package org.lreqpcr.experiment_ui.components;

import java.awt.Toolkit;
import java.util.List;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.ExperimentDbInfo;
import org.lreqpcr.core.data_objects.ExptDbInfo;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Bob Rutledge
 */
public class ExptDbUpdate {

    /**
     * Converts ExperimentDbInfo to the new ExptDbInfo first implemented in
     * version 0.8.6.
     *
     * @since version 0.8.6
     * @param exptDB
     */
    public static void exptDbConversion086(DatabaseServices exptDB) {
        //Retrieve the old DB info file
//        ExperimentDbInfo oldDbInfo = (ExperimentDbInfo) exptDB.getAllObjects(ExperimentDbInfo.class).get(0);
//        ExptDbInfo newDbInfo = new ExptDbInfo();
//        //Copy all values into the newDbInfo
//        newDbInfo.setOcf(oldDbInfo.getOcf());
//        newDbInfo.setIsTargetQuantityNormalizedToFmax(oldDbInfo.isTargetQuantityNormalizedToFax());
//        exptDB.saveObject(newDbInfo);
//        exptDB.deleteObject(oldDbInfo);
//        nonlinearRegressionUpdate(exptDB);
//    }

    /**
     * Applies nonlinear regression analysis to pre Version 0.9 database files
     *
     * @since version 0.9.0
     * @param exptDB the Experiment database service maintaining the database
     * file to be processed
     */
//    public static void nonlinearRegressionUpdate(DatabaseServices exptDB) {
//        Toolkit.getDefaultToolkit().beep();
//        String msg = "This database appears to predate nonlinear regression analysis.\n\n"
//                + "Do you want to apply nonlinear regression to the database?";
//        boolean reply = RunImportUtilities.requestYesNoAnswer("Database predates nonlinear regression", msg);
//        if (!reply){
//            return;//This should be all that is necessary
//        }
//        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_WAIT_CURSOR);
//        //Convert the old ExptDbInfo
//        List info = exptDB.getAllObjects(ExptDbInfo.class);
//        ExptDbInfo oldDbInfo = (ExptDbInfo) info.get(0);
//        ExptDbInfo newDbInfo = updateExptDbInfo(oldDbInfo);
//        exptDB.saveObject(newDbInfo);
//        exptDB.deleteObject(oldDbInfo);
        //Apply nonlinear regression
//        NonlinearRegressionUtilities.applyNonlinearRegression(exptDB);
//        List<LreWindowSelectionParameters> l = exptDB.getAllObjects(LreWindowSelectionParameters.class);
//        LreWindowSelectionParameters selectionParameters = l.get(0);
//        LreAnalysisService lreAnalysisService =
//                Lookup.getDefault().lookup(LreAnalysisService.class);
//        List<AverageProfile> profileList = exptDB.getAllObjects(AverageProfile.class);
//        if (profileList.isEmpty()) {
//            return;
//        }
//        for (AverageProfile avProfile : profileList) {
//            //Need to update the replicate profiles first in order to test if <10N
//            for (Profile profile : avProfile.getReplicateProfileList()) {
//                ProfileSummary prfSum = new ProfileSummaryImp(profile, exptDB);
//                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, selectionParameters);
//                exptDB.saveObject(profile);
//            }
//            if (!avProfile.isTheReplicateAverageNoLessThan10Molecules() && avProfile.areTheRepProfilesSufficientlyClustered()) {
//                //The AverageProfile is valid thus reinitialize it
//                Profile profile = (Profile) avProfile;
//                ProfileSummary prfSum = new ProfileSummaryImp(profile, exptDB);
//                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, selectionParameters);
//                exptDB.saveObject(avProfile);
//            }
//        }
//        exptDB.commitChanges();
//        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_DEFAULT_CURSOR);
    }

//    private static ExptDbInfo updateExptDbInfo(ExptDbInfo oldDbInfo) {
//        ExptDbInfo newDbInfo = new ExptDbInfo();
//        newDbInfo.setAvRunFmax(oldDbInfo.getAvRunFmax());
//        newDbInfo.setAvRunFmaxCV(oldDbInfo.getAvRunFmaxCV());
//        newDbInfo.setIsTargetQuantityNormalizedToFmax(oldDbInfo.isTargetQuantityNormalizedToFmax());
//        newDbInfo.setOcf(oldDbInfo.getOcf());
//        return newDbInfo;
//    }
}
