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
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.RunImpl;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Deletes selected Runs
 * @author Bob Rutledge
 */
public class DeleteRunAction extends AbstractAction {

    private ExplorerManager mgr;

    public DeleteRunAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Delete Run(s)");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent arg0) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode lreNode = (LreNode) nodes[0];
        DatabaseServices db = lreNode.getDatabaseServices();
        LreNode parentNode = (LreNode) lreNode.getParentNode();
        if (nodes.length == 1) {
            LreObject o = lreNode.getLookup().lookup(LreObject.class);
            Run run = (Run) o;
            String msg = "Are you sure you want to delete the '" + run.getName() + "' Run?";
            int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Run",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                db.deleteObject(run);
                db.commitChanges();
            }
        } else {
            String msg = "Are you sure you want to delete " + String.valueOf(nodes.length)
                    + " Runs?";
            int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Runs",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_WAIT_CURSOR);
                for (Node node : nodes) {
                    LreObject o = node.getLookup().lookup(LreObject.class);
                    Run run = (Run) o;
                    db.deleteObject(run);
                    db.commitChanges();
                }
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_DEFAULT_CURSOR);
            }
        }
        //Reset the tree
        LreObjectChildren children = (LreObjectChildren) parentNode.getChildren();
        children.setLreObjectList((List<? extends LreObject>) db.getAllObjects(RunImpl.class));
        children.addNotify();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.RUN_DELETED);
    }
}
