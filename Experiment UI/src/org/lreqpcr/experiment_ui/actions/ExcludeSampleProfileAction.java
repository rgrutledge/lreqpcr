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
package org.lreqpcr.experiment_ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.lreqpcr.core.utilities.ProfileUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
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
        Node[] sampleProfileNodes = mgr.getSelectedNodes();
        LreNode sampleProfileLreNode = (LreNode) sampleProfileNodes[0];
        db = sampleProfileLreNode.getDatabaseServices();
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
            }
        } else {
            return;
        }
        for (Node node : sampleProfileNodes) {
            sampleProfileLreNode = (LreNode) node;
            SampleProfile sampleProfile = sampleProfileLreNode.getLookup().lookup(SampleProfile.class);
            AverageSampleProfile parentAvProfile = (AverageSampleProfile) sampleProfile.getParent();
            List<SampleProfile> repProfileList = parentAvProfile.getReplicateProfileList();

            //Need to confirm that at least one Profile will remain active
            if (parentAvProfile.getTheNumberOfActiveReplicateProfiles() == 1) {//Only one Profile active
                String msg = "It appears that there is only one Replicate Profile that is active "
                        + "and thus cannot be excluded.\n\n"
                        + "                                           Exclude the Average Profile instead";
                JOptionPane.showMessageDialog(
                        WindowManager.getDefault().getMainWindow(),
                        msg,
                        "Unable to exclude the "
                        + "selected Replicate Profile", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            //This will force the profile's Run to recalculate its average Fmax
            sampleProfile.setExcluded(true);
            //The average Tm must be updated in the parent AverageSampleProfile
            parentAvProfile.calculateAvAmpTm();
            sampleProfileLreNode.refreshNodeLabel();
            db.saveObject(sampleProfile);

            //Update the parent Average Sample Profile
            parentAvProfile.setRawFcReadings(ProfileUtilities.generateAverageFcDataset(repProfileList));
            parentAvProfile.setFcReadings(null);//This is necessary
            //Reinitialize the Average Profile
            //Need to determine if this is a valid average profile
            if (parentAvProfile.areTheRepProfilesSufficientlyClustered()
                    && !parentAvProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                LreAnalysisService lreAnalysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
                //Reinitialize the LRE window
                ProfileSummary prfSum = new ProfileSummary(parentAvProfile, db);
                lreAnalysisService.lreWindowInitialization(prfSum, selectionParameters);
                //Apply nonlinear regression optimization
                lreAnalysisService.optimizeLreWindowUsingNonlinearRegression(prfSum, selectionParameters);
            }
            //Update the tree
            //Test to see if the parent node is a run node used in the well view
            if (sampleProfileLreNode.getParentNode().getLookup().lookup(Run.class) == null) {
                //Must be an average profile node
                LreNode avSampleProfileLreNode = (LreNode) sampleProfileNodes[0].getParentNode();
                avSampleProfileLreNode.refreshNodeLabel();
                //Determine if the AverageSample parent node is a Run node
                if (avSampleProfileLreNode.getParentNode().getLookup().lookup(Run.class) != null) {
                    LreNode runLreNode = (LreNode) avSampleProfileLreNode.getParentNode();
                    runLreNode.refreshNodeLabel();
                    db.saveObject(sampleProfile.getRun());
                }
                LreObjectChildren parentChildren = (LreObjectChildren) avSampleProfileLreNode.getChildren();
                parentChildren.setLreObjectList(repProfileList);
                parentChildren.addNotify();
            }//Else do nothing
        }
        db.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_EXCLUDED);
    }
}
