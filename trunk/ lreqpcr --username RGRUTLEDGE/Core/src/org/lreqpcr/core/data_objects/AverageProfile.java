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
 * A replicate profile is based on averaging the Fc datasets
 * produced by technical replicates (i.e. same sample and amplicon).
 * This average Fc dataset is then used for LRE analysis
 * This increases Fc read precision which in turn
 * generates a more reliable analysis.
 *
 * @author Bob Rutledge
 */
public interface AverageProfile {

    public ArrayList<? extends Profile> getReplicateProfileList();

    public void setReplicateProfileList(ArrayList<? extends Profile> replicateProfileList);

}
