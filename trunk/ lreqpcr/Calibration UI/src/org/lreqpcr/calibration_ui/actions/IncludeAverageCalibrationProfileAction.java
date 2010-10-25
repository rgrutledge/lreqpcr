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
package org.lreqpcr.calibration_ui.actions;

import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.ui_elements.LreNode;
import java.awt.event.ActionEvent;
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
class IncludeAverageCalibrationProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public IncludeAverageCalibrationProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Include Calibration Profile");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        db = selectedNode.getDatabaseServices();
        AverageCalibrationProfile selectedProfile = (AverageCalibrationProfile) selectedNode.getLookup().lookup(CalibrationProfile.class);
        selectedProfile.setExcluded(false);
        db.saveObject(selectedProfile);
        //Include all replicate profiles
        for(CalibrationProfile prf : selectedProfile.getReplicateProfileList()){
            prf.setExcluded(false);
            db.saveObject(prf);
        }
        selectedNode.refreshNodeLabel();
        //Refresh the replicate profile node labels
        Node[] replNodes = selectedNode.getChildren().getNodes();
        for(int i=0; i<replNodes.length; i++){
            LreNode n = (LreNode) replNodes[i];
            n.refreshNodeLabel();
        }
        db.commitChanges();
        //Update the Calibration panels
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_INCLUDED);
    }
}
