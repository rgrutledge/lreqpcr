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
package org.lreqpcr.experiment_ui.actions;

import org.lreqpcr.core.ui_elements.LreActionFactory;
import java.util.TreeMap;
import javax.swing.Action;
import org.openide.explorer.ExplorerManager;

/**
 *
 * @author Bob Rutledge
 */
public class ExperimentTreeNodeActions implements LreActionFactory {

    private TreeMap<String, Action[]> actionMap = new TreeMap<String, Action[]>();

    public ExperimentTreeNodeActions(ExplorerManager mgr) {

        //Average Sample Profile actions
        Action[] actions = new Action[]{
            new FixSampleProfileEmaxTo100percentAction(mgr),
            new ReturnSampleProfileToLreDerivedEmaxAction(mgr),
            null,
            new DeleteAverageSampleProfileAction(mgr),
            null,
            new IncludeAverageSampleProfileAction(mgr),
            new ExcludeAverageSampleProfileAction(mgr)
        };
        actionMap.put("AverageSampleProfile", actions);

        //Replicate Sample Profile actions
        actions = new Action[]{
                    new ExcludeSampleProfileAction(mgr),
                    new IncludeSampleProfileAction(mgr), //Disallow SampleProfile deletion for now
                    null,
                    new FixSampleProfileEmaxTo100percentAction(mgr),
                    new ReturnSampleProfileToLreDerivedEmaxAction(mgr)
         //Rather use Exclude than to permanently deleting a replicate profile
                //            null,
                //            new DeleteSampleProfileAction(mgr)
                };
        actionMap.put("SampleProfile", actions);

        //Run actions
        actions = new Action[]{
   //Deactivated due to a bug that can generate long delays when importing the files
                    //            new SaveCyclerDatafileAction(mgr),
                    //            new RetrieveDatafileAction(mgr),
                    //            new RetrieveExcelImportFileAction(mgr),
                    new ApplyRunSpecficOCF(mgr),
                    null,
                    new DeleteRunAction(mgr),
                    null,
                    new FixRunProfilesEmaxTo100percentAction(mgr),
                    new ReturnRunProfilesEmaxToLreDerivedEmaxAction(mgr)
                };
        actionMap.put("RunImpl", actions);
    }

    public Action[] getActions(String className) {
        return actionMap.get(className);
    }
}
