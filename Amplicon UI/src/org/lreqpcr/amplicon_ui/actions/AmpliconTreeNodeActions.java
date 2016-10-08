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

package org.lreqpcr.amplicon_ui.actions;

import java.util.TreeMap;

import javax.swing.Action;

import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.openide.explorer.ExplorerManager;

/**
 *
 * @author Bob Rutledge
 */
public class AmpliconTreeNodeActions implements LreActionFactory {

    TreeMap<String, Action[]> actionMap = new TreeMap<>();

    public AmpliconTreeNodeActions(ExplorerManager mgr) {
        //Amplicon actions
        Action[] actions = new Action[]{
            new DeleteAmpliconAction(mgr)
        };
        actionMap.put("Amplicon", actions);
    }

    public Action[] getActions(String className) {
        return actionMap.get(className);
    }

}
