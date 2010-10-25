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

package org.lreqpcr.amplicon_ui.actions;

import org.lreqpcr.core.data_objects.AmpliconImpl;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class DeleteAmpliconAction extends AbstractAction {

    private AmpliconImpl amplicon;
    private ExplorerManager mgr;//Defines the tree upon which these actions will be applied

    public DeleteAmpliconAction(ExplorerManager mgr) {
        putValue(NAME, "Delete Amplicon(s)");
        this.mgr = mgr;
    }

    /**
     * Assumes that an Amplicon database has been opened. If more than one Amplicon 
     * database is open, the first database is used. 
     * 
     * @param e the ActionEvent
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        //Must assume that an amplicon database has been opened if this action has been called
        //This is based on the assumption that only one amplicon database is open at one time
        //This will have to be restructured when multiple database files will be open
        DatabaseServices ampliconDB =
                (DatabaseServices) UniversalLookup.getDefault().getAll(DatabaseType.AMPLICON).get(0);
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        LreNode rootNode = (LreNode) mgr.getRootContext();
        LreObjectChildren children = (LreObjectChildren) rootNode.getChildren();
        if (nodes.length == 1) {
            amplicon = selectedNode.getLookup().lookup(AmpliconImpl.class);
            String msg = "Are you sure you want to delete '" + amplicon.getName() +
                    "'?";
             int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Amplicon",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                ampliconDB.deleteObject(amplicon);
                ampliconDB.commitChanges();
            }
        } else {
            String msg = "Are you sure you want to delete " + String.valueOf(nodes.length) +
                    " Amplicons?";
             int n = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Delete Amplicons",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                for (Node node : nodes) {
                    amplicon = node.getLookup().lookup(AmpliconImpl.class);
                    ampliconDB.deleteObject(amplicon);
                    ampliconDB.commitChanges();
                }
            }
        }
        //Update the tree
        children.setLreObjectList((List<? extends LreObject>) ampliconDB.getAllObjects(AmpliconImpl.class));
        //Refresh the tree, moving the selection to the node below the deleted node
        children.addNotify();
    }
}
