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
import javax.swing.AbstractAction;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 * Only one Profile can be excluded at one time.
 *
 * @author Bob Rutledge
 */
class IncludeAverageSampleProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public IncludeAverageSampleProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Include Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] avSampleProfileNodeList = mgr.getSelectedNodes();
        LreNode avSampleProfileNode = (LreNode) avSampleProfileNodeList[0];
        db = avSampleProfileNode.getDatabaseServices();
        for (Node node : avSampleProfileNodeList) {
            avSampleProfileNode = (LreNode) node;
            AverageSampleProfile avSampleProfile = (AverageSampleProfile) avSampleProfileNode.getLookup().lookup(SampleProfile.class);
            avSampleProfile.setExcluded(false);
            db.saveObject(avSampleProfile);
            //Include all replicate profiles
            for (SampleProfile prf : avSampleProfile.getReplicateProfileList()) {
                //This forces it's Run to recalculate average Fmax
                prf.setExcluded(false);
                db.saveObject(prf);
            }
            db.saveObject(avSampleProfile.getRun());
             //Determine if the parent node is a Run node
            if (avSampleProfileNode.getParentNode().getLookup().lookup(Run.class) != null) {
                //Refresh the Run label to update the average Fmax
                LreNode runNode = (LreNode) avSampleProfileNode.getParentNode();
                runNode.refreshNodeLabel();
            }
            avSampleProfileNode.refreshNodeLabel();
            //Refresh the replicate profile node labels
            Node[] replNodes = avSampleProfileNode.getChildren().getNodes();
            for (int i = 0; i < replNodes.length; i++) {
                LreNode n = (LreNode) replNodes[i];
                n.refreshNodeLabel();
            }
        }
        db.commitChanges();
        //Update the Calibration panels
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_INCLUDED);
    }
}
