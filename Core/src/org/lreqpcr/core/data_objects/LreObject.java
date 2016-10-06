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

package org.lreqpcr.core.data_objects;

/**
 * Base abstract class for LRE data objects structured on 
 * a Composite (family) pattern for display within a
 * tree, with pointers to the parent object and its name,
 * if one exists. By specifying the child class,
 * children of this LreObject can be retrieved from the database
 * by retrieving all objects of the child class withe a parent
 * that matches the specified parent object
 * 
 * @author Bob Rutledge
 */
public abstract class LreObject implements Family<LreObject>, Comparable {

    private LreObject parent;//Parent object, if one exsists
    private Class childClass; //Child Class, if one exsists
    
    private String name;//Name of this LRE object
    private String shortDescription;
    private String longDescription;

    /**
     * Set the parent object.
     * @param parent the parent object
     */
    public void setParent(LreObject parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent object
     * @return the parent
     */
    public LreObject getParent() {
        return parent;
    }

     /**
      * Returns the name of the parent object
      * @return the name of the parent
      */
     public String getParentName() {
        return parent.getParentName();
    }

    /**
     * Returns the child type (Class)
     * @return the child type
     */
    public Class getChildClass() {
        return childClass;
    }

    /**
     * Sets the child type (Class)
     * @param childClass the child type
     */
    public void setChildClass(Class childClass) {
        this.childClass = childClass;
    }

    /**
     * Returns a long description which are most often notes about the object.
     * @return the long description of this LRE object
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Sets or replaces the existing the long description
     * which are most often notes about the object.
     * @param longDescription the new long description of this LRE object
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }
    
    /**
     * Appends the supplied string to the end of the long description, along
     * with a line break
     * @param appendingString the string to append to the long description
     */
    public void appendLongDescription(String appendingString){
        if (longDescription == null) {
            longDescription = appendingString;
        } else {
//        This is necessary in order to make String.concat() work
            appendingString = " " + appendingString;
            String r = longDescription.concat(appendingString);
            longDescription = r;
        }
    }

    /**
     * Returns the name of this LRE object.
     * @return the object's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this LRE object.
     * @param name the object's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the short description of this LRE object which is most often
     * used for generating a fly out description when the mouse hovers over a node
     * within a tree.
     * @return the object's short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets the short description of this LRE object which is most often
     * used for generating a fly out description when the mouse hovers over a node
     * within a tree.
     * @param shortDescription the object's short description
     */
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
