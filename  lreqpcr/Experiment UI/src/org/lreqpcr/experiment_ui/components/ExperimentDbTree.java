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
package org.lreqpcr.experiment_ui.components;

import java.awt.Toolkit;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.experiment_ui.actions.ExperimentTreeNodeActions;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Tree-based view of an Experiment database
 *
 * This is test version that attempts to increase
 * tree display performance by moving to a run-specific
 * node construction that avoids searching the all objects
 * within the database
 * 
 * @author Bob Rutledge
 */
public class ExperimentDbTree extends JPanel {

    private ExplorerManager mgr;
    private DatabaseServices experimentDB;
    private double averageOCF;
    private ExperimentDbInfo dbInfo;
    private LreActionFactory nodeActionFactory;
    private LabelFactory runNodeLabelFactory;
    private LabelFactory sortNodeLabelFactory;
    private DecimalFormat df = new DecimalFormat();

    /** Creates new form ExperimentDbTree */
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
                    String ocf = ocfDisplay.getText();
                    while (ocf.contains(",")) {
                        int index = ocf.indexOf(",");
                        ocf = ocf.substring(0, index) + ocf.substring(index + 1);
                    }
                    try {
                        averageOCF = Double.valueOf(ocf);
                        df.applyPattern(FormatingUtilities.decimalFormatPattern(averageOCF));
                        ocfDisplay.setText(df.format(averageOCF));
                        dbInfo.setOcf(averageOCF);
                        experimentDB.saveObject(dbInfo);
                        updateAllNo();
                    } catch (NumberFormatException nan) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(null,
                                "The OCF must be a valid number" + "\n"
                                + "   Please click on the OK button",
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
        runNodeLabelFactory = (LabelFactory) new RunTreeNodeLabels();
        sortNodeLabelFactory = (LabelFactory) new ProfileTreeNodeLabels();
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
            return;
        }
        dbInfo = (ExperimentDbInfo) experimentDB.getAllObjects(ExperimentDbInfo.class).get(0);
        File dbFile = experimentDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        String displayName = dbFileName.substring(0, length - 4);
        averageOCF = dbInfo.getOcf();
        df.applyPattern(FormatingUtilities.decimalFormatPattern(averageOCF));
        ocfDisplay.setText(df.format(averageOCF));
//      Retrieval all Runs from the database
        List<? extends Run> runList =
                (List<? extends Run>) experimentDB.getAllObjects(Run.class);
        LreNode root = new LreNode(new RootRunChildren(mgr, experimentDB, runList, nodeActionFactory,
                runNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
        root.setDatabaseService(experimentDB);
        root.setDisplayName(displayName);
        root.setShortDescription(dbFile.getAbsolutePath());

        mgr.setRootContext(root);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    /**
     * Creates a tree displaying all AverageSampleProfiles created using the designated
     * amplicon.
     * @param ampName the amplicon used to create the AverageSampleProfile
     */
    @SuppressWarnings(value = "unchecked")
    public void creatAmpliconTree(String ampName) {
        if (experimentDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfile that used the amplicon "ampName"
            List avSampleProfileList = experimentDB.retrieveUsingFieldValue(AverageSampleProfile.class, "ampliconName", ampName);
            //Display the list of AverageSampleProfiles
            LreNode root = new LreNode(new AvSampleProfileListChildren(mgr, experimentDB, avSampleProfileList, nodeActionFactory,
                    sortNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
            root.setName(ampName + " (" + String.valueOf(avSampleProfileList.size()) + ")");
            root.setDatabaseService(experimentDB);
            mgr.setRootContext(root);
            runViewButton.setSelected(false);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        }
    }

    /**
     * Creates a tree displaying all AverageSampleProfiles created using 
     * the designated sample.
     * 
     * @param sampleName the sample used to create the AverageSampleProfile
     */
    @SuppressWarnings(value = "unchecked")
    public void createSampleTree(String sampleName) {
        if (experimentDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfiles generated by the sample "sampleName
            List avSampleProfileList = experimentDB.retrieveUsingFieldValue(AverageSampleProfile.class, "sampleName", sampleName);
            //Display the list of AverageSampleProfiles
            LreNode root = new LreNode(new AvSampleProfileListChildren(mgr, experimentDB, avSampleProfileList, nodeActionFactory, sortNodeLabelFactory),
                    Lookups.singleton(dbInfo), new Action[]{});
            root.setName(sampleName + " (" + String.valueOf(avSampleProfileList.size()) + ")");
            root.setDatabaseService(experimentDB);
            mgr.setRootContext(root);
            runViewButton.setSelected(false);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        }
    }

    public void createEmptyTree() {
        AbstractNode root = new AbstractNode(Children.LEAF);
        mgr.setRootContext(root);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
    }

    @SuppressWarnings(value = "unchecked")
    private void updateAllNo() {
        if (!experimentDB.isDatabaseOpen()) {
            return;
        }
        List<Profile> profileList =
                (List<Profile>) experimentDB.getAllObjects(Profile.class);
        for (Profile profile : profileList) {
            //Necessary for prerelease compatiblity 
            if (profile.getRun() != null) {
                //If the runOCF > 0 then a run-specific ocf has been set, so do nothing
                if (profile.getRun().getRunOCF() == 0) {
                    //Reset the profile ocf to the average ocf
                    profile.setOCF(averageOCF);
                    profile.updateProfile();
                    experimentDB.saveObject(profile);
                }
            } else {
                profile.setOCF(averageOCF);
                profile.updateProfile();
                experimentDB.saveObject(profile);
            }
        }
        createTree();
    }

    public double getOCF() {
        return averageOCF;
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
        jLabel1 = new javax.swing.JLabel();
        ocfDisplay = new javax.swing.JTextField();
        runViewButton = new javax.swing.JRadioButton();
        fixEmaxTo100Percent = new javax.swing.JRadioButton();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

        beanTree.setPreferredSize(new java.awt.Dimension(200, 100));
        jScrollPane1.setViewportView(beanTree);

        jLabel1.setText("Av. OCF (FU/ng):");

        ocfDisplay.setColumns(8);

        runViewButton.setText("Run View");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        fixEmaxTo100Percent.setText("Fix Emax to 100%");
        fixEmaxTo100Percent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixEmaxTo100PercentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runViewButton)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fixEmaxTo100Percent)
                        .addContainerGap(198, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(runViewButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixEmaxTo100Percent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
}//GEN-LAST:event_runViewButtonActionPerformed

    private void fixEmaxTo100PercentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixEmaxTo100PercentActionPerformed
        //Retrieve all sample profiles
        List allSampleProfiles = experimentDB.getAllObjects(SampleProfile.class);
        if (fixEmaxTo100Percent.isSelected()){
            //Set the flag to true
            for(Object object : allSampleProfiles){
                SampleProfile profile = (SampleProfile) object;
                profile.setIsEmaxFixedTo100Percent(true);
                profile.updateProfile();
            }
        }else{
            //Retrieve all sample profiles and set the flag to false
            for(Object object : allSampleProfiles){
                SampleProfile profile = (SampleProfile) object;
                profile.setIsEmaxFixedTo100Percent(false);
                profile.updateProfile();
            }
        }
    }//GEN-LAST:event_fixEmaxTo100PercentActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JRadioButton fixEmaxTo100Percent;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField ocfDisplay;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables
}
