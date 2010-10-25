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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lreqpcr.core.database_services.DatabaseServices;

/**
 * Stores singleton Objects based on the NetBeans Lookup model in which the keys are
 * classes and the value is an instance of the class. Only singleton instances
 * are stored, so that adding a duplicate type removes the previously
 * added object of the same Class.
 *
 * @author Bob Rutledge
 */
public class LreLookup {

    private static LreLookup instance;
    private Map<Class, Object> lookupMap;
    private List<ListnerWrapper> listnerList;

    public LreLookup() {
        instance = this;
        lookupMap = Collections.synchronizedMap(new HashMap<Class, Object>());
        listnerList = Collections.synchronizedList(new ArrayList<ListnerWrapper>());
    }

    /**
     * Retrieves the instance corresponding to the class, or null if none
     * exsists.
     * @param clazz the Class to lookup
     * @return the corresponding stored object
     */
    public Object lookup(Class clazz) {
        return lookupMap.get(clazz);
    }

    /**
     * Adds the Object to the Lookup.
     * @param o the Object to add to the Lookup
     */
    public void addToLookup(Object o) {
        if (o == null) {
            return;
        }
        Class type = null;
        if (DatabaseServices.class.isAssignableFrom(o.getClass())) {
            lookupMap.put(DatabaseServices.class, o);
            type = DatabaseServices.class;
        } else {
            lookupMap.put(o.getClass(), o);
            type = o.getClass();
        }
        ArrayList<LreLookupListener> listeners = new ArrayList<LreLookupListener>();
        for (ListnerWrapper listener : listnerList) {
            if (listener.getClassToLisnterFor() == type) {
                listeners.add(listener.getTheListner());
            }
        }
        for (LreLookupListener listener : listeners) {
            listener.lreLookupAddEvent(o);
        }
    }

    public void addLookupListner(LreLookupListener l, Class clazz) {
        listnerList.add(new ListnerWrapper(clazz, l));
    }

    public static LreLookup getDefault() {
        if (instance == null) {
            instance = new LreLookup();
            return instance;
        } else {
            return instance;
        }
    }

    private class ListnerWrapper {

        Class classToLisnterFor;
        LreLookupListener theListner;

        public ListnerWrapper(Class classToLisnterFor, LreLookupListener theListner) {
            this.classToLisnterFor = classToLisnterFor;
            this.theListner = theListner;
        }

        public Class getClassToLisnterFor() {
            return classToLisnterFor;
        }

        public LreLookupListener getTheListner() {
            return theListner;
        }
    }
}
