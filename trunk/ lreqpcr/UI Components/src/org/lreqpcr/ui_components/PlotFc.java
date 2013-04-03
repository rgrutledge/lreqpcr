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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationRun;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class PlotFc extends javax.swing.JPanel {

    private boolean isInitiated;
    private double maxFc;//The height of the profile
    private Graphics2D g2;
    private int lreWinSize; //LRE window size
    private Cycle strCycle, zeroCycle; //LRE window start cycle
    private Profile profile;
    private boolean clearPlot;
    double avFmax;

    /**
     * Generates a plot of reaction fluorescence and predicted fluorescence
     * versus cycle number
     */
    public PlotFc() {
        initComponents();
        fbLabel.setVisible(false);
        fmaxNrmzd.setVisible(false);
    }

    public void clearPlot() {
        fbDisplay.setText("");
        fbLabel.setVisible(false);
        fmaxNrmzd.setVisible(false);
        graphTitle.setText("Fc Plot (Fc vs. Cycle)");
        Dimension size = this.getSize();
        if (g2 != null) {
            g2.clearRect(0, 0, size.width, size.height);
            clearPlot = true;
            repaint();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        graphTitle = new javax.swing.JLabel();
        fbLabel = new javax.swing.JLabel();
        fbDisplay = new javax.swing.JLabel();
        fmaxNrmzd = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(244, 245, 247));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setEnabled(false);
        setMaximumSize(new java.awt.Dimension(400, 100));
        setMinimumSize(new java.awt.Dimension(400, 100));
        setName("Fc Plot"); // NOI18N
        setPreferredSize(new java.awt.Dimension(400, 100));

        graphTitle.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        graphTitle.setForeground(new java.awt.Color(204, 0, 51));
        graphTitle.setText("Fc Plot (Fc vs. Cycle)");

        fbLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        fbLabel.setText("Fb:");
        fbLabel.setToolTipText("Calculated from the average of the Fo values within the LRE window");

        fbDisplay.setText("     ");

        fmaxNrmzd.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        fmaxNrmzd.setForeground(new java.awt.Color(204, 0, 51));
        fmaxNrmzd.setText("Fmax Normalized");
        fmaxNrmzd.setToolTipText("Tartget or OCF normalized to the average Run Fmax");

        jLabel1.setText("                                                            ");
        jLabel1.setToolTipText("Av Fmax");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(graphTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(fmaxNrmzd)))
                .addContainerGap(244, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fbLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fbDisplay)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(graphTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fbLabel)
                    .addComponent(fbDisplay)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fmaxNrmzd)
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Set Fc, predicted Fc and R2
     *
     * @param prfSum the Profile Summary to display
     */
    public void iniPlot(ProfileSummary prfSum) {
        if (prfSum == null) {
            clearPlot();
            return;
        }
        clearPlot = false;
        zeroCycle = prfSum.getZeroCycle();
        strCycle = prfSum.getStrCycle();
        profile = prfSum.getProfile();
        if (profile instanceof SampleProfile){
            SampleProfile samPrf = (SampleProfile) profile;
            if (samPrf.isTargetQuantityNormalizedToFmax()){
                fmaxNrmzd.setVisible(true);
            }
        } else if (profile instanceof CalibrationProfile){
            CalibrationProfile calPrf = (CalibrationProfile) profile;
            if (calPrf.isOcfNormalizedToFmax()){
                fmaxNrmzd.setVisible(true);
            }
        }
        lreWinSize = profile.getLreWinSize();
        DecimalFormat df = new DecimalFormat();
        df.applyPattern(FormatingUtilities.decimalFormatPattern(profile.getFb()));
        fbDisplay.setText(df.format(profile.getFb()));
        fbLabel.setVisible(true);
        df.applyLocalizedPattern("0.00");
        graphTitle.setText("C1/2: " + df.format(profile.getMidC()));
        df.applyPattern("0.0000");
        //Need to determin scale for Fc (Y-axis)
        if(profile.getRun() instanceof CalibrationRun){
            avFmax = profile.getRun().getAverageFmax();
        }else if (profile.getRun().getAverageFmax() == 0) {
            profile.getRun().calculateAverageFmax();
            if (profile.getRun().getAverageFmax() == 0) {
                //Most likely due to old dataset in which Fmax was not implemented
                //Failed average Fmax calculation...likely a very rare event
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        "This Run appears to lack an average Fmax.\n"
                        + "This prevents profiles from being viewed. ",
                        "Fc readings cannot be displayed",
                        JOptionPane.ERROR_MESSAGE);
                clearPlot();
                return;
            }else {avFmax = profile.getRun().getAverageFmax();}
        }else {avFmax = profile.getRun().getAverageFmax();}
        maxFc = avFmax * 1.5;//Provides 50% spacing for the top of the profile
        isInitiated = true;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2 = (Graphics2D) g;
        if (clearPlot || zeroCycle == null) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /*Define the plot area*/
        int width = getWidth();
        int height = getHeight();
        double scalingFactorX = width / (50 * 0.99); //pixels/C provides relative X scale
        double scalingFactorY = height / maxFc; //pixels/FU provides relative Y scale
        g2.setColor(Color.BLACK);
        double offsetX = width * 0.03;
        double offsetY = height * 0.1;
        double ptSize = 10;
        if (isInitiated) {
            //Draw a line at Average Fmax
            Cycle runner = zeroCycle.getNextCycle();//Cycle #1
            //Move to cycle 12
            for(int i = 0; i<12; i++){
                runner = runner.getNextCycle();
            }
            double yFmax = avFmax * scalingFactorY * (0.5);
            double xMin = runner.getCycNum() * scalingFactorX;
            //Run to the end of the profile
            while (runner.getNextCycle() != null){
                runner = runner.getNextCycle();
            }
            double xMax = runner.getCycNum() * scalingFactorX;
            Line2D.Double lreLine = new Line2D.Double(xMin, yFmax, xMax, yFmax);
            g2.draw(lreLine);
            //Go back to cycle 1
            runner = zeroCycle.getNextCycle();//Cycle #1
            //Allows display of the Fc plot if a LRE window has not been found
            if (!profile.hasAnLreWindowBeenFound() || profile.isExcluded()) {
            do {
                double x = (runner.getCycNum() * scalingFactorX) - offsetX;
                    double y = height - (runner.getFc() * scalingFactorY) - offsetY;
                    Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.08 * ptSize, y + 0.08 * ptSize, ptSize * 0.32, ptSize * 0.32); //XY offset = 25% of ptSize
                    g2.setColor(Color.RED);
                    g2.fill(pt); //Actual Fc
                    runner = runner.getNextCycle();
                } while (runner != null);
                return;
            }
            do {
                double x = (runner.getCycNum() * scalingFactorX) - offsetX;
                double y = height - (runner.getFc() * scalingFactorY) - offsetY;
                double yPrd = height - (runner.getPredFc() * scalingFactorY) - offsetY;
                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - ptSize * 0.25, yPrd - ptSize * 0.25, ptSize, ptSize); //XY offset = 25% of ptSize
                g2.setColor(Color.BLACK);
                g2.draw(pt1); //Predicted Fc
                Ellipse2D.Double pt = new Ellipse2D.Double(x + 0.08 * ptSize, y + 0.08 * ptSize, ptSize * 0.32, ptSize * 0.32); //XY offset = 25% of ptSize
                g2.setColor(Color.BLUE);
                g2.fill(pt); //Actual Fc
                runner = runner.getNextCycle();
            } while (runner != null);
            //Red circles demark LRE window cycles
            if (strCycle.getPrevCycle() == null) {
                clearPlot();
                return;
            }
            runner = strCycle.getPrevCycle();
            g2.setColor(Color.RED);
            for (int i = 0; i < lreWinSize + 1; i++) { //Draw red circles specifying the Fc values included within the LRE window
                double x = (runner.getCycNum() * scalingFactorX) - offsetX;
                double y = height - (runner.getPredFc() * scalingFactorY) - offsetY;
                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - ptSize * 0.25, y - ptSize * 0.25, ptSize, ptSize); //XY offset = 25% of ptSize
                g2.draw(pt1); //Predicted Fc within the LRE window
                runner = runner.getNextCycle();
                if (runner == null) {
                    return;
                }
            }
            //Megenta circles denote the flourescence background window
            runner = zeroCycle;
            //Run to the beginning of the Fb window
            for (int i = 0; i < profile.getFbStart(); i++) {
                runner = runner.getNextCycle();
            }
            g2.setColor(Color.MAGENTA);
            for (int i = 0; i < profile.getFbWindow(); i++) {
                double x = (runner.getCycNum() * scalingFactorX) - offsetX;
                double yPrd = height - (runner.getPredFc() * scalingFactorY) - offsetY;
                Ellipse2D.Double pt1 = new Ellipse2D.Double(x - ptSize * 0.25, yPrd - ptSize * 0.25, ptSize, ptSize); //XY offset = 25% of ptSize
                g2.draw(pt1);
                runner = runner.getNextCycle();
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fbDisplay;
    private javax.swing.JLabel fbLabel;
    private javax.swing.JLabel fmaxNrmzd;
    private javax.swing.JLabel graphTitle;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
