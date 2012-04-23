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
package org.lreqpcr.amplicon_ui;

import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.lreqpcr.amplicon_ui.actions.AmpliconTreeNodeActions;
import org.lreqpcr.amplicon_ui.components.AmpliconTreeNodeLabels;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.data_objects.AmpliconDbInfo;
import org.lreqpcr.core.data_objects.AmpliconImpl;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServiceFactory;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.lreqpcr.amplicon_ui//Amplicon//EN",
autostore = false)
public final class AmpliconTopComponent extends TopComponent
        implements ExplorerManager.Provider, DatabaseProvider, UniversalLookupListener {

    private static AmpliconTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "AmpliconTopComponent";
    private UniversalLookup universalLookup = UniversalLookup.getDefault();
    private DatabaseServices ampliconDB;
    private ExplorerManager mgr = new ExplorerManager();
    private AmpliconDbInfo dbInfo;
    private LreNode root;
    private LreActionFactory nodeActionFactory = new AmpliconTreeNodeActions(mgr);
    private LabelFactory nodeLabelFactory = new AmpliconTreeNodeLabels();

    @SuppressWarnings(value = "unchecked")
    public AmpliconTopComponent() {
        initComponents();
//        setName(NbBundle.getMessage(AmpliconTopComponent.class, "CTL_AmpliconTopComponent"));
        setToolTipText(NbBundle.getMessage(AmpliconTopComponent.class, "HINT_AmpliconTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        setName("Amplicon DB");
        associateLookup(ExplorerUtils.createLookup(mgr, this.getActionMap()));
        ampliconDB = Lookup.getDefault().lookup(DatabaseServiceFactory.class).createDatabaseService(DatabaseType.AMPLICON);
        if (ampliconDB != null) {
            universalLookup.add(DatabaseType.AMPLICON, ampliconDB);
        }
        universalLookup.addListner(PanelMessages.UPDATE_AMPLICON_PANELS, this);
        createTree();

    }

    @SuppressWarnings(value = "unchecked")
    public void createTree() {
        if (!ampliconDB.isDatabaseOpen()) {
            AbstractNode emptyRoot = new AbstractNode(Children.LEAF);
            emptyRoot.setName("No Amplicon database is open");
            mgr.setRootContext(emptyRoot);
            return;
        }
        // The AmpliconDbInfo allows Editing of Name and Notes for the database
        List l = ampliconDB.getAllObjects(AmpliconDbInfo.class);
        if (l.isEmpty()) {
            dbInfo = new AmpliconDbInfo();
            ampliconDB.saveObject(dbInfo);
        } else {
            dbInfo = (AmpliconDbInfo) l.get(0);
        }
        File dbFile = ampliconDB.getDatabaseFile();
        String dbFileName = dbFile.getName();
        int length = dbFileName.length();
//Simple tree listing all amplicons, i.e. not sorted in relation to target groups
//or the parent Target which are not implemented in this version
        List<? extends LreObject> childList =
                (List<? extends LreObject>) ampliconDB.getAllObjects(AmpliconImpl.class);
        root = new LreNode(new LreObjectChildren(mgr, ampliconDB, childList, nodeActionFactory,
                nodeLabelFactory), Lookups.singleton(dbInfo), new Action[]{});
        root.setExplorerManager(mgr);
        root.setDatabaseService(ampliconDB);
        String displayName = dbFileName.substring(0, length - 4) 
                + " (" + String.valueOf(childList.size()) + ")";
        root.setName(displayName);
        root.setShortDescription(dbFile.getAbsolutePath());
        mgr.setRootContext(root);
    }

    public void changeRootNodeChildren(LreObjectChildren newChildren) {
        root.changeChildren(newChildren);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openDBbutton = new javax.swing.JButton();
        newDBbutton = new javax.swing.JButton();
        closeDBbutton = new javax.swing.JButton();
        newAmpliconButton = new javax.swing.JButton();
        jScrollPane1 = new BeanTreeView();
        loadLastDatabaseButton = new javax.swing.JButton();

        openDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/amplicon_ui/Open24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(openDBbutton, null);
        openDBbutton.setToolTipText("Open an Amplicon database");
        openDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBbuttonActionPerformed(evt);
            }
        });

        newDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/amplicon_ui/New24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(newDBbutton, null);
        newDBbutton.setToolTipText("Create a new Amplicon database");
        newDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDBbuttonActionPerformed(evt);
            }
        });

        closeDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/amplicon_ui/Stop24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(closeDBbutton, null);
        closeDBbutton.setToolTipText("Close the Amplicon database");
        closeDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDBbuttonActionPerformed(evt);
            }
        });

        newAmpliconButton.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(newAmpliconButton, "New Amplicon");
        newAmpliconButton.setToolTipText("Create a new Amplicon");
        newAmpliconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newAmpliconButtonActionPerformed(evt);
            }
        });

        loadLastDatabaseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/amplicon_ui/Back24.gif"))); // NOI18N
        loadLastDatabaseButton.setToolTipText("Open the previoust Amplicon database");
        loadLastDatabaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadLastDatabaseButtonActionPerformed(evt);
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
                        .addComponent(loadLastDatabaseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newAmpliconButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 89, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeDBbutton, newDBbutton, openDBbutton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(openDBbutton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadLastDatabaseButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newDBbutton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newAmpliconButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings(value = "unchecked")
    private void openDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBbuttonActionPerformed
        if (ampliconDB.openUserSelectDatabaseFile()){
            createTree();
            String dbFileName = ampliconDB.getDatabaseFile().getName();
            int length = dbFileName.length();
            setDisplayName(dbFileName.substring(0, length - 4));
            setToolTipText(ampliconDB.getDatabaseFile().getName());
        }
    }//GEN-LAST:event_openDBbuttonActionPerformed

    private void newDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDBbuttonActionPerformed
        if (ampliconDB.createNewDatabaseFile()){
            createTree();
            String dbFileName = ampliconDB.getDatabaseFile().getName();
            int length = dbFileName.length();
            setDisplayName(dbFileName.substring(0, length - 4));
            setToolTipText(ampliconDB.getDatabaseFile().getName());
        }
    }//GEN-LAST:event_newDBbuttonActionPerformed

    private void closeDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDBbuttonActionPerformed
        if (ampliconDB.closeDatabase()) {
            createTree();
            setDisplayName("Amplicon DB");
            setToolTipText("Amplicon DB Explorer");
        }
    }//GEN-LAST:event_closeDBbuttonActionPerformed

    @SuppressWarnings(value = "unchecked")
    private void newAmpliconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newAmpliconButtonActionPerformed
        if (!ampliconDB.isDatabaseOpen()) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "An amplicon database has not been opened";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "No amplicon database",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Amplicon newAmplicon = new AmpliconImpl();
        newAmplicon.setName("");
        //This allows the key listener to process key strokes
        LreNode newNode = new LreNode(Children.LEAF, Lookups.singleton(newAmplicon), new Action[]{});
        newNode.setDatabaseService(ampliconDB);
        newNode.saveLreObject();//This also saves the new amplicon to disk
        //Need to update the tree and put the selection on the new node
        //Construct a new root children list, which in this version is a simply a list of amplicons
        List newChildList = ampliconDB.getAllObjects(AmpliconImpl.class);
        nodeActionFactory = new AmpliconTreeNodeActions(mgr);
        LreObjectChildren newChildren = new LreObjectChildren(mgr,
                ampliconDB, newChildList, nodeActionFactory, new AmpliconTreeNodeLabels());
        changeRootNodeChildren(newChildren);

        newNode = (LreNode) newChildren.findChild(newAmplicon.getName());
        try {
            mgr.setSelectedNodes(new Node[]{newNode});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_newAmpliconButtonActionPerformed

    private void loadLastDatabaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadLastDatabaseButtonActionPerformed
        if(ampliconDB.openLastDatabaseFile()){
            createTree();
            String dbFileName = ampliconDB.getDatabaseFile().getName();
            int length = dbFileName.length();
            setDisplayName(dbFileName.substring(0, length - 4));
            setToolTipText(ampliconDB.getDatabaseFile().getName());
        }
    }//GEN-LAST:event_loadLastDatabaseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeDBbutton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadLastDatabaseButton;
    private javax.swing.JButton newAmpliconButton;
    private javax.swing.JButton newDBbutton;
    private javax.swing.JButton openDBbutton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized AmpliconTopComponent getDefault() {
        if (instance == null) {
            instance = new AmpliconTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the AmpliconTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized AmpliconTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(AmpliconTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof AmpliconTopComponent) {
            return (AmpliconTopComponent) win;
        }
        Logger.getLogger(AmpliconTopComponent.class.getName()).warning(
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
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
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
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public DatabaseServices getDatabaseServices() {
        return ampliconDB;
    }

    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.UPDATE_AMPLICON_PANELS) {
            createTree();
        }
    }
}
