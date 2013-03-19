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

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
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
public class FixAvCaibnPrfEmaxTo100PercentAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;
    private LreAnalysisService analysisService;

    public FixAvCaibnPrfEmaxTo100PercentAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Fix Emax to 100%");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation, 
//except for very old database versions
                selectionParameters = l.get(0);
            } else {
                //This should never happen
                return;
            }
            for (Node n : nodes) {
                LreNode node = (LreNode) n;
                //Only average profiles have access to this action and thus this must be an average calibration profile
                AverageCalibrationProfile avCalbnPrf = node.getLookup().lookup(AverageCalibrationProfile.class);
                avCalbnPrf.setIsEmaxFixedTo100(true);
                analysisService.initializeProfileSummary(avCalbnPrf, selectionParameters);
                db.saveObject(avCalbnPrf);
                node.refreshNodeLabel();
                //Get the replicate profile nodes
                Node[] replicateNodes = node.getChildren().getNodes();
                //Set all to Emax fixed to 100% and process them
                for (Node n2 : replicateNodes) {
                    LreNode node2 = (LreNode) n2;
                    CalibrationProfile calbnPrf = node2.getLookup().lookup(CalibrationProfile.class);
                    calbnPrf.setIsEmaxFixedTo100(true);
                    analysisService.initializeProfileSummary(calbnPrf, selectionParameters);
                    db.saveObject(calbnPrf);
                    node2.refreshNodeLabel();
                }
            }//End of node for loop
        }
        db.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_CHANGED);
    }
}
