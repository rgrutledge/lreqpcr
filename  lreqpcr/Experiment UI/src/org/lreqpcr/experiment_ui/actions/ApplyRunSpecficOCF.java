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
package org.lreqpcr.experiment_ui.actions;

import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.ExperimentDbInfo;
import org.lreqpcr.core.data_objects.ExptDbInfo;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Allows the user to set a run-specific OCF
 *
 * @author Bob Rutledge
 */
class ApplyRunSpecficOCF extends AbstractAction {

    private ExplorerManager mgr;
    private DatabaseServices db;

    public ApplyRunSpecficOCF(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Apply Run-specific OCF");
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        double runOCF;
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedRunNode = (LreNode) nodes[0];
        db = selectedRunNode.getDatabaseServices();
        Run selectedRun = selectedRunNode.getLookup().lookup(Run.class);
        List<AverageProfile> averageProfileList = selectedRun.getAverageProfileList();
        String s = JOptionPane.showInputDialog(WindowManager.getDefault().getMainWindow(),
                "Enter the OCF, or zero to reset to the average OCF",
                "Apply a Run-specific OCF",
                JOptionPane.PLAIN_MESSAGE);
        if (s == null) {
            return;//Likely because the Dialog was cancelled by the user
        }
        //Remove any commas from the minimum Fc
        while (s.contains(",")) {
            int index = s.indexOf(",");
            s = s.substring(0, index) + s.substring(index + 1);
        }
        try {
            runOCF = Double.parseDouble(s);
        } catch (NumberFormatException nan) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    "The OCF must be a valid number",
                    "Not a valid number",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        selectedRun.setRunSpecificOCF(runOCF);
        db.saveObject(selectedRun);
        ExptDbInfo dbInfo = (ExptDbInfo) db.getAllObjects(ExptDbInfo.class).get(0);
        double avOCF = dbInfo.getOcf();
        for (AverageProfile avProfile : averageProfileList) {
            AverageSampleProfile avSamplePrf = (AverageSampleProfile) avProfile;
            //Zero indicates to revert the OCF back to the average OCF
            if (runOCF == 0) {
                avSamplePrf.setOCF(avOCF);
            } else {
                //apply the run ocf
                avSamplePrf.setOCF(runOCF);
            }
            db.saveObject(avSamplePrf);
            List<SampleProfile> sampleProfileList = avSamplePrf.getReplicateProfileList();
            for (SampleProfile sampleProfile : sampleProfileList) {
                if (runOCF == 0) {
                    //This resets the ocf to the average OCF
                    sampleProfile.setOCF(avOCF);
                } else {
                    //apply the run ocf
                    sampleProfile.setOCF(runOCF);
                }
                db.saveObject(sampleProfile);
            }
        }
        db.commitChanges();
        selectedRunNode.refreshNodeLabel();

        //Update the tree
        LreObjectChildren runChildren = (LreObjectChildren) selectedRunNode.getChildren();
        //Must cast to AverageSampleProfile
        List<AverageSampleProfile> avSamplePrfList = new ArrayList<AverageSampleProfile>();
        for (AverageProfile avPrf : selectedRun.getAverageProfileList()){
            AverageSampleProfile avSamplePrf = (AverageSampleProfile) avPrf;
            avSamplePrfList.add(avSamplePrf);
        }
        runChildren.setLreObjectList(avSamplePrfList);
        //Update the Run tree
        //Call createTree
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
        //Open the tree to the modified Run node
        LreObjectChildren children = (LreObjectChildren) mgr.getRootContext().getChildren();
        LreNode newRunNode = (LreNode) children.findChild(selectedRun.getName());
        mgr.setExploredContext(newRunNode);
    }
}
