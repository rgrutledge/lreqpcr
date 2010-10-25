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
 * Primarily for facilitating the organization of an Amplicon database,
 * by providing the ability to generate groups of Amplicons.
 * The intention of a groupList is to allow Amplicons to be assigned to more than
 * one group, and is compatible with asigning a Target as the parent of an Amplicon.
 * However, details of how best to implement and view AmpliconGroups is unclear.
 *
 * ...NOT YET IMPLEMENTED
 *
 * @author Bob Rutledge
 */
public class AmpliconGroup extends ObjectGroup<Amplicon> {

}
