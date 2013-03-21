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
package org.lreqpcr.experiment_ui.components;

import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.ProfileUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.experiment_ui.actions.ExperimentTreeNodeActions;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * Tree-based view of an Experiment database
 *
 * This is test version that attempts to increase tree display performance by
 * moving to a run-specific node construction that avoids searching the all
 * objects within the database
 *
 * @author Bob Rutledge
 */
public class ExperimentDbTree extends JPanel {

    private ExplorerManager mgr;
    private DatabaseServices experimentDB;
    private double ocf;
    private ExperimentDbInfo dbInfo;
    private LreActionFactory nodeActionFactory;
    private LabelFactory runNodeLabelFactory;
    private DecimalFormat df = new DecimalFormat();
    private double avRunFmax = 0;
    private double avRunFmaxCV = 0;

    /**
     * Creates new form ExperimentDbTree
     */
    public ExperimentDbTree() {
        initComponents();
        runViewButton.setSelected(true);
        ocfDisplay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!experimentDB.isDatabaseOpen()) {
                    return;
                }
                super.keyReleased(e);
                if (!experimentDB.isDatabaseOpen()) {
                    return;
                }
                if (e.getKeyCode() == 10) {//"Return" key
                    //Remove any commas
                    String userSuppliedOcf = ocfDisplay.getText();
                    while (userSuppliedOcf.contains(",")) {
                        int index = userSuppliedOcf.indexOf(",");
                        userSuppliedOcf = userSuppliedOcf.substring(0, index) + userSuppliedOcf.substring(index + 1);
                    }
                    try {
                        ocf = Double.valueOf(userSuppliedOcf);
                        df.applyPattern(FormatingUtilities.decimalFormatPattern(ocf));
                        ocfDisplay.setText(df.format(ocf));
                        dbInfo.setOcf(ocf);
                        experimentDB.saveObject(dbInfo);
                        resetToNewOcf();
                    } catch (NumberFormatException nan) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                "The OCF must be a valid number",
                                "Invalid OCF",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    public void initTreeView(ExplorerManager mgr, DatabaseServices db) {
        this.mgr = mgr;
        experimentDB = db;
        nodeActionFactory = new ExperimentTreeNodeActions(mgr);
        runNodeLabelFactory = (LabelFactory) new SampleTreeNodeLabels();
        createTree();
    }

    /**
     * Creates a tree displaying all runs within the experiment database
     */
    @SuppressWarnings(value = "unchecked")
    public void createTree() {
        runViewButton.setSelected(true);
        if (!experimentDB.isDatabaseOpen()) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No Experiment database is open");
            mgr.setRootContext(root);
            ocfDisplay.setText("");
            fmaxNormalizeChkBox.setSelected(false);
            return;
        }
        dbInfo = (ExperimentDbInfo) experimentDB.getAllObjects(ExperimentDbInfo.class).get(0);
        fmaxNormalizeChkBox.setSelected(dbInfo.isTargetQuantityNormalizedToFax());
        File dbFile = experimentDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        String displayName = dbFileName.substring(0, length - 4);
        ocf = dbInfo.getOcf();
        df.applyPattern(FormatingUtilities.decimalFormatPattern(ocf));
        ocfDisplay.setText(df.format(ocf));
        //Calculate the average Run Fmax, which could reduce performance for larget databases
        //This is clearly a lazy and dirty method
        //Retrieval all Runs from the database
        List<? extends Run> runList =
                (List<? extends Run>) experimentDB.getAllObjects(Run.class);
        LreNode root = new LreNode(new SampleRootRunChildren(mgr, experimentDB, runList, nodeActionFactory,
                runNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
        root.setDatabaseService(experimentDB);
        //Determine if the average Run Fmax should be displayed, i.e. when >1 Run is present
        if (runList.size() > 1) {
            //Calculate and display the average Run Fmax along with correlation of coefficient
            //This could be intensive but currently deemed acceptable as it avoids complex situtations with new exp databases
            ProfileUtilities.calcAvFmaxForAllRuns(experimentDB);
            df.applyPattern("#0.0");
            String cv = df.format(avRunFmaxCV * 100);
            df.applyPattern(FormatingUtilities.decimalFormatPattern(avRunFmax));
            root.setDisplayName(displayName + " [Av Run Fmax: " + df.format(avRunFmax) + " ±" + cv + "%]");
        } else {
            root.setDisplayName(displayName);
        }
        root.setShortDescription(dbFile.getAbsolutePath());
        mgr.setRootContext(root);
    }//End of create tree

    /**
     * Creates a tree displaying all AverageSampleProfiles created using the
     * designated amplicon.
     *
     * @param ampName the amplicon used to create the AverageSampleProfile
     */
    @SuppressWarnings(value = "unchecked")
    public void creatAmpliconTree(String ampName) {
        if (experimentDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfile that used the amplicon "ampName"
            List avSampleProfileList = experimentDB.retrieveUsingFieldValue(AverageSampleProfile.class, "ampliconName", ampName);
            //Display the list of AverageSampleProfiles
            LreNode root = new LreNode(new SampleRunChildren(mgr, experimentDB, avSampleProfileList, nodeActionFactory,
                    runNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
            root.setName(ampName + " (" + String.valueOf(avSampleProfileList.size()) + ")");
            root.setDatabaseService(experimentDB);
            mgr.setRootContext(root);
            runViewButton.setSelected(false);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        }
    }

    /**
     * Creates a tree displaying all AverageSampleProfiles created using the
     * designated sample.
     *
     * @param sampleName the sample used to create the AverageSampleProfile
     */
    @SuppressWarnings(value = "unchecked")
    public void createSampleTree(String sampleName) {
        if (experimentDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfiles generated by the sample "sampleName
            List avSampleProfileList = experimentDB.retrieveUsingFieldValue(AverageSampleProfile.class, "sampleName", sampleName);
            //Display the list of AverageSampleProfiles
            LreNode root = new LreNode(new SampleRunChildren(mgr, experimentDB, avSampleProfileList, nodeActionFactory,
                    runNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
            root.setName(sampleName + " (" + String.valueOf(avSampleProfileList.size()) + ")");
            root.setDatabaseService(experimentDB);
            mgr.setRootContext(root);
            runViewButton.setSelected(false);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void resetToNewOcf() {
        if (!experimentDB.isDatabaseOpen()) {
            return;
        }
        List<Profile> avSampleProfileList =
                (List<Profile>) experimentDB.getAllObjects(AverageSampleProfile.class);
        for (Profile profile : avSampleProfileList) {
            AverageSampleProfile avProfile = (AverageSampleProfile) profile;
            //This is needed for back compatability due to Run not being set < version 0.8.0
            Run run;
            if (profile.getRun() == null) {
                run = (Run) profile.getParent();
            } else {
                run = profile.getRun();
            }
            if (run.getRunSpecificOCF() == 0) {
                //Reset the profile ocf to the new ocf
                //Neeed to update the replicate profiles before updating the average profiles
                //This is because average profile updating depends on replicate profile No values
                for (SampleProfile repProfile : avProfile.getReplicateProfileList()) {
                    repProfile.setOCF(ocf);
                    experimentDB.saveObject(repProfile);
                }
                avProfile.setOCF(ocf);
//Need to check if the LRE window needs reinitialization when av No increases to above >10 molecules
                if (!avProfile.isTheReplicateAverageNoLessThan10Molecules() && !avProfile.hasAnLreWindowBeenFound()) {
//>10N but no LRE window found indicates that the LRE window needs to be reiniitialized
                    LreAnalysisService lreAnalysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
                    LreWindowSelectionParameters selectionParameters = (LreWindowSelectionParameters) experimentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    lreAnalysisService.conductAutomatedLreWindowSelection(avProfile, selectionParameters);
                }
                experimentDB.saveObject(avProfile);
            }
        }
        experimentDB.commitChanges();
        createTree();
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
        jLabel1 = new javax.swing.JLabel();
        ocfDisplay = new javax.swing.JTextField();
        runViewButton = new javax.swing.JRadioButton();
        fmaxNormalizeChkBox = new javax.swing.JCheckBox();
        fixEmaxBox = new javax.swing.JCheckBox();

        setMinimumSize(new java.awt.Dimension(300, 400));
        setPreferredSize(new java.awt.Dimension(425, 600));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

        beanTree.setPreferredSize(new java.awt.Dimension(350, 500));
        jScrollPane1.setViewportView(beanTree);

        jLabel1.setText("OCF (FU/ng):");
        jLabel1.setToolTipText("Converts fluorescence target quantities to the number of molecules");

        ocfDisplay.setColumns(8);
        ocfDisplay.setToolTipText("Manually enter an OCF value that will be applied to all profiles");

        runViewButton.setText("Run View");
        runViewButton.setToolTipText("Return to viewing Runs");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        fmaxNormalizeChkBox.setText("Fmax Normalize");
        fmaxNormalizeChkBox.setToolTipText("Normalize target quantity to average Fmax");
        fmaxNormalizeChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fmaxNormalizeChkBoxActionPerformed(evt);
            }
        });

        fixEmaxBox.setText("<100%> Emax");
        fixEmaxBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixEmaxBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(runViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixEmaxBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fmaxNormalizeChkBox)
                .addContainerGap(9, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runViewButton)
                    .addComponent(jLabel1)
                    .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fmaxNormalizeChkBox)
                    .addComponent(fixEmaxBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 573, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
}//GEN-LAST:event_runViewButtonActionPerformed
    @SuppressWarnings(value = "unchecked")
    private void fmaxNormalizeChkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fmaxNormalizeChkBoxActionPerformed
        if (fmaxNormalizeChkBox.isSelected()) {
            //The checkbox is checked and so normalize all No values within the exp database to the average Fmax
            //Retrieve all of the SampleProfiles from the database, which should include the AverageSampleProfiles
            if (!experimentDB.isDatabaseOpen()) {
                fmaxNormalizeChkBox.setSelected(false);
                return;
            }
            List<SampleProfile> sampleProfileList =
                    (List<SampleProfile>) experimentDB.getAllObjects(SampleProfile.class);
            for (SampleProfile sampleProfile : sampleProfileList) {
                sampleProfile.setIsTargetQuantityNormalizedToFmax(true);
                experimentDB.saveObject(sampleProfile);
            }
            dbInfo.setIsTargetQuantityNormalizedToFax(true);
            createTree();
        } else {//The checkbox must be unchecked
            fmaxNormalizeChkBox.setSelected(false);
            if (!experimentDB.isDatabaseOpen()) {
                return;
            }
            List<SampleProfile> sampleProfileList =
                    (List<SampleProfile>) experimentDB.getAllObjects(SampleProfile.class);
            for (SampleProfile sampleProfile : sampleProfileList) {
                sampleProfile.setIsTargetQuantityNormalizedToFmax(false);
                experimentDB.saveObject(sampleProfile);
            }
            dbInfo.setIsTargetQuantityNormalizedToFax(false);
            createTree();
        }
        experimentDB.saveObject(dbInfo);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
    }//GEN-LAST:event_fmaxNormalizeChkBoxActionPerformed

    private void fixEmaxBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixEmaxBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fixEmaxBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JCheckBox fixEmaxBox;
    private javax.swing.JCheckBox fmaxNormalizeChkBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField ocfDisplay;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables
}
