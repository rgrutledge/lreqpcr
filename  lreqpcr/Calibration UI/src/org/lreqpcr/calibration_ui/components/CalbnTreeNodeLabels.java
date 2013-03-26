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
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Run;
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
        if (member instanceof Run) {
            Run run = (Run) member;
            //Place the Run average Fmax into the short description
            double avFmax = run.getAverageFmax();
            df.applyPattern(FormatingUtilities.decimalFormatPattern(avFmax));
            String avFmaxString = df.format(avFmax);
            df.applyPattern("#0");
            String cv = df.format(run.getAvFmaxCV() * 100);
            run.setShortDescription(" [Av Fmax: " + avFmaxString + " ±" + cv + "%]");
            //Recalculate the Run avOCF
            //This is not expected to impact performance
            run.calculateAverageOCF();
            //Display Run's average OCF
            if (run.getAvOCF() != 0) {
                double avOCF = run.getAvOCF();
                df.applyPattern(FormatingUtilities.decimalFormatPattern(avOCF));
                String avOCFstring = df.format(avOCF);
                df.applyPattern("#0");
                String ocfCV = df.format(run.getAvOcfCV() * 100);
                if (run.getAvOcfCV() != 0){
                return sdf.format(run.getRunDate()) + ":  Av OCF " + avOCFstring + " ±" + ocfCV + "%";
                } else {//No CV to display
                    return sdf.format(run.getRunDate()) + ":  Av OCF = " + avOCFstring;
                }
            } else {
                return sdf.format(run.getRunDate()) + ": OCF not available";
            }
        }
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
        String emax;
        //Determine what to display for Emax
        if (calbrnProfile.isEmaxFixedTo100() && calbrnProfile.hasAnLreWindowBeenFound()) {
            df.applyPattern("#0.0");
            calbrnProfile.setShortDescription("Emax fixed to 100%");
            emax = "<100%>";
        } else {
            if (!calbrnProfile.hasAnLreWindowBeenFound()) {
                emax = "<LRE window not found>";
                calbrnProfile.setShortDescription("An LRE window could not be found, likely due to being a flat profile"
                        + " or the Min Fc is set too high");
            } else {
                df.applyPattern("#0.0");
                emax = "(" + df.format(calbrnProfile.getEmax() * 100) + "%) ";
            }
        }
        df.applyPattern(FormatingUtilities.decimalFormatPattern(calbrnProfile.getOCF()));
        String ocf = "OCF= " + df.format(calbrnProfile.getOCF());
        return rundate + ": " + profileName + emax + " " + ocf;

    }
}