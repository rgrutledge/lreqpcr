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
package org.lreqpcr.calibration_ui.actions;

import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.ui_elements.LreNode;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 * Only one Profile can be excluded at one time.
 *
 * @author Bob Rutledge
 */
class ExcludeAverageCalibrationProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public ExcludeAverageCalibrationProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Exclude Calibration Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        db = selectedNode.getDatabaseServices();
        for (Node node : nodes) {
            selectedNode = (LreNode) node;
            AverageCalibrationProfile selectedProfile = (AverageCalibrationProfile) selectedNode.getLookup().lookup(CalibrationProfile.class);
            selectedProfile.setExcluded(true);
            db.saveObject(selectedProfile);
            //Exclude all replicate profiles
            List l = selectedProfile.getReplicateProfileList();
            for (CalibrationProfile prf : selectedProfile.getReplicateProfileList()) {
                prf.setExcluded(true);
                db.saveObject(prf);
            }
            selectedNode.refreshNodeLabel();
            //Refresh the replicate profile node labels
            Node[] replNodes = selectedNode.getChildren().getNodes();
            for (int i = 0; i < replNodes.length; i++) {
                LreNode n = (LreNode) replNodes[i];
                n.refreshNodeLabel();
            }
        }

        db.commitChanges();
        //Update the Calibration panels
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_EXCLUDED);
    }
}
