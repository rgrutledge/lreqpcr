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
package org.lreqpcr.experiment_ui.components;

import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.ui_elements.LabelFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.utilities.FormatingUtilities;

/**
 * Provides node labels for the Experiment database tree
 *
 * @author Bob Rutledge
 */
public class RunTreeNodeLabels implements LabelFactory {

    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private DecimalFormat df = new DecimalFormat();

    public String getNodeLabel(LreObject member) {
        if (member instanceof Run) {
            Run run = (Run) member;
            double runOCF = run.getRunOCF();
            //Display a Run-specific OCF if one has been applied to this Run
            if (runOCF != 0) {
                df.applyPattern(FormatingUtilities.decimalFormatPattern(runOCF));
                return sdf.format(run.getRunDate()) + " (" + df.format(runOCF) + ")-" + member.getName();
            } else {
                return sdf.format(run.getRunDate()) + "-" + member.getName();
            }

        }
        if (member instanceof Profile) {
            Profile profile = (Profile) member;
            profile.setShortDescription("");
            //Label madeup of three components
            String profileName = profile.getAmpliconName() + "@" + profile.getSampleName();
            //If excluded no Emax or No is displayed
            if (profile.isExcluded()) {
                if (profile instanceof AverageSampleProfile) {
                    profile.setShortDescription("This Profile has been excluded by the user");
                } else {//Must be a SampleProfile
                    profile.setShortDescription("This Sample Profile has been excluded by the user and will not be included in the Average Profile");
                }
                return profileName + " ...PROFILE IS EXCLUDED";
            }
            String emax;
            String no;
            //Determine what to display for Emax
            if (profile.isEmaxOverridden() && profile.hasAnLreWindowBeenFound()) {
                df.applyPattern("#0.0");
                profile.setShortDescription("Emax overridden");
                emax = " (" + df.format(profile.getOverriddendEmaxValue() * 100)
                        + "%<-- " + df.format(profile.getEmax() * 100) + "%)";
            } else {
                if (!profile.hasAnLreWindowBeenFound()) {
                    emax = " (LRE window not found) ";
                    profile.setShortDescription("An LRE window could not be found, likely due to being a flat profile"
                            + " or the Min Fc is set too high");
                } else {
                    df.applyPattern("#0.0");
                    emax = " (" + df.format(profile.getEmax() * 100) + "%) ";
                }
            }
            if (profile instanceof AverageSampleProfile) {
                AverageSampleProfile avPrf = (AverageSampleProfile) profile;
                if (avPrf.isReplicateAverageNoLessThan10Molecules()
                        && !avPrf.isExcluded()
                        && avPrf.getNumberOfActiveReplicateProfiles() > 1) {
                    df.applyPattern("0.00");
                    profile.setShortDescription("A valid average profile could not be generated due the low target quantity");
                    return profileName + " <10 Molecules: avReplc N= " + df.format(avPrf.getNo());
                }
            }
            //Determine what to display for No
            if (profile.getNo() < 10) {
                df.applyPattern("0.00");
            } else {
                df.applyPattern("###,###");
            }
            if (!profile.hasAnLreWindowBeenFound()) {
                no = " N= 0";
            } else {
                no = " N= " + df.format(profile.getNo());
            }
            return profileName + emax + no;
        }
        return "";
    }
}
