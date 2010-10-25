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
 * Based on the concept of a RDML-defined experiment except that details of the
 * how an amplification is conducted is relegated to Reaction Setup,
 * which is a major component of the Calibration database. This is because
 * the primary outcome of each type of Reaction Setup is an optical calibration
 * factor that is dependent on to how the amplification was conducted, which is
 * described by the corresponding Reaction Setup object. This generalization
 * could allow Experiments to contain Runs that use different Reaction Setups.
 * It also could allow a single Run to contain different types of amplification
 * reactions, each described by a corresponding Reaction Setup.
 *
 * NOT IMPLEMENTED
 *
 * @author Bob Rutledge
 */
public abstract class Experiment extends LreObject {

    private ArrayList<String> investigators = new ArrayList<String>();
    private ArrayList<Run> runList; //List of Runs within this Experiment

    public Experiment() {
    }
    public ArrayList<String> getInvestigatorList() {
        return investigators;
    }

    public void addAnInvestigator(String investigatorName) {
        investigators.add(investigatorName);
    }

    public ArrayList<Run> getRunList() {
        return runList;
    }

    public void setRunList(ArrayList<Run> runList) {
        this.runList = runList;
    }

}
