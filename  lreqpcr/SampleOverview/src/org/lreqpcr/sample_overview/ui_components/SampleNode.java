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

package org.lreqpcr.sample_overview.ui_components;

import org.lreqpcr.core.database_services.DatabaseServices;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Bob Rutledge
 */
public class SampleNode extends AbstractNode implements org.lreqpcr.core.ui_elements.SampleNode{
    private final DatabaseServices db;

        public SampleNode(Children children, Lookup lookup, DatabaseServices db) {
            super(children, lookup);
            this.db = db;
        }

    @Override
        public DatabaseServices getDatabase() {
            return db;
        }

}
