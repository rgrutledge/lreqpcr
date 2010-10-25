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

import org.lreqpcr.amplicon_ui.amplicon_io.AmpliconExcelExport;
import java.io.IOException;
import jxl.write.WriteException;
import org.lreqpcr.core.data_objects.AmpliconImpl;
import org.lreqpcr.core.ui_elements.LreNode;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.lreqpcr.amplicon_ui.amplicon_io.AmpliconImport;
import org.lreqpcr.amplicon_ui.amplicon_io.AmpliconImportExcelTemplate;
import org.lreqpcr.core.database_services.DatabaseServiceFactory;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Bob Rutledge
 */
public class AmpliconDbFrame extends javax.swing.JFrame implements PropertyChangeListener,
        ExplorerManager.Provider {

    private UniversalLookup universalLookup = UniversalLookup.getDefault();
    private SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);
    private DatabaseServices ampliconDB;
    private ExplorerManager mgr;

    @SuppressWarnings(value = "unchecked")
    public AmpliconDbFrame() {

        initComponents();
        ampliconDB = Lookup.getDefault().lookup(DatabaseServiceFactory.class).createDatabaseService(DatabaseType.AMPLICON);
        universalLookup.add(DatabaseType.AMPLICON, ampliconDB);
        File lastDBfile = settingsDB.getLastAmpliconDatabaseFile();
        if (lastDBfile != null) {
            if (lastDBfile.exists()) {
      
                ampliconDB.openDatabase(lastDBfile);
                universalLookup.fireChangeEvent(DatabaseType.AMPLICON);
            }
        }
//        mgr = ampliconTree1.getExplorerManager();
        mgr.addPropertyChangeListener(this);
//        ampliconTree1.createTree(ampliconDB);
//        ampliconEditPanel1.initAmpEditPanel(ampliconDB);
//        ampliconEditPanel1.reopenAmpEditPanel(mgr);

//        addWindowListener(new WindowAdapter() {
//
//            @Override
//            public void windowClosing(WindowEvent e) {
//      //Note that although presenting amplicons as a simple list prevents the production of duplicates,
//      //closing the database file has been deactivated because
//      //it is known that duplicates can be produced when a database file is closed and
//      //then reopened. This is produced by the generation of
//      //multiple instances of the db4o object container.
////                if (ampliconDB.isDatabaseOpen()) {
////                    ampliconDB.closeDatabase();
////                }
////                universalLookup.fireChangeEvent(DatabaseType.AMPLICON);
//                dispose();
//            }
//        });

//        setLocationRelativeTo(null);
    }

    /**
     * This method has been inactivated in order to prevent the production 
     * of duplicates that occur when the same database file is closed and then reopened.
     * This appears to be caused by the fact that the DB4O object container 
     * remains active even when a database file is closed. Thus, reopening a 
     * database file will generate two instances of the DB4O object containers
     * connected to the same database file,
     * which can lead to the generation of duplicate objects within the database
     * file. Review of recently updated DB4O documentation has confirmed this
     */
//    public void reinitDatabase() {
//        File lastDBfile = settingsDB.getLastAmpliconDatabaseFile();
//        if (lastDBfile != null) {
//            if (lastDBfile.exists()) {
//                ampliconDB.openDatabase(lastDBfile);
//                ampliconTree1.createTree(ampliconDB);
//                ampliconEditPanel1.clearPanel();
//                ampliconEditPanel1.reopenAmpEditPanel(mgr);
//                universalLookup.fireChangeEvent(DatabaseType.AMPLICON);
//            }
//        }
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        ampliconEditPanel1 = new org.lreqpcr.ui_components.AmpliconEditPanel();
        newDBbutton = new javax.swing.JButton();
        openDBbutton = new javax.swing.JButton();
        importTemplate = new javax.swing.JButton();
        importAmplicon = new javax.swing.JButton();
        exportAllAmplicons = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModalExclusionType(null);

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setRightComponent(ampliconEditPanel1);

        newDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/amplicon_ui/New24.gif"))); // NOI18N
        newDBbutton.setToolTipText("Create a new database");
        newDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDBbuttonActionPerformed(evt);
            }
        });

        openDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/amplicon_ui/Open24.gif"))); // NOI18N
        openDBbutton.setToolTipText("Open a database");
        openDBbutton.setFocusable(false);
        openDBbutton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openDBbutton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBbuttonActionPerformed(evt);
            }
        });

        importTemplate.setText("Import Template");
        importTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importTemplateActionPerformed(evt);
            }
        });

        importAmplicon.setText("Import Amplicons");
        importAmplicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importAmpliconActionPerformed(evt);
            }
        });

        exportAllAmplicons.setText("Export All Amplicons");
        exportAllAmplicons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAllAmpliconsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(194, 194, 194)
                        .addComponent(importTemplate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importAmplicon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                        .addComponent(exportAllAmplicons)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(importTemplate)
                        .addComponent(importAmplicon)
                        .addComponent(exportAllAmplicons)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This is an unimplemented attempt to share amplicons across multiple
     * Amplicon databases
     * @param evt
     */
    private void importTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importTemplateActionPerformed
        try {
            AmpliconImportExcelTemplate.createAmpliconImportTemplate();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WriteException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_importTemplateActionPerformed

    private void importAmpliconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importAmpliconActionPerformed
        AmpliconImport.importAmplicon(ampliconDB);
//        ampliconTree1.createTree(ampliconDB);
        ampliconEditPanel1.clearPanel();
    }//GEN-LAST:event_importAmpliconActionPerformed

    private void exportAllAmpliconsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAllAmpliconsActionPerformed
        try {
            AmpliconExcelExport.exportAmplicons(ampliconDB);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (WriteException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_exportAllAmpliconsActionPerformed

@SuppressWarnings(value = "unchecked")
    private void newDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDBbuttonActionPerformed
        boolean wasNewFileOpened = ampliconDB.createNewDatabase();
        if (wasNewFileOpened) {
            //Trigger panel updates
            universalLookup.fireChangeEvent(DatabaseType.AMPLICON);
//            ampliconTree1.createTree(ampliconDB);
            ampliconEditPanel1.clearPanel();
//            ampliconEditPanel1.reopenAmpEditPanel(mgr);
        }
    }//GEN-LAST:event_newDBbuttonActionPerformed

    private void openDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBbuttonActionPerformed
        boolean wasNewFileCreated = ampliconDB.openDatabase();
        if (wasNewFileCreated) {
            //This triggers panel updates
            universalLookup.fireChangeEvent(DatabaseType.AMPLICON);
//            ampliconTree1.createTree(ampliconDB);
            ampliconEditPanel1.clearPanel();
//            ampliconEditPanel1.reopenAmpEditPanel(mgr);
        }
    }//GEN-LAST:event_openDBbuttonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.lreqpcr.ui_components.AmpliconEditPanel ampliconEditPanel1;
    private javax.swing.JButton exportAllAmplicons;
    private javax.swing.JButton importAmplicon;
    private javax.swing.JButton importTemplate;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton newDBbutton;
    private javax.swing.JButton openDBbutton;
    // End of variables declaration//GEN-END:variables

    public void propertyChange(PropertyChangeEvent evt) {
//Listens for changes in Node selection in the Amplicon tree
        if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            //a new node has been selected
            Node[] nodes = mgr.getSelectedNodes();
            if (nodes.length > 0) {//Likely not needed
                LreNode selectedNode = (LreNode) nodes[0];
                //Update the edit window 
                if (selectedNode.getLookup().lookup(AmpliconImpl.class) != null) {
//                    ampliconEditPanel1.viewAmpliconNode(selectedNode);
                } else {
                    ampliconEditPanel1.clearPanel();
                }
            }
        }
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }
}
