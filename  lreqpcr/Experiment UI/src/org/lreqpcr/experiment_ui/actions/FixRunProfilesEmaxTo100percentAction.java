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
package org.lreqpcr.experiment_ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Bob Rutledge
 */
public class FixRunProfilesEmaxTo100percentAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;
    private LreAnalysisService analysisService;

    public FixRunProfilesEmaxTo100percentAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Fix all Run Profiles' Emax to 100%");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        //Retrieve the database holding the profiles
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
                selectionParameters = l.get(0);
            } else {
                return;
            }
            for (Node n : nodes) {
                //Retrieve the Run
                LreNode node = (LreNode) n;
                Run run = node.getLookup().lookup(Run.class);
//Process all profiles within the run, including the replicate profiles
                for (AverageSampleProfile avProfile : run.getAverageProfileList()) {
                    avProfile.setIsEmaxFixedTo100(true);//
                    //Process the replicate profiles
                    for (Profile repProfile : avProfile.getReplicateProfileList()) {
                        //Ignore profiles that do not have an LRE window
                        if (repProfile.hasAnLreWindowBeenFound()) {
                            repProfile.setIsEmaxFixedTo100(true);
                            analysisService.initializeProfileSummary(repProfile, selectionParameters);
                            db.saveObject(repProfile);
                        }
                    }
                    //Must test to see if replicate profile modifications brings
                    //the average replicate No >10 as indicated by lacking an LRE
                    //window. If so then an automated LRE window
                    //selection must be conducted on the average profile.
                    if (!avProfile.determineIfTheAverageReplicateNoIsLessThan10Molecules() && !avProfile.hasAnLreWindowBeenFound()) {
                        //Must conduct an automated LRE window selection
                        analysisService.conductAutomatedLreWindowSelection(avProfile, selectionParameters);
                    }
                    //Ignore average sample profiles that do not have an LRE window
                    if (!avProfile.isReplicateAverageNoLessThan10Molecules()) {
                        //Need to update avFo and avNo
                        analysisService.initializeProfileSummary(avProfile, selectionParameters);
                    }//When <10N the averageProfile inherits the average replicate profile No so no need to update
                    db.saveObject(avProfile);
                }
            }
        } else {
            return;
        }
        db.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_CHANGED);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
    }
}