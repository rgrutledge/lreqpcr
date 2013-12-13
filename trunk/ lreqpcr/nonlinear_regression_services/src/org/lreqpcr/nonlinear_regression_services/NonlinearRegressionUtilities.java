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

import java.awt.Toolkit;
import java.util.List;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.DatabaseInfo;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.openide.util.Lookup;

/**
 * Static methods supporting nonlinear regression analysis
 *
 * @author Bob Rutledge
 * @since version 0.9.3
 */
public class NonlinearRegressionUtilities {

    /**
     * Applies nonlinear regression to all of the profiles within the supplied
     * database.
     *
     * @param profileDb the Profile database to apply nonlinear regression-based
     * LRE analysis
     */
    public static void applyNonlinearRegression(DatabaseServices profileDb) {
        Toolkit.getDefaultToolkit().beep();
        String msg = "This database appears to predate nonlinear regression analysis.\n"
                + "Do you want to apply nonlinear regression to the database?";
        boolean reply = RunImportUtilities.requestYesNoAnswer("Database predates nonlinear regression", msg);
        if (!reply) {
            return;//This should be all that is necessary
        }
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_WAIT_CURSOR);
        List<LreWindowSelectionParameters> l = profileDb.getAllObjects(LreWindowSelectionParameters.class);
        if (l.isEmpty()) {
            return;//Likely not a profile database
        }
        LreWindowSelectionParameters selectionParameters = l.get(0);
        //Check to see if the DatabaseInfo has the current version number
        List info = profileDb.getAllObjects(DatabaseInfo.class);
        LreAnalysisService lreAnalysisService =
                Lookup.getDefault().lookup(LreAnalysisService.class);
        List<AverageProfile> profileList;
//Necessary because for unknown reasons Calibration databases fail to retrieve AverageProfiles
        if (profileDb.getDatabaseType() == DatabaseType.CALIBRATION) {
            profileList = (List<AverageProfile>) profileDb.getAllObjects(AverageCalibrationProfile.class);
        } else {
            profileList = profileDb.getAllObjects(AverageProfile.class);
        }
        if (profileList.isEmpty()) {
            return;
        }
        for (AverageProfile avProfile : profileList) {
            //Need to update the replicate profiles first in order to test if <10N
            for (Profile profile : avProfile.getReplicateProfileList()) {
                ProfileSummary prfSum = new ProfileSummaryImp(profile, profileDb);
                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, selectionParameters);
                profileDb.saveObject(profile);
            }
            if (!avProfile.isTheReplicateAverageNoLessThan10Molecules() && avProfile.areTheRepProfilesSufficientlyClustered()) {
                //The AverageProfile is valid thus reinitialize it
                Profile profile = (Profile) avProfile;
                ProfileSummary prfSum = new ProfileSummaryImp(profile, profileDb);
                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, selectionParameters);
                profileDb.saveObject(avProfile);
            }
        }
        profileDb.commitChanges();
    }
}
