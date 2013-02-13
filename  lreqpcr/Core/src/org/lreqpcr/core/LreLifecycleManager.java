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
package org.lreqpcr.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Used to ensure that all database files are closed before exiting the program.
 * @author Bob Rutledge
 */
@ServiceProvider(service = LifecycleManager.class, position = 1)
public class LreLifecycleManager extends LifecycleManager {

    @Override
    public void saveAll() {
    }

    /**
     * Close all database services
     */
    @Override
    public void exit() {
        UniversalLookup uLookup = UniversalLookup.getDefault();
        //Cycle through all registered database types
        for (DatabaseType type : DatabaseType.values()) {
            if (uLookup.containsKey(type)) {
                //Cycle through all exsisting database services for this type
                List<Object> typeList = uLookup.getAll(type);
                for (Object o : typeList) {
                    DatabaseServices service = (DatabaseServices) o;
                    service.closeDatabase();
                }
            }
        }
        Collection c = Lookup.getDefault().lookupAll(LifecycleManager.class);
        for (Iterator i = c.iterator(); i.hasNext();) {
            LifecycleManager man = (LifecycleManager) i.next();
            if (man != this) {
                man.exit();
            }
        }
    }
}
