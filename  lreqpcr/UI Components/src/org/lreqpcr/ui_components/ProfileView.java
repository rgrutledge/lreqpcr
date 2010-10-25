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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JPanel;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.openide.util.Lookup;

/**
 *
 * @author Bob Rutledge
 */
public class ProfileView extends JPanel implements PropertyChangeListener,
        UniversalLookupListener {

    private UniversalLookup universalLookup = UniversalLookup.getDefault();
    private LreObject member;
    private ProfileSummary prfSum;
    protected LreNode selectedNode;
    protected Profile profile;
    private LreAnalysisService analysisService;
    private DecimalFormat df = new DecimalFormat();
    private DecimalFormat dfCV = new DecimalFormat();
    private LreWindowSelectionParameters selectionParameters;
    private DatabaseServices currentDB;

    /** Creates new form ProfileView */
    public ProfileView() {
        initComponents();
    }

    @SuppressWarnings(value = "unchecked")
    public void initProfileView(DatabaseServices db) {
        currentDB = db;
        universalLookup.addListner(DatabaseType.EXPERIMENT, this);
        universalLookup.addListner(DatabaseType.CALIBRATION, this);
        universalLookup.addListner(DatabaseType.AMPLICON, this);
        universalLookup.fireChangeEvent(DatabaseType.EXPERIMENT);//Updates the "Open Databases" panel
        Lookup servicesLookup = Lookup.getDefault();
        analysisService = servicesLookup.lookup(LreAnalysisService.class);
        plotLREs1.addPropertyChangeListener(this);
        plotLREs1.clearPlot();
        clearLreInfo();
//        lreWindowParametersPanel1.initiatePanel(currentDB);
        if(currentDB.isDatabaseOpen()){
            List<LreWindowSelectionParameters> l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
        //This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
        selectionParameters = l.get(0);
        }
        jPanel2.setVisible(false);
    }

    /**
     * Reinitializes the ProfileView panel when the parent panel is reactivated
     * after the parent panel was closed.
     * Note that the database must be restarted by the parent panel.
     */
    public void restartProfileView() {
        clearLreInfo();
//        lreWindowParametersPanel1.updateSelectionParameters();
    }

    /**
     * Initializes and displays the Profile held within the lookup of the
     * supplied Node
     * @param node the node to display
     */
    public void viewNode(LreNode node) {
        selectedNode = node;
        member = selectedNode.getLookup().lookup(LreObject.class);
        if (member == null) {//Precaution
            clearPanels();
            return;
        }
        if (member instanceof Profile) {
            profile = (Profile) member;
            displayProfile(profile);
            return;
        }
        if (member instanceof Run) {
            clearPanels();
            lreObjectInfo1.displayMember(selectedNode);
        } else {
            clearPanels();
            lreObjectInfo1.displayMember(selectedNode);
        }
    }

    private void displayProfile(Profile profile) {
        prfSum = analysisService.initializeProfile(profile);
        if (profile.getLreWinSize() == 0) {
            clearPanels();
            lreObjectInfo1.displayMember(selectedNode);
            return;
        }
        selectedNode.saveLreObject();
        plotLREs1.iniPlotLREs(prfSum);
        numericalTable1.iniNumTable(prfSum);
        plotFc1.iniPlot(prfSum);
        plotFo1.iniPlot(prfSum);
        lreObjectInfo1.displayMember(selectedNode);
        updateProfileInfo();
    }

    private void updateProfileInfo() {
        df.applyPattern("###,###");
        dfCV.applyPattern("0.00%");
        noDisplay.setText(df.format(prfSum.getProfile().getNo()) + "+/-" + dfCV.format(prfSum.getProfile().getAvFoCV()));
        df.applyPattern("#0.0%");
        eMaxDisplay.setText(df.format(profile.getEmax()));
        df.applyPattern("#0.0");
        midCdisplay.setText(df.format(profile.getMidC()));
    }

    private void clearLreInfo() {
        noDisplay.setText("");
        eMaxDisplay.setText("");
        midCdisplay.setText("");
    }

    private void updatePanels() {
        if(selectionParameters == null) {
            clearPanels();
            return;
        }
        if (prfSum != null) {
            analysisService.updateLreWindow(prfSum, selectionParameters);
            selectedNode.saveLreObject();
            plotLREs1.iniPlotLREs(prfSum);
            numericalTable1.iniNumTable(prfSum);
            plotFc1.iniPlot(prfSum);
            plotFo1.iniPlot(prfSum);
            updateProfileInfo();
        } else {
            clearPanels();
        }
    }

    public void clearPanels() {
        profile = null;
        prfSum = null;
        plotFo1.clearPlot();
        plotFc1.clearPlot();
        plotLREs1.clearPlot();
        numericalTable1.clearTable();
        lreObjectInfo1.clearPanel();
        clearLreInfo();
    }

    @SuppressWarnings(value = "unchecked")
    public void databaseHasChanged() {
        clearPanels();
        if(currentDB.isDatabaseOpen()){
            List<LreWindowSelectionParameters> l = currentDB.getAllObjects(LreWindowSelectionParameters.class);
        //This list should never be empty, as a LreWindowSelectionParameters object is created during DB creation
        selectionParameters = l.get(0);
        }
//        lreWindowParametersPanel1.updateSelectionParameters();
//        calcAvFoCVs();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        numericalTable1 = new org.lreqpcr.ui_components.NumericalTable();
        plotLREs1 = new org.lreqpcr.ui_components.LrePlot();
        plotFc1 = new org.lreqpcr.ui_components.PlotFc();
        lreObjectInfo1 = new org.lreqpcr.ui_components.LreObjectInfo();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        noDisplay = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        eMaxDisplay = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        midCdisplay = new javax.swing.JLabel();
        plotFo1 = new org.lreqpcr.ui_components.PlotFo();
        databaseInfoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        exptDbNameDisplay = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        calibrationDbNameDisplay = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ampDbNameDisplay = new javax.swing.JLabel();
        lreWindowParametersPanel1 = new org.lreqpcr.ui_components.LreWindowParametersPanel();

        setBackground(new java.awt.Color(51, 153, 255));
        setMaximumSize(new java.awt.Dimension(630, 594));
        setMinimumSize(new java.awt.Dimension(630, 594));
        setRequestFocusEnabled(false);

        numericalTable1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        numericalTable1.setMaximumSize(new java.awt.Dimension(250, 200));
        numericalTable1.setMinimumSize(new java.awt.Dimension(250, 200));
        numericalTable1.setPreferredSize(new java.awt.Dimension(250, 200));

        jPanel2.setBackground(new java.awt.Color(244, 245, 247));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setMaximumSize(new java.awt.Dimension(150, 100));
        jPanel2.setPreferredSize(new java.awt.Dimension(150, 100));

        jLabel3.setText("# Molecules:");

        noDisplay.setForeground(new java.awt.Color(255, 51, 0));
        noDisplay.setText("0");

        jLabel5.setText("Emax:");

        eMaxDisplay.setText("0");

        jLabel7.setText("C1/2:");

        midCdisplay.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(midCdisplay)
                            .addComponent(eMaxDisplay))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noDisplay)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noDisplay)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(eMaxDisplay))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(midCdisplay))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        plotFo1.setMaximumSize(new java.awt.Dimension(325, 100));
        plotFo1.setMinimumSize(new java.awt.Dimension(325, 100));
        plotFo1.setPreferredSize(new java.awt.Dimension(325, 100));

        databaseInfoPanel.setBackground(new java.awt.Color(244, 245, 247));
        databaseInfoPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        databaseInfoPanel.setMaximumSize(new java.awt.Dimension(325, 84));
        databaseInfoPanel.setMinimumSize(new java.awt.Dimension(325, 84));
        databaseInfoPanel.setPreferredSize(new java.awt.Dimension(325, 84));

        jLabel1.setText("Expt DB:");

        exptDbNameDisplay.setText("Not open");

        jLabel2.setText("Calb DB:");

        calibrationDbNameDisplay.setText("Not open");

        jLabel4.setText("Amp DB:");

        ampDbNameDisplay.setText("Not open");

        javax.swing.GroupLayout databaseInfoPanelLayout = new javax.swing.GroupLayout(databaseInfoPanel);
        databaseInfoPanel.setLayout(databaseInfoPanelLayout);
        databaseInfoPanelLayout.setHorizontalGroup(
            databaseInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseInfoPanelLayout.createSequentialGroup()
                .addGroup(databaseInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(databaseInfoPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(exptDbNameDisplay))
                    .addGroup(databaseInfoPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(8, 8, 8)
                        .addComponent(calibrationDbNameDisplay))
                    .addGroup(databaseInfoPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)
                        .addGap(8, 8, 8)
                        .addComponent(ampDbNameDisplay)))
                .addContainerGap(218, Short.MAX_VALUE))
        );
        databaseInfoPanelLayout.setVerticalGroup(
            databaseInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseInfoPanelLayout.createSequentialGroup()
                .addGroup(databaseInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exptDbNameDisplay)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(calibrationDbNameDisplay)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ampDbNameDisplay)
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lreWindowParametersPanel1.setMinimumSize(new java.awt.Dimension(267, 72));
        lreWindowParametersPanel1.setPreferredSize(new java.awt.Dimension(267, 72));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(plotFc1, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(plotLREs1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(numericalTable1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lreObjectInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(databaseInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lreWindowParametersPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(plotFo1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(plotFc1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plotLREs1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numericalTable1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lreWindowParametersPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(databaseInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(plotFo1, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)))
                    .addComponent(lreObjectInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ampDbNameDisplay;
    private javax.swing.JLabel calibrationDbNameDisplay;
    private javax.swing.JPanel databaseInfoPanel;
    private javax.swing.JLabel eMaxDisplay;
    private javax.swing.JLabel exptDbNameDisplay;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private org.lreqpcr.ui_components.LreObjectInfo lreObjectInfo1;
    private org.lreqpcr.ui_components.LreWindowParametersPanel lreWindowParametersPanel1;
    private javax.swing.JLabel midCdisplay;
    private javax.swing.JLabel noDisplay;
    private org.lreqpcr.ui_components.NumericalTable numericalTable1;
    private org.lreqpcr.ui_components.PlotFc plotFc1;
    private org.lreqpcr.ui_components.PlotFo plotFo1;
    private org.lreqpcr.ui_components.LrePlot plotLREs1;
    // End of variables declaration//GEN-END:variables

    /**
     * Listens to changes in the LRE window generated by the LRE Plot planel
     * @param evt not used
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (selectedNode != null) {
            if (currentDB.isDatabaseOpen()) {
                updatePanels();
                selectedNode.refreshNodeLabel();
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void universalLookupChangeEvent(Object key) {
        //An Experiment key must be present in the universalLookukp
        if (key == DatabaseType.EXPERIMENT) {
            List l = universalLookup.getAll(key);
            if (l.isEmpty()){
                return;
            }
            DatabaseServices expDB = (DatabaseServices) universalLookup.getAll(key).get(0);
            if (expDB.isDatabaseOpen()) {
                //A new database file has been opened
                exptDbNameDisplay.setText(expDB.getDatabaseFile().getName());
                exptDbNameDisplay.setToolTipText(expDB.getDatabaseFile().getAbsolutePath());
            } else {
                exptDbNameDisplay.setText("Not open");
            }
        }
        if (key == DatabaseType.CALIBRATION) {
            if (universalLookup.containsKey(DatabaseType.CALIBRATION)) {
                DatabaseServices calDB = (DatabaseServices) universalLookup.getAll(key).get(0);
                if (calDB.isDatabaseOpen()) {
                    calibrationDbNameDisplay.setText(calDB.getDatabaseFile().getName());
                    calibrationDbNameDisplay.setToolTipText(
                            calDB.getDatabaseFile().getAbsolutePath());
                } else {
                    calibrationDbNameDisplay.setText("Not open");
                }
            } else {
                calibrationDbNameDisplay.setText("Not open");
            }
        }
        if (key == DatabaseType.AMPLICON) {
            if (universalLookup.containsKey(DatabaseType.AMPLICON)) {
                DatabaseServices ampDB = (DatabaseServices) universalLookup.getAll(key).get(0);
                if (ampDB.isDatabaseOpen()) {
                    ampDbNameDisplay.setText(ampDB.getDatabaseFile().getName());
                    ampDbNameDisplay.setToolTipText(ampDB.getDatabaseFile().getAbsolutePath());
                } else {
                    ampDbNameDisplay.setText("Not open");
                }
            } else {
                ampDbNameDisplay.setText("Not open");
            }
        }
    }
}
