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
public class ReturnRunProfilesEmaxToLreDerivedEmaxAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;
    private LreAnalysisService analysisService;

    public ReturnRunProfilesEmaxToLreDerivedEmaxAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Return all Run Profiles to LRE-derived Emax");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        //Retrieve the database holding the profiles
        //Note that this must be a Run node
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
                    //Process the replicate profiles
                    for (Profile repProfile : avProfile.getReplicateProfileList()) {
                        //Ignore profiles that do not have an LRE window
                        if (repProfile.hasAnLreWindowBeenFound()) {
                            repProfile.setIsEmaxOverridden(false);
                            repProfile.setOverridentEmaxValue(0);
                            analysisService.initializeProfileSummary(repProfile, selectionParameters);
                            db.saveObject(repProfile);
                        }
                    }
                    //Ignore average sample profiles that do not have an LRE window
                    if (avProfile.hasAnLreWindowBeenFound()) {
                        avProfile.setIsEmaxOverridden(false);
                        avProfile.setOverridentEmaxValue(0);
                        //Need to update avFo and avNo
                        analysisService.initializeProfileSummary(avProfile, selectionParameters);
                    }else{
                        //Need to update the average profile as it is likely <10 molecule profile
                        avProfile.updateProfile();
                    }
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
