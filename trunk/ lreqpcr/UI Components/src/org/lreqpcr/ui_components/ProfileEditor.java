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
package org.lreqpcr.ui_components;

import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.ui_elements.LreNode;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
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

    /** Creates new form ProfileView */
    public ProfileEditor() {
        initComponents();
        initProfileView();
    }

    @SuppressWarnings(value = "unchecked")
    private void initProfileView() {
        analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
        universalLookup = UniversalLookup.getDefault();
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

    private void displayProfile(Profile profile) {
        if (profile.isExcluded()) {
            clearPanels();
            lreObjectInfo.displayMember(selectedNode);
            return;
        }
//Display and editing of a profile is conducted through the ProfileSummary interface
        prfSum = analysisService.initializeProfile(profile);
        if (profile.getLreWinSize() == 0) {
            clearPanels();
            lreObjectInfo.displayMember(selectedNode);
            return;
        }
        plotLREs.iniPlotLREs(prfSum);
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
        setRequestFocusEnabled(false);

        numericalTable.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        numericalTable.setMaximumSize(new java.awt.Dimension(250, 200));
        numericalTable.setMinimumSize(new java.awt.Dimension(250, 200));
        numericalTable.setPreferredSize(new java.awt.Dimension(250, 200));

        plotFo.setMaximumSize(new java.awt.Dimension(325, 100));
        plotFo.setMinimumSize(new java.awt.Dimension(325, 100));
        plotFo.setPreferredSize(new java.awt.Dimension(325, 100));

        lreWindowParametersPanel.setMinimumSize(new java.awt.Dimension(267, 72));
        lreWindowParametersPanel.setPreferredSize(new java.awt.Dimension(267, 72));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lreObjectInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(plotFo, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(plotFc, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addComponent(plotLREs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lreWindowParametersPanel, 0, 0, Short.MAX_VALUE)
                            .addComponent(numericalTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lreWindowParametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(plotFc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plotLREs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numericalTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plotFo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lreObjectInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        Lookup.Result r = (Result) ev.getSource();
        Collection<LreNode> c = r.allInstances();
        if (!c.isEmpty()) {
            selectedNode = c.iterator().next();
            //Reject if this is an Amplicon database object
            DatabaseType type = selectedNode.getDatabaseServices().getDatabaseType();
            if (type == DatabaseType.AMPLICON) {
                clearPanels();
                return;
            }
            if (selectedNode.getDatabaseServices() != currentDB) {
                //A new database services has been opened
                clearPanels();
                currentDB = selectedNode.getDatabaseServices();
                List<LreWindowSelectionParameters> l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
                if (l.isEmpty()) {
//This occurs with databases created before implementation of the LRE window parameters
                    currentDB.saveObject(new LreWindowSelectionParameters());
                    l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
                }
                selectionParameters = l.get(0);
            }
            LreObject member = selectedNode.getLookup().lookup(LreObject.class);
            if (member instanceof Profile) {
                profile = (Profile) member;
                displayProfile(profile);
                return;
            }
            clearPanels();
            lreObjectInfo.displayMember(selectedNode);
            return;
        }
//This is a crude attempt to clear the panel when the selected window changes,
//but not when the Profile Editor is selected in order to allow profile editing
        TopComponent selectedTC = WindowManager.getDefault().getRegistry().getActivated();
        if (!selectedTC.getName().equals("Profile Editor")){
             clearPanels();//Not an LRE node and not the Profile Editor window
        }
    }

    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.CLEAR_PROFILE_EDITOR) {
            clearPanels();
        }
        if (key == PanelMessages.PROFILE_EXCLUDED 
                || key == PanelMessages.PROFILE_INCLUDED
                || key == PanelMessages.PROFILE_CHANGED) {
            //Need to update the panel
            LreObject member = selectedNode.getLookup().lookup(LreObject.class);
            if (member instanceof Profile) {
                profile = (Profile) member;
                displayProfile(profile);
            } else {
                clearPanels();
            }
            selectedNode.refreshNodeLabel();
        }
    }
}
