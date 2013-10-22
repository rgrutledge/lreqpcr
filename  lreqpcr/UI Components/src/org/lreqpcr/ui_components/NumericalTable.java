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

import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author  Bob Rutledge
 */
public class NumericalTable extends javax.swing.JPanel {

    private ProfileSummary prfSum;
    private NumberFormat nf = NumberFormat.getInstance();

    /** Creates new form numericalTable */
    public NumericalTable() {
        initComponents();
    }

    static class HeaderRender extends DefaultTableCellRenderer {

        public HeaderRender() {
            super();
            setHorizontalAlignment(CENTER);
        }
    }

    static class CycleRender extends DefaultTableCellRenderer {

        public CycleRender() {
            super();
            setHorizontalAlignment(CENTER);
        }

        @Override
        public void setValue(Object value) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(0);
            setText((value == null) ? "" : nf.format(value));
        }
    }

    static class FcRender extends DefaultTableCellRenderer {

        public FcRender() {
            super();
            setHorizontalAlignment(RIGHT);
        }

        @Override
        public void setValue(Object value) {
            if (value == null) {
                setText("");
                return;
            }
            double cF = Double.valueOf(value.toString());
            NumberFormat nf = NumberFormat.getInstance();
            if (cF > 1000) {
                nf.setMaximumFractionDigits(0);
            } else if (cF > 100) {
                nf.setMaximumFractionDigits(1);
            } else if (cF > 10) {
                nf.setMaximumFractionDigits(2);
            } else if (cF > 1) {
                nf.setMaximumFractionDigits(3);
            }
            setText((value == null) ? "" : nf.format(value));
        }
    }

    static class EcRender extends DefaultTableCellRenderer {

        public EcRender() {
            super();
            setHorizontalAlignment(RIGHT);
        }

        @Override
        public void setValue(Object value) {
            if (value == null) {
                setText("");
                return;
            }
            double cv = Double.valueOf(value.toString());
            DecimalFormat df = new DecimalFormat("0.0");
            setText((value == null) ? "" : df.format(cv * 100) + "%");
        }
    }

    static class FoRender extends DefaultTableCellRenderer {

        public FoRender() {
            super();
            setHorizontalAlignment(RIGHT);
        }

        @Override
        public void setValue(Object value) {
            DecimalFormat dfE = new DecimalFormat("0.00E0");
            setText((value == null) ? "" : dfE.format(value));
        }
    }

    public class DiffRender extends DefaultTableCellRenderer {

        public DiffRender() {
            super();
            setHorizontalAlignment(RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value == null) {
                setText("");
            } else {
                nf.setMaximumFractionDigits(2);
                nf.setMinimumFractionDigits(2);
                double oF = Double.valueOf(value.toString());
                setText((value == null) ? "" : nf.format(oF * -100) + "%");
                if (row < prfSum.getProfile().getLreWinSize() + 3 && row > 1) {
                    cell.setForeground(Color.RED);
                } else {
                    cell.setForeground(Color.BLACK);
                }
            }
            return cell;
        }
    }

    static class ColumnHeader extends DefaultTableCellRenderer {

        public ColumnHeader() {
            super();
            setHorizontalAlignment(CENTER);
            setBackground(Color.PINK);
        }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : value.toString());
        }
    }

    public void iniNumTable(ProfileSummary prfSum) {
        this.prfSum = prfSum;
        if (prfSum == null || prfSum.getProfile() == null) {
            clearTable();
            return;
        }
        JTableHeader th = numTable.getTableHeader();
        th.setDefaultRenderer(new ColumnHeader());
        TableColumnModel tcm = numTable.getColumnModel();
        TableColumn tc0 = tcm.getColumn(0);
        tc0.setCellRenderer(new CycleRender());
        tc0.setPreferredWidth(3);
        TableColumn tc1 = tcm.getColumn(1);
        tc1.setCellRenderer(new FcRender());
        tc1.setPreferredWidth(6);
        TableColumn tc2 = tcm.getColumn(2);
        tc2.setCellRenderer(new EcRender());
        tc2.setPreferredWidth(6);
        TableColumn tc4 = tcm.getColumn(3);
        tc4.setCellRenderer(new DiffRender());
        tc4.setPreferredWidth(7);
        Cycle runner = null;
        try {
            runner = prfSum.getLreWindowStartCycle().getPrevCycle().getPrevCycle().getPrevCycle();
        } catch (Exception e) {
            return;
        }
        if (runner == null){
            clearTable();
            return;
        }
        if(runner.getCycNum() == 0) {
                runner = runner.getNextCycle();
            }
        for(int i=0; i<11; i++){
            numTable.setValueAt(runner.getCycNum(), i, 0);
            numTable.setValueAt(runner.getFc(), i, 1);
            numTable.setValueAt(runner.getEc(), i, 2);
            numTable.setValueAt(runner.getFoFracFoAv(), i, 3);
            if(runner.getNextCycle() == null){
                return;
            }
            runner = runner.getNextCycle();
        }
        repaint();
    }

    public void clearTable() {
        for (int i = 0; i < 11; i++) {
            numTable.setValueAt(null, i, 0);
            numTable.setValueAt(null, i, 1);
            numTable.setValueAt(null, i, 2);
            numTable.setValueAt(null, i, 3);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        numTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(225, 200));
        setMinimumSize(new java.awt.Dimension(225, 200));
        setPreferredSize(new java.awt.Dimension(225, 200));

        jScrollPane1.setMaximumSize(new java.awt.Dimension(500, 500));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(175, 200));

        numTable.setBackground(new java.awt.Color(244, 245, 247));
        numTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "C", "Fc", "Ec", "%Av. Fo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        numTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        numTable.setMaximumSize(new java.awt.Dimension(240, 500));
        numTable.setMinimumSize(new java.awt.Dimension(0, 0));
        numTable.setPreferredSize(new java.awt.Dimension(240, 175));
        numTable.setShowHorizontalLines(false);
        numTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(numTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable numTable;
    // End of variables declaration//GEN-END:variables
}
