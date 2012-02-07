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

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.GeneralUtilities;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Only one Profile can be excluded at one time.
 *
 * @author Bob Rutledge
 */
class ExcludeSampleProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;

    public ExcludeSampleProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Exclude Sample Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] selectedNodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) selectedNodes[0];
        db = selectedNode.getDatabaseServices();
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
            }
        } else {
            return;
        }
        for (Node node : selectedNodes) {
            selectedNode = (LreNode) node;
            SampleProfile selectedProfile = selectedNode.getLookup().lookup(SampleProfile.class);
            AverageSampleProfile parentAvProfile = (AverageSampleProfile) selectedProfile.getParent();
            List<SampleProfile> repProfileList = parentAvProfile.getReplicateProfileList();

            //Need to confirm that at least one Profile will remain active
            if (parentAvProfile.numberOfActiveReplicateProfiles() == 1) {//Only one Profile active
                String msg = "It appears that there is only one Profile that is active "
                        + "and thus cannot be excluded. Exclude the average profile instead";
                JOptionPane.showMessageDialog(
                        WindowManager.getDefault().getMainWindow(),
                        msg,
                        "Unable to exclude the "
                        + "selected Profile", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            selectedProfile.setExcluded(true);
            selectedNode.refreshNodeLabel();
            db.saveObject(selectedProfile);

            //Update the parent Average Sample Profile
            LreNode parentNode = (LreNode) selectedNodes[0].getParentNode();
            parentAvProfile.setRawFcReadings(GeneralUtilities.generateAverageFcDataset(repProfileList));
            parentAvProfile.setFcReadings(null);//This will trigger a new Fc dataset to be generated from the raw Fc dataset
            //Reinitialize the Average Profile
            LreAnalysisService profileIntialization =
                    Lookup.getDefault().lookup(LreAnalysisService.class);
            //Conduct automated LRE window selection
            profileIntialization.conductAutomatedLreWindowSelection(parentAvProfile, selectionParameters);
            db.saveObject(parentAvProfile);
            //Update the tree
            parentNode.refreshNodeLabel();
            LreObjectChildren parentChildren = (LreObjectChildren) parentNode.getChildren();
            parentChildren.setLreObjectList(repProfileList);
            parentChildren.addNotify();
        }
        db.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_EXCLUDED);
    }
}
