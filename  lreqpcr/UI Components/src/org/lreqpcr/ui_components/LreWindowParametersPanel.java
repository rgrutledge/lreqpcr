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

import com.google.common.collect.Lists;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class LreWindowParametersPanel extends javax.swing.JPanel implements UniversalLookupListener, PropertyChangeListener {

    private KeyAdapter keyAdapter;
    private Double minFc;//Can be set to zero to reset to automated StartCycle selection
    private Double foThreshold;
    private DecimalFormat df = new DecimalFormat();
    private DatabaseServices currentDB;//Experiment or Calibration database
    private LreWindowSelectionParameters selectionParameters;
    private LreAnalysisService profileInitialization =
            Lookup.getDefault().lookup(LreAnalysisService.class);
    private UniversalLookup universalLookup = UniversalLookup.getDefault();

    /** Creates new form LreWindowParametersPanel */
    public LreWindowParametersPanel() {
        initComponents();
        createKeyAdapter();
        minFcDisplay.addKeyListener(keyAdapter);
        foThresholdDisplay.addKeyListener(keyAdapter);
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(this);
        universalLookup.addListner(PanelMessages.DATABASE_FILE_CHANGED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.PROFILE_EXCLUDED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.PROFILE_INCLUDED, this);
        UniversalLookup.getDefault().addListner(PanelMessages.PROFILE_CHANGED, this);
        updateDisplay();
    }

    @SuppressWarnings("unchecked")
    public void updateSelectionParameters(DatabaseServices database, LreWindowSelectionParameters parameters) {
        if (database == null) {
            return;
        }
        currentDB = database;
        selectionParameters = parameters;
        if (!currentDB.isDatabaseOpen()) {
            minFcDisplay.setText("");
            foThresholdDisplay.setText("");
            replAvFoCvDisplay.setText("");
            return;
        }
        if (selectionParameters != null) {
            foThreshold = selectionParameters.getFoThreshold();
            minFc = selectionParameters.getMinFc();
            updateDisplay();
        }
    }

    private void createKeyAdapter() {
        keyAdapter = new KeyAdapter() {

            @Override
            @SuppressWarnings(value = "unchecked")
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 10) {//"Return" key
                    String minFcString = minFcDisplay.getText();
                    String foThresholdString = foThresholdDisplay.getText();
                    //Remove any commas from the minimum Fc
                    while (minFcString.contains(",")) {
                        int index = minFcString.indexOf(",");
                        minFcString = minFcString.substring(0, index) + minFcString.substring(index + 1);
                    }
                    try {
                        minFc = Double.valueOf(minFcString);
                    } catch (NumberFormatException nan) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                "The minimum Fc must be a valid number",
                                "Invalid Number",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //Remove "%" at the end of the Fo threshold string, if one exsists
                    if (foThresholdString.contains("%")) {
                        int index = foThresholdString.indexOf("%");
                        foThresholdString = new String(foThresholdString.substring(0, index));
                    }
                    try {
                        foThreshold = Double.valueOf(foThresholdString) / 100;
                    } catch (NumberFormatException nan) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                "The Fo Threshold must be a valid number",
                                "Invalid Number",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    selectionParameters.setMinFc(minFc);
                    selectionParameters.setFoThreshold(foThreshold);
                    currentDB.saveObject(selectionParameters);
                    reinitializeAllProfiles();
                    updateDisplay();
                    //Triggers parent panel updates
                    if (currentDB.getDatabaseType() == DatabaseType.EXPERIMENT) {
                        universalLookup.fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
                    }
                    if (currentDB.getDatabaseType() == DatabaseType.CALIBRATION) {
                        universalLookup.fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
                    }
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void reinitializeAllProfiles() {
        if (!currentDB.isDatabaseOpen()) {
            return;
        }
        if (foThreshold == 0) {
            return;
        }
        List<Profile> profileList = currentDB.getAllObjects(Profile.class);
        if (profileList.isEmpty()) {
            return;
        }
        if (minFc == 0) {
            //Fall back to the automated LRE window selection
            for (Profile profile : profileList) {
                //Need to initiate a full reanalysis
                profile.setLreWinSize(0);
                profileInitialization.initializeProfile(profile);
                currentDB.saveObject(profile);
            }
        } else {
            for (Profile profile : profileList) {
                profileInitialization.initializeProfile(profile, selectionParameters);
                currentDB.saveObject(profile);
            }
        }
        currentDB.commitChanges();
        calcReplAvFoCV();
    }

    private void updateDisplay() {
        if (selectionParameters != null) {
            if (minFc != null) {
                if (minFc != 0) {
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(minFc));
                    minFcDisplay.setText(df.format(minFc));
                } else {
                    minFcDisplay.setText("First cycle below C1/2");
                }
            } else {
                minFcDisplay.setText("First cycle below C1/2");
            }
        }
        if (foThreshold != null) {
            df.applyPattern("#.0%");
            foThresholdDisplay.setText(df.format(foThreshold));
        }
        calcReplAvFoCV();
    }

    void clearPanel() {
        minFcDisplay.setText("");
        foThresholdDisplay.setText("");
        replAvFoCvDisplay.setText("");
    }

    @SuppressWarnings(value = "unchecked")
    private void calcReplAvFoCV() {
        if (currentDB == null) {
            replAvFoCvDisplay.setText("");
            return;
        }
        //Retrieve all Average Profiles
        List<AverageProfile> prfList = currentDB.getAllObjects(AverageProfile.class);
        //Calculate and collect the Replicate Fo CVs
        ArrayList<Double> replFoCvValues = Lists.newArrayList();
        for (AverageProfile prf : prfList) {
            ArrayList<Profile> replList = (ArrayList<Profile>) prf.getReplicateProfileList();
            ArrayList<Double> avFoValues = Lists.newArrayList();
            double sum = 0;
            for (Profile sampleProfile : replList) {
                if (!sampleProfile.isExcluded()) {
                    sum = sum + sampleProfile.getAvFo();
                    avFoValues.add(sampleProfile.getAvFo());
                }
            }
            if (avFoValues.size() > 1) {
                double avFo = sum / avFoValues.size();
                replFoCvValues.add(MathFunctions.calcStDev(avFoValues) / avFo);
            }
        }
        //Calculate the average Replicate FoCV
        double sum = 0;
        for (Double cv : replFoCvValues) {
            sum = sum + cv;
        }
        double avReplCv = sum / replFoCvValues.size();
        df.applyPattern("#.0%");
        replAvFoCvDisplay.setText(df.format(avReplCv));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        minFcDisplay = new JTextField();
        jLabel2 = new JLabel();
        foThresholdDisplay = new JTextField();
        jLabel3 = new JLabel();
        replAvFoCvDisplay = new JLabel();

        setBackground(new Color(244, 245, 247));
        setBorder(BorderFactory.createTitledBorder("LRE Window Selection Parameters"));
        setMinimumSize(new Dimension(250, 97));
        setPreferredSize(new Dimension(230, 97));

        jLabel1.setText("Min Fc:");
        jLabel1.setToolTipText("Minimum Fc for the Start Cycle: set to zero for automated selection");

        minFcDisplay.setColumns(12);

        jLabel2.setText("Fo threshold:");
        jLabel2.setToolTipText("Threshold difference with the average Fo, for a Cycle to be added to the top of the LRE window"); // NOI18N

        foThresholdDisplay.setColumns(4);

        jLabel3.setText("Av Repl-Fo CV:");
        jLabel3.setToolTipText("An indicator of the overall precision of target quantification");

        replAvFoCvDisplay.setText("  ");
        replAvFoCvDisplay.setToolTipText("");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(foThresholdDisplay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(replAvFoCvDisplay, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(minFcDisplay, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)))
                .addGap(119, 119, 119))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(minFcDisplay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(foThresholdDisplay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(replAvFoCvDisplay))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField foThresholdDisplay;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JTextField minFcDisplay;
    private JLabel replAvFoCvDisplay;
    // End of variables declaration//GEN-END:variables

    /**
     * This provides update functionality that is independent from the parent panel
     * @param key the event key
     */
    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.DATABASE_FILE_CHANGED) {//Open, New or Close database file change
            currentDB = (DatabaseServices) universalLookup.getAll(PanelMessages.DATABASE_FILE_CHANGED).get(0);
            if (currentDB == null) {
                clearPanel();
            } else {
                if (currentDB.isDatabaseOpen()) {
                    selectionParameters = (LreWindowSelectionParameters) currentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    if (selectionParameters == null) {
                        clearPanel();
                    } else {
                        minFc = selectionParameters.getMinFc();
                        foThreshold = selectionParameters.getFoThreshold();
                        updateDisplay();
                    }
                } else {
                    clearPanel();
                }

            }
        }
        if (key == PanelMessages.PROFILE_INCLUDED
                || key == PanelMessages.PROFILE_EXCLUDED
                || key == PanelMessages.PROFILE_CHANGED
                || key == PanelMessages.NEW_RUN_IMPORTED) {
            //Need to update the avReplCV
            calcReplAvFoCV();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //A new TC window has been selected
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc instanceof DatabaseProvider) {
            DatabaseProvider dbProvider = (DatabaseProvider) tc;
            DatabaseType type = dbProvider.getDatabase().getDatabaseType();
            //Test if the database holds profiles
            if (type == DatabaseType.EXPERIMENT || type == DatabaseType.CALIBRATION) {
                //A new experiment or calibration DB has been selected thus the parameters need to be updated
                currentDB = dbProvider.getDatabase();
                if (currentDB.isDatabaseOpen()) {
                    selectionParameters = (LreWindowSelectionParameters)
                            currentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    if (selectionParameters == null) {
                        clearPanel();
                        return;
                    } else {
                        minFc = selectionParameters.getMinFc();
                        foThreshold = selectionParameters.getFoThreshold();
                        updateDisplay();
                    }
                } else {
                    clearPanel();
                    return;
                }
            } else {
                clearPanel();
            }
        }
    }
}
