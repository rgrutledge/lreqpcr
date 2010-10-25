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
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.FormatingUtilities;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * LRE Plot Panel using a small font
 * <p>This is the primary resource for displaying 
 * and adjusting the LRE analysis of the supplied Profile.
 * 
 * @author  Bob Rutledge
 */
// TODO do something with the class, or deleted it
public class PlotFbNotYetImplemented extends javax.swing.JPanel {

    private ProfileSummary prfSum; //Holds the Profile to be displayed
    private Profile profile;
    private Cycle runner, zeroCycle;
    private Graphics2D g2;
    private double xMaxVal,  yMaxVal;
    private DecimalFormat df = new DecimalFormat();
    private DecimalFormat dfE = new DecimalFormat("0.00E0");
    private boolean clearPlot;
    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private double maxFc;//The height of the profile

    /** 
     * Generates a plot of reaction fluorescenc and predicted fluorescence
     * versus cycle number
     * 
     */
    public PlotFbNotYetImplemented() {
        initComponents();
    }

    /**
     * Intializes the PlotLREs panel using the supplied ProfileSummary
     * 
     * @param prfSum the ProfileSummary holding the Profile that is to be displayed
     */
    public void PlotFb(ProfileSummary prfSum) {
        if (prfSum == null) {
            clearPlot();
            return;
        }
        clearPlot = false;
        this.prfSum = prfSum;
        zeroCycle = prfSum.getZeroCycle();
        profile = prfSum.getProfile();
        //Retrieve the Fb window and condu
        
        //Setup the text fields
        df.applyPattern("###,###");
//        graphTitle.setText(sdf.format(profile.getRunDate()) + "-" + df.format(prfSum.getProfile().getNo())
//                + " molecules");
//        lreWinSizeDisplay.setText(String.valueOf(profile.getLreWinSize()));
//        startCycleDisplay.setText(String.valueOf(profile.getStrCycleInt()));
//        dEdisplay.setText(dfE.format(profile.getDeltaE()));
////        df.applyPattern(FormatingUtilities.decimalFormatPattern(profile.getOcf()));
//        df.applyPattern("##.0");
//        maxEdisplay.setText(df.format(profile.getEmax() * 100) + "%");
//        df.applyPattern("0.0000");
//        r2display.setText(df.format(profile.getR2()));
//        Double fmax = profile.getEmax() / (-1 * profile.getDeltaE());
//        df.applyPattern(FormatingUtilities.decimalFormatPattern(fmax));
//        fmaxDisplay.setText(df.format(fmax));
        //Determine the height of the profile
        Cycle runner = prfSum.getStrCycle();
        //Run to the end of the profile
        while (runner.getNextCycle() != null) {
            runner = runner.getNextCycle();
        }
        //Determine an average Fc over the last
        if (runner.getFc() > runner.getPredFc()) { //Determines which Fc is largest
            maxFc = runner.getFc() * 1.2; //Provides 20% spacing for the top of the profile
        } else {
            maxFc = runner.getPrevCycle().getPrevCycle().getPredFc() * 1.2;
        } //The last two cycles are = zero
        /*Determine the maximum and minimum X&Y values: Dataset specific*/
        /*Determine X maximum*/
        xMaxVal = 0; //Reset
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
        profile = null;
//        graphTitle.setText("");
//        lreWinSizeDisplay.setText("");
//        maxEdisplay.setText("");
//        r2display.setText("");
//        startCycleDisplay.setText("");
//        dEdisplay.setText("");
//        fmaxDisplay.setText("");
        Dimension size = this.getSize();
        if (g2 != null) {
            g2.clearRect(0, 0, size.width, size.height);
            clearPlot = true;
            repaint();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        r2display = new javax.swing.JLabel();
        graphTitle = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        dEdisplay = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        maxEdisplay = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        removeBottom = new javax.swing.JRadioButton();
        addBottom = new javax.swing.JRadioButton();
        removeTop = new javax.swing.JRadioButton();
        addTop = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        fmaxDisplay = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        startCycleDisplay = new javax.swing.JLabel();
        lreWinSizeDisplay = new javax.swing.JLabel();

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

        setBackground(new java.awt.Color(244, 245, 247));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setToolTipText("Cycle efficiency (Ec) vs. reaction fluorescence (Fc)");
        setMaximumSize(new java.awt.Dimension(350, 200));
        setMinimumSize(new java.awt.Dimension(350, 200));
        setName("Fc Plot"); // NOI18N
        setPreferredSize(new java.awt.Dimension(350, 200));

        r2display.setFont(new java.awt.Font("Tahoma", 0, 10));
        r2display.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        r2display.setText("     ");

        graphTitle.setFont(new java.awt.Font("Tahoma", 1, 12));
        graphTitle.setForeground(new java.awt.Color(204, 0, 51));
        graphTitle.setText("LRE Plot (Ec vs. Fc)");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setText("deltaE:");
        jLabel1.setToolTipText("Loss in cycle efficiency (Ec) per Fc unit (slope)");

        dEdisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        dEdisplay.setText("    ");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Emax:");
        jLabel2.setToolTipText("Maximal amplification efficiency (intercept)");

        maxEdisplay.setFont(new java.awt.Font("Tahoma", 1, 11));
        maxEdisplay.setForeground(new java.awt.Color(204, 0, 0));
        maxEdisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        maxEdisplay.setText("    ");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("r2:");
        jLabel3.setToolTipText("Correlation coefficent");

        removeBottom.setBackground(new java.awt.Color(204, 204, 204));
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

        addBottom.setBackground(new java.awt.Color(204, 204, 204));
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

        removeTop.setBackground(new java.awt.Color(204, 204, 204));
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

        addTop.setBackground(new java.awt.Color(204, 204, 204));
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

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel4.setText("Fmax:");

        fmaxDisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        fmaxDisplay.setText("     ");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel5.setText("Win Size:");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel6.setText("Start sycle:");

        startCycleDisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        startCycleDisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        startCycleDisplay.setText("    ");

        lreWinSizeDisplay.setFont(new java.awt.Font("Tahoma", 0, 10));
        lreWinSizeDisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lreWinSizeDisplay.setText("      ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(81, 81, 81)
                        .addComponent(graphTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addBottom)
                            .addComponent(removeBottom)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(startCycleDisplay))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lreWinSizeDisplay)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 166, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxEdisplay))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel1)
                                            .addComponent(jLabel4))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fmaxDisplay))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(dEdisplay))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(r2display)))
                                .addGap(40, 40, 40))
                            .addComponent(removeTop)
                            .addComponent(addTop))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addBottom, removeBottom});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addTop, removeTop});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(graphTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(maxEdisplay)
                            .addComponent(jLabel2))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(dEdisplay))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fmaxDisplay)
                            .addComponent(jLabel4))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(r2display)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addTop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTop))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(startCycleDisplay)
                                    .addComponent(jLabel6)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lreWinSizeDisplay)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addBottom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeBottom)))
                .addGap(70, 70, 70))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTopActionPerformed
        removeTop.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setLreWinSize(profile.getLreWinSize() - 1);
        firePropertyChange(null, null, null);
    }//GEN-LAST:event_removeTopActionPerformed

    private void addTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTopActionPerformed
        addTop.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setLreWinSize(profile.getLreWinSize() + 1);
        firePropertyChange(null, null, null);
    }//GEN-LAST:event_addTopActionPerformed

    private void removeBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBottomActionPerformed
        removeBottom.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setStrCycleInt(profile.getStrCycleInt() + 1);
        prfSum.setStrCycle(prfSum.getStrCycle().getNextCycle());
        profile.setLreWinSize(profile.getLreWinSize() - 1);
        firePropertyChange(null, null, null);
    }//GEN-LAST:event_removeBottomActionPerformed

    private void addBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBottomActionPerformed
        addBottom.setSelected(false);
        if (profile == null) {
            return;
        }
        profile.setStrCycleInt(profile.getStrCycleInt() - 1);
        prfSum.setStrCycle(prfSum.getStrCycle().getPrevCycle());
        profile.setLreWinSize(profile.getLreWinSize() + 1);
        firePropertyChange(null, null, null);
    }//GEN-LAST:event_addBottomActionPerformed

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
        //This is based on the Fc plot method for scaling
        double scalingFactorX = width / (50 * 0.99); //pixels/C provides relative X scale
        double scalingFactorY = height / maxFc; //pixels/FU provides relative Y scale
        g2.setColor(Color.BLACK);
        double offsetX = width * 0.03;
        double offsetY = height * 0.1;
        double ptSize = 10;
        //Plot the Fc readings within the Fb window + 2 cycles at each end
        //run to two cycles before the Fb window
        runner = zeroCycle.getNextCycle();
        while(runner.getCycNum() < profile.getFbStart() -2){
            runner = runner.getNextCycle();
        }
        //Plot the Fc readings to +2 cycles past the Fb window
        for(int i = 0; i<profile.getFbWindow() + 4; i++){
            double x = (runner.getCycNum() * scalingFactorX) - offsetX;
                double y = height - (runner.getFc() * scalingFactorY) - offsetY;
//                double yPrd = height - (runner.getPredFc() * scalingFactorY) - offsetY;
//                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - ptSize * 0.25, yPrd - ptSize * 0.25, ptSize, ptSize); //XY offset = 25% of ptSize
                g2.setColor(Color.BLACK);
//                g2.draw(pt1); //Predicted Fc
                Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.08 * ptSize, y + 0.08 * ptSize, ptSize * 0.32, ptSize * 0.32); //XY offset = 25% of ptSize
                g2.fill(pt); //Actual Fc
                //Draw a circle around the Fb window cycles
                if(runner.getCycNum() >= profile.getFbStart() ||
                    runner.getCycNum() <= profile.getFbStart() + profile.getFbWindow()){
                    g2.setColor(Color.MAGENTA);
                    x = (runner.getCycNum() * scalingFactorX) - offsetX;
                y = height - (runner.getPredFc() * scalingFactorY) - offsetY;
                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - ptSize * 0.25, y - ptSize * 0.25, ptSize, ptSize); //XY offset = 25% of ptSize
                g2.draw(pt1);
                runner = runner.getNextCycle();
        }
        }
            
             
           
        }
//        /*Define the plot area*/
//        double xMin = width * 0.05; //Y axis off set
//        double yMin = height * 0.05; //X axis off set
//        double xMax = (width - xMin);
//        double yMax = (height - yMin);
//        /*Calculate the X&Y scaling factors*/
//        double xScalingFactor = xMax / (xMaxVal * 1.05); //pixels/C relative X scale with 5% X axis reduction
//        double yScalingFactor = yMax / (yMaxVal * 1.05); //pixels/FU relative Y scale with 5% Y axis reduction
//        
//        g2.setColor(Color.BLACK);
//        double x = 0;
//        double y = 0;
//        if (profile != null) {
//            /*Draw the LRE line*/
//            x = xMin + (((profile.getFbIntercept() / profile.getFbSlope()) * -1) * xScalingFactor);
//            y = yMax - (profile.getFbIntercept() * yScalingFactor);
//            Line2D.Double lreLine = new Line2D.Double(xMin, y, x, yMax);
//            g2.draw(lreLine);
//        }
//
//        /*Draw the profile and window LRE plots*/
//        double ptSize = 16; //Point size
////    Draw the LRE points
//        if (prfSum != null) {
//            runner = prfSum.getStrCycle();
//            if (runner == null) return;
//            //Run to 5 cycles below the LRE window
//            for (int i = 0; i < 5; i++) {
//                runner = runner.getPrevCycle();
//            }
//            while (runner.getNextCycle() != null) {
//                x = xMin + (runner.getFc() * xScalingFactor);
//                y = yMax - (runner.getEc() * yScalingFactor);
//                Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.08 * ptSize, y + 0.08 * ptSize, ptSize * 0.32, ptSize * 0.3); //XY offset = 25% of ptSize to center the ellispe
//                g2.fill(pt);
//                runner = runner.getNextCycle();
//            }
////    Draw circles around the LRE window points
//            runner = prfSum.getStrCycle();
//            if (runner == null) return;
//            for (int i = 0; i < profile.getLreWinSize(); i++) {
//                x = xMin + (runner.getFc() * xScalingFactor);
//                y = yMax - (runner.getEc() * yScalingFactor);
//                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - (ptSize * 0.25), y - (ptSize * 0.25), ptSize, ptSize); //XY offset = 25% of ptSize
//                g2.draw(pt1);//Circle designating this point is within the LRE window
//                runner = runner.getNextCycle();
//                 if (runner == null) return;
//            }
//        }
//    }

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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lreWinSizeDisplay;
    private javax.swing.JLabel maxEdisplay;
    private javax.swing.JLabel r2display;
    private javax.swing.JRadioButton removeBottom;
    private javax.swing.JRadioButton removeTop;
    private javax.swing.JLabel startCycleDisplay;
    // End of variables declaration//GEN-END:variables
}
