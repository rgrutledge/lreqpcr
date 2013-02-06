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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.ui_elements.LabelFactory;
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
            // TODO displaying run-specfic OCF must be reviewed
            if (runOCF != 0) {
                df.applyPattern(FormatingUtilities.decimalFormatPattern(runOCF));
                return sdf.format(run.getRunDate()) + " <" + df.format(runOCF) + ">-" + member.getName();
            } else {
                if (run.getAverageFmax() == 0) {
                    run.calculateAverageFmax();//This is temporary for testing
                }
                df.applyPattern("#0.0");
                String cv = df.format(run.getAvFmaxCV() * 100);
                df.applyPattern(FormatingUtilities.decimalFormatPattern(run.getAverageFmax()));
                return sdf.format(run.getRunDate()) + "-" + member.getName()
                        + " [Av Fmax: " + df.format(run.getAverageFmax())
                        + " ±" + cv + "%]";
            }

        }
        //Label madeup of three components: name + Emax + No
        if (member instanceof Profile) {
            SampleProfile profile = (SampleProfile) member;
            profile.setShortDescription("");

            String profileName = profile.getAmpliconName() + "@ " + profile.getSampleName() + " ";
            //If excluded no Emax or No to be displayed
            if (profile.isExcluded()) {
                if (profile instanceof AverageSampleProfile) {
                    profile.setShortDescription("This Average Profile has been excluded by the user");
                } else {//Must be a SampleProfile
                    profile.setShortDescription("This Sample Profile has been excluded "
                            + "by the user and will not be included in the Average Profile");
                }
                return profileName + "<PROFILE EXCLUDED>";
            }

            if (profile instanceof AverageSampleProfile) {
                AverageSampleProfile avPrf = (AverageSampleProfile) profile;
                //This assumes that excluded profiles would not reach to this point
                if (avPrf.isTheReplicateAverageNoLessThan10Molecules()) {
                    //An average profile does not exist, so No is inherented 
                    //from the average replicate No
                    //Do any of the replicate profiles have an LRE window?
                    df.applyPattern("0.00");
                    profile.setShortDescription("Less than 10 molecules requires averaging the replicate profiles quantities");
                    double no = avPrf.getNo();
                    if (avPrf.isEmaxFixedTo100()) {
                        return profileName + "<100%> [avRep= " + df.format(no) + "]";
                    }
                    return profileName + "[avRep= " + df.format(no) + "]";
                }
            }
            String emax;
            String no;
            //Determine what to display for Emax
            if (!profile.hasAnLreWindowBeenFound()) {
//This assumes an AverageSampleProfile that has <10N would not reach here 
                profile.setShortDescription("An LRE window could not be found, likely due to being a flat profile"
                        + " or the Min Fc being set too high");
                //Emax and No do not exsist
                return profileName + "<LRE window not found>";
            }
            //Profile is OK
            if (profile.isEmaxFixedTo100() && profile.hasAnLreWindowBeenFound()) {
                profile.setShortDescription("Emax is fixed to 100%");
                emax = "<100%> ";
            } else {
                df.applyPattern("#0.0");
                emax = "(" + df.format(profile.getEmax() * 100) + "%) ";
            }
            //Determine what to display for No
            if (profile.getAmpliconSize() == 0) {
                profile.setShortDescription("Target quantity could not be determined because an amplicon size has not been provided");
                return profileName + emax + "n.d.<Amplicon size absent> ";
            }
            if (!(profile.getOCF() >= 0)) {
                profile.setShortDescription("Target quantity could not be determined because an OCF has not been applied");
                return profileName + emax + "n.d. <No OCF>";
            }

            if (profile.getNo() < 10) {
                df.applyPattern("0.00");
            } else {
                df.applyPattern("###,###");
            }
            if (!profile.hasAnLreWindowBeenFound()) {
                no = "";
            } else {
                no = df.format(profile.getNo());
            }
            return profileName + emax + no;
        }
        return "";
    }
}
