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

import org.lreqpcr.core.ui_elements.PanelMessages;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.nodes.Node;

/**
 * Multi-functional panel for displaying LRE object information. Depending on
 * the specific LRE object, various labels and text boxes are available. All
 * text fields are editable, with changes immediately made to the LRE object.
 * Note also, any modifications that change the node label (e.g. amplicon
 * size)will also be immediately updated.
 *
 * @author Bob Rutledge
 */
public class LreObjectInfo extends JPanel {

    private LreObject member;
    private Profile profile;
    private KeyAdapter keyListener;
    private LreNode selectedNode;
    private DecimalFormat df = new DecimalFormat("00.0");

    public LreObjectInfo() {
        initComponents();
        keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (member == null) {
                    return;
                }
                if (evt.getComponent() == nameDisplay) {
                    member.setName(nameDisplay.getText());
                    selectedNode.refreshNodeLabel();
                }
                if (evt.getComponent() == ampNameDisplay) {
                    String ampName = ampNameDisplay.getText();
                    profile.setAmpliconName(ampName);
                    selectedNode.refreshNodeLabel();
                    if (profile instanceof AverageProfile) {
                        AverageProfile avProfile = (AverageProfile) profile;
                        for (Profile prf : avProfile.getReplicateProfileList()) {
                            prf.setAmpliconName(ampName);
                        }
                        updateReplicateProfileNodeLabels();
                    }
                }
                if (evt.getComponent() == notesDisplay) {
                    member.setLongDescription(notesDisplay.getText());
                }
                if (evt.getComponent() == sampleNameDisplay) {
                    String sampleName = sampleNameDisplay.getText();
                    profile.setSampleName(sampleName);
                    selectedNode.refreshNodeLabel();
                    if (profile instanceof AverageProfile) {
                        AverageProfile avProfile = (AverageProfile) profile;
                        for (Profile prf : avProfile.getReplicateProfileList()) {
                            prf.setSampleName(sampleName);
                        }
                        updateReplicateProfileNodeLabels();
                    }
                }
                if (evt.getComponent() == ampSizeDisplay) {
                    int i;
                    try {
                        i = Integer.parseInt(ampSizeDisplay.getText());
                    } catch (NumberFormatException nan) {
                        nanErrorDisplay.setVisible(true);
                        nanErrorDisplay.setText("Must be an integer");
                        return;
                    }
                    //Be sure amplicon size is not 0 or negative
                    if (i < 1) {
                        nanErrorDisplay.setVisible(true);
                        nanErrorDisplay.setText("Must be >0");
                        return;
                    }
                    nanErrorDisplay.setText("");
                    profile.setAmpliconSize(i);
                    selectedNode.refreshNodeLabel();
                    //Update the replicate profiles if this is an average profile
                    if (profile instanceof AverageProfile) {
                        AverageProfile avProfile = (AverageProfile) profile;
                        for (Profile prf : avProfile.getReplicateProfileList()) {
                            prf.setAmpliconSize(i);
                        }
                        updateReplicateProfileNodeLabels();
                        //Changing amplicon size changes target quantity and so fire a profile change event
                        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_CHANGED);
                    }
                }
                selectedNode.saveLreObject();
            }
        };

        wellLabelLabel.setVisible(false);
        wellLabelDisplay.setVisible(false);
        ampLabel.setVisible(false);
        ampNameDisplay.setVisible(false);
        ampSizeLabel.setVisible(false);
        ampSizeDisplay.setVisible(false);
        tmLabel.setVisible(false);
        tmDisplay.setVisible(false);
        sampleLabel.setVisible(false);
        sampleNameDisplay.setVisible(false);
        nanErrorDisplay.setVisible(false);
        ssDNAcheckBox1.setVisible(false);
        nameDisplay.addKeyListener(keyListener);
        notesDisplay.addKeyListener(keyListener);
        ampNameDisplay.addKeyListener(keyListener);
        sampleNameDisplay.addKeyListener(keyListener);
        ampSizeDisplay.addKeyListener(keyListener);
    }

    @SuppressWarnings(value = "unchecked")
    public void iniInfo() {
        if (selectedNode == null) {
            return;
        }
        member = selectedNode.getLookup().lookup(LreObject.class);
        if (member == null) {
            clearPanel();
            return;
        }
        if (member.getName() != null) {
            nameDisplay.setText(member.getName());
        } else {
            nameDisplay.setText("");
        }
        if (member.getLongDescription() != null) {
            notesDisplay.setText(member.getLongDescription());
        } else {
            notesDisplay.setText("");
        }
        if (member instanceof Profile) {
            nanErrorDisplay.setVisible(false);
            ampLabel.setVisible(true);
            ampNameDisplay.setVisible(true);
            ampSizeLabel.setVisible(true);
            ampSizeDisplay.setVisible(true);
            sampleLabel.setVisible(true);
            sampleLabel.setVisible(true);
            sampleNameDisplay.setVisible(true);
            profile = (Profile) member;
            //If this is a sample profile, set the target standedness
            if (profile instanceof SampleProfile) {
                ssDNAcheckBox1.setVisible(true);
                if (profile.getTargetStrandedness() == TargetStrandedness.SINGLESTRANDED) {
                    ssDNAcheckBox1.setSelected(true);
                } else {//This must be a Calibration profile and so is double stranded
                    ssDNAcheckBox1.setSelected(false);
                }
            }
            if (profile.getWellLabel() != null) {//This should never be null
                wellLabelLabel.setVisible(true);
                wellLabelDisplay.setVisible(true);
                wellLabelDisplay.setText(profile.getWellLabel());

            } else {
                wellLabelLabel.setVisible(false);
                wellLabelDisplay.setVisible(false);
            }
            ampNameDisplay.setText(profile.getAmpliconName());
            if (profile.getAmpliconSize() == 0) {
                ampSizeDisplay.setText("Unknown");
            } else {
                ampSizeDisplay.setText(String.valueOf(profile.getAmpliconSize()));
            }
            sampleNameDisplay.setText(profile.getSampleName());
            //Display the average Tm, if available
            if (profile.getAmpTm() != -1
                    && profile.hasAnLreWindowBeenFound()
                    && !profile.isExcluded()) {
                //Determine which Tm label to use
                if (profile instanceof AverageProfile) {
                    AverageProfile avPrf = (AverageProfile) profile;
                    if(avPrf.getTheNumberOfActiveReplicateProfiles() > 1){
                        tmLabel.setText("Av. Tm");
                    }
                } else {
                    tmLabel.setText("Tm");
                }
                tmLabel.setVisible(true);
                //Display the Tm
                tmDisplay.setText(df.format(profile.getAmpTm()) + " °C");
                tmDisplay.setVisible(true);
            }
        } else {//This memeber is not a Profile
            wellLabelLabel.setVisible(false);
            wellLabelDisplay.setVisible(false);
            ampLabel.setVisible(false);
            ampNameDisplay.setVisible(false);
            ampSizeLabel.setVisible(false);
            ampSizeDisplay.setVisible(false);
            tmLabel.setVisible(false);
            tmDisplay.setVisible(false);
            tmLabel.setVisible(false);
            tmDisplay.setVisible(false);
            sampleLabel.setVisible(false);
            sampleNameDisplay.setVisible(false);
            nanErrorDisplay.setVisible(false);
            ssDNAcheckBox1.setVisible(false);
        }
    }//End of iniInfo

    public void clearPanel() {
        member = null;
        nameDisplay.setText("");
        notesDisplay.setText("");
        tmDisplay.setText("");
        tmDisplay.setText("");
        ampLabel.setVisible(false);
        ampNameDisplay.setVisible(false);
        ampSizeLabel.setVisible(false);
        ampSizeDisplay.setVisible(false);
        sampleLabel.setVisible(false);
        sampleNameDisplay.setVisible(false);
        nanErrorDisplay.setVisible(false);
        wellLabelLabel.setVisible(false);
        wellLabelDisplay.setVisible(false);
        tmDisplay.setVisible(false);
        tmDisplay.setVisible(false);
        tmLabel.setVisible(false);
        tmLabel.setVisible(false);
        ssDNAcheckBox1.setVisible(false);
    }

    public void displayMember(LreNode node) {
        selectedNode = node;
        iniInfo();
    }

    private void updateReplicateProfileNodeLabels() {
        //Update the replicate profile node labels
        LreObjectChildren children = (LreObjectChildren) selectedNode.getChildren();
        Node[] nodes = children.getNodes();
        for (Node node : nodes) {
            LreNode ln = (LreNode) node;
            ln.refreshNodeLabel();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        nameDisplay = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        ampLabel = new javax.swing.JLabel();
        ampSizeLabel = new javax.swing.JLabel();
        sampleLabel = new javax.swing.JLabel();
        ampNameDisplay = new javax.swing.JTextField();
        ampSizeDisplay = new javax.swing.JTextField();
        sampleNameDisplay = new javax.swing.JTextField();
        nanErrorDisplay = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        notesDisplay = new javax.swing.JTextArea();
        wellLabelLabel = new javax.swing.JLabel();
        wellLabelDisplay = new javax.swing.JTextField();
        ssDNAcheckBox1 = new javax.swing.JCheckBox();
        tmLabel = new javax.swing.JLabel();
        tmDisplay = new javax.swing.JTextField();

        setBackground(new java.awt.Color(244, 245, 247));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(300, 450));
        setMinimumSize(new java.awt.Dimension(300, 260));
        setPreferredSize(new java.awt.Dimension(300, 260));

        jLabel1.setText("Name:");

        nameDisplay.setColumns(15);
        nameDisplay.setText(null);

        jLabel2.setText("Notes");

        ampLabel.setText("Amplicon:");

        ampSizeLabel.setText("Amplicon Size:");

        sampleLabel.setText("Sample:");

        ampNameDisplay.setColumns(25);

        ampSizeDisplay.setColumns(4);

        sampleNameDisplay.setColumns(25);

        nanErrorDisplay.setForeground(new java.awt.Color(255, 0, 0));
        nanErrorDisplay.setText("     ");

        notesDisplay.setColumns(20);
        notesDisplay.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        notesDisplay.setLineWrap(true);
        notesDisplay.setRows(5);
        notesDisplay.setWrapStyleWord(true);
        jScrollPane2.setViewportView(notesDisplay);

        wellLabelLabel.setText("Well:");

        wellLabelDisplay.setEditable(false);
        wellLabelDisplay.setColumns(3);
        wellLabelDisplay.setMaximumSize(new java.awt.Dimension(55, 20));
        wellLabelDisplay.setMinimumSize(new java.awt.Dimension(55, 20));
        wellLabelDisplay.setPreferredSize(new java.awt.Dimension(55, 20));

        ssDNAcheckBox1.setBackground(new java.awt.Color(244, 245, 247));
        ssDNAcheckBox1.setText("ssDNA Target");
        ssDNAcheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ssDNAcheckBox1ActionPerformed(evt);
            }
        });

        tmLabel.setText("Av. Tm:");

        tmDisplay.setEditable(false);
        tmDisplay.setColumns(3);
        tmDisplay.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                        .addComponent(ssDNAcheckBox1)
                        .addGap(79, 79, 79))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(wellLabelLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wellLabelDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sampleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sampleNameDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                        .addGap(19, 19, 19))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ampLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ampNameDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ampSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ampSizeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(tmLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tmDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nanErrorDisplay)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(ssDNAcheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ampLabel)
                    .addComponent(ampNameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ampSizeLabel)
                    .addComponent(ampSizeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nanErrorDisplay)
                    .addComponent(tmLabel)
                    .addComponent(tmDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sampleLabel)
                    .addComponent(sampleNameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wellLabelLabel)
                    .addComponent(wellLabelDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ssDNAcheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ssDNAcheckBox1ActionPerformed
        if (ssDNAcheckBox1.isSelected()) {
            profile.setTargetStrandedness(TargetStrandedness.SINGLESTRANDED);
        } else {
            profile.setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
        }
        selectedNode.saveLreObject();
        selectedNode.refreshNodeLabel();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.PROFILE_CHANGED);
    }//GEN-LAST:event_ssDNAcheckBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ampLabel;
    private javax.swing.JTextField ampNameDisplay;
    private javax.swing.JTextField ampSizeDisplay;
    private javax.swing.JLabel ampSizeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nameDisplay;
    private javax.swing.JLabel nanErrorDisplay;
    private javax.swing.JTextArea notesDisplay;
    private javax.swing.JLabel sampleLabel;
    private javax.swing.JTextField sampleNameDisplay;
    private javax.swing.JCheckBox ssDNAcheckBox1;
    private javax.swing.JTextField tmDisplay;
    private javax.swing.JLabel tmLabel;
    private javax.swing.JTextField wellLabelDisplay;
    private javax.swing.JLabel wellLabelLabel;
    // End of variables declaration//GEN-END:variables
}
