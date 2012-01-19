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
            String label = profile.getAmpliconName() + "@" + profile.getSampleName();
            if (profile instanceof AverageSampleProfile) {
                AverageSampleProfile avPrf = (AverageSampleProfile) profile;
                if (avPrf.getOCF() != 0) {
                    //Test to see if the replicate No average <10
                    double sum = 0;
                    int counter = 0;
                    for (Profile repPrf : avPrf.getReplicateProfileList()) {
                        //It is important not to include excluded profiles
                        if (!repPrf.isExcluded()) {
                            sum = sum + repPrf.getNo();
                            counter++;
                        }
                    }
                    if (counter == 0) {//All replicates are excluded
                        profile.setShortDescription("No replicate profiles available");
                        return label + " -->No Replicate Profiles Included";
                    }
//Assume that excluded replicate profiles are zero molecule aliquots and thus must be included into the average
                    double avNo = sum / counter;
                    if (avNo < 10) {
                        profile.setShortDescription("Replicate No average");
                        df.applyPattern("0.00");
                        return label + " <10 Molecules: avReplc N= " + df.format(avNo);
                    } else {
                        profile.setShortDescription("");
                    }
                }
            }
            df.applyPattern("###,###");
            if (profile.getNo() < 10) {
                df.applyPattern("0.00");
            }
            String no = " N= " + df.format(profile.getNo());
            String emax = "";
            //Check if the Emax has been overridden
            if (profile.isEmaxOverridden()) {
                df.applyPattern("#0.0");
                profile.setShortDescription("Emax overridden");
                //Denote overridden Emax using asterics
                emax = " (" + df.format(profile.getOverriddendEmaxValue() * 100)
                        + "%<-- " + df.format(profile.getEmax() * 100) + "%)";
            } else {
                df.applyPattern("#0.0");
                profile.setShortDescription("");
                emax = " (" + df.format(profile.getEmax() * 100) + "%) ";
            }
            if (profile.isExcluded()) {
                return label + emax + no + " EXCLUDED";
            } else {
                return label + emax + no;
            }
        }
        return "";
    }
}
