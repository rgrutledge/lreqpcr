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

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.ProfileUtilities;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Only one Profile can be excluded at one time.
 *
 * @author Bob Rutledge
 */
class IncludeCalibrationProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;

    public IncludeCalibrationProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Include Calibration Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        db = selectedNode.getDatabaseServices();
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
            } else {
                return;
            }
        }
        for (Node node : nodes) {
            selectedNode = (LreNode) node;
            CalibrationProfile selectedProfile = selectedNode.getLookup().lookup(CalibrationProfile.class);
            AverageCalibrationProfile parentAvProfile = (AverageCalibrationProfile) selectedProfile.getParent();
            List<CalibrationProfile> profileList = parentAvProfile.getReplicateProfileList();

            selectedProfile.setExcluded(false);
            selectedNode.refreshNodeLabel();
            db.saveObject(selectedProfile);

            //Update the parent Average Sample Profile
            parentAvProfile.setRawFcReadings(ProfileUtilities.generateAverageFcDataset(profileList));
            parentAvProfile.setFcReadings(null);//This is necessary

            //Reinitialize the LRE window
            //Need to determine if this is a valid average profile
            if (parentAvProfile.areTheRepProfilesSufficientlyClustered()
                    && !parentAvProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                LreAnalysisService lreAnalysisService =
                        Lookup.getDefault().lookup(LreAnalysisService.class);
                ProfileSummary prfSum = new ProfileSummaryImp(parentAvProfile, db);
                lreAnalysisService.lreWindowInitialization(prfSum, selectionParameters);
                //Apply nonlinear regression to optimize the LRE window
                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, selectionParameters);
            }

            //Update the tree
            LreNode parentNode = (LreNode) nodes[0].getParentNode();
            parentNode.refreshNodeLabel();
            LreObjectChildren parentChildren = (LreObjectChildren) parentNode.getChildren();
            parentChildren.setLreObjectList(profileList);
            parentChildren.addNotify();
        }
        db.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_INCLUDED);
    }
}
