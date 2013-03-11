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
public class ReturnAllCalibrationProfileEmaxToLreAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;
    private LreAnalysisService analysisService;

    public ReturnAllCalibrationProfileEmaxToLreAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Return all Profiles to LRE-derived Emax");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        if (db != null) {
            List<AverageCalibrationProfile> avProfileList;
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
                 avProfileList = db.getAllObjects(AverageCalibrationProfile.class);
            } else {
                //This should never happen
                return;
            }
            for (AverageCalibrationProfile avProfile : avProfileList) {
                avProfile.setIsEmaxFixedTo100(false);
                analysisService.initializeProfileSummary(avProfile, selectionParameters);
                db.saveObject(avProfile);
                for (CalibrationProfile profile : avProfile.getReplicateProfileList()) {
                    profile.setIsEmaxFixedTo100(false);
                    analysisService.initializeProfileSummary(profile, selectionParameters);
                    db.saveObject(avProfile);
                }
            }
            db.commitChanges();
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
        }
    }
}