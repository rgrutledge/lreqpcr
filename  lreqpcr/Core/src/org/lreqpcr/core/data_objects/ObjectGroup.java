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

import java.util.ArrayList;

/**
 * Early attempt at implementing groups for organizing database objects, such as Amplicons
 * or Samples, for more effective object identification and retrieval (e.g. for setting up a Run).
 * Note that this approach is differenct from the Family interface
 * in that it relies on a Parent field for grouping children objects together via
 * Object database searching which does not require maintaining a Child list. Also,
 * this list-based approach does not require a Parent field.
 *
 * NOT YET IMPLEMENTED AND COULD BE DELETED OR RENAMED...THUS DO NOT USE
 *
 * @author Bob Rutledge
 */
public abstract class ObjectGroup<T> extends LreObject{

    private ArrayList<T> objectList = new ArrayList<T>();

    public ArrayList<T> getObjectList() {
        return objectList;
    }

    public void addObject(T t){
        objectList.add(t);
    }

    public void removeObject (T t){
        objectList.remove(t);
    }

}
