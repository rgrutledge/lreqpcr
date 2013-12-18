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
package org.lreqpcr.amplicon_overview;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.lreqpcr.amplicon_overview.ui_components.AmpliconChildren;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.data_objects.AmpliconImpl;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.lreqpcr.data_export_services.DataExportServices;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.lreqpcr.amplicon_overview//AmpliconOverview//EN",
        autostore = false)
public final class AmpliconOverviewTopComponent extends TopComponent
        implements ExplorerManager.Provider, PropertyChangeListener, UniversalLookupListener {

    private static AmpliconOverviewTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "AmpliconOverviewTopComponent";
    private ExplorerManager mgr = new ExplorerManager();
    private DatabaseServices currentDB;
    private DatabaseType dbType;

    public AmpliconOverviewTopComponent() {
        initComponents();
//        setName(NbBundle.getMessage(AmpliconOverviewTopComponent.class, "CTL_AmpliconOverviewTopComponent"));
        setToolTipText(NbBundle.getMessage(AmpliconOverviewTopComponent.class, "HINT_AmpliconOverviewTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.FALSE);
        setName("Sort by Amplicon");
        associateLookup(ExplorerUtils.createLookup(mgr, this.getActionMap()));
        iniServices();
    }

    private void iniServices() {
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(this);
        UniversalLookup.getDefault().addListner(PanelMessages.RUN_VIEW_SELECTED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.NEW_DATABASE, this);
        UniversalLookup.getDefault().addListner(PanelMessages.RUN_IMPORTED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.RUN_DELETED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.PROFILE_DELETED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.PROFILE_EXCLUDED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.PROFILE_INCLUDED, this);
    }

    @SuppressWarnings("unchecked")
    private void createTree() {
        if (currentDB == null) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No database is open");
            mgr.setRootContext(root);
            exportProfilePanel.setVisible(true);
            return;
        }
        if (!currentDB.isDatabaseOpen()) {
            AbstractNode root = new AbstractNode(Children.LEAF);
            root.setName("No database is open");
            mgr.setRootContext(root);
//            if (dbType == DatabaseType.CALIBRATION) {
//                exportProfilePanel.setVisible(true);
//            } else {
//                exportProfilePanel.setVisible(true);
//            }
            return;
        }
//        if (dbType == DatabaseType.CALIBRATION) {
//            exportProfilePanel.setVisible(true);
//        } else {
//            exportProfilePanel.setVisible(true);
//        }
        List<Amplicon> ampList = getAmpliconList();
        Collections.sort(ampList);
        AbstractNode root = new AbstractNode(new AmpliconChildren(currentDB, ampList));
        root.setName("Amplicon Overview (" + String.valueOf(ampList.size()) + ")");
        mgr.setRootContext(root);
    }

    private List<Amplicon> getAmpliconList() {
        List<Amplicon> ampliconList = new ArrayList<Amplicon>();
        List avProfileList;
        if (dbType == DatabaseType.CALIBRATION) {
            avProfileList = currentDB.getAllObjects(AverageCalibrationProfile.class);
        } else {//Must be an Experiment database
            avProfileList = currentDB.getAllObjects(AverageSampleProfile.class);
        }
        //Collect all the amplicon names from the average profile list
        ArrayList<String> ampNameList = new ArrayList<String>();
        for (Object o : avProfileList) {
            Profile profile = (Profile) o;
            String ampName = profile.getAmpliconName();
            if (!ampNameList.contains(ampName)) {
                ampNameList.add(ampName);
            }
        }
        //Determine average Emax and its CV based only on AverageSampleProfiles with No >10 molecules
        //Construct a list of Amplicons using the ampList name
        for (String ampName : ampNameList) {
            Amplicon facadeAmplicon = new AmpliconImpl();
            //Amplicon objects have not yet been implemented in Experiment/Calibration databases
            //That is, an Amplicon is only represented as String name
            //Thus this facadeAmplicon is only used for display purposes
            facadeAmplicon.setName(ampName);
            //Retrieve all average profiles derived from this amplicon
            List ampliconNameAverageProfileList = currentDB.retrieveUsingFieldValue(AverageProfile.class, "ampliconName", ampName);
            //Compile a list of all Emax values
            ArrayList<Double> emaxArrayList = new ArrayList<Double>();
            //Generate an Emax average and CV for this list
            double emaxTotal = 0;
            for (int i = 0; i < ampliconNameAverageProfileList.size(); i++) {
                //Ignore the replicate profiles, i.e. this is based only on AverageSampleProfiles
                Profile profile = (Profile) ampliconNameAverageProfileList.get(i);
                AverageProfile avPrf = (AverageProfile) ampliconNameAverageProfileList.get(i);
                //If the profile is exclucded, do not include it
                if (!profile.isExcluded()
                        //Check if a profile is present i.e. not flat
                        && profile.hasAnLreWindowBeenFound()
                        && avPrf.areTheRepProfilesSufficientlyClustered()) {
                    if (dbType == DatabaseType.EXPERIMENT) {
                        AverageSampleProfile sampleProfile = (AverageSampleProfile) avPrf;
                        //Only includd AverageSampleProfiles with >10 molecules
                        if (!sampleProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                            emaxArrayList.add(profile.getEmax());
                            emaxTotal = emaxTotal + profile.getEmax();
                        }
                    } else {//Must be a CalibrationProfile
                        emaxArrayList.add(profile.getEmax());
                        emaxTotal = emaxTotal + profile.getEmax();
                    }
                }
            }
            facadeAmplicon.setEmaxAverage(emaxTotal / emaxArrayList.size());
            if (emaxArrayList.size() > 1) {
                facadeAmplicon.setEmaxCV(MathFunctions.calcStDev(emaxArrayList) / facadeAmplicon.getEmaxAverage());
            } else {
                facadeAmplicon.setEmaxCV(0);
            }
            ampliconList.add(facadeAmplicon);
        }
        return ampliconList;
    }

    private void noNodesSelectedError() {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                "No Amplicons have not been selected",
                "Must select an Amplicon(s) to export",
                JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, List<AverageSampleProfile>> getAvSamplePrfForSelectedAmplicons() {
        Node[] nodes = mgr.getSelectedNodes();
        HashMap<String, List<AverageSampleProfile>> groupList = new HashMap<String, List<AverageSampleProfile>>();
        for (Node node : nodes) {
            AmpliconImpl amplicon = node.getLookup().lookup(AmpliconImpl.class);
            if (amplicon != null) {
                List profileList = currentDB.retrieveUsingFieldValue(AverageSampleProfile.class, "ampliconName", amplicon.getName());
                groupList.put(amplicon.getName(), new ArrayList<AverageSampleProfile>(profileList));
            }
        }
        return groupList;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, List<AverageCalibrationProfile>> getAvCalibrationPrfForSelectedAmplicons() {
        Node[] nodes = mgr.getSelectedNodes();
        HashMap<String, List<AverageCalibrationProfile>> groupList = new HashMap<String, List<AverageCalibrationProfile>>();
        for (Node node : nodes) {
            AmpliconImpl amplicon = node.getLookup().lookup(AmpliconImpl.class);
            if (amplicon != null) {
                List profileList = currentDB.retrieveUsingFieldValue(AverageCalibrationProfile.class, "ampliconName", amplicon.getName());
                groupList.put(amplicon.getName(), new ArrayList<AverageCalibrationProfile>(profileList));
            }
        }
        return groupList;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, List<SampleProfile>> getSamplePrfsForSelectedAmplicons() {
        Node[] nodes = mgr.getSelectedNodes();
        HashMap<String, List<SampleProfile>> groupList = new HashMap<String, List<SampleProfile>>();
        for (Node node : nodes) {
            AmpliconImpl amplicon = node.getLookup().lookup(AmpliconImpl.class);
            if (amplicon != null) {
                List<SampleProfile> allProfileList = currentDB.retrieveUsingFieldValue(SampleProfile.class, "ampliconName", amplicon.getName());
                //This will also retrieve average profiles, so they must be removed
                //But the db4o list causes an error to revert to creating an ArrayList
                List<SampleProfile> samplePrfList = new ArrayList<SampleProfile>();
                for (SampleProfile samplePrf : allProfileList) {
                    if (samplePrf instanceof AverageProfile) {
                        //Do nothing
                    } else {
                        samplePrfList.add(samplePrf);
                    }
                }
                groupList.put(amplicon.getName(), samplePrfList);
            }
        }
        return groupList;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, List<CalibrationProfile>> getCalPrfsForSelectedAmplicons() {
        Node[] nodes = mgr.getSelectedNodes();
        HashMap<String, List<CalibrationProfile>> groupList = new HashMap<String, List<CalibrationProfile>>();
        for (Node node : nodes) {
            AmpliconImpl amplicon = node.getLookup().lookup(AmpliconImpl.class);
            if (amplicon != null) {
                List<CalibrationProfile> allProfileList = currentDB.retrieveUsingFieldValue(CalibrationProfile.class, "ampliconName", amplicon.getName());
                //This will also retrieve average profiles, so they must be removed
                //But the db4o list needs to be converted to an ArrayList
                List<CalibrationProfile> calibrationPrfList = new ArrayList<CalibrationProfile>();
                for (CalibrationProfile calPrf : allProfileList) {
                    if (calPrf instanceof AverageProfile) {
                        //Do nothing
                    } else {
                        calibrationPrfList.add(calPrf);
                    }
                }
                groupList.put(amplicon.getName(), calibrationPrfList);
            }
        }
        return groupList;
    }
        //Amplicon/Sample based export of Calibration profiles is not available in this version
//    private HashMap<String, List<CalibrationProfile>> getCalPrfsForSelectedAmplicons(){
//        Node[] nodes = mgr.getSelectedNodes();
//        HashMap<String, List<CalibrationProfile>> groupList = new HashMap<String, List<CalibrationProfile>>();
//        for (Node node : nodes) {
//            AmpliconImpl amplicon = node.getLookup().lookup(AmpliconImpl.class);
//            if (amplicon != null) {
//                    List<CalibrationProfile> profileList = currentDB.retrieveUsingFieldValue(CalibrationProfile.class, "ampliconName", amplicon.getName());
//                    //This will also retrieve average profiles, so they must be removed
//                    for (CalibrationProfile calPrf : profileList){
//                        if (calPrf instanceof AverageCalibrationProfile){
//                            profileList.remove(calPrf);
//                        }
//                    }
//                    groupList.put(amplicon.getName(), new ArrayList<CalibrationProfile>(profileList));
//            }
//        }
//        return groupList;
//    }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ampliconTree = new BeanTreeView();
        exportProfilePanel = new javax.swing.JPanel();
        exportAvProfileButton = new javax.swing.JButton();
        exportRepPrfsButton = new javax.swing.JButton();

        exportProfilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AmpliconOverviewTopComponent.class, "AmpliconOverviewTopComponent.exportProfilePanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(exportAvProfileButton, org.openide.util.NbBundle.getMessage(AmpliconOverviewTopComponent.class, "AmpliconOverviewTopComponent.exportAvProfileButton.text")); // NOI18N
        exportAvProfileButton.setToolTipText(org.openide.util.NbBundle.getMessage(AmpliconOverviewTopComponent.class, "AmpliconOverviewTopComponent.exportAvProfileButton.toolTipText")); // NOI18N
        exportAvProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAvProfileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(exportRepPrfsButton, org.openide.util.NbBundle.getMessage(AmpliconOverviewTopComponent.class, "AmpliconOverviewTopComponent.exportRepPrfsButton.text")); // NOI18N
        exportRepPrfsButton.setToolTipText(org.openide.util.NbBundle.getMessage(AmpliconOverviewTopComponent.class, "AmpliconOverviewTopComponent.exportRepPrfsButton.toolTipText")); // NOI18N
        exportRepPrfsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportRepPrfsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout exportProfilePanelLayout = new javax.swing.GroupLayout(exportProfilePanel);
        exportProfilePanel.setLayout(exportProfilePanelLayout);
        exportProfilePanelLayout.setHorizontalGroup(
            exportProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportProfilePanelLayout.createSequentialGroup()
                .addComponent(exportAvProfileButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(exportRepPrfsButton)
                .addContainerGap())
        );
        exportProfilePanelLayout.setVerticalGroup(
            exportProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportProfilePanelLayout.createSequentialGroup()
                .addGroup(exportProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportAvProfileButton)
                    .addComponent(exportRepPrfsButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ampliconTree, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(exportProfilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(exportProfilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ampliconTree, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportAvProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAvProfileButtonActionPerformed
        //Profile export is only available for Sample in this version so this check is redundant
        if (currentDB.getDatabaseType() == DatabaseType.EXPERIMENT) {
            HashMap<String, List<AverageSampleProfile>> groupList = getAvSamplePrfForSelectedAmplicons();
            if (!groupList.isEmpty()) {
                Lookup.getDefault().lookup(DataExportServices.class).exportAverageSampleProfiles(groupList);
            } else {
                noNodesSelectedError();
            }
        } else if (currentDB.getDatabaseType() == DatabaseType.CALIBRATION) {
            HashMap<String, List<AverageCalibrationProfile>> groupList = getAvCalibrationPrfForSelectedAmplicons();
            if (!groupList.isEmpty()) {
                Lookup.getDefault().lookup(DataExportServices.class).exportAverageCalibrationProfiles(groupList);
            } else {
                noNodesSelectedError();
            }
        }
    }//GEN-LAST:event_exportAvProfileButtonActionPerformed

    private void exportRepPrfsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportRepPrfsButtonActionPerformed
        //Profile export is only available for Sample in this version
        if (currentDB.getDatabaseType() == DatabaseType.EXPERIMENT) {
            HashMap<String, List<SampleProfile>> groupList = getSamplePrfsForSelectedAmplicons();
            if (!groupList.isEmpty()) {
                Lookup.getDefault().lookup(DataExportServices.class).exportReplicateSampleProfiles(groupList);
            } else {
                noNodesSelectedError();
            }
        } else if (currentDB.getDatabaseType() == DatabaseType.CALIBRATION) {
            HashMap<String, List<CalibrationProfile>> groupList = getCalPrfsForSelectedAmplicons();
            if (!groupList.isEmpty()) {
                Lookup.getDefault().lookup(DataExportServices.class).exportReplicateCalibrationProfiles(groupList);
            } else {
                noNodesSelectedError();
            }
        }
    }//GEN-LAST:event_exportRepPrfsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ampliconTree;
    private javax.swing.JButton exportAvProfileButton;
    private javax.swing.JPanel exportProfilePanel;
    private javax.swing.JButton exportRepPrfsButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized AmpliconOverviewTopComponent getDefault() {
        if (instance == null) {
            instance = new AmpliconOverviewTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the AmpliconOverviewTopComponent instance. Never call
     * {@link #getDefault} directly!
     */
    public static synchronized AmpliconOverviewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(AmpliconOverviewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof AmpliconOverviewTopComponent) {
            return (AmpliconOverviewTopComponent) win;
        }
        Logger.getLogger(AmpliconOverviewTopComponent.class.getName()).warning(
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

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
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

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //A new TC window has been selected
        TopComponent tc = getRegistry().getActivated();
        if (tc instanceof DatabaseProvider) {
            DatabaseProvider dbProvider = (DatabaseProvider) tc;
            if (dbProvider.getDatabaseServices() != currentDB) {
                //A new explorer window has been selected
                currentDB = dbProvider.getDatabaseServices();
                dbType = currentDB.getDatabaseType();
                createTree();//Not sure if this will be slow when large numbers of profiles are present in the database
            }
        }
    }

    @Override
    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.NEW_DATABASE) {//A New database has been opened
            DatabaseServices newDB = (DatabaseServices) UniversalLookup.getDefault().getAll(PanelMessages.NEW_DATABASE).get(0);
            if (newDB == null) {
                dbType = null;
                createTree();
                return;
            }
            if (newDB.getDatabaseType() == DatabaseType.EXPERIMENT || newDB.getDatabaseType() == DatabaseType.CALIBRATION) {
                currentDB = newDB;
                dbType = currentDB.getDatabaseType();
                createTree();//Not sure if this will be slow when large numbers of profiles are present in the database
                return;
            } else {//Must be an Amplicon database
                currentDB = null;
                dbType = null;
                createTree();
                return;
            }
        }
        if (key == PanelMessages.PROFILE_DELETED 
                || key == PanelMessages.RUN_IMPORTED
                || key == PanelMessages.RUN_DELETED
                || key == PanelMessages.PROFILE_EXCLUDED 
                || key == PanelMessages.PROFILE_INCLUDED
                || key == PanelMessages.PROFILE_DELETED) {
            createTree();//Not sure if this will be slow when large numbers of profiles are present in the database
        }
    }
}
