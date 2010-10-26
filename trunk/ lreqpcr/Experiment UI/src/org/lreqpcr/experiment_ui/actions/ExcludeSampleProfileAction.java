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
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Only one Profile can be excluded at one time.
 *
 * @author Bob Rutledge
 */
class ExcludeSampleProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public ExcludeSampleProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Exclude Sample Profile");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        SampleProfile selectedProfile = selectedNode.getLookup().lookup(SampleProfile.class);
        AverageSampleProfile parentAvProfile = (AverageSampleProfile) selectedProfile.getParent();
        ArrayList<SampleProfile> profileList = parentAvProfile.getReplicateProfileList();

        //Need to confirm that at least one Profile will remain active
        int numberOfActiveProfiles = 0;
        for (SampleProfile profile: profileList) {
            if (!profile.isExcluded()){
                numberOfActiveProfiles++;
            }
        }
        if (numberOfActiveProfiles <2){//Only one Profile active
            String msg = "It appears that there is only one Profile that is active " +
                    "and thus cannot be excluded. Exclude the average profile instead";
            JOptionPane.showMessageDialog(null, msg, "Unable to exclude the " +
                    "selected Profile", JOptionPane.ERROR_MESSAGE);
            return;
        }

        db = selectedNode.getDatabaseServices();
        selectedProfile.setExcluded(true);
        selectedNode.refreshNodeLabel();
        db.saveObject(selectedProfile);

        //Update the parent Average Sample Profile
        LreNode parentNode = (LreNode) nodes[0].getParentNode();
        parentAvProfile.setFcReadings(null);//Fb will need to be recalculated
        parentAvProfile.setRawFcReadings(GeneralUtilities.
                generateAverageFcDataset(profileList));
        //Reinitialize the Average Profile
        LreAnalysisService profileIntialization =
                Lookup.getDefault().lookup(LreAnalysisService.class);
        profileIntialization.initializeProfile(parentAvProfile);
        db.saveObject(parentAvProfile);
        db.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_EXCLUDED);

        //Update the tree
        parentNode.refreshNodeLabel();
        LreObjectChildren parentChildren = (LreObjectChildren) parentNode.getChildren();
        parentChildren.setLreObjectList(profileList);
        parentChildren.addNotify();
    }
}