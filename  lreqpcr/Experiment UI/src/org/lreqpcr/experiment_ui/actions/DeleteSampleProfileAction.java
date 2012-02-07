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
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.utilities.GeneralUtilities;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
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
 *
 * @author Bob Rutledge
 */
public class DeleteSampleProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;

    public DeleteSampleProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Delete Sample Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
            }
        }
        if (nodes.length == 1) {
            SampleProfile profile = nodes[0].getLookup().lookup(SampleProfile.class);
            String msg = "Are you sure you want to delete '" + profile.getName() +
                    "'?\nThis will permenantly remove this profile.";
             int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete a Replicate Sample Profile: " + profile.getName(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                deleteProfile(profile);
            }
        } else {
            String msg = "Are you sure you want to delete " + String.valueOf(nodes.length) +
                    " Replicate Sample Profiles?\n This will permantly remove these profiles.";
             int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Replicate Sample Profiles",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                for (Node node : nodes) {
                    SampleProfile profile = node.getLookup().lookup(SampleProfile.class);
                    deleteProfile(profile);
                }
            }
        }
        //Update the tree
        LreNode parentNode = (LreNode) nodes[0].getParentNode();
        parentNode.refreshNodeLabel();
        LreObjectChildren parentChildren = (LreObjectChildren) parentNode.getChildren();
        LreObject parentLreObject = parentNode.getLookup().lookup(LreObject.class);
        parentChildren.setLreObjectList((List<? extends LreObject>)
                db.getChildren(parentLreObject, parentLreObject.getChildClass()));
        parentChildren.addNotify();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_CHANGED);
    }

    @SuppressWarnings("unchecked")
    private void deleteProfile(SampleProfile sampleProfile) {

//Need to remove the Profile from the AverageProfile replicate profile list
            AverageSampleProfile avProfile =
                    (AverageSampleProfile) sampleProfile.getParent();
            List<SampleProfile> samplePrfList = avProfile.getReplicateProfileList();
            //Test to sure that at least one Sample Profile will remain
            if (samplePrfList.size() < 2) {
                String msg = "There appears to be only one remaining replicate Profile, " +
                        "which cannot be deleted.\nDelete the corresponding average " +
                        "Profile instead.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        msg,
                        "Cannot delete the last remaining replicate Profile",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            samplePrfList.remove(sampleProfile);
            db.saveObject(samplePrfList);
            //Need to recalculate the average Fc dataset in the AverageSample Profile
            avProfile.setFcReadings(null);//Fb will need to be recalculated
            avProfile.setRawFcReadings(GeneralUtilities.generateAverageFcDataset(samplePrfList));
            //Reinitialize the Average Profile
            LreAnalysisService profileIntialization =
                    Lookup.getDefault().lookup(LreAnalysisService.class);
            profileIntialization.initializeProfileSummary(avProfile, selectionParameters);
            db.saveObject(avProfile);
            db.deleteObject(sampleProfile);

            db.commitChanges();
    }
}
