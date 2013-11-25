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
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Deletes the selection AverageCalibrationProfiles
 *
 * @author Bob Rutledge
 */
public class DeleteAverageCalibrationProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;
    private LreWindowSelectionParameters selectionParameters;

    public DeleteAverageCalibrationProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Delete Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        List<AverageProfile> avPrfList = db.getAllObjects(AverageProfile.class);
        if (avPrfList.size()<2){
            String msg = "It appears that the database contains only one\n"
                    + " Average Calibration Profile\n"
                    + "...Please delete the Run instead";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg, 
                    "Cannot delete this Average Profile",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (db != null) {
            if (db.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
            }
        }
        if (nodes.length == 1) {
            AverageCalibrationProfile profile = nodes[0].getLookup().lookup(AverageCalibrationProfile.class);
            String msg = "Are you sure you want to delete '" + profile.getName()
                    + "'?\n" + "This will permenantly remove this Calibration Profile. ";
            int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Calibration Profile: " + profile.getName(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                deleteProfile(profile);
            }
        } else {
            String msg = "Are you sure you want to delete " + String.valueOf(nodes.length)
                    + " Profiles?"
                    + "'?\n" + "This will permenantly remove these Calibration Profiles.";
            int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete  Calibration Profiles",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                for (Node node : nodes) {
                    AverageCalibrationProfile profile = node.getLookup().lookup(AverageCalibrationProfile.class);
                    deleteProfile(profile);
                }
            }
        }
        //Update the tree
        LreNode parentNode = (LreNode) nodes[0].getParentNode();
        parentNode.refreshNodeLabel();
        LreObjectChildren parentChildren = (LreObjectChildren) parentNode.getChildren();
        LreObject parentLreObject = parentNode.getLookup().lookup(LreObject.class);
        parentChildren.setLreObjectList((List<? extends LreObject>) db.getChildren(parentLreObject, parentLreObject.getChildClass()));
        parentChildren.addNotify();
        //Trigger Calibration panel update
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_DELETED);
    }

    private void deleteProfile(AverageCalibrationProfile avCalPrf) {
        //Need to remove the AverageProfile from the Run Profile list
        Run run = avCalPrf.getRun();
        List<AverageProfile> avCalPrfList = run.getAverageProfileList();
        avCalPrfList.remove(avCalPrf);
        db.saveObject(avCalPrfList);
        db.deleteObject(avCalPrf);
        db.saveObject(run);
        db.commitChanges();
    }
}
