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
package org.lreqpcr.core.ui_elements;

import javax.swing.Action;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Node representation of an LRE object, the instance of which is held within the Node's
 * Lookup. This Node also has a reference to the database holding the LRE object,
 * providing the capability to save changes to the LRE object.
 * These Nodes also have the ability to change their Actions and Children,
 * in addition to the ability to change/refresh the node label (display name). 
 *
 * @author Bob Rutledge
 */
public class LreNode extends AbstractNode implements ExplorerManager.Provider, DatabaseProvider {

    private Action[] actions;
    private ExplorerManager mgr;//Defines the tree holding this node
    private DatabaseServices db;//The DB holding the LRE object

    /**
     * Standard constructor which supplies the node lookup.
     *
     * @param children the node children
     * @param lookup the node lookup
     * @param nodeActions the node actions
     */
    public LreNode(Children children, Lookup lookup, Action[] nodeActions) {
        super(children, lookup);
        actions = nodeActions;
    }

    /**
     * Copied from the getActions in the Node class
     * Get the set of actions that are associated with this node. This set is 
     * used to construct the context menu for the node.
     * By default this method delegates to the deprecated getActions or 
     * getContextActions method depending on the value of supplied argument. 
     * @param context whether to find actions for context meaning or for the node itself 
     * @return that actions of this node
     */
    @Override
    public Action[] getActions(boolean context) {
        return actions;
    }
    
    /**
     * Replaces the node actions with the supplied actions.
     *
     * @param actions The new actions.
     */
    public void changeActions(Action[] actions) {
        this.actions = actions;
    }

    /**
     * Replaces the node children.
     *
     * @param children The new children.
     */
    public void changeChildren(Children children) {
        super.setChildren(children);
    }

    /**
     * Updates the node label.
     */
    public void refreshNodeLabel() {
//The children object provides the necessary nodeLabelFactory
        Node parentNode = getParentNode();
        if (parentNode != null) {
            LreObjectChildren children = (LreObjectChildren) parentNode.getChildren();
            children.refreshNodeLabel(this);
        }
    }

    /**
     * Provides the database in which the member of this node is stored
     * 
     * @return the database being viewed
     */
    public DatabaseServices getDatabaseServices() {
        return db;
    }

    /**
     * Set the database in which the member of this node is stored
     * @param db the database holding the member of this node
     */
    public void setDatabaseService(DatabaseServices db) {
        this.db = db;
    }

    /**
     * This saves the LRE object and commits the changes to disk.
     */
    public void saveLreObject() {
        if (!db.isDatabaseOpen()) {
            return;
        }
        db.saveObject(getLookup().lookup(LreObject.class));
        db.commitChanges();
    }

    /**
     * Provides the ExplorerManager of the corresponding view.
     * 
     * @return the manager of the view
     */
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    /**
     * Replaces the ExplorerManager of the view.
     * 
     * @param mgr this new ExplorerManages
     */
    public void setExplorerManager(ExplorerManager mgr) {
        this.mgr = mgr;
    }
}
