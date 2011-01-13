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

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.GeneralUtilities;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.LreObject;
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
public class DeleteCalibrationProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public DeleteCalibrationProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Delete Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        if (nodes.length == 1) {
            Profile profile = nodes[0].getLookup().lookup(Profile.class);
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
                    Profile profile = node.getLookup().lookup(Profile.class);
                    deleteProfile(profile);
                }
            }
        }
        //Update the tree
        LreNode parentNode = (LreNode) nodes[0].getParentNode();
        parentNode.refreshNodeLabel();
        LreObjectChildren parentChildren = (LreObjectChildren) parentNode.getChildren();
        LreObject parentLreObject = parentNode.getLookup().lookup(LreObject.class);
        if (parentLreObject != null) {
            parentChildren.setLreObjectList((List<? extends LreObject>) db.getChildren(parentLreObject, parentLreObject.getChildClass()));
        }
        parentChildren.addNotify();
        //Trigger Calibration panel update
        UniversalLookup.getDefault().add(PanelMessages.UPDATE_CALIBRATION_PANELS, null);
    }

    private void deleteProfile(Profile profile) {

        if (profile instanceof AverageCalibrationProfile) {
            AverageCalibrationProfile prf = (AverageCalibrationProfile) profile;
            List<CalibrationProfile> lambdaProfileList = prf.getReplicateProfileList();
            //Delete all of the replicate profiles
            for (CalibrationProfile cp : lambdaProfileList) {
                db.deleteObject(cp);
            }
            //likely not needed
            db.deleteObject(lambdaProfileList);
            //Remove the average profile
            db.deleteObject(prf);
            db.commitChanges();
            return;
        }
        if (profile instanceof CalibrationProfile) {
//Need to remove the Profile from the AverageCalibrationProfile profile list
            CalibrationProfile calibrationProfile = (CalibrationProfile) profile;
            AverageCalibrationProfile avProfile =
                    (AverageCalibrationProfile) calibrationProfile.getParent();
            List<CalibrationProfile> calibrationPrfList = avProfile.getReplicateProfileList();
            //Test to sure that at least one Sample Profile will remain
// TODO present an error dialog to indicate that the last calibration profile cannot be deleted
            if (calibrationPrfList.size() < 2) {
                return;
            }
            calibrationPrfList.remove(calibrationProfile);
            db.saveObject(calibrationPrfList);
            //Need to recalculate the average Fc dataset in the AverageSample Profile
            avProfile.setFcReadings(null);//Fb will need to be recalculated
            avProfile.setRawFcReadings(GeneralUtilities.generateAverageFcDataset(calibrationPrfList));
            //Reinitialize the Average Profile
            LreAnalysisService profileIntialization =
                    Lookup.getDefault().lookup(LreAnalysisService.class);
            profileIntialization.initializeProfile(avProfile);
            db.deleteObject(profile);
            db.commitChanges();
        }
    }
}
