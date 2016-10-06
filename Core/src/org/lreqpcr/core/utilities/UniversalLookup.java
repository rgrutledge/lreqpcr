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
package org.lreqpcr.core.utilities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.List;

/**
 * Singleton object for storage of multiple objects associated with a single key.
 * Based on Google Collection's ArrayListMultimap 
 * Lists of element can be associated with a key, which itself can be of any object type.
 * This provides versatile functionality beyond that possible with the NetBeans Lookup.
 *
 * @author Bob Rutledge
 */
public class UniversalLookup {

    private static UniversalLookup instance;//Singleton object
    private ListMultimap<Object, Object> lookupMultimap = ArrayListMultimap.create();
    private ListMultimap<Object, UniversalLookupListener> listeners = ArrayListMultimap.create();

    private UniversalLookup() {
        lookupMultimap = Multimaps.synchronizedListMultimap(lookupMultimap);
        listeners = Multimaps.synchronizedListMultimap(listeners);
    }

    /**
     * Adds the element object to a list associated with the key. Only one instance 
     * of an element can be stored.
     * 
     * @param key the key to which the element object will be linked
     * @param element the object to store
     * @return true if the element was successfully added
     */
    public boolean add(Object key, Object element) {
        //This is necessary to prevent multiple entries of the same element
        if (lookupMultimap.get(key).contains(element)) {
            fireChangeEvent(key);
            return true;
        }
        boolean b = lookupMultimap.put(key, element);
        fireChangeEvent(key);
        return b;
    }

    /**
     * Add the element object to a list associated with the key. Any preexisting
     * elements are removed.
     *
     * @param key the key to which the element object will be linked
     * @param element the single object to stored
     * @return true if the element was successfully added
     */
    public boolean addSingleton(Object key, Object element) {
        if (lookupMultimap.containsKey(key)) {
            //Remove all elements associated with this key
            while (!lookupMultimap.get(key).isEmpty()) {
                lookupMultimap.get(key).remove(0);
            }
        }
        lookupMultimap.put(key, element);
        return true;
    }

    /**
     * Remove an element from the list associated with the key.
     * 
     * @param key the key for the list
     * @param element the element to remove from the list
     * @return true if the removal was successful
     */
    //    @SuppressWarnings("unchecked")
    public boolean remove(Object key, Object element) {
        boolean b = lookupMultimap.get(key).remove(element);
        fireChangeEvent(key);
        return b;
    }

    /**
     * Determines whether the supplied key is present in the lookup.
     * @param key the key to search for
     * @return true if the key does exist
     */
    public boolean containsKey(Object key) {
        return lookupMultimap.containsKey(key);
    }

    /**
     * Removes all the elements associated with the provided key, in addition to
     * removing the key.
     *
     * @param key the key to remove
     */
    public void removeAll(Object key) {
        lookupMultimap.removeAll(key);
    }

    /**
     * Returns an unmodifiable List of all elements associated with the key.
     *
     * @param key the key to the element List
     * @return an unmodifiable List containing the elements
     */
    public List<Object> getAll(Object key) {
        return Collections.unmodifiableList(lookupMultimap.get(key));
    }

    /**
     * Add a listener to the lookup.
     * @param listener the listener
     * @param key the key value to listen for
     */
    public void addListner(Object key, UniversalLookupListener listener) {
        listeners.put(key, listener);
    }

    /**
     * Remove a listener.
     * @param key the associated key object
     * @param listener the listener to remove
     */
    public void removeListner(Object key, UniversalLookupListener listener) {
        listeners.get(key).remove(listener);
        //Some cleanup
        if (listeners.get(key).isEmpty()) {
            listeners.removeAll(key);
        }
    }

    /**
     * Fire a change event based on the supplied key.
     * @param key the key that triggered the change event
     */
    public void fireChangeEvent(Object key) {
        for (UniversalLookupListener l : listeners.get(key)) {
            l.universalLookupChangeEvent(key);
        }
    }

    /**
     * Provides the singleton object of this lookup.
     * @return the UniversalLookup object
     */
    public static UniversalLookup getDefault() {
        if (instance == null) {
            instance = new UniversalLookup();
            return instance;
        } else {
            return instance;
        }
    }
}
