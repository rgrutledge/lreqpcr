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
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Delete the selected AverageSampleProfiles
 * @author Bob Rutledge
 */
public class DeleteAverageSampleProfileAction extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public DeleteAverageSampleProfileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Delete Average Sample Profile(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        db = lreNode.getDatabaseServices();
        if (nodes.length == 1) {
            AverageSampleProfile avSampleProfile = nodes[0].getLookup().lookup(AverageSampleProfile.class);
            String msg = "Are you sure you want to delete '" + avSampleProfile.getName()
                    + "'?\n" + "This will permenantly remove this Average Sample Profile. ";
            int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Average Sample Profile",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                deleteProfile(avSampleProfile);
            }
        } else {
            String msg = "Are you sure you want to delete " + String.valueOf(nodes.length)
                    + " profiles?\nThis will permantly remove these Average Sample Profiles. ";
            int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Average Sample Profiles",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_WAIT_CURSOR);
                for (Node node : nodes) {
                    AverageSampleProfile profile = node.getLookup().lookup(AverageSampleProfile.class);
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
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_DELETED);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_DEFAULT_CURSOR);
    }

    @SuppressWarnings("unchecked")
    private void deleteProfile(AverageSampleProfile avSampleProfile) {

        //Need to remove the AverageProfile from the Run Profile list
        Run run = avSampleProfile.getRun();
        List<AverageProfile> avSamplePrfList = run.getAverageProfileList();
        avSamplePrfList.remove(avSampleProfile);
        db.saveObject(avSamplePrfList);
        db.deleteObject(avSampleProfile);
        //Update the Run's average Fmax and save it
        run.calculateAverageFmax();
        db.saveObject(run);

        db.commitChanges();
    }
}
