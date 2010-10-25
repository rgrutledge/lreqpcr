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

import java.util.Map;

/**
 * Plate is currently not implemented
 *
 * @author Bob Rutledge
 */
public abstract class Plate {
    // TODO decide whether to delete the Plate data object...likely so
    //Not clear how a plate should be represented; is it a template or
    //should it be an object

    //Template...likely an interface
    public abstract Map<String, ?> getPlate();

    //Object...could this be used to contruct a Plate GUI??...of course
    public abstract String getAmplicon(String well);
    public abstract String getSample(String well);
    public abstract String getRxnSetup(String well);

}
