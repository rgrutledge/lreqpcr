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
package org.lreqpcr.core.ui_elements;

import org.lreqpcr.core.data_objects.LreObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Basic Children implementation for LRE objects, which provides pointers to
 * both the corresponding Explorer manager and the database holding the 
 * LRE object, along with generating
 * node labels and actions via factories. This greatly simplifies generating
 * labels and actions for diverse types of LRE objects.
 * @author Bob Rutledge
 */
public class LreObjectChildren extends Children.Keys<LreObject> {

    public ExplorerManager mgr;//Defines the tree in which the node is held
    public DatabaseServices db;//Defines the database in which LreObjects are stored
    private List<? extends LreObject> lreObjectList;//Children of lreObject
    public LreActionFactory nodeActionFactory;
    public LabelFactory nodeLabelFactory;
    public Action[] actions;
    private Comparator customComparator;//For sorting of the children nodes

    /**
     * Generates children nodes containing the provided List of LRE objects.
     * 
     * @param mgr the manager of the view
     * @param db the database that is being viewed
     * @param childrenObjects the list holding the children objects
     * @param actionFactory the node action factory
     * @param labelFactory the node label factory
     */
    public LreObjectChildren(ExplorerManager mgr, DatabaseServices db, List<? extends LreObject> childrenObjects,
            LreActionFactory actionFactory, LabelFactory labelFactory) {
        this.mgr = mgr;
        this.db = db;
        lreObjectList = childrenObjects;
        nodeActionFactory = actionFactory;
        this.nodeLabelFactory = labelFactory;
    }

    /**
     * Generates children nodes containing the children LRE objects
     * of a parent LRE object. The children LRE objects are retrieved from the
     * database being viewed. However, this can dramatically reduce perform
     * when the database is large.
     * 
     * @param mgr the manager of the view
     * @param db the database that is being viewed
     * @param partentLreObject the parent LRE object
     * @param actionFactory the node action factory
     * @param labelFactory the node label factory
     */
    @SuppressWarnings(value = "unchecked")
    public LreObjectChildren(ExplorerManager mgr, DatabaseServices db, LreObject partentLreObject,
            LreActionFactory actionFactory, LabelFactory labelFactory) {
        this.mgr = mgr;
        this.db = db;
        nodeActionFactory = actionFactory;
//      This search can seriously reduce perform when the database is large
        lreObjectList = (List<? extends LreObject>) db.getChildren(partentLreObject, partentLreObject.getChildClass());
        nodeLabelFactory = labelFactory;
    }

    /**
     * Changes the child LRE objects of this parent LRE Node, in order to allow
     * a refresh after addition or deletion of a child Node
     *
     * @param lreObjectList list of the children objects
     */
    public void setLreObjectList(List<? extends LreObject> lreObjectList) {
        this.lreObjectList = lreObjectList;
    }

    /**
     * Updates the label of the specified Node
     * 
     * @param node the Node to refresh
     */
    @SuppressWarnings(value = "unchecked")
    public void refreshNodeLabel(Node node) {
        LreObject modifiedMember = node.getLookup().lookup(LreObject.class);
        node.setDisplayName(nodeLabelFactory.getNodeLabel(modifiedMember));
        node.setShortDescription(modifiedMember.getShortDescription());
    }

    public void refreshChildrenLabels(){

    }

    /**
     * Calling this should update display of the children nodes. 
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    public void addNotify() {
        if (!db.isDatabaseOpen() || lreObjectList == null) {
            return;
        }
        //This is necessary because DB4O lists cannot be sorted via Collections.sort
        ArrayList<LreObject> lreObjectArray = new ArrayList<LreObject>(lreObjectList);
        if (customComparator == null) {
            Collections.sort(lreObjectArray);
        } else {
            Collections.sort(lreObjectArray, customComparator);
        }
        setKeys(lreObjectArray);
    }

    /**
     * Releases the children nodes, allowing garbage collection.
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    /**
     * Provides a list of the child LRE objects.
     * @return
     */
    public List<? extends LreObject> getChildList() {
        return lreObjectList;
    }

    /**
     * Provides the comparator used to sort the children nodes.
     * 
     * @return the comparator
     */
    public Comparator getCustomComparator() {
        return customComparator;
    }

    /**
     * Replaces the comparator used for sorting the child nodes.
     * 
     * @param customComparator the new comparator
     */
    public void setCustomComparator(Comparator customComparator) {
        this.customComparator = customComparator;
    }

    /**
     * Creates LRE nodes with children of the supplied LRE object, is any exist, 
     * along with with the corresponding node labels, actions (if any exist) and 
     * the corresponding Explorer manager and database which is being viewed. 
     * 
     * 
     * @param lreObject
     * @return
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected Node[] createNodes(LreObject lreObject) {
        if (nodeActionFactory == null) {
            actions = new Action[]{};//i.e. no Actions have been set
        } else {
            actions = nodeActionFactory.getActions(lreObject.getClass().getSimpleName());
            if (actions == null) {
                actions = new Action[]{};//i.e. there are no Actions for this Node
            }
        }
        //Test if this parent has children
        LreNode node = null;
        if (lreObject.getChildClass() == null) {//No children
            //This eliminates +Nodes which do not have children
            node = new LreNode(Children.LEAF, Lookups.singleton(lreObject), actions);
        } else {
            node = new LreNode(new LreObjectChildren(mgr, db, lreObject, nodeActionFactory, nodeLabelFactory),
                    Lookups.singleton(lreObject), actions);
        }
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
