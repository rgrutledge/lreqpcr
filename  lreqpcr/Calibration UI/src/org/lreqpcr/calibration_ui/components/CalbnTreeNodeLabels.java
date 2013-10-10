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
package org.lreqpcr.calibration_ui.components;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationRun;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.utilities.FormatingUtilities;

/**
 *
 * @author Bob Rutledge
 */
public class CalbnTreeNodeLabels implements LabelFactory {

    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private DecimalFormat df = new DecimalFormat();

    public String getNodeLabel(LreObject member) {
        if (member instanceof CalibrationRun) {
            CalibrationRun run = (CalibrationRun) member;
            //Place the Run average Fmax into the node label
            double avFmax = run.getAverageFmax();
            if (avFmax == 0) {
                return sdf.format(run.getRunDate()) + ": Excluded";
            }
            df.applyPattern(FormatingUtilities.decimalFormatPattern(avFmax));
            String avFmaxString = df.format(avFmax);
            df.applyPattern("#0");
            String fmaxCV = df.format(run.getAvFmaxCV() * 100);
            return sdf.format(run.getRunDate()) + ": Av Fmax= "
                    + avFmaxString + " ±" + fmaxCV + "%";
        }
//Must be a CalibrationProfile
        CalibrationProfile calbrnProfile = (CalibrationProfile) member;
        calbrnProfile.setShortDescription("");
        //Label madeup of four components: run date, name, Emax and OCF
        String rundate = "";
        if (calbrnProfile.getRunDate() != null) {
            rundate = sdf.format(calbrnProfile.getRunDate());
        }
        String profileName = calbrnProfile.getAmpliconName() + "@" + calbrnProfile.getSampleName() + " ";
        //If excluded no Emax or OCF is displayed
        if (calbrnProfile.isExcluded()) {
            if (calbrnProfile instanceof AverageCalibrationProfile) {
                calbrnProfile.setShortDescription("This Profile has been excluded by the user");
            } else {//Must be a CalibrationProfile
                calbrnProfile.setShortDescription("This Calibration Profile has been excluded by the user and will not be included in the Average Profile");
            }
            return rundate + ": " + profileName + "<PROFILE EXCLUDED>";
        }
        //Determine what to display for Emax
        String emax;
        String wellLabel;
        if (calbrnProfile instanceof AverageProfile){
            wellLabel = "";
        }else{//Must be a CalibrationProfile replicate
            wellLabel = calbrnProfile.getWellLabel() + ": ";
        }
        if (!calbrnProfile.hasAnLreWindowBeenFound()) {
            emax = "<LRE window not found>";
            calbrnProfile.setShortDescription("An LRE window could not be found, likely due to being a flat profile"
                    + " or that the Min Fc is set too high");
        } else {
            df.applyPattern("#0.0");
            emax = "(" + df.format(calbrnProfile.getEmax() * 100) + "%) ";
        }
        //Determine what to display for the OCF
        String ocf;
        df.applyPattern(FormatingUtilities.decimalFormatPattern(calbrnProfile.getOCF()));
        //Determine if Fmax normalization has been set
        if (calbrnProfile.isOcfNormalizedToFmax()) {
            ocf = " OCF= " + df.format(calbrnProfile.getOCF()) + "*";
            calbrnProfile.setShortDescription("The OCF has been normalized to the Run's average Fmax");
        } else {
            ocf = " OCF= " + df.format(calbrnProfile.getOCF());
        }
        return wellLabel + profileName + emax + ocf;

    }
}
