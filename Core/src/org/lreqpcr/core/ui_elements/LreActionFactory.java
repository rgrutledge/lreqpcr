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


/**
 * A factory interface for generating actions for a specific Class based on its name. This is
 * used primarily to provide actions for nodes representing the specified Class.
 */
public interface LreActionFactory {

    /**
     * Used to generate actions for a Class identified by its name.
     * @param className name of the class to which the actions are to be applied
     * @return the actions
     */
    Action[] getActions(String className);
}
