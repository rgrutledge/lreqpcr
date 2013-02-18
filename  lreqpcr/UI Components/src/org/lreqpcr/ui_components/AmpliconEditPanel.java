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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.data_objects.AmpliconImpl;
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
 * Panel for viewing and editing of Amplicons within the Amplicon database
 *
 * @author Bob Rutledge
 */
public class AmpliconEditPanel extends JPanel
        implements LookupListener, UniversalLookupListener {

    private DatabaseServices ampliconDB;
    private KeyAdapter keyListener;
    private LreNode selectedNode;
    private Amplicon selectedAmplicon;
    private final Result<LreNode> nodeResult;

    public AmpliconEditPanel() {
        initComponents();
        nameErrorDisplay.setText("");
        keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (selectedAmplicon == null) {
                    return;
                }
                if (evt.getComponent() == nameDisplay) {
                    if (nameDisplay.getText().equals("")) {
                        nameErrorDisplay.setText("");
                        nameErrorDisplay.setText("Amplicon name is not unique");
                        return;
                    }
                    if (!isAmpliconNameUnique(nameDisplay.getText())) {
                        nameErrorDisplay.setText("Amplicon name is not unique");
                        return;//Prevents saving of a non-unique amplicon
                    } else {//Name is unique
                        if (selectedAmplicon != null) {
                            selectedAmplicon.setName(nameDisplay.getText());
                            nameErrorDisplay.setText("");
                            selectedNode.saveLreObject();
                            selectedNode.refreshNodeLabel();
                            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_AMPLICON_TREE);
                        }
                    }
                }
                if (evt.getComponent() == shortDescriptionDisplay) {
                    selectedAmplicon.setShortDescription(shortDescriptionDisplay.getText());
                }
                if (evt.getComponent() == notesDisplay) {
                    selectedAmplicon.setLongDescription(notesDisplay.getText());
                }
                if (evt.getComponent() == upPrimerDisplay) {
                    selectedAmplicon.setUpPrimer(upPrimerDisplay.getText());
                }
                if (evt.getComponent() == dnPrimerDisplay) {
                    selectedAmplicon.setDownPrimer(dnPrimerDisplay.getText());
                }
                if (evt.getComponent() == uniGeneDisplay) {
                    selectedAmplicon.setUniGene(uniGeneDisplay.getText());
                }
                if (evt.getComponent() == ampSeqDisplay) {
                    String ampSeq = ampSeqDisplay.getText();
                    //Additional check for allowable characters would be implement here
                    String newAmpSeq = ampSeq.replace("\n", "");//Remove all line breaks
                    selectedAmplicon.setAmpSequence(newAmpSeq.trim());//Remove any terminal white spaces
                    ampSeqLabel.setText("Amplicon Sequence ("
                            + String.valueOf(selectedAmplicon.getAmpSequence().length())
                            + " bp)");
                }
                if (evt.getComponent() == ampliconSizeDisplay) {
                    int i;
                    try {
                        i = Integer.parseInt(ampliconSizeDisplay.getText());
                    } catch (NumberFormatException nan) {
                        nanErrorDisplay.setVisible(true);
                        nanErrorDisplay.setText("Amplicon size must be an integer");
                        return;
                    }
                    //Be sure amplicon size is not 0 or negative
                    if (i < 1) {
                        nanErrorDisplay.setVisible(true);
                        nanErrorDisplay.setText("Must be >0");
                        return;
                    }
                    nanErrorDisplay.setText("");
                    selectedAmplicon.setAmpliconSize(i);
                }
                selectedNode.saveLreObject();
            }
        };
        nameDisplay.addKeyListener(keyListener);
        notesDisplay.addKeyListener(keyListener);
        ampliconSizeDisplay.addKeyListener(keyListener);
        upPrimerDisplay.addKeyListener(keyListener);
        dnPrimerDisplay.addKeyListener(keyListener);
        shortDescriptionDisplay.addKeyListener(keyListener);
        ampSeqDisplay.addKeyListener(keyListener);
        uniGeneDisplay.addKeyListener(keyListener);

        uniGeneDisplay.setToolTipText("UniGene or Accession Number");

        nodeResult = Utilities.actionsGlobalContext().lookupResult(LreNode.class);
        nodeResult.allItems();
        nodeResult.addLookupListener(this);
        UniversalLookup.getDefault().addListner(PanelMessages.UPDATE_AMPLICON_PANELS, this);
    }

    private boolean isAmpliconNameUnique(String ampName) {
        List nameList = ampliconDB.retrieveUsingFieldValue(AmpliconImpl.class, "name", ampName);
        if (nameList.size() > 0) {
            AmpliconImpl amplicon = (AmpliconImpl) nameList.get(0);
            if (amplicon == selectedAmplicon) {//Likely not needed
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void clearPanel() {
        selectedAmplicon = null;
        nameDisplay.setText("");
        ampliconSizeDisplay.setText("");
        upPrimerDisplay.setText("");
        dnPrimerDisplay.setText("");
        shortDescriptionDisplay.setText("");
        notesDisplay.setText("");
        nameErrorDisplay.setText("");
        ampSeqDisplay.setText("");
        uniGeneDisplay.setText("");
        ampSeqLabel.setText("Amplcon Sequence");
    }

    private void displayAmpInfo() {
        if (selectedAmplicon == null) {
            clearPanel();
            return;
        }
        nanErrorDisplay.setText("");
        if (selectedAmplicon.getName() != null) {
            if (selectedAmplicon.getName().equals("New Amplicon")) {
                nameDisplay.setText("");
            } else {
                nameDisplay.setText(selectedAmplicon.getName());
            }
        } else {
            nameDisplay.setText("");
        }
        if (selectedAmplicon.getAmpliconSize() != 0) {
            ampliconSizeDisplay.setText(String.valueOf(selectedAmplicon.getAmpliconSize()));
        } else {
            ampliconSizeDisplay.setText("");
        }
        if (selectedAmplicon.getUpPrimer() != null) {
            upPrimerDisplay.setText(selectedAmplicon.getUpPrimer());
        } else {
            upPrimerDisplay.setText("");
        }
        if (selectedAmplicon.getDownPrimer() != null) {
            dnPrimerDisplay.setText(selectedAmplicon.getDownPrimer());
        } else {
            dnPrimerDisplay.setText("");
        }
        if (selectedAmplicon.getShortDescription() != null) {
            shortDescriptionDisplay.setText(selectedAmplicon.getShortDescription());
        } else {
            shortDescriptionDisplay.setText("");
        }
        if (selectedAmplicon.getLongDescription() != null) {
            notesDisplay.setText(selectedAmplicon.getLongDescription());
        } else {
            notesDisplay.setText("");
        }
        if (selectedAmplicon.getAmpSequence() != null) {
            ampSeqDisplay.setText(selectedAmplicon.getAmpSequence());
            ampSeqLabel.setText("Amplicon Sequence (" + String.valueOf(selectedAmplicon.getAmpSequence().length()) + " bp)");
        } else {
            ampSeqDisplay.setText("");
            ampSeqLabel.setText("Amplicon Sequence");
        }
        if (selectedAmplicon.getUniGene() != null) {
            uniGeneDisplay.setText(selectedAmplicon.getUniGene());
        } else {
            uniGeneDisplay.setText("");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        nameDisplay = new javax.swing.JTextField();
        ampliconSizeDisplay = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        shortDescriptionDisplay = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nanErrorDisplay = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        dnPrimerDisplay = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        upPrimerDisplay = new javax.swing.JTextField();
        nameErrorDisplay = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notesDisplay = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        ampSeqDisplay = new javax.swing.JTextArea();
        ampSeqLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        uniGeneDisplay = new javax.swing.JTextField();

        jLabel1.setText("Amplicon Name:");

        nameDisplay.setColumns(20);
        nameDisplay.setText(null);

        ampliconSizeDisplay.setColumns(5);

        jLabel2.setText("Notes");

        jLabel4.setText("Target Description");

        shortDescriptionDisplay.setColumns(30);

        jLabel3.setText("Amplicon Size:");

        nanErrorDisplay.setForeground(new java.awt.Color(204, 0, 0));
        nanErrorDisplay.setText("      ");

        jLabel7.setText("3' Primer:");

        dnPrimerDisplay.setColumns(40);

        jLabel6.setText("5' Primer:");

        upPrimerDisplay.setColumns(40);

        nameErrorDisplay.setForeground(new java.awt.Color(204, 51, 0));

        notesDisplay.setColumns(20);
        notesDisplay.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        notesDisplay.setLineWrap(true);
        notesDisplay.setRows(5);
        notesDisplay.setWrapStyleWord(true);
        jScrollPane1.setViewportView(notesDisplay);

        ampSeqDisplay.setColumns(20);
        ampSeqDisplay.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        ampSeqDisplay.setLineWrap(true);
        ampSeqDisplay.setRows(5);
        ampSeqDisplay.setWrapStyleWord(true);
        jScrollPane2.setViewportView(ampSeqDisplay);

        ampSeqLabel.setText("Amplicon Sequence");

        jLabel8.setText("UniGene/Acc#:");

        uniGeneDisplay.setColumns(20);
        uniGeneDisplay.setText(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jScrollPane2))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(nameErrorDisplay))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ampliconSizeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nanErrorDisplay)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(uniGeneDisplay))
                                    .addComponent(shortDescriptionDisplay, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dnPrimerDisplay, 0, 1, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(upPrimerDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ampSeqLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameErrorDisplay)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(ampliconSizeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nanErrorDisplay)
                    .addComponent(jLabel8)
                    .addComponent(uniGeneDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(upPrimerDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(dnPrimerDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shortDescriptionDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ampSeqLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea ampSeqDisplay;
    private javax.swing.JLabel ampSeqLabel;
    private javax.swing.JTextField ampliconSizeDisplay;
    private javax.swing.JTextField dnPrimerDisplay;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nameDisplay;
    private javax.swing.JLabel nameErrorDisplay;
    private javax.swing.JLabel nanErrorDisplay;
    private javax.swing.JTextArea notesDisplay;
    private javax.swing.JTextField shortDescriptionDisplay;
    private javax.swing.JTextField uniGeneDisplay;
    private javax.swing.JTextField upPrimerDisplay;
    // End of variables declaration//GEN-END:variables

    /**
     * Retrieve and display a selected Amplicon Node.
     *
     * @param ev the lookup event
     */
    @SuppressWarnings("unchecked")
    public void resultChanged(LookupEvent ev) {
        //If the active window is the Amplicon editor, ignore
        TopComponent activeTC = WindowManager.getDefault().getRegistry().getActivated();
        if (activeTC == null) {
            return;
        }
        if (activeTC.getName().compareTo("Amplicon Editor") == 0) {
            return;
        }
        Lookup.Result r = (Result) ev.getSource();
        Collection<LreNode> c = r.allInstances();
        if (!c.isEmpty()) {
            selectedNode = c.iterator().next();
            //Test to see if this node is from an amplicon database
            DatabaseType type = selectedNode.getDatabaseServices().getDatabaseType();
            if (type != DatabaseType.AMPLICON) {
//                clearPanel();
                return;
            }
            if (selectedNode.getDatabaseServices() != ampliconDB) {
                ampliconDB = selectedNode.getDatabaseServices();
            }
            selectedAmplicon = selectedNode.getLookup().lookup(Amplicon.class);
            displayAmpInfo();
            if (selectedAmplicon == null) {
                return;
            }
            if (selectedAmplicon.getName().compareTo("") == 0) {
                //This appears to be a new Amplicon
                nameDisplay.requestFocusInWindow();
            }
        }
    }

    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.UPDATE_AMPLICON_PANELS) {
            //Open, New or Close database file changed
            if (ampliconDB == null) {
                clearPanel();
            } else {
                if (!ampliconDB.isDatabaseOpen()) {
                    clearPanel();
                }
            }
        }
    }
}
