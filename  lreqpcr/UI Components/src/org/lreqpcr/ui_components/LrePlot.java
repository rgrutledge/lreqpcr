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

import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.data_processing.*;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.util.Lookup;

/**
 * LRE Plot Panel using a small font
 * <p>This is the primary resource for displaying 
 * and adjusting the LRE analysis of the supplied Profile.
 * 
 * @author  Bob Rutledge
 */
@SuppressWarnings("unchecked")
public class LrePlot extends javax.swing.JPanel {

    private ProfileSummary prfSum; //Holds the Profile to be displayed
    private Profile profile;
    private Cycle runner;
    private Graphics2D g2;
    private double xMaxVal, yMaxVal;
    private DecimalFormat df = new DecimalFormat();
    private DecimalFormat dfE = new DecimalFormat("0.00E0");
    private boolean clearPlot;
    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private UniversalLookup universalLookup = UniversalLookup.getDefault();
    private DatabaseServices db;//The database in which the Profile is stored

    /** 
     * Generates a plot of reaction fluorescence and predicted fluorescence
     * versus cycle number
     * 
     */
    public LrePlot() {
        initComponents();
    }

    /**
     * Initializes the LrePlot panel using the supplied ProfileSummary. Note
     * that it is assumed that the provided Profile has a valid LRE window.
     * 
     * @param prfSum the ProfileSummary holding the Profile that is to be displayed
     */
    public void iniPlotLREs(ProfileSummary prfSum, DatabaseServices db) {
        if (prfSum == null) {
            profile = null;
            clearPlot();
            return;
        }
        this.db = db;
        clearPlot = false;
        this.prfSum = prfSum;
        profile = prfSum.getProfile();
        if (profile instanceof SampleProfile) {
            SampleProfile samplePrf = (SampleProfile) profile;
            double no = samplePrf.getNo();
            if (no < 10) {
                df.applyPattern("#0.00");
            } else {
                df.applyPattern("###,###");
            }
            String numTargetMolecs = df.format(samplePrf.getNo()) + " molecules";
            graphTitle.setText(sdf.format(samplePrf.getRunDate()) + "  " + numTargetMolecs);
        }
        if (profile instanceof CalibrationProfile) {
            CalibrationProfile calPrf = (CalibrationProfile) profile;
            double ocf = calPrf.getOCF();
            if (ocf < 10) {
                df.applyPattern("#0.00");
            } else {
                df.applyPattern("###,###");
            }
            String ocfLabel = "  OCF= " + df.format(calPrf.getOCF());
            graphTitle.setText(sdf.format(calPrf.getRunDate()) + ocfLabel);
        }
        lreWinSizeDisplay.setText(String.valueOf(profile.getLreWinSize()));
        startCycleDisplay.setText(String.valueOf(profile.getStrCycleInt()));
        dEdisplay.setText(dfE.format(profile.getDeltaE()));
        df.applyPattern("##.0");
        maxEdisplay.setText(df.format(profile.getEmax() * 100) + "%");
        df.applyPattern("0.0000");
        r2display.setText(df.format(profile.getR2()));
        Double fmax = profile.getEmax() / (-1 * profile.getDeltaE());
        df.applyPattern(FormatingUtilities.decimalFormatPattern(fmax));
        fmaxDisplay.setText(df.format(fmax));
        startCycleLabel.setVisible(true);
        winSizeLabel.setVisible(true);
        lreParametersPanel.setVisible(true);
        resetButton.setVisible(true);
        upperWindowAdjustPanel.setVisible(true);
        lowerWindowAdjustPanel.setVisible(true);
        /*Determine the maximum and minimum X&Y values: Dataset specific*/
        /*Determine X maximum*/
        xMaxVal = 0; //Reset
        if (prfSum.getZeroCycle() == null) {
            return;
        }
        runner = prfSum.getZeroCycle();
        while (runner.getNextCycle() != null) {
            if (xMaxVal < runner.getFc()) {
                xMaxVal = runner.getFc();
            }
            runner = runner.getNextCycle();
        }
        yMaxVal = 1.0; //Ec maximum is 100% for display purposes
        repaint();
    }

    public void clearPlot() {
        if (profile != null){
            if(!profile.hasAnLreWindowBeenFound()){
            graphTitle.setText("LRE Plot (Ec vs. Fc)/n" + "AN LRE WINDOW WAS NOT FOUND");
            }
        } else {
            graphTitle.setText("LRE Plot (Ec vs. Fc)");
        }
        profile = null;
        lreWinSizeDisplay.setText("");
        maxEdisplay.setText("");
        r2display.setText("");
        startCycleDisplay.setText("");
        dEdisplay.setText("");
        fmaxDisplay.setText("");
        startCycleLabel.setVisible(false);
        winSizeLabel.setVisible(false);
        lreParametersPanel.setVisible(false);
        resetButton.setVisible(false);
        upperWindowAdjustPanel.setVisible(false);
        lowerWindowAdjustPanel.setVisible(false);
        Dimension size = this.getSize();
        if (g2 != null) {
            g2.clearRect(0, 0, size.width, size.height);
            clearPlot = true;
            repaint();
        }
    }

    private void processModifiedLreWindow() {
//This function relies on the assumption that this profile has a valid LRE window
//which should be true if the profile is being displayed so that the user can modify it
        //Update ProfileSummary using the new LRE parameters
        ProfileInitializer.calcLreParameters(prfSum);
//Note that of 9Aug12 the profile updates itself following changes to the avFo
        db.saveObject(profile);
        //The AverageSampleProfile may need to be updated if this is a SampleProfile
        if (profile instanceof SampleProfile && !(profile instanceof AverageProfile)) {
            AverageSampleProfile avProfile = (AverageSampleProfile) profile.getParent();
            //This function will update the AverageSampleProfile if it is <10N
            if (avProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                db.saveObject(avProfile);
            }
        }
        universalLookup.fireChangeEvent(PanelMessages.PROFILE_CHANGED);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        graphTitle = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        lreParametersPanel = new javax.swing.JPanel();
        r2display = new javax.swing.JLabel();
        fmaxDisplay = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        maxEdisplay = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        dEdisplay = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lowerWindowAdjustPanel = new javax.swing.JPanel();
        addBottom = new javax.swing.JRadioButton();
        removeBottom = new javax.swing.JRadioButton();
        upperWindowAdjustPanel = new javax.swing.JPanel();
        addTop = new javax.swing.JRadioButton();
        removeTop = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        lreWinSizeDisplay = new javax.swing.JLabel();
        startCycleDisplay = new javax.swing.JLabel();
        startCycleLabel = new javax.swing.JLabel();
        winSizeLabel = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(204, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setToolTipText("Cycle efficiency (Ec) vs. reaction fluorescence (Fc)");
        setMaximumSize(new java.awt.Dimension(350, 200));
        setMinimumSize(new java.awt.Dimension(350, 200));
        setName("Fc Plot"); // NOI18N
        setPreferredSize(new java.awt.Dimension(350, 200));

        graphTitle.setFont(new java.awt.Font("Tahoma", 1, 12));
        graphTitle.setForeground(new java.awt.Color(204, 0, 51));
        graphTitle.setText("LRE Plot (Ec vs. Fc)");

        resetButton.setFont(new java.awt.Font("Tahoma", 0, 10));
        resetButton.setText("Reset");
        resetButton.setToolTipText("Performs an automated LRE window selection");
        resetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        lreParametersPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        r2display.setFont(new java.awt.Font("Tahoma", 0, 10));
        r2display.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        r2display.setText("     ");

        fmaxDisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        fmaxDisplay.setText("     ");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Emax:");
        jLabel2.setToolTipText("Maximal amplification efficiency (intercept)");

        maxEdisplay.setFont(new java.awt.Font("Tahoma", 1, 11));
        maxEdisplay.setForeground(new java.awt.Color(204, 0, 0));
        maxEdisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        maxEdisplay.setText("    ");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setText("deltaE:");
        jLabel1.setToolTipText("Loss in cycle efficiency (Ec) per Fc unit (slope)");

        dEdisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        dEdisplay.setText("    ");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("r2:");
        jLabel3.setToolTipText("Correlation coefficent");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel4.setText("Fmax:");

        javax.swing.GroupLayout lreParametersPanelLayout = new javax.swing.GroupLayout(lreParametersPanel);
        lreParametersPanel.setLayout(lreParametersPanelLayout);
        lreParametersPanelLayout.setHorizontalGroup(
            lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lreParametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lreParametersPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxEdisplay))
                    .addGroup(lreParametersPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fmaxDisplay)
                            .addComponent(dEdisplay, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(lreParametersPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(r2display)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lreParametersPanelLayout.setVerticalGroup(
            lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lreParametersPanelLayout.createSequentialGroup()
                .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxEdisplay)
                    .addComponent(jLabel2))
                .addGap(1, 1, 1)
                .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dEdisplay)
                    .addComponent(jLabel1))
                .addGap(1, 1, 1)
                .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fmaxDisplay)
                    .addComponent(jLabel4))
                .addGap(1, 1, 1)
                .addGroup(lreParametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(r2display)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        lowerWindowAdjustPanel.setBackground(new java.awt.Color(204, 255, 255));

        addBottom.setBackground(new java.awt.Color(204, 255, 255));
        addBottom.setFont(new java.awt.Font("Tahoma", 0, 10));
        addBottom.setText("+Cycle");
        addBottom.setToolTipText("Add a cycle to the bottom of the LRE window");
        addBottom.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addBottom.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBottomActionPerformed(evt);
            }
        });

        removeBottom.setBackground(new java.awt.Color(204, 255, 255));
        removeBottom.setFont(new java.awt.Font("Tahoma", 0, 10));
        removeBottom.setText("-Cycle");
        removeBottom.setToolTipText("Remove a cycle from the bottom of the LRE window");
        removeBottom.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        removeBottom.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBottomActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lowerWindowAdjustPanelLayout = new javax.swing.GroupLayout(lowerWindowAdjustPanel);
        lowerWindowAdjustPanel.setLayout(lowerWindowAdjustPanelLayout);
        lowerWindowAdjustPanelLayout.setHorizontalGroup(
            lowerWindowAdjustPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lowerWindowAdjustPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(lowerWindowAdjustPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(removeBottom)
                    .addComponent(addBottom)))
        );

        lowerWindowAdjustPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addBottom, removeBottom});

        lowerWindowAdjustPanelLayout.setVerticalGroup(
            lowerWindowAdjustPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lowerWindowAdjustPanelLayout.createSequentialGroup()
                .addComponent(addBottom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeBottom))
        );

        upperWindowAdjustPanel.setBackground(new java.awt.Color(204, 255, 255));

        addTop.setBackground(new java.awt.Color(204, 255, 255));
        addTop.setFont(new java.awt.Font("Tahoma", 0, 10));
        addTop.setText("+Cycle");
        addTop.setToolTipText("Add a cycle to the top of the LRE window");
        addTop.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addTop.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTopActionPerformed(evt);
            }
        });

        removeTop.setBackground(new java.awt.Color(204, 255, 255));
        removeTop.setFont(new java.awt.Font("Tahoma", 0, 10));
        removeTop.setText("-Cycle");
        removeTop.setToolTipText("Remove a cycle from the top of the LRE window");
        removeTop.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        removeTop.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upperWindowAdjustPanelLayout = new javax.swing.GroupLayout(upperWindowAdjustPanel);
        upperWindowAdjustPanel.setLayout(upperWindowAdjustPanelLayout);
        upperWindowAdjustPanelLayout.setHorizontalGroup(
            upperWindowAdjustPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, upperWindowAdjustPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(upperWindowAdjustPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeTop)
                    .addComponent(addTop))
                .addContainerGap())
        );

        upperWindowAdjustPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addTop, removeTop});

        upperWindowAdjustPanelLayout.setVerticalGroup(
            upperWindowAdjustPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperWindowAdjustPanelLayout.createSequentialGroup()
                .addComponent(addTop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(removeTop))
        );

        jPanel5.setBackground(new java.awt.Color(204, 255, 255));

        lreWinSizeDisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        lreWinSizeDisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lreWinSizeDisplay.setText("      ");

        startCycleDisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        startCycleDisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        startCycleDisplay.setText("    ");

        startCycleLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
        startCycleLabel.setText("Start cycle:");

        winSizeLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
        winSizeLabel.setText("Win Size:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(winSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lreWinSizeDisplay))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(startCycleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startCycleDisplay))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(winSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lreWinSizeDisplay))
                .addGap(1, 1, 1)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startCycleLabel)
                    .addComponent(startCycleDisplay)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lowerWindowAdjustPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 208, Short.MAX_VALUE)
                        .addComponent(upperWindowAdjustPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(77, 77, 77)
                        .addComponent(resetButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(260, Short.MAX_VALUE)
                        .addComponent(lreParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addComponent(graphTitle)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(graphTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(lreParametersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(resetButton)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lowerWindowAdjustPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(upperWindowAdjustPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTopActionPerformed
        removeTop.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setLreWinSize(profile.getLreWinSize() - 1);
        processModifiedLreWindow();
    }//GEN-LAST:event_removeTopActionPerformed

    private void addTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTopActionPerformed
        addTop.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setLreWinSize(profile.getLreWinSize() + 1);
        processModifiedLreWindow();
    }//GEN-LAST:event_addTopActionPerformed

    private void removeBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBottomActionPerformed
        removeBottom.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setStrCycleInt(profile.getStrCycleInt() + 1);
        prfSum.setStrCycle(prfSum.getStrCycle().getNextCycle());
        profile.setLreWinSize(profile.getLreWinSize() - 1);
        processModifiedLreWindow();
        universalLookup.fireChangeEvent(PanelMessages.PROFILE_CHANGED);
    }//GEN-LAST:event_removeBottomActionPerformed

    private void addBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBottomActionPerformed
        addBottom.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setStrCycleInt(profile.getStrCycleInt() - 1);
        prfSum.setStrCycle(prfSum.getStrCycle().getPrevCycle());
        profile.setLreWinSize(profile.getLreWinSize() + 1);
        processModifiedLreWindow();
    }//GEN-LAST:event_addBottomActionPerformed

private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
    if (profile == null) {
        return;
    }
    //Need to conduct a new automated LRE window selection
    profile.setHasAnLreWindowBeenFound(false);
    List<LreWindowSelectionParameters> l = db.getAllObjects(LreWindowSelectionParameters.class);
    LreWindowSelectionParameters selectionParameters = l.get(0);
    LreAnalysisService analysisService = Lookup.getDefault().lookup(LreAnalysisService.class);
    analysisService.conductAutomatedLreWindowSelection(profile, selectionParameters);
    processModifiedLreWindow();
}//GEN-LAST:event_resetButtonActionPerformed

    /**
     *Three elements: the LRE line (axis to axis), the LRE window
     * Fc-Ec and the profile FcEc with early cycles trimmed away
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2 = (Graphics2D) g;
        if (clearPlot) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /*Define the plot area*/
        int width = getWidth();
        int height = getHeight();
        /*Define the plotting area*/
        double xMin = width * 0.05; //Y axis label area
        double yMin = height * 0.05; //X axis label area
        double xMax = (width - xMin);
        double yMax = (height - yMin);
        /*Calculate the X&Y scaling factors*/
        double xScalingFactor = xMax / (xMaxVal * 1.05); //pixels/FU relative X scale with 5% X axis reduction
        double yScalingFactor = yMax / (yMaxVal * 1.05); //pixels/FU relative Y scale with 5% Y axis reduction
        g2.setColor(Color.BLACK);
        double x;
        double y;
        if (profile != null) {
            /*Draw the LRE line*/
            x = xMin + (((profile.getEmax() / profile.getDeltaE()) * -1) * xScalingFactor); //Fmax
            y = yMax - (profile.getEmax() * yScalingFactor); //Emax
            Line2D.Double lreLine = new Line2D.Double(xMin, y, x, yMax);
            g2.draw(lreLine);

            //Determine the C1/2 position and show it as a red dot on the LRE line
            double midFc = (prfSum.getProfile().getEmax() / -prfSum.getProfile().getDeltaE()) / 2;
            double midEc = midFc * prfSum.getProfile().getDeltaE() + prfSum.getProfile().getEmax();
            x = xMin + (midFc * xScalingFactor);
            y = yMax - (midEc * yScalingFactor);
            g2.setColor(Color.RED);
            double ptSize = 30; //Point size
            //This midEc-Fc point was used to center the LRE points (see below) onto the LRE line
            Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.13 * ptSize, y + 0.03 * ptSize, ptSize * 0.32, ptSize * 0.3);
            g2.fill(pt);
        }

        /*Draw the profile and window LRE plots*/
        g2.setColor(Color.BLACK);
        double ptSize = 16; //Point size
        //Draw the LRE points
        if (prfSum != null) {
            runner = prfSum.getStrCycle();
            if (runner == null) {
                return;
            }
            //Run to 5 cycles below the LRE window
            if (runner == null || runner.getPrevCycle() == null) {
                return;
            }
            for (int i = 0; i < 7; i++) {
                if (runner.getPrevCycle() == null) {
                    return;
                }
                runner = runner.getPrevCycle();
            }
            while (runner.getNextCycle() != null) {
                x = xMin + (runner.getFc() * xScalingFactor);
                y = yMax - (runner.getEc() * yScalingFactor);
                Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.13 * ptSize, y + 0.03 * ptSize, ptSize * 0.32, ptSize * 0.3);
                g2.fill(pt);
                runner = runner.getNextCycle();
            }
            //Draw circles around the LRE window points
            runner = prfSum.getStrCycle();
            if (runner == null) {
                return;
            }
            for (int i = 0; i < profile.getLreWinSize(); i++) {
                x = xMin + (runner.getFc() * xScalingFactor);
                y = yMax - (runner.getEc() * yScalingFactor);
                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - (ptSize * 0.25), y - (ptSize * 0.25), ptSize, ptSize); //XY offset = 25% of ptSize
                g2.setColor(Color.RED);
                g2.draw(pt1);//Circle designating this point is within the LRE window
                runner = runner.getNextCycle();
                if (runner == null) {
                    return;
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton addBottom;
    private javax.swing.JRadioButton addTop;
    private javax.swing.JLabel dEdisplay;
    private javax.swing.JLabel fmaxDisplay;
    private javax.swing.JLabel graphTitle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel lowerWindowAdjustPanel;
    private javax.swing.JPanel lreParametersPanel;
    private javax.swing.JLabel lreWinSizeDisplay;
    private javax.swing.JLabel maxEdisplay;
    private javax.swing.JLabel r2display;
    private javax.swing.JRadioButton removeBottom;
    private javax.swing.JRadioButton removeTop;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel startCycleDisplay;
    private javax.swing.JLabel startCycleLabel;
    private javax.swing.JPanel upperWindowAdjustPanel;
    private javax.swing.JLabel winSizeLabel;
    // End of variables declaration//GEN-END:variables
}
