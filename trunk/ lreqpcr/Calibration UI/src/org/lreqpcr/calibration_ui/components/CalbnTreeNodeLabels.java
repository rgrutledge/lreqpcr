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
package org.lreqpcr.calibration_ui.components;

import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.ReactionSetupImpl;
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
        if (member instanceof ReactionSetupImpl) {
            return member.getName();
        }
        if (member instanceof AverageCalibrationProfile) {
            AverageCalibrationProfile calbnProfile = (AverageCalibrationProfile) member;
            String label = calbnProfile.getAmpliconName() + "@" + calbnProfile.getSampleName();
            String ocf = "";
            df.applyPattern("##.0");
            String emax = "";
            String rundate = "Data import failed";
            if (calbnProfile.getRunDate() != null) {
                rundate = sdf.format(calbnProfile.getRunDate());
            }
            if (calbnProfile.isExcluded()) {
                return rundate + ": " + label + " EXCLUDED ";
            } else {
                df.applyPattern(FormatingUtilities.decimalFormatPattern(calbnProfile.getOCF()));
                ocf = "OCF= " + df.format(calbnProfile.getOCF());
                df.applyPattern("##.0");
                if (calbnProfile.isEmaxOverridden()) {
                    calbnProfile.setShortDescription("Emax overridden");
                    emax = " (" + df.format(calbnProfile.getOverriddendEmaxValue() * 100) + "%<--"
                            + df.format(calbnProfile.getEmax() * 100) + "%)";
                } else {
                    calbnProfile.setShortDescription("");
                    emax = " (" + df.format(calbnProfile.getEmax() * 100) + "%) ";
                }
                return rundate + ": " + label + emax + " " + ocf;
            }
        }
        if (member instanceof CalibrationProfile) {
            CalibrationProfile calbnProfile = (CalibrationProfile) member;
            String rundate = sdf.format(calbnProfile.getRunDate());
            String label = calbnProfile.getAmpliconName() + "@" + calbnProfile.getSampleName();
            df.applyPattern(FormatingUtilities.decimalFormatPattern(calbnProfile.getOCF()));
            String ocf = df.format(calbnProfile.getOCF());
            String emax = "";
            df.applyPattern("##.0");
            if (calbnProfile.isEmaxOverridden()) {
                    calbnProfile.setShortDescription("Emax overridden");
                    //Denote overridden Emax using asterics
                    emax = " (**" + df.format(calbnProfile.getOverriddendEmaxValue() * 100) + "%) ";
                } else {
                    calbnProfile.setShortDescription("");
                    emax = " (" + df.format(calbnProfile.getEmax() * 100) + "%) ";
                }
            
            if (calbnProfile.isExcluded()) {
                return label + " EXCLUDED ";
            } else {
                return rundate + " " + label + emax + " " + ocf;
            }
        }
        return "";
    }
}
