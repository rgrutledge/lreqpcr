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

import com.google.common.collect.Lists;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Displays and processes changes to the LRE window selection parameters.
 *
 * @author Bob Rutledge
 */
public class LreWindowParametersPanel extends javax.swing.JPanel implements UniversalLookupListener, PropertyChangeListener {

    private KeyAdapter keyAdapter;
    private Double minFc;//Can be set to zero to reset to automated StartCycle selection
    private Double foThreshold = 0d;
    private DecimalFormat df = new DecimalFormat();
    private DatabaseServices currentDB;//Experiment or Calibration database
    private LreWindowSelectionParameters selectionParameters;
    private LreAnalysisService lreAnalysisService =
            Lookup.getDefault().lookup(LreAnalysisService.class);
    private UniversalLookup universalLookup = UniversalLookup.getDefault();
    private double averageFmax;

    /**
     * Displays and processes changes to the LRE window selection parameters.
     */
    public LreWindowParametersPanel() {
        initComponents();
        createKeyAdapter();
        minFcDisplay.addKeyListener(keyAdapter);
        foThresholdDisplay.addKeyListener(keyAdapter);
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(this);
        universalLookup.addListner(PanelMessages.NEW_DATABASE, this);
        universalLookup.addListner(PanelMessages.UPDATE_CALIBRATION_PANELS, this);
        universalLookup.addListner(PanelMessages.UPDATE_EXPERIMENT_PANELS, this);
        updateDisplay();
        avReplCvDisplay.setText("");
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
            avReplCvDisplay.setText("");
            avReplCvDisplay.setText("");
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
                    if (e.getComponent().equals(minFcDisplay)) {
                        double newMinFc = -1;//Signifies that an acceptable value has not been found
                        //Reset minFcDisplay
                        String minFcString = minFcDisplay.getText();
                        if (!minFcString.equals("")) {
                            //Process the retrieved String
                            newMinFc = parseMinFcString(minFcString);//Returns -1 if parse fails
                            //If zero is returned, a zero must have been entered, indicating  reset to default (minFc = 0)
                            if (newMinFc > 0 && !determineIfminFcIsAcceptable(newMinFc)) {
                                //newMinFc is not within an acceptable range
                                //So do nothing, as signified by -1
                                newMinFc = -1;
                            }//Else the newMinFc has a valid value
                        } else {
                            //Must be a blank entry
                            //This signals that first cycle below C1/2 is to be used for minFc (default)
                            newMinFc = 0;
                        }
                        //Process the newMinFc value
                        if (newMinFc != -1) {//-1 signifies that the current minFc is not to be changed
                            minFc = newMinFc;
                            //Set the new value
                            selectionParameters.setMinFc(minFc);
                            //Now reset
                            resetSelectionParameters();
                        }else {//Display the previous minFc value
                            updateDisplay();
                        }
                    }
                    if (e.getComponent().equals(foThresholdDisplay)) {
                        double newFoThreshold = 0;
                        //Process the new Fo threshold
                        String foThresholdString = foThresholdDisplay.getText();
                        //Remove "%" at the end of the Fo threshold string, if one exsists
                        if (foThresholdString.contains("%")) {
                            int index = foThresholdString.indexOf("%");
                            foThresholdString = new String(foThresholdString.substring(0, index));
                        }
                        try {
                            newFoThreshold = Double.valueOf(foThresholdString) / 100;
                        } catch (NumberFormatException nan) {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    "The Fo Threshold must be a valid number",
                                    "Invalid Number",
                                    JOptionPane.ERROR_MESSAGE);
                            //Do not change the current Fo threshold
                        }
                        if (newFoThreshold < 0) {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    "The Fo Theshold must be greater than zero",
                                    "Invalid Fo Theshold",
                                    JOptionPane.ERROR_MESSAGE);
                            //Do not change the current Fo threshold
                        }
                        if (newFoThreshold != 0) {
                            foThreshold = newFoThreshold;
                            //A vailid Fo threshold was entered, so store it
                            selectionParameters.setFoThreshold(foThreshold);
                            //Now reset
                            resetSelectionParameters();
                        }
                    }
                }
            }
        };
    }

    /**
     * Returns -1 if parse fails.
     *
     * @param minFcString
     * @return
     */
    private double parseMinFcString(String minFcString) {
        //Remove any commas from the minimum Fc
        while (minFcString.contains(",")) {
            int index = minFcString.indexOf(",");
            minFcString = minFcString.substring(0, index) + minFcString.substring(index + 1);
        }
        try {
            return Double.valueOf(minFcString);
        } catch (NumberFormatException nan) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    "The minimum Fc must be a valid number",
                    "Invalid Number",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    /**
     * The new minimum Fmax must not less than or equal to zero. 
     * @param newMinFmax
     * @return whether newMinFmax is within an acceptable range (5-60% of the average Fmax)
     */
    private boolean determineIfminFcIsAcceptable(double newMinFmax) {
        if (newMinFmax <= 0){
            return false;
        }
//Uses the average Fmax to provide scale for determiing is the newMinFc is acceptable
         //Average Fmax across all runs is calculated by the Experiment panel Tree 
         //everytime a new profile database is opened
            averageFmax = selectionParameters.getAvRunFmax();
            double fractionOfFmax = newMinFmax/averageFmax;
        if (fractionOfFmax > 0.6) {//Greater then 60%
            Toolkit.getDefaultToolkit().beep();
            boolean yes = RunImportUtilities.requestYesNoAnswer("Minimum Fc too high?",
                    "The Minimum Fc appears to be too high.\n Do you want to continue?");
            if (yes) {
                return true;
            }else {return false;}
        }
        if (fractionOfFmax < 0.05) {//<5%
            Toolkit.getDefaultToolkit().beep();
            boolean yes = RunImportUtilities.requestYesNoAnswer("Minimum Fc too low?",
                    "The Minimum Fc appears to be too low.\n Do you want to continue?");
            if (yes) {
                return true;
            }else {return false;}
        }
        return true;
    }

    private void resetSelectionParameters() {
        currentDB.saveObject(selectionParameters);
        reinitializeAllProfiles();
        updateDisplay();
        //Clear the profile editor display
        universalLookup.fireChangeEvent(PanelMessages.CLEAR_PROFILE_EDITOR);
        //Triggers parent panel updates
        if (currentDB.getDatabaseType() == DatabaseType.EXPERIMENT) {
            universalLookup.fireChangeEvent(PanelMessages.UPDATE_EXPERIMENT_PANELS);
        }
        if (currentDB.getDatabaseType() == DatabaseType.CALIBRATION) {
            universalLookup.fireChangeEvent(PanelMessages.UPDATE_CALIBRATION_PANELS);
        }
    }

    @SuppressWarnings("unchecked")
    private void reinitializeAllProfiles() {
        if (!currentDB.isDatabaseOpen()) {
            return;
        }
        if (foThreshold <= 0) {
            return;
        }
        List<AverageProfile> profileList;
//This is necessary becuase for unknown reasons retrieving AverageProfiles 
//objects fail for calibration profiles
        if (currentDB.getDatabaseType() == DatabaseType.CALIBRATION) {
            profileList = currentDB.getAllObjects(AverageCalibrationProfile.class);
        } else {
            profileList = currentDB.getAllObjects(AverageProfile.class);
        }

        if (profileList.isEmpty()) {
            return;
        }
        for (AverageProfile avProfile : profileList) {
            //Need to update the replicate profiles first in order to test if <10N
            for (Profile profile : avProfile.getReplicateProfileList()) {
                lreAnalysisService.conductAutomatedLreWindowSelection(profile, selectionParameters);
                currentDB.saveObject(profile);
            }
            if (!avProfile.isTheReplicateAverageNoLessThan10Molecules()) {
                //The AverageProfile is valid thus reinitialize it
                Profile profile = (Profile) avProfile;
                lreAnalysisService.conductAutomatedLreWindowSelection(profile, selectionParameters);
                currentDB.saveObject(avProfile);
            }
        }
        currentDB.commitChanges();
    }

    private void updateDisplay() {
        if (selectionParameters != null) {
            if (minFc != null && minFc != 0) {
                df.applyPattern(FormatingUtilities.decimalFormatPattern(minFc));
                minFcDisplay.setText(df.format(minFc));
            } else {
                minFcDisplay.setText("First cycle below C1/2");
            }
        }
        if (foThreshold != null) {
            df.applyPattern("#.0%");
            foThresholdDisplay.setText(df.format(foThreshold));
        } else {
            foThresholdDisplay.setText("ERROR");//This absolutely should never happen...
        }
        avReplCvDisplay.setText("");
    }

    void clearPanel() {
        minFcDisplay.setText("");
        foThresholdDisplay.setText("");
        avReplCvDisplay.setText("");
    }

    /**
     * Provides an indicate of quantitative variance based on variance of the
     * replicate profile CV, that is average No CV for sample profiles when an
     * OCF has been applied. Note that replicates with less than 10 molecules
     * are excluded for SampleProfile and that it is expected that calibration
     * profiles to always have target quantities above 10 molecules.
     *
     * @return the replicate CV or -1 if the replicate CV cannot be determined
     * or 0 if too few replicates are available.
     */
    @SuppressWarnings(value = "unchecked")
    private double calcReplicateFoCV() {
        if (currentDB == null || !currentDB.isDatabaseOpen()) {
            return -1;
        }
        ArrayList<Double> replCvValues = Lists.newArrayList();
        if (currentDB.getDatabaseType() == DatabaseType.EXPERIMENT) {
            //Retrieve all Average Sample Profiles
            List<AverageSampleProfile> avSampleProfileList = currentDB.getAllObjects(AverageSampleProfile.class);
            if (avSampleProfileList.isEmpty()) {
                //Database does not contain any profiles so abort
                return 0;
            }
            if (!(avSampleProfileList.get(0).getOCF() >= 0)) {
                //An OCF has not been applied and thus No values are not available
                Toolkit.getDefaultToolkit().beep();
                String msg = "An OCF has not yet been entered and target quantities are thus not available. \n"
                        + "Therefore the average replicate CV can not be determined.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "Target quantities are not available",
                        JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            //Calculate and collect the Replicate Fo CVs
            for (AverageSampleProfile avProfile : avSampleProfileList) {
                List<Double> noValues = Lists.newArrayList();
                if (!avProfile.isTheReplicateAverageNoLessThan10Molecules()) {
//Only include replicate that are >10 molecules in order to avoid scattering produced by Poisson distribution 
                    double sum = 0;
                    for (SampleProfile profile : avProfile.getReplicateProfileList()) {
                        if (profile.hasAnLreWindowBeenFound() && !profile.isExcluded()) {
                            noValues.add(profile.getNo());
                            sum += profile.getNo();
                        }
                        if (noValues.size() > 1) {
                            double avNo = sum / noValues.size();
                            replCvValues.add(MathFunctions.calcStDev(noValues) / avNo);
                        }
                    }
                }
            }
        }
        if (currentDB.getDatabaseType() == DatabaseType.CALIBRATION) {
            List<AverageCalibrationProfile> avCalProfileList = currentDB.getAllObjects(AverageCalibrationProfile.class);
            for (AverageCalibrationProfile avProfile : avCalProfileList) {
                List<Double> foValues = Lists.newArrayList();
                //Don't be concerned about target quantity, so focus solely on avFo
                double sum = 0;
                for (Profile profile : avProfile.getReplicateProfileList()) {
                    if (profile.hasAnLreWindowBeenFound() && !profile.isExcluded()) {
                        foValues.add(profile.getAvFo());
                        sum += profile.getAvFo();
                    }
                }
                if (foValues.size() > 1) {//A CV can only be determined if more than one replicate is available
                    double avFo = sum / foValues.size();
                    replCvValues.add(MathFunctions.calcStDev(foValues) / avFo);
                }
            }
        }
        if (replCvValues.size() > 1) {//If only one replicate is available, a CV cannot be determined
            double cvSum = 0;
            for (Double cv : replCvValues) {
                //Check to see if this is a real number
                String number = String.valueOf(cv);
                if (!number.contains("NaN")) {//This is caused by lacking an amplicon size or OCF
                    cvSum += cv;
                }
            }
            return cvSum / replCvValues.size();
        }
        return 0;//Too few replicates are available
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        minFcDisplay = new JTextField();
        jLabel2 = new JLabel();
        foThresholdDisplay = new JTextField();
        avReplCvDisplay = new JLabel();
        calcAvReplCV = new JRadioButton();

        setBackground(new Color(244, 245, 247));
        setBorder(BorderFactory.createTitledBorder("LRE Window Selection Parameters"));
        setMinimumSize(new Dimension(250, 100));
        setPreferredSize(new Dimension(230, 100));

        jLabel1.setText("Min Fc:");
        jLabel1.setToolTipText("Minimum Fc is used to select the Start Cycle or set to zero for automated selection");

        minFcDisplay.setColumns(12);

        jLabel2.setText("Fo threshold:");
        jLabel2.setToolTipText("Threshold difference with the average Fo, for a Cycle to be added to the top of the LRE window"); // NOI18N

        foThresholdDisplay.setColumns(4);

        avReplCvDisplay.setText("  ");
        avReplCvDisplay.setToolTipText("Note that Replicat Profiles with <10 molecules are not included");

        calcAvReplCV.setBackground(new Color(244, 245, 247));
        calcAvReplCV.setText("Calc Av Repl CV:");
        calcAvReplCV.setToolTipText("Calculates the average quantitative CV produced by each set of replicate profiles");
        calcAvReplCV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                calcAvReplCVActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(calcAvReplCV)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(avReplCvDisplay, GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(minFcDisplay, GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(foThresholdDisplay, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(minFcDisplay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(foThresholdDisplay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(avReplCvDisplay)
                    .addComponent(calcAvReplCV))
                .addContainerGap(34, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void calcAvReplCVActionPerformed(ActionEvent evt) {//GEN-FIRST:event_calcAvReplCVActionPerformed
        calcAvReplCV.setSelected(false);
        df.applyPattern("#0.0%");
        double avCV = calcReplicateFoCV();
        if (!(avCV <= 0)) {
            avReplCvDisplay.setText(df.format(avCV));
        } else {
            if (avCV == 0) {
                avReplCvDisplay.setText("Too few replicates");
            } else {
                avReplCvDisplay.setText("");
            }
        }
    }//GEN-LAST:event_calcAvReplCVActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel avReplCvDisplay;
    private JRadioButton calcAvReplCV;
    private JTextField foThresholdDisplay;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JTextField minFcDisplay;
    // End of variables declaration//GEN-END:variables

    /**
     * This provides update functionality that is independent from the parent
     * panel
     *
     * @param key the event key
     */
    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.NEW_DATABASE) {//Open, New or Close database file change
            currentDB = (DatabaseServices) universalLookup.getAll(PanelMessages.NEW_DATABASE).get(0);
            if (currentDB == null) {
                clearPanel();
            } else {
                if (currentDB.isDatabaseOpen()) {
                    selectionParameters = (LreWindowSelectionParameters) currentDB.getAllObjects(LreWindowSelectionParameters.class).get(0);
                    if (selectionParameters == null) {
                        //This should never happen
                        clearPanel();
                    } else {
                        minFc = selectionParameters.getMinFc();
                        foThreshold = selectionParameters.getFoThreshold();
                        updateDisplay();
                    }
                } else {
                    selectionParameters = null;
                    clearPanel();
                }
            }
        }
        if (key == PanelMessages.UPDATE_CALIBRATION_PANELS || key == PanelMessages.UPDATE_EXPERIMENT_PANELS) {
            avReplCvDisplay.setText("");
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //This is called everytime a node is selected, which is redundant most of the time
        //This initiates an updateDisplay call which can be very intensive when the database is large
        //Actually, it is called twice everytime a node is selected!!!!
        //Maybe a new Top Component window has been selected but not necessarily
        //Be sure to never include anything in this Method that is time intensive
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc instanceof DatabaseProvider) {
            DatabaseProvider dbProvider = (DatabaseProvider) tc;
            DatabaseType type = dbProvider.getDatabaseServices().getDatabaseType();
            //Test if the database holds profiles
            if (type == DatabaseType.EXPERIMENT || type == DatabaseType.CALIBRATION) {
                if (currentDB != dbProvider.getDatabaseServices()) {
                    //A new Experiment or Calibration TC has been selected thus the parameters need to be updated
                    currentDB = dbProvider.getDatabaseServices();
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
//                        currentDB = null;
                        clearPanel();
                    }
                }
            }
        }
    }
}
