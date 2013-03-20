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
package org.lreqpcr.calibration_ui.components;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Displays Run nodes for the supplied list of Runs
 *
 * @author Bob Rutledge
 */
public class CalRootChildren extends LreObjectChildren {

    private LreAnalysisService analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);

    /**
     * Generates AverageProfile nodes for a Run.
     * 
     * @param mgr the manager of the view
     * @param db the experiment database that is being viewed
     * @param runList list of Runs to be displayed
     * @param actionFactory the node action factory
     * @param labelFactory the node label factory
     */
    public CalRootChildren(ExplorerManager mgr, DatabaseServices db, List<? extends Run> runList,
            LreActionFactory actionFactory, LabelFactory labelFactory) {
        super(mgr, db, runList, actionFactory, labelFactory);
    }

    /**
     * Creates LRE nodes with children of the supplied LRE object, if any exist, 
     * along with with the corresponding node labels, actions (if any exist) and 
     * the corresponding Explorer manager and database which is being viewed. 
     * 
     * @param lreObject
     * @return the new node
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected Node[] createNodes(LreObject lreObject) {
        Run run = (Run) lreObject;
        if (run.getAverageProfileList() == null){
            return null;
        }
        //Oddly when first implemented and using older databases, unrelated Runs  
        //were found to be present that contain average profile list containing AverageSampleProfiles
        if (run.getAverageProfileList().get(0) instanceof AverageSampleProfile){
            return null;
        }
        if (nodeActionFactory == null) {
            actions = new Action[]{};//i.e. no Actions have been set
        } else {
            actions = nodeActionFactory.getActions(lreObject.getClass().getSimpleName());
            if (actions == null) {
                actions = new Action[]{};//i.e. there are no Actions for this Node
            }
        }
        List<AverageProfile> avProfileList = run.getAverageProfileList();
        List<AverageCalibrationProfile> avCalPrfList = new ArrayList<AverageCalibrationProfile>();
        //Must cast to AverageSampleProfile
        for (AverageProfile avPrf : avProfileList){
            AverageCalibrationProfile avSamplePrf = (AverageCalibrationProfile) avPrf;
            avCalPrfList.add(avSamplePrf);
        }
        LreNode node = new LreNode(new CalRunChildren(mgr, db, avCalPrfList, nodeActionFactory, nodeLabelFactory),
                Lookups.singleton(lreObject), actions);
        node.setExplorerManager(mgr);
        node.setDatabaseService(db);
        node.setName(lreObject.getName());
        if (nodeLabelFactory == null) {
            node.setDisplayName("");
        } else {
            node.setDisplayName(nodeLabelFactory.getNodeLabel(lreObject));
            node.setShortDescription(lreObject.getShortDescription());
        }
        return new Node[]{node};
    }
}
