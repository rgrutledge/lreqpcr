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
package org.lreqpcr.experiment_ui.components;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.lreqpcr.core.data_objects.AverageProfile;
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
public class SampleTreeNodeLabels implements LabelFactory {

    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private DecimalFormat df = new DecimalFormat();

    public String getNodeLabel(LreObject member) {

        if (member instanceof Run) {
            Run run = (Run) member;

            double runOCF = run.getRunSpecificOCF();//Finding it difficult to display this value
            //Place the Run average Fmax into the short description
            df.applyPattern("#0");
            double avFmax = run.getAverageFmax();
            df.applyPattern(FormatingUtilities.decimalFormatPattern(avFmax));
            String avFmaxString = df.format(avFmax);

            //Indicate if a Run-specific OCF if one has been applied to this Run 
            String RSO = "";
            if (run.getRunSpecificOCF() != 0) {
                RSO = "*";
                df.applyPattern(FormatingUtilities.decimalFormatPattern(run.getRunSpecificOCF()));
                String rso = df.format(run.getRunSpecificOCF());
                run.setShortDescription("Run-specific OCF = " + rso);
            }
            if (run.getAvFmaxCV() != 0) {
                String cv = df.format(run.getAvFmaxCV() * 100);
                return RSO + sdf.format(run.getRunDate()) + "-" + member.getName() + "  [Av Fmax: " + avFmaxString + " ±" + cv + "%]";
            } else {
                return RSO + sdf.format(run.getRunDate()) + "-" + member.getName() + "  [Av Fmax: " + avFmaxString;
            }
        }

        //Label madeup of three components: name + Emax + No + Replicate Scatter Warning
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
                    return profile.getWellLabel() + ": " + profileName + "<EXCLUDED>";
                }
                return profileName + "<EXCLUDED>";
            }

            if (profile instanceof AverageSampleProfile) {
                AverageSampleProfile avPrf = (AverageSampleProfile) profile;
                if (!(avPrf.getOCF() > 0)){
                    //The averge profile cannot be initialized...so there is no Emax or No values
                    profile.setShortDescription("Invalid Average Profile: OCF has not been entered");
                    return profileName + "  <No OCF>";
                }
                if (avPrf.getAmpliconSize() == 0) {
                profile.setShortDescription("Invalid Average Profile: an amplicon size is unknown");
                return profileName + "n.d.<Amplicon Size Unknown> ";
            }
                //This assumes that excluded profiles would not reach to this point
                if (avPrf.isTheReplicateAverageNoLessThan10Molecules()) {
    //This average profile is invalid, so No is inherented from the average replicate No
                    df.applyPattern("0.00");
                    profile.setShortDescription("Invalid Average Profile: replicate profile average is less than 10 molecules");
                    double no = avPrf.getReplicatePrfAvNo();
                    return profileName + "  <10N  [avRep= " + df.format(no) + "]";
                }
                if (!avPrf.areTheRepProfilesSufficientlyClustered()){
                    df.applyPattern("0.00");
                    profile.setShortDescription("Invlaid Average Profile: replicate profiles are too scattered");
                    double no = avPrf.getReplicatePrfAvNo();
                    return profileName + ": Replicate Profiles are scattered [avRep= " + df.format(no) + "]";
                }
            }
            String emax;
            String no;
            String wellLabel;
            if (profile instanceof AverageProfile) {
                wellLabel = "";
            } else {//Must be a replicate SampleProfile
                wellLabel = profile.getWellLabel() + ": ";
            }
            //Determine what to display for Emax
            if (!profile.hasAnLreWindowBeenFound() && profile.getOCF() > 0) {
//This assumes an AverageSampleProfile that has <10N would not reach here 
                profile.setShortDescription("An LRE window could not be found, likely due to being a flat profile"
                        + " or the Min Fc being set too high");
                //Emax and No do not exsist
                return wellLabel + profileName + "<LRE window not found>";
            }
            //Profile is OK
            df.applyPattern("#0.0");
            emax = "(" + df.format(profile.getEmax() * 100) + "%) ";
            //Determine what to display for No
            if (profile.getAmpliconSize() == 0) {
                profile.setShortDescription("Target quantity could not be determined because the amplicon size is unknown");
                return wellLabel + profileName + emax + "n.d.<Amplicon Size Unknown> ";
            }
            if (!(profile.getOCF() > 0)) {//This is needed for replicate profiles
                profile.setShortDescription("Target quantity could not be determined because an OCF has not been entered");
                return wellLabel + profileName + emax + " <No OCF>";
            }

            if (profile.getNo() < 10) {
                df.applyPattern("0.00");
            } else {
                df.applyPattern("###,###");
            }
            if (!profile.hasAnLreWindowBeenFound()) {
                no = "";
            }
            if (profile.isTargetQuantityNormalizedToFmax()) {
                no = "  *N= " + df.format(profile.getNo());
                profile.setShortDescription("Target quantity has been normalized to the Run's average Fmax");
            } else {
                no = "  N= " + df.format(profile.getNo());
            }
            return wellLabel + profileName + emax + no;
        }
        return "";
    }
}
