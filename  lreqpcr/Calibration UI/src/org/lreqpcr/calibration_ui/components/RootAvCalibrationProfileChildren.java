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
package org.lreqpcr.calibration_ui.components;

import org.lreqpcr.core.data_objects.LreObject;
import java.util.List;
import javax.swing.Action;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
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
public class RootAvCalibrationProfileChildren extends LreObjectChildren {

    private LreAnalysisService analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
    private LreWindowSelectionParameters selectionParameters;

    /**
     * Generates nodes containing the Runs within the supplied experiment database.
     * 
     * @param mgr the manager of the view
     * @param db the experiment database that is being viewed
     * @param avCalProfileList list of AverageCalibration profiles to be displayed
     * @param actionFactory the node action factory
     * @param labelFactory the node label factory
     */
    @SuppressWarnings(value = "unchecked")
    public RootAvCalibrationProfileChildren(ExplorerManager mgr, DatabaseServices db, List<AverageCalibrationProfile> avCalProfileList,
            LreActionFactory actionFactory, LabelFactory labelFactory) {
        super(mgr, db, avCalProfileList, actionFactory, labelFactory);

        //Check to see if the database contains any profile
        if (avCalProfileList != null && avCalProfileList.size() >0) {
            //Check the version of the AverageProfiles...if not 0.8.0 then all the profiles need to be updated.
            //Assume that the first profile is indicative of all the profiles in the database
            if (!avCalProfileList.get(0).isProfileVer0_8_0()) {
                //All of the profiles within this database need to be converted to version 0.8.0
                for (AverageCalibrationProfile avProfile : avCalProfileList) {
                    analysisService.convertProfileToNewVersion(avProfile);
                    //Must save the Profiles, including that replicate SampleProfiles
                    db.saveObject(avProfile);
                    for (Profile repProfile : avProfile.getReplicateProfileList()) {
                        analysisService.convertProfileToNewVersion(repProfile);
                        db.saveObject(repProfile);
                    }
                }
                db.commitChanges();
            }
        }
    }

    /**
     * Creates LRE nodes with children of the supplied LRE object, is any exist, 
     * along with with the corresponding node labels, actions (if any exist) and 
     * the corresponding Explorer manager and database which is being viewed. 
     * 
     * @param lreObject
     * @return
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected Node[] createNodes(LreObject lreObject) {
        AverageCalibrationProfile avCalProfile = (AverageCalibrationProfile) lreObject;
        if (nodeActionFactory == null) {
            actions = new Action[]{};//i.e. no Actions have been set
        } else {
            actions = nodeActionFactory.getActions(lreObject.getClass().getSimpleName());
            if (actions == null) {
                actions = new Action[]{};//i.e. there are no Actions for this Node
            }
        }

        LreNode node = new LreNode(new AvCalibrationProfileChildren(mgr, db, avCalProfile.getReplicateProfileList(), nodeActionFactory, nodeLabelFactory),
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
