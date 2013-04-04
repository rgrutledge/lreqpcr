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
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.calibration_ui.UpdateCalbrationDatabase;
import org.lreqpcr.calibration_ui.actions.CalbnTreeNodeActions;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationDbInfo;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationRun;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.core.utilities.ProfileUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class CalbnTree extends JPanel {

    private ExplorerManager mgr;
    private DatabaseServices calbnDB;
    private LreAnalysisService analysisService;
    LreWindowSelectionParameters selectionParameters;
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
        createTree();
    }

    @SuppressWarnings(value = "unchecked")
    public void createTree() {
        //A new calibration database has been opened
        if (!calbnDB.isDatabaseOpen()) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No CalbnDB is open");
            mgr.setRootContext(root);
            avProfileOCFdisplay.setText("");
            fixEmaxBox.setSelected(false);
            fmaxNrmzBox.setSelected(false);
//            if (statusLineMessage != null) {
//                statusLineMessage.clear(1);
//            }
            return;
        }
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        selectionParameters = (LreWindowSelectionParameters) calbnDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
        List<AverageCalibrationProfile> avCalPrfList =
                (List<AverageCalibrationProfile>) calbnDB.getAllObjects(AverageCalibrationProfile.class);
        if (!avCalPrfList.isEmpty()) {
            //Check if this version is too old to process, i.e. lacks a Run
            if (avCalPrfList.get(0).getRun() == null) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        "The selected calibration database is incompatible\n"
                        + " with this version of the LRE Analyzer",
                        "Unable to process this calibration database",
                        JOptionPane.ERROR_MESSAGE);
                AbstractNode root = new AbstractNode(Children.LEAF);
                root.setName("No CalbnDB is open");
                mgr.setRootContext(root);
                avProfileOCFdisplay.setText("");
                calbnDB.closeDatabase();
                return;
            }
        }
        List<CalibrationDbInfo> l = calbnDB.getAllObjects(CalibrationDbInfo.class);
        //Check if CalibrationDbInfo is present in the database
        //If not this must be an unconverted database
        if (l.isEmpty()) {
            //Assumes no Emax or Fmax normalization has not been applied, 
            //but this most certainly does not mattter
            calDbInfo = new CalibrationDbInfo();
            calbnDB.saveObject(calDbInfo);
            //Must be an old, unconverted database
            //For back compatablity, mainly for Fc plot, set the Run average Fmax
            UpdateCalbrationDatabase.updateCalibrationProfiles(calbnDB);
        } else {
            calDbInfo = l.get(0);
        }
        fixEmaxBox.setSelected(calDbInfo.isEmaxFixTo100Percent());
        fmaxNrmzBox.setSelected(calDbInfo.isOcfNormalizedToFmax());
        //This is only for testing
        List<AverageSampleProfile> avSamPrfList = calbnDB.getAllObjects(AverageSampleProfile.class);
        System.out.println("The number of average Calibration profiles = " + avCalPrfList.size());
        System.out.println("The number of average Sample profiles = " + avSamPrfList.size());

        //Setup the tree with CalbrationDbInfo in root
        //Retrieval all Calibration Runs from the database
        List<CalibrationRun> runList = (List<CalibrationRun>) calbnDB.getAllObjects(CalibrationRun.class);
        CalRootChildren calRootChildren = new CalRootChildren(mgr, calbnDB, runList, nodeActionFactory, nodeLabelFactory);
        LreNode root = new LreNode(calRootChildren, Lookups.singleton(calDbInfo), new Action[]{});
        root.setDatabaseService(calbnDB);
        File dbFile = calbnDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        String displayName = dbFileName.substring(0, length - 4);
        //Determine if the average Run Fmax should be displayed, i.e. when >1 Run is present
        if (runList.size() > 1) {
            //Calculate and display the average Run Fmax along with correlation of coefficient
            ProfileUtilities.calcAvFmaxForAllRuns(calbnDB);
            df.applyPattern("#0.0");
            String cv = df.format(calDbInfo.getAvRunFmaxCV() * 100);
            df.applyPattern(FormatingUtilities.decimalFormatPattern(calDbInfo.getAvRunFmax()));
            root.setDisplayName(displayName + " [Av Run Fmax: " + df.format(calDbInfo.getAvRunFmax()) + " ±" + cv + "%]");
        } else {
            root.setDisplayName(displayName);
        }
//        statusLineMessage = StatusDisplayer.getDefault().setStatusText(dbFile.getAbsolutePath(), 1);
        mgr.setRootContext(root);
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
            LreNode root = new LreNode(new CalRunChildren(mgr, calbnDB, avCalPrfList, nodeActionFactory,
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
        LreNode root = new LreNode(new CalRunChildren(mgr, calbnDB, avCalProfilList, nodeActionFactory, nodeLabelFactory),
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        beanTree = new BeanTreeView();
        runViewButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        avProfileOCFdisplay = new javax.swing.JTextField();
        fixEmaxBox = new javax.swing.JCheckBox();
        fmaxNrmzBox = new javax.swing.JCheckBox();

        setMinimumSize(new java.awt.Dimension(300, 570));
        setPreferredSize(new java.awt.Dimension(420, 570));
        setRequestFocusEnabled(false);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

        beanTree.setPreferredSize(new java.awt.Dimension(400, 500));
        jScrollPane1.setViewportView(beanTree);

        runViewButton.setText("View All");
        runViewButton.setToolTipText("Return to viewing all Calibration profiles");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("OCF:");
        jLabel2.setToolTipText("Average OCF derived from all of the average calibration profiles");

        avProfileOCFdisplay.setEditable(false);
        avProfileOCFdisplay.setColumns(8);
        avProfileOCFdisplay.setToolTipText("The average OCF +/-CV");

        fixEmaxBox.setText("<100%> Emax");
        fixEmaxBox.setToolTipText("Fix Emax to 100%");
        fixEmaxBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixEmaxBoxActionPerformed(evt);
            }
        });

        fmaxNrmzBox.setText("Fmax Normalize");
        fmaxNrmzBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fmaxNrmzBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runViewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fixEmaxBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fmaxNrmzBox)
                        .addGap(0, 2, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runViewButton)
                    .addComponent(jLabel2)
                    .addComponent(avProfileOCFdisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fixEmaxBox)
                    .addComponent(fmaxNrmzBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        runViewButton.setSelected(true);
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
    }//GEN-LAST:event_runViewButtonActionPerformed

    private void fixEmaxBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixEmaxBoxActionPerformed
        if (!calbnDB.isDatabaseOpen()) {
            fixEmaxBox.setSelected(false);
            return;
        }
        boolean arg = fixEmaxBox.isSelected();
        List<CalibrationRun> runList = calbnDB.getAllObjects(CalibrationRun.class);
        for (CalibrationRun run : runList) {
            for (AverageProfile avPrf : run.getAverageProfileList()) {
                if (run.getAverageProfileList() != null) {
                    AverageCalibrationProfile avCalPrf = (AverageCalibrationProfile) avPrf;
                    avCalPrf.setIsEmaxFixedTo100(arg);
                    analysisService.conductAutomatedLreWindowSelection(avCalPrf, selectionParameters);
                    calbnDB.saveObject(avCalPrf);
                    run.calculateAverageOCF();
                    calbnDB.saveObject(run);
                    for (Profile repProfile : avPrf.getReplicateProfileList()) {
                        //Ignore profiles that do not have an LRE window
                        if (repProfile.hasAnLreWindowBeenFound()) {
                            repProfile.setIsEmaxFixedTo100(arg);
                            analysisService.initializeProfileSummary(repProfile, selectionParameters);
                            calbnDB.saveObject(repProfile);
                        }
                    }
                }
            }
        }//End of Run loop
        calDbInfo.setIsEmaxFixTo100Percent(arg);
        calbnDB.saveObject(calDbInfo);
        calbnDB.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
    }//GEN-LAST:event_fixEmaxBoxActionPerformed

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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField avProfileOCFdisplay;
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JCheckBox fixEmaxBox;
    private javax.swing.JCheckBox fmaxNrmzBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables
}
