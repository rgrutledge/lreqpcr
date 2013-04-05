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
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 *
 * @author  Bob Rutledge
 */
public class PlotFo extends javax.swing.JPanel {

    double winAvFo, maxFo, winAvFoCV;
    int lreWinSize, strCycleInt;
    Graphics2D g2;
    double xMaxVal, yMaxVal;
    private ProfileSummary prfSum;
    private Cycle strCycle;
    private Profile profile;
    private DecimalFormat df = new DecimalFormat();
    private DecimalFormat dfE = new DecimalFormat("0.00E0");
    private boolean clearPlot;

    /** 
     * Generates a plot of cycle fluorescence and predicted fluorescence
     * versus cycle number
     */
    public PlotFo() {
        initComponents();
    }

    public void clearPlot() {//Not yet implemented
        avFoDisplay.setText("");
        avFoLabel.setVisible(false);
        Dimension size = this.getSize();
        if (g2 != null) {
            g2.clearRect(0, 0, size.width, size.height);
            clearPlot = true;
            repaint();
        }

    }

    /**
     *Set Fc, predicted Fc and R2
     *
     *
     * @param prfSum the object holding profile summary data
     */
    public void iniPlot(ProfileSummary prfSum) {
        if (prfSum == null) {
            clearPlot();
            return;
        }
        this.prfSum = prfSum;
        strCycle = prfSum.getStrCycle();
        if (strCycle == null) {
            clearPlot();
            return;
        }
        clearPlot = false;

        profile = prfSum.getProfile();
        winAvFo = profile.getAvFo();
        winAvFoCV = profile.getAvFoCV();
        lreWinSize = profile.getLreWinSize();
        avFoLabel.setVisible(true);
        avFoDisplay.setText(dfE.format(winAvFo) + " +/-" + df.format(winAvFoCV * 100) + "%");
        df.applyPattern("0.00");

        /*Determine the maximum X&Y values: Dataset specific*/
        Cycle runner = strCycle;
        //Run to end of the LRE window
        for (int i = 1; i < lreWinSize; i++) {
            if(runner.getNextCycle() == null){
                clearPlot();
                return;
            }
            runner = runner.getNextCycle();
        }
        yMaxVal = winAvFo * 2;
        if (runner == null) {
            return;
        }
        xMaxVal = runner.getFc(); //Set the initial maximum X value
        /*Prepare the Fo points for plotting*/
        for (int i = 1; i < 5; i++) { //Maximum 4 cycles beyond the LRE window
            runner = runner.getNextCycle();
            if (runner == null) {
                break;
            }
            double nextFc = runner.getFc();
            double nextFo = runner.getFo();
            if (nextFo > 0) {
                if (nextFo / winAvFo < 5) { //Maximum 500% increase
                    if (nextFc > xMaxVal) {
                        xMaxVal = nextFc;
                    }
                    if (nextFo > yMaxVal) {
                        yMaxVal = nextFo;
                    }
                } else {
                    if (nextFo / winAvFo < 0.1) { //Maximum 500% decrease
                        if (nextFc > xMaxVal) {
                            xMaxVal = nextFc;
                        }
                        if (nextFo > yMaxVal) {
                            yMaxVal = nextFo;
                        }
                    }
                }
            } else {
                break;
            }
        }
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        graphTitle = new javax.swing.JLabel();
        avFoDisplay = new javax.swing.JLabel();
        avFoLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(244, 245, 247));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setToolTipText("Conversion of Fc readings into target quantity expressed in fluorescence units (Fo)");
        setMaximumSize(new java.awt.Dimension(1000, 1000));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setName("Fc Plot"); // NOI18N
        setPreferredSize(new java.awt.Dimension(250, 100));
        setRequestFocusEnabled(false);

        graphTitle.setFont(new java.awt.Font("Tahoma", 1, 12));
        graphTitle.setForeground(new java.awt.Color(204, 0, 51));
        graphTitle.setText("Fo Plot (Fo vs. Fc)");
        graphTitle.setToolTipText("Fc-derived target quantity (Fo) vs. Fc (reaction fluorescence) ");

        avFoDisplay.setForeground(new java.awt.Color(255, 0, 0));
        avFoDisplay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        avFoDisplay.setText("      ");
        avFoDisplay.setToolTipText("LRE-derived target quantity expressed in fluorescence units");

        avFoLabel.setForeground(new java.awt.Color(255, 0, 0));
        avFoLabel.setText("Av. Fo:");
        avFoLabel.setToolTipText("The average Fo calculated from the Fo values within the LRE window (red circles)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(graphTitle)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(avFoLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(avFoDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                        .addGap(77, 77, 77)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(graphTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(avFoLabel)
                    .addComponent(avFoDisplay))
                .addContainerGap(50, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        g2 = (Graphics2D) g;
        if (clearPlot) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /*Define the painted area*/
        int width = getWidth();
        int height = getHeight();
        /*Define the plotting area*/
        double xMin = width * 0.10; //Y axis label area
        double yMin = height * 0.10; //X axis label area
        double xMax = (width - xMin) - (width * 0.05);
        double yMax = (height - yMin) - (height * 0.05);
        /*Calculate the X&Y scaling factors*/
        double xScalingFactor = xMax / xMaxVal; //pixels/FU provides relative X scale
        double yScalingFactor = yMax / yMaxVal; //pixels/FU provides relative Y scale

        g2.setColor(Color.BLACK);
        double xOffset = width * 0.05; //5% right border
        double yOffset = height * 0.05; //5% top border
        double ptSize = 16;
        if (prfSum != null) {
            if (prfSum.getStrCycle() == null || prfSum.getStrCycle().getCycNum() == 0) {
                clearPlot();
                return;
            }
            Cycle runner = prfSum.getStrCycle();
            //Run back 5 cycles below the start cycle
            for (int i = 1; i < 5; i++) {
                if (runner == null) {
                    clearPlot();
                    return;
                }
                runner = runner.getPrevCycle();
            }
            for (int i = 0; i < 15; i++) {
                if (runner == null) {
                    break;
                }
                double x = (runner.getFc() * xScalingFactor) + xOffset;
                double y = height - ((runner.getFo() * yScalingFactor) - yOffset);
                Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.08 * ptSize, y + 0.08 * ptSize, ptSize * 0.32, ptSize * 0.32); //XY offset = 25% of ptSize to center the ellispe
                g2.fill(pt);
                runner = runner.getNextCycle();
            }
            g2.setColor(Color.RED);
            //Draw red circles specifying the cycles included within the LRE window
            runner = prfSum.getStrCycle().getPrevCycle();
            if (runner == null) {
                return;
            }
            for (int i = 1; i < profile.getLreWinSize() + 2; i++) {
                double x = (runner.getFc() * xScalingFactor) + xOffset;
                double y = height - ((runner.getFo() * yScalingFactor) - yOffset);
                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - ptSize * 0.25, y - ptSize * 0.25, ptSize, ptSize); //XY offset = 25% of ptSize
                g2.draw(pt1); //Predicted Fc within the LRE window
                runner = runner.getNextCycle();
                if (runner == null) {
                    return;
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avFoDisplay;
    private javax.swing.JLabel avFoLabel;
    private javax.swing.JLabel graphTitle;
    // End of variables declaration//GEN-END:variables
}
