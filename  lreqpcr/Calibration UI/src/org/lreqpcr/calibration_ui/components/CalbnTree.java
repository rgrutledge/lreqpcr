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

import com.google.common.collect.Lists;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.data_objects.ReactionSetupImpl;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.calibration_ui.actions.CalbnTreeNodeActions;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPanel;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Bob Rutledge
 */
public class CalbnTree extends JPanel {

    private ExplorerManager mgr;
    private DatabaseServices calbnDB;
    private LreActionFactory nodeActionFactory;
    private LabelFactory nodeLabelFactory = new CalbnTreeNodeLabels();
    private DecimalFormat df = new DecimalFormat();
    private DecimalFormat dfCV = new DecimalFormat();

    /** Creates new form RunTree */
    public CalbnTree() {
        initComponents();
    }

    @SuppressWarnings(value = "unchecked")
    public void initCalbnTree(ExplorerManager mgr, DatabaseServices db) {
        this.mgr = mgr;
        calbnDB = db;
        nodeActionFactory = new CalbnTreeNodeActions(mgr);
        createTree();
    }

    @SuppressWarnings(value = "unchecked")
    public void createTree() {
        if (!calbnDB.isDatabaseOpen()) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No CalbnDB is open");
            mgr.setRootContext(root);
            avProfileOCFdisplay.setText("");
            return;
        }
        File dbFile = calbnDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        String displayName = dbFileName.substring(0, length - 4);
        //This assumes that the calibration database contains a single reaction setup
        List<? extends LreObject> rxnSetupList = (List<? extends LreObject>) calbnDB.getAllObjects(ReactionSetupImpl.class);
        LreNode root = new LreNode(new LreObjectChildren(mgr, calbnDB, rxnSetupList, nodeActionFactory,
                nodeLabelFactory), null, new Action[]{});
        root.setDatabaseService(calbnDB);
        root.setDisplayName(displayName);
        root.setShortDescription(dbFile.getAbsolutePath());
        mgr.setRootContext(root);
        //An attempt to set the selection to the Reaction Setup node
        LreObjectChildren children = (LreObjectChildren) root.getChildren();
        LreNode firstNode = (LreNode) children.getNodes()[0];
        mgr.setExploredContext(firstNode);
        calcAverageOCF();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    @SuppressWarnings(value = "unchecked")
    public void creatAmpliconTree(String ampName) {
        List ampList = calbnDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "ampliconName", ampName);
        LreNode root = new LreNode(new LreObjectChildren(mgr, calbnDB, ampList, nodeActionFactory,
                nodeLabelFactory), null, new Action[]{});
        root.setDisplayName(ampName + " (" + String.valueOf(ampList.size()) + ")");
        root.setDatabaseService(calbnDB);
        mgr.setRootContext(root);
        runViewButton.setSelected(false);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    @SuppressWarnings(value = "unchecked")
    public void createSampleTree(String sampleName) {
        List sampleList = calbnDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "sampleName", sampleName);
        LreNode root = new LreNode(new LreObjectChildren(mgr, calbnDB, sampleList, nodeActionFactory, nodeLabelFactory),
                null, new Action[]{});
        root.setDisplayName(sampleName + " (" + String.valueOf(sampleList.size()) + ")");
        root.setDatabaseService(calbnDB);
        mgr.setRootContext(root);
        runViewButton.setSelected(false);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    @SuppressWarnings(value = "unchecked")
    public void calcAverageOCF() {
        if (!calbnDB.isDatabaseOpen()) {
            avProfileOCFdisplay.setText("");
            return;
        }
        //This returns both replicat and average profiles
        List<? extends LreObject> calbnProfileList =
                (List<? extends LreObject>) calbnDB.getAllObjects(CalibrationProfile.class);
        double ocfSum = 0;
        ArrayList ocfArray = Lists.newArrayList();
        dfCV.applyPattern("0.0");
        calbnProfileList =
                (List<? extends LreObject>) calbnDB.getAllObjects(AverageCalibrationProfile.class);
        if (!calbnProfileList.isEmpty()) {
            for (int i = 0; i < calbnProfileList.size(); i++) {
                CalibrationProfile profile = (CalibrationProfile) calbnProfileList.get(i);
                if (!profile.isExcluded()) {
                    if (profile.getEmax() > 1) {
                        ocfSum += profile.getAdjustedOCF();
                        ocfArray.add(profile.getAdjustedOCF());
                    } else {
                        ocfSum += profile.getRunOCF();
                        ocfArray.add(profile.getRunOCF());
                    }
                }
            }
            double averageOCF = ocfSum / ocfArray.size();
            double sd = MathFunctions.calcStDev(ocfArray);
            double cv = sd / averageOCF;
            df.applyPattern(FormatingUtilities.decimalFormatPattern(averageOCF));
            avProfileOCFdisplay.setText(df.format(averageOCF) + " +/-" + dfCV.format(cv * 100) + "%");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        beanTree = new BeanTreeView();
        runViewButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        avProfileOCFdisplay = new javax.swing.JTextField();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

        beanTree.setPreferredSize(new java.awt.Dimension(200, 100));
        jScrollPane1.setViewportView(beanTree);

        runViewButton.setText("Run View");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Av. OCF:");
        jLabel2.setToolTipText("Average OCF derived from all of the average profiles");

        avProfileOCFdisplay.setColumns(8);
        avProfileOCFdisplay.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(runViewButton)
                .addContainerGap(172, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
    }//GEN-LAST:event_runViewButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField avProfileOCFdisplay;
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables
}
