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

import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Only one Profile can be excluded at one time.
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
        double runOCF = 0;
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedRunNode = (LreNode) nodes[0];
        db = selectedRunNode.getDatabaseServices();
        Run selectedRun = selectedRunNode.getLookup().lookup(Run.class);
        List<AverageSampleProfile> averageSampleProfileList = selectedRun.getAverageProfileList();
        String s = JOptionPane.showInputDialog(WindowManager.getDefault().getMainWindow(),
                "Enter the OCF",
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
            selectedRun.setRunOCF(runOCF);
            db.saveObject(selectedRun);
//If the returned OCF is zero, updateProfile will revert to the average OCF
            for (AverageSampleProfile avProfile : averageSampleProfileList) {
                avProfile.setRunOCF(runOCF);
                avProfile.updateProfile();
                db.saveObject(avProfile);
                List<SampleProfile> sampleProfileList = avProfile.getReplicateProfileList();
                for (SampleProfile sampleProfile : sampleProfileList) {
                    if (!sampleProfile.isExcluded()) {
                        sampleProfile.setRunOCF(runOCF);
                        sampleProfile.updateProfile();
                        db.saveObject(sampleProfile);
                    }
                }
            }
            db.commitChanges();
            selectedRunNode.refreshNodeLabel();

            //Update the tree
            LreObjectChildren runChildren = (LreObjectChildren) selectedRunNode.getChildren();
            runChildren.setLreObjectList(selectedRun.getAverageProfileList());
            //Update the Run tree
            //Call createTree
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
            //Open the tree to the modified Run node
            LreObjectChildren children = (LreObjectChildren) mgr.getRootContext().getChildren();
            LreNode newRunNode = (LreNode) children.findChild(selectedRun.getName());
            mgr.setExploredContext(newRunNode);
        }
    }
