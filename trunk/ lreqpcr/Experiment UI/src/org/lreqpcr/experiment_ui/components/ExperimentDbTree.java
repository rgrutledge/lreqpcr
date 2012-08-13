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
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.UniversalLookup;
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
    private double ocf;
    private ExperimentDbInfo dbInfo;
    private LreActionFactory nodeActionFactory;
    private LabelFactory runNodeLabelFactory;
    private LabelFactory sortedNodeLabelFactory;
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
        runNodeLabelFactory = (LabelFactory) new RunTreeNodeLabels();
        sortedNodeLabelFactory = (LabelFactory) new SortedProfileTreeNodeLabels();
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
            totalNumberOfProfiles.setText("n/a");
            return;
        }
        dbInfo = (ExperimentDbInfo) experimentDB.getAllObjects(ExperimentDbInfo.class).get(0);
        File dbFile = experimentDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        displayTotalNumberOfProfilesInTheDatabase();
        String displayName = dbFileName.substring(0, length - 4);
        ocf = dbInfo.getOcf();
        df.applyPattern(FormatingUtilities.decimalFormatPattern(ocf));
        ocfDisplay.setText(df.format(ocf));
        //Retrieval all Runs from the database
        List<? extends Run> runList =
                (List<? extends Run>) experimentDB.getAllObjects(Run.class);
        LreNode root = new LreNode(new RootRunChildren(mgr, experimentDB, runList, nodeActionFactory,
                runNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
        root.setDatabaseService(experimentDB);
        root.setDisplayName(displayName);
        root.setShortDescription(dbFile.getAbsolutePath());
        mgr.setRootContext(root);
    }

    public void displayTotalNumberOfProfilesInTheDatabase() {
        //Determine the total number of profiles in the database
        List profiles = experimentDB.getAllObjects(Profile.class);
        totalNumberOfProfiles.setText(String.valueOf(profiles.size()));
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
            LreNode root = new LreNode(new RunChildren(mgr, experimentDB, avSampleProfileList, nodeActionFactory,
                    sortedNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
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
            LreNode root = new LreNode(new RunChildren(mgr, experimentDB, avSampleProfileList, nodeActionFactory,
                    sortedNodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
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
    private void resetToNewOcf() {
        if (!experimentDB.isDatabaseOpen()) {
            return;
        }
        List<Profile> avSampleProfileList =
                (List<Profile>) experimentDB.getAllObjects(AverageSampleProfile.class);
        for (Profile profile : avSampleProfileList) {
            AverageSampleProfile avProfile = (AverageSampleProfile) profile;
            //This is needed for back compatability due to Run not being set < version 0.8.0
            Run run = null;
            if (profile.getRun() == null){
                run = (Run) profile.getParent();
            }else {
                run = profile.getRun();
            }
            if (run.getRunOCF() == 0) {
                //Reset the profile ocf to the new ocf
                //Neeed to update the replicate profiles before updating the average profiles
                //This is because average profile updating depends on replicate profile No values
                for (SampleProfile repProfile : avProfile.getReplicateProfileList()) {
                    repProfile.setOCF(ocf);
                    experimentDB.saveObject(repProfile);
                }
                avProfile.setOCF(ocf);
//Need to check if the LRE window needs reinitialization when av No increases to above >10 molecules
                if (!avProfile.isReplicateAverageNoLessThan10Molecules() && !avProfile.hasAnLreWindowBeenFound()){
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
        jLabel2 = new javax.swing.JLabel();
        totalNumberOfProfiles = new javax.swing.JLabel();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

        beanTree.setPreferredSize(new java.awt.Dimension(200, 100));
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

        jLabel2.setText("Total # of Profiles:");
        jLabel2.setToolTipText("The total number of profiles present in the database");

        totalNumberOfProfiles.setText("       ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runViewButton)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalNumberOfProfiles, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runViewButton)
                    .addComponent(jLabel1)
                    .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(totalNumberOfProfiles))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
}//GEN-LAST:event_runViewButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField ocfDisplay;
    private javax.swing.JRadioButton runViewButton;
    private javax.swing.JLabel totalNumberOfProfiles;
    // End of variables declaration//GEN-END:variables
}
