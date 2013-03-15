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

import com.google.common.collect.Lists;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPanel;
import org.lreqpcr.calibration_ui.actions.CalbnTreeNodeActions;
import org.lreqpcr.calibration_ui.actions.FixAllCalibrationProfileEmaxTo100percentAction;
import org.lreqpcr.calibration_ui.actions.ReturnAllCalibrationProfileEmaxToLreAction;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
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
        List<AverageCalibrationProfile> avCalProfileList = (List<AverageCalibrationProfile>) calbnDB.getAllObjects(AverageCalibrationProfile.class);
   //This is necessary because DB4O lists cannot be sorted via Collections.sort
        ArrayList<AverageCalibrationProfile> lreObjectArray = new ArrayList<AverageCalibrationProfile>(avCalProfileList);
        displayName = displayName + " (" + String.valueOf(lreObjectArray.size()) + ")";
        Collections.sort(lreObjectArray);
        Action[] actions = new Action[]{
            new FixAllCalibrationProfileEmaxTo100percentAction(mgr),
            new ReturnAllCalibrationProfileEmaxToLreAction(mgr)
        };
        LreNode root = new LreNode(new RootCalibrationChildren(mgr, calbnDB, avCalProfileList, nodeActionFactory,
                nodeLabelFactory), null, actions);
        root.setDatabaseService(calbnDB);
        root.setDisplayName(displayName);
        root.setShortDescription(dbFile.getAbsolutePath());
        mgr.setRootContext(root);
        calcAverageOCF();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    @SuppressWarnings(value = "unchecked")
    public void creatAmpliconTree(String ampName) {
        List ampList = calbnDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "ampliconName", ampName);
        LreNode root = new LreNode(new RootCalibrationChildren(mgr, calbnDB, ampList, nodeActionFactory,
                nodeLabelFactory), null, new Action[]{});
        root.setDisplayName(ampName + " (" + String.valueOf(ampList.size()) + ")");
        root.setDatabaseService(calbnDB);
        mgr.setRootContext(root);
        runViewButton.setSelected(false);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    @SuppressWarnings(value = "unchecked")
    public void createSampleTree(String sampleName) {
        List avCalProfilList = calbnDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "sampleName", sampleName);
        LreNode root = new LreNode(new RootCalibrationChildren(mgr, calbnDB, avCalProfilList, nodeActionFactory, nodeLabelFactory),
                null, new Action[]{});
        root.setDisplayName(sampleName + " (" + String.valueOf(avCalProfilList.size()) + ")");
        root.setDatabaseService(calbnDB);
        mgr.setRootContext(root);
        runViewButton.setSelected(false);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    /**
     * Calculates the average OCF using only the average calibration profiles
     *
     * @param calbnProfileList list of AverageCalibrationProfiles
     */
    @SuppressWarnings(value = "unchecked")
    public void calcAverageOCF() {
        List<AverageCalibrationProfile> avCalProfileList = (List<AverageCalibrationProfile>) calbnDB.getAllObjects(AverageCalibrationProfile.class);
        if (avCalProfileList.isEmpty()){
            avProfileOCFdisplay.setText("");
            return;
        }
        double ocfSum = 0;
        ArrayList ocfArray = Lists.newArrayList();
        dfCV.applyPattern("0.0");
        for (AverageCalibrationProfile avProfile : avCalProfileList) {
            if (!avProfile.isExcluded()) {
                ocfSum += avProfile.getOCF();
                ocfArray.add(avProfile.getOCF());
            }
        }
        double averageOCF = ocfSum / ocfArray.size();
        double sd = MathFunctions.calcStDev(ocfArray);
        double cv = sd / averageOCF;
        df.applyPattern(FormatingUtilities.decimalFormatPattern(averageOCF));
        if (ocfArray.size() == 1) {
            avProfileOCFdisplay.setText(df.format(averageOCF));
        } else {
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

        runViewButton.setText("View all Profiles");
        runViewButton.setToolTipText("Return to viewing all Calibration profiles");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Av. OCF:");
        jLabel2.setToolTipText("Average OCF derived from all of the average calibration profiles");

        avProfileOCFdisplay.setColumns(8);
        avProfileOCFdisplay.setEditable(false);
        avProfileOCFdisplay.setToolTipText("The average OCF +/-CV");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(runViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runViewButton)
                    .addComponent(jLabel2)
                    .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        runViewButton.setSelected(true);
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
    }//GEN-LAST:event_runViewButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField avProfileOCFdisplay;
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables
}
