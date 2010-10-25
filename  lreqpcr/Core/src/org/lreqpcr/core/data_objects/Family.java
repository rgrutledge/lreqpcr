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

package org.lreqpcr.core.data_objects;

/**
 * Provides the framework for retrieving and 
 * displaying Objects based on a single parent/multiple children
 * pattern, in which children are designated by Class. Children objects for a Parent
 * are thus retrieved by searching all Child objects (ID'd by Class) for those
 * in which it is set as the Parent. This Object database "centric" approach
 * foregoes the need to  maintain a List of children, where e.g. when a child
 * is deleted, it is necessary to first remove to the child from the list
 * in order to avoid null pointers within the List. Nevertheless, retrieving
 * children in this way does require searching of all children objects in the
 * database, which could become a performance issue. It is anticipated,
 * however, that indexing the Parent field within the Child objects would
 * overcome such performance issues, as this avoids instantiation of
 * the Child objects.
 *
 * @author Bob Rutledge
 */
public interface Family<T> {

    /**
     * Sets the Parent Object
     * @param parent the Parent Object
     */
    public void setParent(T parent);

    /**
     * Returns the Parent Object or null if none exsists
     * @return the parent Object
     */
    public T getParent();

    /**
     * Sets the Child class that allows retrieval of children objects.
     * Assumes only one type of child.
     * @param childClass the Child class
     */
    public void setChildClass(Class childClass);

    public Class getChildClass();
}
