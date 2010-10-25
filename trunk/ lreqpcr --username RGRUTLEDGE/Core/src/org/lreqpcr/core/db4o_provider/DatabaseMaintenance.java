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

package org.lreqpcr.core.db4o_provider;

//import ca.lre.utilities.IOUtilities;
//import com.db4o.Db4o;
//import com.db4o.ObjectContainer;
//import com.db4o.defragment.Defragment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;

/**
 *
 * @author Bob Rutledge
 */
public class DatabaseMaintenance extends javax.swing.JFrame {
    // TODO do something with this...

    private File dbFile;
//    private ObjectContainer db4o;

    /** Creates new form DatabaseMaintenance */
    public DatabaseMaintenance() {
        initComponents();
        setLocationRelativeTo(null);
//        addWindowListener(new WindowAdapter() {
//
//            @Override
//            public void windowClosing(WindowEvent e) {
//                closeDatabase();
//            }
//        });
    }

    private void updateDisplay() {
//        dbNameDisplay.setText(dbFile.getName());
//        dbDirectoryDisplay.setText(dbFile.getParent());
//        dbSizeDisplay.setText(String.valueOf(dbFile.length()));
////        numOfObjectsDisplay.setText(String.valueOf(numberOfObjects(Object.class)));
//        numOfRunsDisplay.setText(String.valueOf(numberOfObjects(Run.class)));
//        numOfProfilessDisplay.setText(String.valueOf(numberOfObjects(Profile.class)));
//        numOfDatafilesDisplay.setText(String.valueOf(numberOfObjects(byte[].class)));
    }

    private void clearDisplay() {
        dbNameDisplay.setText("");
        dbDirectoryDisplay.setText("");
        dbSizeDisplay.setText("");
        numOfRunsDisplay.setText("");
        numOfProfilessDisplay.setText("");
        numOfDatafilesDisplay.setText("");
    }


//    @SuppressWarnings(value = "unchecked")
//    private int numberOfObjects(Class clazz) {
//        List list = db4o.query(clazz);
//        return list.size();
//    }

//    private void closeDatabase(){
//        if (db4o != null) {
//            db4o.close();
//        }
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        openDBbutton = new javax.swing.JButton();
        ImageIcon icon = new ImageIcon("Open24.gif");
        openDBbutton.setIcon(icon);
        jLabel1 = new javax.swing.JLabel();
        dbNameDisplay = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        dbDirectoryDisplay = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        dbSizeDisplay = new javax.swing.JTextField();
        defragButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        numOfRunsDisplay = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        numOfProfilessDisplay = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        numOfDatafilesDisplay = new javax.swing.JTextField();
        closeDbButton = new javax.swing.JButton();

        setTitle("DB4O maintenance");

        openDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/lre/db4oprovider/services/Open24.gif"))); // NOI18N
        openDBbutton.setToolTipText("Open a DB40 database");
        openDBbutton.setFocusable(false);
        openDBbutton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openDBbutton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBbuttonActionPerformed(evt);
            }
        });

        jLabel1.setText("Database Name:");

        dbNameDisplay.setColumns(40);
        dbNameDisplay.setEditable(false);
        dbNameDisplay.setText("  ");

        jLabel2.setText("Database Directory:");

        dbDirectoryDisplay.setColumns(70);
        dbDirectoryDisplay.setEditable(false);
        dbDirectoryDisplay.setText("  ");

        jLabel3.setText("Database Size:");

        dbSizeDisplay.setColumns(40);
        dbSizeDisplay.setEditable(false);
        dbSizeDisplay.setText("  ");

        defragButton.setText("Defragment DB");
//        defragButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                defragButtonActionPerformed(evt);
//            }
//        });

        jLabel5.setText("# of Runs:");

        numOfRunsDisplay.setColumns(20);
        numOfRunsDisplay.setEditable(false);
        numOfRunsDisplay.setText("  ");

        jLabel6.setText("# of Profiles:");

        numOfProfilessDisplay.setColumns(20);
        numOfProfilessDisplay.setEditable(false);
        numOfProfilessDisplay.setText("  ");

        jLabel7.setText("# of Datafiles:");

        numOfDatafilesDisplay.setColumns(20);
        numOfDatafilesDisplay.setEditable(false);
        numOfDatafilesDisplay.setText("  ");

        closeDbButton.setText("Close Database");
        closeDbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                closeDbButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(144, 144, 144)
                                .addComponent(closeDbButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbNameDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)))
                        .addGap(294, 294, 294))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbDirectoryDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                        .addGap(162, 162, 162))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbSizeDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addGap(349, 349, 349))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(defragButton)
                        .addContainerGap(572, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numOfRunsDisplay)
                                .addGap(18, 18, 18))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numOfProfilessDisplay)
                                .addGap(7, 7, 7))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numOfDatafilesDisplay)))
                        .addGap(437, 437, 437))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeDbButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(dbNameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dbDirectoryDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(dbSizeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(numOfRunsDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(numOfProfilessDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(numOfDatafilesDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addComponent(defragButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>

    private void openDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {
//        dbFile = IOUtilities.selectFile();
//        if (dbFile.exists()) {
//            closeDatabase();
//            try {
//                db4o = Db4o.openFile(dbFile.getAbsolutePath());
//            } catch (Exception e) {
//                String msg = "The database file " + dbFile.getName() + " could not be opened.";
//                JOptionPane.showMessageDialog(null, msg, "Unable to open the database file",
//                        JOptionPane.ERROR_MESSAGE);
//            }
//            clearDisplay();
//            updateDisplay();
//        }

    }
//
//    private void defragButtonActionPerformed(java.awt.event.ActionEvent evt) {
//        closeDatabase();
//        try {
//            Defragment.defrag(dbFile.getCanonicalPath());
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
//
//    private void closeDbButtonActionPerformed(java.awt.event.ActionEvent evt) {
//        if (db4o != null) {
//            db4o.close();
//        }
//        clearDisplay();
//    }

//    /**
//    * @param args the command line arguments
//    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new DatabaseMaintenance().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify
    private javax.swing.JButton closeDbButton;
    private javax.swing.JTextField dbDirectoryDisplay;
    private javax.swing.JTextField dbNameDisplay;
    private javax.swing.JTextField dbSizeDisplay;
    private javax.swing.JButton defragButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField numOfDatafilesDisplay;
    private javax.swing.JTextField numOfProfilessDisplay;
    private javax.swing.JTextField numOfRunsDisplay;
    private javax.swing.JButton openDBbutton;
    // End of variables declaration
}
