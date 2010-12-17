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
package org.lreqpcr.experiment_ui;

import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.data_objects.ExperimentDbInfo;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.RunImpl;
import org.lreqpcr.core.data_objects.Sample;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServiceFactory;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.lreqpcr.core.ui_elements.AmpliconNode;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.ui_elements.SampleNode;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.lreqpcr.data_export_services.DataExportServices;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Explorer window that displays sample profiles within an experiment database.
 */
@ConvertAsProperties(dtd = "-//org.lreqpcr.experiment_ui//Experiment//EN",
autostore = false)
public final class ExperimentTopComponent extends TopComponent
        implements ExplorerManager.Provider, DatabaseProvider, LookupListener, UniversalLookupListener {

    private static ExperimentTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ExperimentTopComponent";
    private ExplorerManager mgr = new ExplorerManager();
    private Lookup servicesLookup = Lookup.getDefault();
    private DatabaseServices experimentDB;
    private SettingsServices settingsDB;
    private final Result<SampleNode> sampleNodeResult;
    private final Result<AmpliconNode> ampliconNodeResult;

    public ExperimentTopComponent() {
        initComponents();
//        setName(NbBundle.getMessage(ExperimentTopComponent.class, "CTL_ExperimentTopComponent"));
        setToolTipText(NbBundle.getMessage(ExperimentTopComponent.class, "HINT_ExperimentTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.TRUE);

        setName("Experiment");
        associateLookup(ExplorerUtils.createLookup(mgr, this.getActionMap()));
        initServices();
        ampliconNodeResult = Utilities.actionsGlobalContext().lookupResult(AmpliconNode.class);
        ampliconNodeResult.allItems();
        ampliconNodeResult.addLookupListener(this);
        sampleNodeResult = Utilities.actionsGlobalContext().lookupResult(SampleNode.class);
        sampleNodeResult.allItems();
        sampleNodeResult.addLookupListener(this);
        experimentDbTree.initTreeView(mgr, experimentDB);
        UniversalLookup.getDefault().addListner(PanelMessages.NEW_RUN_IMPORTED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.UPDATE_EXPERIMENT_PANELS, this);
    }

    @SuppressWarnings(value = "unchecked")
    private void initServices() {
        experimentDB = servicesLookup.lookup(DatabaseServiceFactory.class).createDatabaseService(DatabaseType.EXPERIMENT);
        if (experimentDB != null) {
            UniversalLookup.getDefault().add(DatabaseType.EXPERIMENT, experimentDB);
        }
        settingsDB = servicesLookup.lookup(SettingsServices.class);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        experimentDbTree = new org.lreqpcr.experiment_ui.components.ExperimentDbTree();
        openDBbutton = new javax.swing.JButton();
        newDBbutton = new javax.swing.JButton();
        openLastDBbutton = new javax.swing.JButton();
        closeDBbutton = new javax.swing.JButton();
        exportRunsButton = new javax.swing.JButton();

        openDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/experiment_ui/Open24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(openDBbutton, org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.openDBbutton.text")); // NOI18N
        openDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.openDBbutton.toolTipText")); // NOI18N
        openDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBbuttonActionPerformed(evt);
            }
        });

        newDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/experiment_ui/New24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(newDBbutton, org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.newDBbutton.text")); // NOI18N
        newDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.newDBbutton.toolTipText")); // NOI18N
        newDBbutton.setPreferredSize(new java.awt.Dimension(33, 23));
        newDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDBbuttonActionPerformed(evt);
            }
        });

        openLastDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/experiment_ui/Back24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(openLastDBbutton, org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.openLastDBbutton.text")); // NOI18N
        openLastDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.openLastDBbutton.toolTipText")); // NOI18N
        openLastDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openLastDBbuttonActionPerformed(evt);
            }
        });

        closeDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/experiment_ui/Stop24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(closeDBbutton, org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.closeDBbutton.text")); // NOI18N
        closeDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.closeDBbutton.toolTipText")); // NOI18N
        closeDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDBbuttonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(exportRunsButton, org.openide.util.NbBundle.getMessage(ExperimentTopComponent.class, "ExperimentTopComponent.exportRunsButton.text")); // NOI18N
        exportRunsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportRunsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openLastDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(exportRunsButton)
                .addContainerGap(60, Short.MAX_VALUE))
            .addComponent(experimentDbTree, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeDBbutton, newDBbutton, openDBbutton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(closeDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(openLastDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(exportRunsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(experimentDbTree, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBbuttonActionPerformed
        boolean wasNewFileOpened = experimentDB.openDatabase();
        //False if the open was cancelled, so do nothing
        if (wasNewFileOpened) {
            experimentDbTree.createTree();
            UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, experimentDB);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
        }
    }//GEN-LAST:event_openDBbuttonActionPerformed

    private void newDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDBbuttonActionPerformed
        boolean wasNewFileCreated = experimentDB.createNewDatabase();
        //False if the action was cancelled, so do nothing
        if (wasNewFileCreated) {
//All Experiment databases must contain a lre window parameters and db info object
            experimentDB.saveObject(new LreWindowSelectionParameters());
            experimentDB.saveObject(new ExperimentDbInfo());
            experimentDB.commitChanges();
            experimentDbTree.createTree();
            UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, experimentDB);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
        }
    }//GEN-LAST:event_newDBbuttonActionPerformed

    private void openLastDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLastDBbuttonActionPerformed
        File previousFile = experimentDB.getDatabaseFile();
        File lastDBfile = settingsDB.getLastExperimentDatabaseFile();
        if (lastDBfile != null) {
            if (lastDBfile.exists()) {
                if (previousFile != null) {
                    settingsDB.setLastExperimentDatabaseFile(previousFile);
                }
                experimentDB.openDatabase(lastDBfile);
                experimentDbTree.createTree();
                UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, experimentDB);
                UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
            }
        } else {
            // TODO present an error dialog
        }
    }//GEN-LAST:event_openLastDBbuttonActionPerformed

    private void closeDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDBbuttonActionPerformed
        if (experimentDB.isDatabaseOpen()) {
            settingsDB.setLastExperimentDatabaseFile(experimentDB.getDatabaseFile());
            experimentDB.closeDatabase();
            experimentDbTree.createTree();
            UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, null);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
        }
    }//GEN-LAST:event_closeDBbuttonActionPerformed

    private void exportRunsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportRunsButtonActionPerformed
        Node[] nodes = mgr.getSelectedNodes();
        if (nodes.length == 0) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    "No Runs have not been selected",
                    "Must select a Run(s)",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        ArrayList<Run> runList = new ArrayList<Run>();
        for (Node node : nodes) {
            RunImpl run = node.getLookup().lookup(RunImpl.class);
            if (run != null) {//Excludes non-Run nodes such as the root or profile nodes
                runList.add(run);
            }
        }
        Lookup.getDefault().lookup(DataExportServices.class).exportAverageSampleProfiles(runList);
        return;
    }//GEN-LAST:event_exportRunsButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeDBbutton;
    private org.lreqpcr.experiment_ui.components.ExperimentDbTree experimentDbTree;
    private javax.swing.JButton exportRunsButton;
    private javax.swing.JButton newDBbutton;
    private javax.swing.JButton openDBbutton;
    private javax.swing.JButton openLastDBbutton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ExperimentTopComponent getDefault() {
        if (instance == null) {
            instance = new ExperimentTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ExperimentTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ExperimentTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ExperimentTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ExperimentTopComponent) {
            return (ExperimentTopComponent) win;
        }
        Logger.getLogger(ExperimentTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        //This is NOT called when the program is closed
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public DatabaseServices getDatabase() {
        return experimentDB;
    }

    @SuppressWarnings(value = "unchecked")
    public void resultChanged(LookupEvent ev) {
        Lookup.Result r = (Result) ev.getSource();
        Object[] c = r.allInstances().toArray();
        if (c.length > 0) {
            if (c[0] instanceof AmpliconNode) {
                AmpliconNode ampNode = (AmpliconNode) c[0];
                //Test to be sure that this node was derived from this window's database
                if (ampNode.getDatabase() != experimentDB) {
                    return;
                }
                Amplicon amplicon = ampNode.getLookup().lookup(Amplicon.class);
                experimentDbTree.creatAmpliconTree(amplicon.getName());
                return;
            }
            if (c[0] instanceof SampleNode) {
                SampleNode sampleNode = (SampleNode) c[0];
                //Test to be sure that this node was derived from this window's database
                if (sampleNode.getDatabase() != experimentDB) {
                    return;
                }
                Sample sample = sampleNode.getLookup().lookup(Sample.class);
                experimentDbTree.createSampleTree(sample.getName());
                return;
            }
        }
    }

    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.NEW_RUN_IMPORTED) {
            experimentDbTree.createTree();
            Run newRun = (Run) UniversalLookup.getDefault().getAll(key).get(0);
            //Open the tree to the new Run node
            LreObjectChildren children = (LreObjectChildren) mgr.getRootContext().getChildren();
            LreNode newRunNode = (LreNode) children.findChild(newRun.getName());
            mgr.setExploredContext(newRunNode);
        }
        if (key == PanelMessages.UPDATE_EXPERIMENT_PANELS) {
            experimentDbTree.createTree();
        }
    }
}
