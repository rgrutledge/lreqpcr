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
 * Base abstract class for LRE data objects structured on 
 * a Composite (family) pattern for display within a
 * tree, with pointers to the parent object and its name,
 * if one exsists. By specifying the child class,
 * children of this LreObject can be retrieved from the database
 * by retrieving all objects of the child class
 * and matching the child's parent field to the specified parent.
 * 
 * @param <T> LreObject type
 * @author Bob Rutledge
 */
public abstract class LreObject implements Family<LreObject>, Comparable {

    private LreObject parent;//Parent object, if one exsists
    private Class childClass; //Child Class, if one exsists
    
    private String name;//Name of this LRE object
    private String shortDescription;
    private String longDescription;

    public void setParent(LreObject parent) {
        this.parent = parent;
    }

     public LreObject getParent() {
        return parent;
    }

    public String getParentName() {
        return parent.getParentName();
    }

    public Class getChildClass() {
        return childClass;
    }

    public void setChildClass(Class childClass) {
        this.childClass = childClass;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Implementation of the Visitor pattern for future functionality
     * @param v the Visitor
     */
    public void accept(LreVisitor v){
        v.visit(this);
    }

    /**
     * Sorts by the LreObject name
     * @param o
     * @return 
     */
    public int compareTo(Object o){
        LreObject lreObject = (LreObject) o;
        return name.compareTo(lreObject.getName());
    }
}
