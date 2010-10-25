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
        if (member instanceof AverageCalibrationProfile){
            AverageCalibrationProfile calbnProfile = (AverageCalibrationProfile) member;
            
            String ocf = "";
            df.applyPattern("##.0");
            String emax = " (" + df.format(calbnProfile.getEmax()*100) + "%)";
            String rundate = "Data import failed";
            if (calbnProfile.getRunDate() != null){
                rundate = sdf.format(calbnProfile.getRunDate());
            }
            if(calbnProfile.isExcluded()){
                return  rundate + ": " + calbnProfile.getAmpliconName() + " EXCLUDED ";
            } else{
                if (calbnProfile.getEmax() > 1.00) {
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(calbnProfile.getAdjustedOCF()));
                    ocf = df.format(calbnProfile.getAdjustedOCF());
                    calbnProfile.setShortDescription("Normalized to 100% Emax");
                } else {
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(calbnProfile.getRunOCF()));
                    ocf = df.format(calbnProfile.getRunOCF());
                    calbnProfile.setShortDescription("");
                }
                return rundate + ": " + calbnProfile.getAmpliconName() + emax + " " + ocf;
            }
        }
        if (member instanceof CalibrationProfile) {
            CalibrationProfile calbnProfile = (CalibrationProfile) member;
            df.applyPattern(FormatingUtilities.decimalFormatPattern(calbnProfile.getRunOCF()));
            String ocf = "";
            if (calbnProfile.getEmax() > 1.00) {
                ocf = df.format(calbnProfile.getAdjustedOCF());
                calbnProfile.setShortDescription("Normalized to 100% Emax");
            } else {
                ocf = df.format(calbnProfile.getRunOCF());
                calbnProfile.setShortDescription("");
            }
            df.applyPattern("##.0");
            String emax = " (" + df.format(calbnProfile.getEmax()*100) + "%)";
            String rundate = sdf.format(calbnProfile.getRunDate());
            if (calbnProfile.isExcluded()) {
                return  calbnProfile.getName() + " EXCLUDED ";
            } else {
                return rundate + " " + calbnProfile.getAmpliconName() + emax + " " + ocf;
            }
        }
        return "";
    }

}
