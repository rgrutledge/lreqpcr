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

package org.lreqpcr.calibration_ui.actions;

import org.lreqpcr.core.ui_elements.LreActionFactory;
import java.util.TreeMap;
import javax.swing.Action;
import org.openide.explorer.ExplorerManager;

/**
 * Only provides for deletion of Calibration Profiles because only a single
 * reaction setup is provided in this verion. This thus requires separate
 * databases for each reaction setup...this could be changed in future
 * verions.
 * @author Bob Rutledge
 */
public class CalbnTreeNodeActions implements LreActionFactory {

    private TreeMap<String, Action[]> actionMap = new TreeMap<String, Action[]>();
//    private ExplorerManager mgr;//Defines the tree upon which these actions will be applied

    public CalbnTreeNodeActions(ExplorerManager mgr) {
//        this.mgr = mgr;
        //Average Calbn Profile actions
        Action[] actions = new Action[]{
            new ExcludeAverageCalibrationProfileAction(mgr),
            new IncludeAverageCalibrationProfileAction(mgr),
            null,
            new DeleteCalibrationProfileAction(mgr),
            null,
            new FixCalibrationProfileEmaxTo100percentAction(mgr),
            new ReturnCalibrationProfileEmaxToLreAction(mgr)
        };
        actionMap.put("AverageCalibrationProfile", actions);
        //Calbn Profile actions
        actions = new Action[]{
            new ExcludeCalibrationProfileAction(mgr),
            new IncludeCalibrationProfileAction(mgr),
            null,
            new FixCalibrationProfileEmaxTo100percentAction(mgr),
            new ReturnCalibrationProfileEmaxToLreAction(mgr)
        };
        actionMap.put("CalibrationProfile", actions);
    }
    
    public Action[] getActions(String className) {
        return actionMap.get(className);
    }

}
