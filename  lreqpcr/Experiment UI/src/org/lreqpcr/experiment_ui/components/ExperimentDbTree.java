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

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.data_processing.ProfileSummaryImp;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.experiment_ui.actions.ExperimentTreeNodeActions;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.Lookup.Result;
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
public class ExperimentDbTree extends JPanel implements LookupListener {

    private ExplorerManager mgr;
    private DatabaseServices exptDB;
    private LreWindowSelectionParameters selectionParameters;
    private double ocf;
    private ExptDbInfo exptDbInfo;
    private LreActionFactory nodeActionFactory;
    private LabelFactory runNodeLabelFactory;
    private DecimalFormat df = new DecimalFormat();
    private Lookup.Result nodeResult;
    protected LreNode selectedNode;
    private boolean repView = false;
    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * Creates new form ExperimentDbTree
     */
    public ExperimentDbTree() {
        initComponents();
        runViewButton.setSelected(true);
        ocfDisplay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!exptDB.isDatabaseOpen()) {
                    return;
                }
                super.keyReleased(e);
                if (!exptDB.isDatabaseOpen()) {
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
                        exptDbInfo.setOcf(ocf);
                        exptDB.saveObject(exptDbInfo);
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
        nodeResult = Utilities.actionsGlobalContext().lookupResult(LreNode.class);
        nodeResult.allItems();
        nodeResult.addLookupListener(this);
    }

    public void initTreeView(ExplorerManager mgr, DatabaseServices db) {
        this.mgr = mgr;
        exptDB = db;
        //Note the the db does not have a database file open yet as this is called during component construction
        nodeActionFactory = new ExperimentTreeNodeActions(mgr);
        runNodeLabelFactory = new SampleTreeNodeLabels();
        createTree();
    }

    /**
     * Creates a tree displaying all runs within the experiment database
     */
    @SuppressWarnings(value = "unchecked")
    //A new experiment database has been opened
    public void createTree() {
        setCursor(waitCursor);
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        runViewButton.setSelected(true);
        if (!exptDB.isDatabaseOpen()) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No Experiment database is open");
            mgr.setRootContext(root);
            ocfDisplay.setText("");
            fmaxNormalizeChkBox.setSelected(false);
            setCursor(defaultCursor);
            return;
        }
        //Check if ExperimentDbInfo requires conversion to the new ExptDbInfo which extends DatabaseInfo
        //This is necessary because DatabaseInfo handles Fmax normalization
        //for databases containing Profiles as of 0.8.6
        List l = exptDB.getAllObjects(ExptDbInfo.class);
        if (l.isEmpty()) {
            ExptDbUpdate.exptDbConversion086(exptDB);
        }
        exptDbInfo = (ExptDbInfo) exptDB.getAllObjects(ExptDbInfo.class).get(0);
        selectionParameters = (LreWindowSelectionParameters) exptDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
        fmaxNormalizeChkBox.setSelected(exptDbInfo.isTargetQuantityNormalizedToFmax());
        File dbFile = exptDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
        String displayName = dbFileName.substring(0, length - 4);
        ocf = exptDbInfo.getOcf();
        df.applyPattern(FormatingUtilities.decimalFormatPattern(ocf));
        ocfDisplay.setText(df.format(ocf));
        //Calculate the average Run Fmax, which could reduce performance for larget databases
        //This is clearly a lazy and dirty method
        //Retrieval all Runs from the database
        LreNode root;
        List<? extends Run> runList = (List<? extends Run>) exptDB.getAllObjects(Run.class);
        if (!repView) {
            root = new LreNode(new RunNodesWithAvSampleProfileChildren(mgr, exptDB, runList, nodeActionFactory,
                    runNodeLabelFactory), Lookups.singleton(exptDbInfo), new Action[]{});
        } else {
            root = new LreNode(new RunNodesWithSampleProfileChildren(mgr, exptDB, runList, nodeActionFactory,
                    runNodeLabelFactory), Lookups.singleton(exptDbInfo), new Action[]{});
        }
        root.setDatabaseService(exptDB);
        root.setDisplayName(displayName);
        root.setShortDescription(dbFile.getAbsolutePath());
        mgr.setRootContext(root);
        setCursor(defaultCursor);
    }//End of create tree

    /**
     * Creates a tree displaying all AverageSampleProfiles generated by the
     * designated amplicon.
     *
     * @param ampName the amplicon used to generate the AverageSampleProfile
     */
    @SuppressWarnings(value = "unchecked")
    public void creatAmpliconTree(String ampName) {
        if (exptDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfile that used the amplicon "ampName"
            List avSampleProfileList = exptDB.retrieveUsingFieldValue(AverageSampleProfile.class, "ampliconName", ampName);
            //Display the list of AverageSampleProfiles
            LreNode root = new LreNode(new AvProfileNodesWithSampleProfileChildren(mgr, exptDB, avSampleProfileList, nodeActionFactory,
                    runNodeLabelFactory), Lookups.singleton(exptDbInfo), new Action[]{});
            root.setName(ampName + " (" + String.valueOf(avSampleProfileList.size()) + ")");
            root.setDatabaseService(exptDB);
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
        if (exptDB.isDatabaseOpen()) {
            //Retrieve all AverageSampleProfiles generated by the sample "sampleName
            List avSampleProfileList = exptDB.retrieveUsingFieldValue(AverageSampleProfile.class, "sampleName", sampleName);
            //Display the list of AverageSampleProfiles
            LreNode root = new LreNode(new AvProfileNodesWithSampleProfileChildren(mgr, exptDB, avSampleProfileList, nodeActionFactory,
                    runNodeLabelFactory), Lookups.singleton(exptDbInfo), new Action[]{});
            root.setName(sampleName + " (" + String.valueOf(avSampleProfileList.size()) + ")");
            root.setDatabaseService(exptDB);
            mgr.setRootContext(root);
            runViewButton.setSelected(false);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void resetToNewOcf() {
        setCursor(waitCursor);
        if (!exptDB.isDatabaseOpen()) {
            setCursor(defaultCursor);
            return;
        }
        List<Profile> avSampleProfileList =
                (List<Profile>) exptDB.getAllObjects(AverageSampleProfile.class);
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
                    exptDB.saveObject(repProfile);
                }
                avProfile.setOCF(ocf);
//Need to check if the LRE window needs reinitialization when av No increases to above >10 molecules
                if (!avProfile.isTheReplicateAverageNoLessThan10Molecules() && !avProfile.hasAnLreWindowBeenFound()) {
//>10N but no LRE window found indicates that the LRE window needs to be reiniitialized
                    LreAnalysisService lreAnalysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
                    selectionParameters = (LreWindowSelectionParameters) exptDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    ProfileSummary prfSum = new ProfileSummaryImp(avProfile, exptDB);
                    lreAnalysisService.lreWindowOptimizationUsingNonlinearRegression(prfSum, selectionParameters);
                }
            }
        }
        exptDB.commitChanges();
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

        beanTree = new BeanTreeView();
        jPanel1 = new javax.swing.JPanel();
        ocfLabel = new javax.swing.JLabel();
        ocfDisplay = new javax.swing.JTextField();
        fmaxNormalizeChkBox = new javax.swing.JCheckBox();
        runViewButton = new javax.swing.JRadioButton();
        repViewButton = new javax.swing.JRadioButton();

        setMinimumSize(new java.awt.Dimension(425, 170));
        setPreferredSize(new java.awt.Dimension(425, 170));
        setRequestFocusEnabled(false);

        beanTree.setMinimumSize(new java.awt.Dimension(250, 100));
        beanTree.setPreferredSize(new java.awt.Dimension(250, 100));

        jPanel1.setPreferredSize(new java.awt.Dimension(420, 24));

        ocfLabel.setText("OCF=");
        ocfLabel.setToolTipText("Converts fluorescence target quantities to the number of molecules");

        ocfDisplay.setColumns(10);
        ocfDisplay.setToolTipText("Manually enter an OCF value that will be applied to all profiles");

        fmaxNormalizeChkBox.setText("Fmax Normalize");
        fmaxNormalizeChkBox.setToolTipText("Normalize target quantities to the Run's average Fmax");
        fmaxNormalizeChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fmaxNormalizeChkBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(ocfLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fmaxNormalizeChkBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ocfLabel)
                    .addComponent(ocfDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fmaxNormalizeChkBox)))
        );

        runViewButton.setText("Run View");
        runViewButton.setToolTipText("Return to viewing Runs");
        runViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runViewButtonActionPerformed(evt);
            }
        });

        repViewButton.setText("Well View");
        repViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repViewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(beanTree, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(runViewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(repViewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(runViewButton)
                        .addComponent(repViewButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beanTree, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runViewButtonActionPerformed
        createTree();
        UniversalLookup.getDefault().add(PanelMessages.RUN_VIEW_SELECTED, null);
}//GEN-LAST:event_runViewButtonActionPerformed
    @SuppressWarnings(value = "unchecked")
    private void fmaxNormalizeChkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fmaxNormalizeChkBoxActionPerformed
        boolean arg = fmaxNormalizeChkBox.isSelected();
        if (!exptDB.isDatabaseOpen()) {
            fmaxNormalizeChkBox.setSelected(false);
            return;
        }
        setCursor(waitCursor);
        //Note that this is applied to all profiles in the database
        List<Run> runList = exptDB.getAllObjects(Run.class);
        for (Run run : runList) {
            for (AverageProfile avPrf : run.getAverageProfileList()) {
                AverageSampleProfile avSamPrf = (AverageSampleProfile) avPrf;
                avSamPrf.setIsTargetQuantityNormalizedToFmax(arg);
                exptDB.saveObject(avSamPrf);
                for (SampleProfile prf : avSamPrf.getReplicateProfileList()) {
                    prf.setIsTargetQuantityNormalizedToFmax(arg);
                    //There is no need to reinitiaze the profiles because 
                    //normalization DOES NOT CHANGE Fo
                    exptDB.saveObject(prf);
                }
            }
        }//End of Run for loop
        exptDbInfo.setIsTargetQuantityNormalizedToFmax(arg);
        exptDB.saveObject(exptDbInfo);
        exptDB.commitChanges();
        createTree();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
    }//GEN-LAST:event_fmaxNormalizeChkBoxActionPerformed

    private void repViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repViewButtonActionPerformed
        if (repViewButton.isSelected()) {
            repView = true;
            createTree();
        } else {
            repView = false;
            createTree();
        }
    }//GEN-LAST:event_repViewButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane beanTree;
    private javax.swing.JCheckBox fmaxNormalizeChkBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField ocfDisplay;
    private javax.swing.JLabel ocfLabel;
    private javax.swing.JRadioButton repViewButton;
    private javax.swing.JRadioButton runViewButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Retrieve and display a selected LRE Node.
     *
     * @param ev the lookup event
     */
    @SuppressWarnings(value = "unchecked")
    public void resultChanged(LookupEvent ev) {
        //A new node has been selected
        Lookup.Result r = (Result) ev.getSource();
        Collection<LreNode> c = r.allInstances();
        if (!c.isEmpty()) {
            selectedNode = c.iterator().next();
            //Reject if this is not an Sample node, that is derived from an Experiment database
            DatabaseType type = selectedNode.getDatabaseServices().getDatabaseType();
            if (type != DatabaseType.EXPERIMENT) {
                return;
            }
            LreObject member = selectedNode.getLookup().lookup(LreObject.class);
            if (member instanceof Profile) {
                Profile profile = (Profile) member;
                if (profile.getRun().getRunSpecificOCF() != 0) {
                    double rsOCF = profile.getRun().getRunSpecificOCF();
                    ocfLabel.setText("RS-OCF=");
                    ocfLabel.setToolTipText("Run-specific OCF");
                    ocfDisplay.setEditable(false);
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(rsOCF));
                    ocfDisplay.setText(df.format(rsOCF));
                } else {
                    ocfLabel.setText("OCF=");
                    ocfLabel.setToolTipText("Average OCF");
                    ocfDisplay.setEditable(true);
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(ocf));
                    ocfDisplay.setText(df.format(ocf));
                }
            }
            if (member instanceof Run) {
                Run run = (Run) member;
                if (run.getRunSpecificOCF() != 0) {
                    double rsOCF = run.getRunSpecificOCF();
                    ocfLabel.setText("RS-OCF=");
                    ocfLabel.setToolTipText("Run-specific OCF");
                    ocfDisplay.setEditable(false);
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(rsOCF));
                    ocfDisplay.setText(df.format(rsOCF));
                } else {
                    ocfLabel.setText("OCF=");
                    ocfLabel.setToolTipText("Average OCF");
                    ocfDisplay.setEditable(true);
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(ocf));
                    ocfDisplay.setText(df.format(ocf));
                }
            }
        }
    }
}
