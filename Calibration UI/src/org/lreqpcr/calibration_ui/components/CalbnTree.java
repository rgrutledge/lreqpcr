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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jxl.write.WriteException;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.calibration_ui.actions.CalbnTreeNodeActions;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.CalibrationDbInfo;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationRun;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.lreqpcr.nonlinear_regression_services.NonlinearRegressionUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class CalbnTree extends JPanel {

    private final LreAnalysisService lreAnalysisService = Lookup.getDefault().lookup(LreAnalysisService.class);

    private LreWindowSelectionParameters selectionParameters;
    private LreNode rootLreNode;
    private ExplorerManager mgr;
    private DatabaseServices calbnDB;
    private LreActionFactory nodeActionFactory;
    private LabelFactory nodeLabelFactory;
    private DecimalFormat df = new DecimalFormat();
    private DecimalFormat dfCV = new DecimalFormat();
    private CalibrationDbInfo calDbInfo;
//    private StatusDisplayer.Message statusLineMessage;

    /**
     * Creates new form RunTree
     */
    public CalbnTree() {
        initComponents();
    }

    @SuppressWarnings(value = "unchecked")
    public void initCalbnTree(ExplorerManager mgr, DatabaseServices db) {
        this.mgr = mgr;
        calbnDB = db;
        nodeActionFactory = new CalbnTreeNodeActions(mgr);
        nodeLabelFactory = new CalbnTreeNodeLabels();
        jButton1.setVisible(false);
        createTree();
    }

    @SuppressWarnings(value = "unchecked")
    public void createTree() {
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        if (!calbnDB.isDatabaseOpen()) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No CalbnDB is open");
            mgr.setRootContext(root);
            avProfileOCFdisplay.setText("");
            fmaxNrmzBox.setSelected(false);
            return;
        }
        List<CalibrationDbInfo> infoDbList = (List<CalibrationDbInfo>) calbnDB.getAllObjects(CalibrationDbInfo.class);
        if (infoDbList.isEmpty()) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    "The selected calibration database is incompatible\n"
                    + " with this version of the LRE Analyzer.",
                    "Unable to load this calibration database",
                    JOptionPane.ERROR_MESSAGE);
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No CalbnDB is open");
            mgr.setRootContext(root);
            avProfileOCFdisplay.setText("");
            calbnDB.closeDatabase();
            return;
        }
        calDbInfo = infoDbList.get(0);
        if (calDbInfo.getVerionNumber() == 0) {//Version number == 0 signifies pre 0.9.3
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_WAIT_CURSOR);
            //Convert to the new version
            CalibrationDbInfo newCalDbInfo = new CalibrationDbInfo();
            //Copy all values into the newDbInfo
            newCalDbInfo.setAvRunFmax(calDbInfo.getAvRunFmax());
            newCalDbInfo.setAvRunFmaxCV(calDbInfo.getAvRunFmaxCV());
            newCalDbInfo.setIsOcfNormalizedToFmax(calDbInfo.isOcfNormalizedToFmax());
            calbnDB.saveObject(newCalDbInfo);
            calbnDB.deleteObject(calDbInfo);
            NonlinearRegressionUtilities.applyNonlinearRegression(calbnDB);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.SET_DEFAULT_CURSOR);
        }
        selectionParameters = (LreWindowSelectionParameters) calbnDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
        fmaxNrmzBox.setSelected(calDbInfo.isOcfNormalizedToFmax());
        List<CalibrationRun> runList = (List<CalibrationRun>) calbnDB.getAllObjects(CalibrationRun.class);
        RunNodesWithAvCalChildren calRootChildren = new RunNodesWithAvCalChildren(mgr, calbnDB, runList, nodeActionFactory, nodeLabelFactory);
        rootLreNode = new LreNode(calRootChildren, Lookups.singleton(calDbInfo), new Action[]{});
        rootLreNode.setDatabaseService(calbnDB);
        File dbFile = calbnDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        String displayName = dbFileName.substring(0, length - 4);
        rootLreNode.setDisplayName(displayName);
        mgr.setRootContext(rootLreNode);
        calcAverageOCF();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    /**
     * Creates a tree displaying all AverageSampleProfiles generated by the
     * designated amplicon.
     *
     * @param ampName the amplicon used to generate the
     * AverageCalibrationProfile
     */
    @SuppressWarnings(value = "unchecked")
    public void creatAmpliconTree(String ampName) {
        if (calbnDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfile that used the amplicon "ampName"    
            List avCalPrfList = calbnDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "ampliconName", ampName);
            LreNode root = new LreNode(new AvCalProfileNodesWithCalProfileChildren(mgr, calbnDB, avCalPrfList, nodeActionFactory,
                    nodeLabelFactory), null, new Action[]{});
            root.setDisplayName(ampName + " (" + String.valueOf(avCalPrfList.size()) + ")");
            root.setDatabaseService(calbnDB);
            mgr.setRootContext(root);
            runViewButton.setSelected(false);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void createSampleTree(String sampleName) {
        List avCalProfilList = calbnDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "sampleName", sampleName);
        LreNode root = new LreNode(new AvCalProfileNodesWithCalProfileChildren(mgr, calbnDB, avCalProfilList, nodeActionFactory, nodeLabelFactory),
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
        if (avCalProfileList.isEmpty()) {
            avProfileOCFdisplay.setText("");
            return;
        }
        double ocfSum = 0;
        ArrayList ocfArray = Lists.newArrayList();
        dfCV.applyPattern("0.0");
        for (AverageCalibrationProfile avProfile : avCalProfileList) {
            //Display the curve fitting derived OCF..OFF
            if (!avProfile.isExcluded() && avProfile.getOCF() != Double.POSITIVE_INFINITY) {
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        beanTree = new BeanTreeView();
        jPanel1 = new javax.swing.JPanel();
        runViewButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        avProfileOCFdisplay = new javax.swing.JTextField();
        fmaxNrmzBox = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        expandAllButton = new javax.swing.JButton();
        collapseAllButton = new javax.swing.JButton();
        resetAllButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(425, 250));
        setPreferredSize(new java.awt.Dimension(425, 250));
        setRequestFocusEnabled(false);

        beanTree.setMinimumSize(new java.awt.Dimension(250, 100));
        beanTree.setPreferredSize(new java.awt.Dimension(425, 400));

        jPanel1.setPreferredSize(new java.awt.Dimension(425, 23));

        runViewButton.setText("Run View");
        runViewButton.setToolTipText("Return to viewing all Calibration profiles");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Av OCF:");
        jLabel2.setToolTipText("Average OCF derived from all of the average calibration profiles");

        avProfileOCFdisplay.setEditable(false);
        avProfileOCFdisplay.setColumns(8);
        avProfileOCFdisplay.setToolTipText("The average OCF +/-CV");

        fmaxNrmzBox.setText("Fmax Normalize");
        fmaxNrmzBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fmaxNrmzBoxActionPerformed(evt);
            }
        });

        jButton1.setText("X");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        expandAllButton.setText("Expand All");
        expandAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandAllButtonActionPerformed(evt);
            }
        });

        collapseAllButton.setText("Collapse All");
        collapseAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapseAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(runViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(expandAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(collapseAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(232, 232, 232)
                .addComponent(fmaxNrmzBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runViewButton)
                    .addComponent(jLabel2)
                    .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fmaxNrmzBox)
                    .addComponent(jButton1)
                    .addComponent(expandAllButton)
                    .addComponent(collapseAllButton))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        resetAllButton.setText("Reset All");
        resetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(beanTree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(resetAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetAllButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beanTree, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        runViewButton.setSelected(true);
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
    }//GEN-LAST:event_runViewButtonActionPerformed

    @SuppressWarnings("unchecked")
    private void fmaxNrmzBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fmaxNrmzBoxActionPerformed
        boolean arg = fmaxNrmzBox.isSelected();
        if (!calbnDB.isDatabaseOpen()) {
            fmaxNrmzBox.setSelected(false);
            return;
        }
        //Note that this is applied to all profiles in the database
        List<CalibrationRun> runList = calbnDB.getAllObjects(CalibrationRun.class);
        for (CalibrationRun run : runList) {
            for (AverageProfile avPrf : run.getAverageProfileList()) {
                AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) avPrf;
                avCalPrf.setIsOcfNormalizedToFmax(arg);
                calbnDB.saveObject(avCalPrf);
                run.calculateAverageOCF();
                calbnDB.saveObject(run);
                for (CalibrationProfile prf : avCalPrf.getReplicateProfileList()) {
                    prf.setIsOcfNormalizedToFmax(arg);
                    //There is no need to reinitiaze the profiles because 
                    //normalization DOES NOT CHANGE the Fo
                    calbnDB.saveObject(prf);
                }
            }
        }//End of Run for loop
        calDbInfo.setIsOcfNormalizedToFmax(arg);
        calbnDB.saveObject(calDbInfo);
        calbnDB.commitChanges();
        calcAverageOCF();
        createTree();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
    }//GEN-LAST:event_fmaxNrmzBoxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            CalFcExport.exportCalibrationProfileFcReadings(calbnDB);
        } catch (WriteException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void expandAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandAllButtonActionPerformed
        Queue<Node> nodesToExpand = new LinkedList<Node>();
        nodesToExpand.add(rootLreNode);
        Node currentNode;
        while ((currentNode = nodesToExpand.poll()) != null) {
            mgr.setExploredContext(currentNode);
            Node[] childNodes = currentNode.getChildren().getNodes();
            if (childNodes.length > 0) {
                nodesToExpand.addAll(Arrays.asList(childNodes));
            }
        }
    }//GEN-LAST:event_expandAllButtonActionPerformed

    private void collapseAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapseAllButtonActionPerformed
        createTree();
    }//GEN-LAST:event_collapseAllButtonActionPerformed

    private void resetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllButtonActionPerformed
        Queue<Node> nodesToReset = new LinkedList<>();
        nodesToReset.add(rootLreNode);
        Node currentNode;
        while ((currentNode = nodesToReset.poll()) != null) {
            LreObject profile = currentNode.getLookup().lookup(LreObject.class);
            if (profile instanceof Profile) {
                ProfileSummary profileSummary = new ProfileSummary((Profile) profile, calbnDB);
                lreAnalysisService.lreWindowInitialization(profileSummary, selectionParameters);
                ((LreNode) currentNode).refreshNodeLabel();
            }
            Node[] childNodes = currentNode.getChildren().getNodes();
            if (childNodes.length > 0) {
                nodesToReset.addAll(Arrays.asList(childNodes));
            }
        }
    }//GEN-LAST:event_resetAllButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField avProfileOCFdisplay;
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JButton collapseAllButton;
    private javax.swing.JButton expandAllButton;
    private javax.swing.JCheckBox fmaxNrmzBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton resetAllButton;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables
}
