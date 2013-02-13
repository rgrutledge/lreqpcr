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

import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.ui_elements.LabelFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.utilities.FormatingUtilities;

/**
 *
 * @author Bob Rutledge
 */
public class CalbnTreeNodeLabels implements LabelFactory {

    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private DecimalFormat df = new DecimalFormat();

    public String getNodeLabel(LreObject member) {
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
            } else {//Must be a SampleProfile
                calbrnProfile.setShortDescription("This Calibration Profile has been excluded by the user and will not be included in the Average Profile");
            }
            return rundate + ": " + profileName + " ...PROFILE IS EXCLUDED";
        }
        String emax;
        //Determine what to display for Emax
        if (calbrnProfile.isEmaxFixedTo100() && calbrnProfile.hasAnLreWindowBeenFound()) {
            df.applyPattern("#0.0");
            calbrnProfile.setShortDescription("Emax overridden");
            emax = "(100%<-- " + df.format(calbrnProfile.getEmax() * 100) + "%)";
        } else {
            if (!calbrnProfile.hasAnLreWindowBeenFound()) {
                emax = "(LRE window not found) ";
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
