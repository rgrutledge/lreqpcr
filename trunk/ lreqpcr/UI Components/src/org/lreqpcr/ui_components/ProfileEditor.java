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
package org.lreqpcr.ui_components;

import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class ProfileEditor extends JPanel implements
        LookupListener, UniversalLookupListener {

    private ProfileSummary prfSum;
    protected LreNode selectedNode;
    protected Profile profile;
    private LreAnalysisService analysisService;
    private LreWindowSelectionParameters selectionParameters;
    private DatabaseServices currentDB;
    private Lookup.Result nodeResult;
    private UniversalLookup universalLookup;

    /**
     * Creates new form ProfileView
     */
    public ProfileEditor() {
        initComponents();
        initProfileView();
    }

    @SuppressWarnings(value = "unchecked")
    private void initProfileView() {
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        universalLookup = UniversalLookup.getDefault();
        universalLookup.addListner(PanelMessages.NEW_DATABASE, this);
        universalLookup.addListner(PanelMessages.CLEAR_PROFILE_EDITOR, this);
        universalLookup.addListner(PanelMessages.PROFILE_EXCLUDED, this);
        universalLookup.addListner(PanelMessages.PROFILE_INCLUDED, this);
        universalLookup.addListner(PanelMessages.PROFILE_CHANGED, this);
        nodeResult = Utilities.actionsGlobalContext().lookupResult(LreNode.class);
        nodeResult.allItems();
        nodeResult.addLookupListener(this);
        plotLREs.clearPlot();
        if (currentDB != null) {
            if (currentDB.isDatabaseOpen()) {
                List<LreWindowSelectionParameters> l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
//This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
                selectionParameters = l.get(0);
            }
        }
    }

    private void displayNewProfile(Profile profile) {
        this.profile = profile;
        if (profile.isExcluded()) {
            clearPanels();
            lreObjectInfo.displayMember(selectedNode);
            return;
        }
        if (profile.hasAnLreWindowBeenFound()) {
//Display and editing of a profile is conducted through the ProfileSummary interface
            prfSum = analysisService.initializeProfileSummary(profile, selectionParameters);
            updatePanels();
        } else {//LRE window not found
            clearPanels();
            lreObjectInfo.displayMember(selectedNode);
        }

    }

    private void updatePanels() {
        //Test to see if this is an AverageProfile with just one replicate
        if (profile instanceof AverageSampleProfile) {
            AverageSampleProfile avSampleProfile = (AverageSampleProfile) profile;
            if (avSampleProfile.getTheNumberOfActiveReplicateProfiles() == 1
                    && !avSampleProfile.isExcluded()) {
//Display the AverageSampleProfile because it has only one replicate SampleProfile
                plotLREs.iniPlotLREs(prfSum, currentDB);
                numericalTable.iniNumTable(prfSum);
                plotFc.iniPlot(prfSum);
                plotFo.iniPlot(prfSum);
                lreObjectInfo.displayMember(selectedNode);
                return;
            }
        }
        if (profile instanceof AverageProfile) {
            AverageProfile avProfile = (AverageProfile) profile;
            if (avProfile.isTheReplicateAverageNoLessThan10Molecules()
                    || profile.isExcluded() //                    || !profile.hasAnLreWindowBeenFound()
                    ) {
                clearPanels();
                lreObjectInfo.displayMember(selectedNode);
                return;
            }
        }
        plotLREs.iniPlotLREs(prfSum, currentDB);
        numericalTable.iniNumTable(prfSum);
        plotFc.iniPlot(prfSum);
        plotFo.iniPlot(prfSum);
        lreObjectInfo.displayMember(selectedNode);
    }

    private void clearPanels() {
        profile = null;
        prfSum = null;
        plotFo.clearPlot();
        plotFc.clearPlot();
        plotLREs.clearPlot();
        numericalTable.clearTable();
        lreObjectInfo.clearPanel();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        numericalTable = new org.lreqpcr.ui_components.NumericalTable();
        plotLREs = new org.lreqpcr.ui_components.LrePlot();
        plotFc = new org.lreqpcr.ui_components.PlotFc();
        lreObjectInfo = new org.lreqpcr.ui_components.LreObjectInfo();
        plotFo = new org.lreqpcr.ui_components.PlotFo();
        lreWindowParametersPanel = new org.lreqpcr.ui_components.LreWindowParametersPanel();

        setBackground(new java.awt.Color(51, 153, 255));
        setMaximumSize(new java.awt.Dimension(630, 594));
        setMinimumSize(new java.awt.Dimension(630, 594));
        setPreferredSize(new java.awt.Dimension(630, 639));
        setRequestFocusEnabled(false);

        numericalTable.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        numericalTable.setMaximumSize(new java.awt.Dimension(250, 200));
        numericalTable.setMinimumSize(new java.awt.Dimension(250, 200));
        numericalTable.setPreferredSize(new java.awt.Dimension(250, 200));

        lreObjectInfo.setMaximumSize(new java.awt.Dimension(300, 300));
        lreObjectInfo.setPreferredSize(new java.awt.Dimension(300, 300));

        plotFo.setMaximumSize(new java.awt.Dimension(325, 100));
        plotFo.setMinimumSize(new java.awt.Dimension(325, 100));
        plotFo.setPreferredSize(new java.awt.Dimension(325, 100));

        lreWindowParametersPanel.setPreferredSize(new java.awt.Dimension(250, 100));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lreObjectInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plotFc, 0, 0, Short.MAX_VALUE)
                    .addComponent(plotLREs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plotFo, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numericalTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lreWindowParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(plotFc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plotLREs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lreObjectInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lreWindowParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(numericalTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(plotFo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.lreqpcr.ui_components.LreObjectInfo lreObjectInfo;
    private org.lreqpcr.ui_components.LreWindowParametersPanel lreWindowParametersPanel;
    private org.lreqpcr.ui_components.NumericalTable numericalTable;
    private org.lreqpcr.ui_components.PlotFc plotFc;
    private org.lreqpcr.ui_components.PlotFo plotFo;
    private org.lreqpcr.ui_components.LrePlot plotLREs;
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
            //Reject if this is an Amplicon database object
            DatabaseType type = selectedNode.getDatabaseServices().getDatabaseType();
            if (type == DatabaseType.AMPLICON) {
//                clearPanels();
                return;
            }
            if (selectedNode.getDatabaseServices() != currentDB) {
                //A new database has been opened
                clearPanels();
                currentDB = selectedNode.getDatabaseServices();
                List<LreWindowSelectionParameters> l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
                if (l.isEmpty()) {
                    // TODO delete this back compatability correction
//This occurs with databases created before implementation of the LRE window parameters
                    currentDB.saveObject(new LreWindowSelectionParameters());
                    l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
                }
                selectionParameters = l.get(0);
            }
            LreObject member = selectedNode.getLookup().lookup(LreObject.class);
            if (member instanceof Profile) {
                profile = (Profile) member;
                displayNewProfile(profile);
                return;
            }
            clearPanels();
            lreObjectInfo.displayMember(selectedNode);
            return;
        }
//This is a crude attempt to clear the panel when the selected window changes,
//but not when the Profile Editor is selected in order to allow profile editing
        TopComponent selectedTC = WindowManager.getDefault().getRegistry().getActivated();
        if (!selectedTC.getName().equals("Profile Editor")) {
//            clearPanels();//Not an LRE node and not the Profile Editor window
        }
    }

    /**
     * A general policy is that the parent panel is responsible for responding
     * to changes within it's children panels. E.g. if the user changes the LRE
     * window, this panel is responsible for initiating profile and display
     * updating.
     *
     * @param key
     */
    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.CLEAR_PROFILE_EDITOR) {
            clearPanels();
        }
        if (key == PanelMessages.PROFILE_EXCLUDED
                || key == PanelMessages.PROFILE_INCLUDED) {
            //Need to update the panel
            LreObject member = selectedNode.getLookup().lookup(LreObject.class);
            if (member instanceof Profile) {
                profile = (Profile) member;
                if (prfSum == null) {
                    displayNewProfile(profile);
                } else {
                    updatePanels();
                }
            } else {
                clearPanels();
            }
            selectedNode.refreshNodeLabel();
        }
        if (key == PanelMessages.PROFILE_CHANGED) {
            if (prfSum == null) {
                return;
            }
//Note that it is assumed that all necessary data processing has been conducted by the broadcasting function
            selectedNode.refreshNodeLabel();
            if (!(profile instanceof AverageProfile)) {
                AverageProfile avProfile = (AverageProfile) profile.getParent();
                if (avProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                    //Need to update the AveragProfile parent node labels
                    LreNode parentNode = (LreNode) selectedNode.getParentNode();
                    parentNode.refreshNodeLabel();
                }
            }
            updatePanels();
        }
        if (key == PanelMessages.NEW_DATABASE) {
            currentDB = (DatabaseServices) universalLookup.getAll(PanelMessages.NEW_DATABASE).get(0);
            if (currentDB == null) {
                selectionParameters = null;
                clearPanels();
            } else {
                if (currentDB.isDatabaseOpen()) {
                    selectionParameters = (LreWindowSelectionParameters) currentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                } else {
                    //This could cause problems 
                    selectionParameters = null;
                }
                clearPanels();
            }
        }
    }
}
