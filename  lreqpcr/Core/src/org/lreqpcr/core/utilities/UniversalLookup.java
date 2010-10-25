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
package org.lreqpcr.core.utilities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.List;

/**
 * A Universal Lookup based on Google Collection's ArrayListMultimap in which
 * elements Lists are associated with a key. This allows storage of multiple
 * objects associated with a single key.
 *
 * @author Bob Rutledge
 */
public class UniversalLookup<K extends Object, E extends Object> {

    private static UniversalLookup instance;
    private ListMultimap<K, E> lookupMultimap = ArrayListMultimap.create();
    private ListMultimap<K, UniversalLookupListener> listeners = ArrayListMultimap.create();

    public UniversalLookup() {
//     TODO delete:   instance = this;
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
    public boolean add(K key, E element) {
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
    public boolean addSingleton(K key, E element) {
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
    public boolean remove(K key, E element) {
        boolean b = lookupMultimap.get(key).remove(element);
        fireChangeEvent(key);
        return b;
    }

    public boolean containsKey(Object key) {
        return lookupMultimap.containsKey(key);
    }

    /**
     * Removes all the elements associated with the provided key, in addition to
     * removing the key
     *
     * @param key the key to remove
     */
    public void removeAll(K key) {
        lookupMultimap.removeAll(key);
    }

    /**
     * Returns an unmodifiable List of all elements associated with the key.
     *
     * @param key the key to the element List
     * @return an unmodifiable List containing the elements
     */
    public List<E> getAll(K key) {
        return Collections.unmodifiableList(lookupMultimap.get(key));
    }

    /**
     * 
     * @param listener the listener
     * @param key the key value to listen for
     */
    public void addListner(K key, UniversalLookupListener listener) {
        listeners.put(key, listener);
    }

    public void removeListner(K key, UniversalLookupListener listener) {
        listeners.get(key).remove(listener);
        //Some cleanup
        if (listeners.get(key).isEmpty()) {
            listeners.removeAll(key);
        }
    }

    public void fireChangeEvent(K key) {
        for (UniversalLookupListener l : listeners.get(key)) {
            l.universalLookupChangeEvent(key);
        }
    }

    public static UniversalLookup getDefault() {
        if (instance == null) {
            instance = new UniversalLookup();
            return instance;
        } else {
            return instance;
        }
    }
}
