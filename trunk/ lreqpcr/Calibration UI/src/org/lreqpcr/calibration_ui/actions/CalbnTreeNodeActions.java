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

package org.lreqpcr.calibration_ui.actions;

import java.util.TreeMap;
import javax.swing.Action;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.openide.explorer.ExplorerManager;

/**
 * Provides actions for a Calibration database explorer tree.
 * @author Bob Rutledge
 */
public class CalbnTreeNodeActions implements LreActionFactory {

    private TreeMap<String, Action[]> actionMap = new TreeMap<String, Action[]>();

    public CalbnTreeNodeActions(ExplorerManager mgr) {

        //Average Calbn Profile actions
        Action[] actions = new Action[]{
            new ExcludeAverageCalibrationProfileAction(mgr),
            new IncludeAverageCalibrationProfileAction(mgr),
            null,
            new DeleteAverageCalibrationProfileAction(mgr),
//            null,
//            new FixAvCaibnPrfEmaxTo100PercentAction(mgr),
//            new ReturnCalibrationProfileEmaxToLreAction(mgr)
        };
        actionMap.put("AverageCalibrationProfile", actions);
        //Calbn Profile actions
        actions = new Action[]{
            new ExcludeCalibrationProfileAction(mgr),
            new IncludeCalibrationProfileAction(mgr),
        };
        actionMap.put("CalibrationProfile", actions);
    }
    
    public Action[] getActions(String className) {
        return actionMap.get(className);
    }

}
