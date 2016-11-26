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
package org.lreqpcr.experiment_ui.components;

import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Displays AvergeSampleProifles with SamplProfiles as children.
 *
 * @author Bob Rutledge
 */
public class SampleProfileNodesSortedByWell extends LreObjectChildren {

    public SampleProfileNodesSortedByWell(ExplorerManager mgr, DatabaseServices db, List<? extends SampleProfile> sampleProfileList, LreActionFactory actionFactory, LabelFactory labelFactory) {
        super(mgr, db, sampleProfileList, actionFactory, labelFactory);
        setCustomComparator(new Comparator<SampleProfile>() {
            public int compare(SampleProfile prf1, SampleProfile prf2) {
                if (prf1.getWellNumber() > prf2.getWellNumber()) {
                    return 1;
                } else {
                    if (prf1.getWellNumber() < prf2.getWellNumber()) {
                        return -1;
                    }
                }
                return 0;
            }
        });
    }

    /**
     * Creates SampleProfile nodes from the member list of
     * SampleProfileNodesSortedByWell.
     *
     * @param lreObject
     * @return
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected Node[] createNodes(LreObject lreObject) {
        SampleProfile sampleProfile = (SampleProfile) lreObject;
        if (nodeActionFactory == null) {
            actions = new Action[]{};//i.e. no Actions have been set
        } else {
            actions = nodeActionFactory.getActions(lreObject.getClass().getSimpleName());
            if (actions == null) {
                actions = new Action[]{};//i.e. there are no Actions for this Node
            }
        }
        LreNode node = new LreNode(Children.LEAF, Lookups.singleton(sampleProfile), actions);
        node.setExplorerManager(mgr);
        node.setDatabaseService(db);
        node.setName(lreObject.getName());
        if (nodeLabelFactory == null) {
            node.setDisplayName("");
        } else {
            node.setDisplayName(nodeLabelFactory.getNodeLabel(lreObject));
            node.setShortDescription(lreObject.getShortDescription());
        }
        return new Node[]{node};
    }
}
