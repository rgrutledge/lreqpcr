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

package org.lreqpcr.experiment_ui.actions;

import org.lreqpcr.core.data_objects.RunImpl;
import org.lreqpcr.core.ui_elements.LreNode;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.lreqpcr.core.database_services.SettingsServices;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * DEACTIVATED Retrieves the cycler machine datafile, saving it with the same name
 * as the original datafile into a directory specified by the user
 * 
 * @author Bob Rutledge
 */
public class RetrieveDatafileAction extends AbstractAction {

    private ExplorerManager mgr;

    public RetrieveDatafileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Retrieve Cycler Datafile");
    }

    public void actionPerformed(ActionEvent arg0) {
        SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        RunImpl run = selectedNode.getLookup().lookup(RunImpl.class);
//        if (run.getMachineDataFile() == null) {
//            String msg = "No Cycler Datafile has been imported";
//            NotifyDescriptor d =
//                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
//            return;
//        }
//        File saveToFile = new File(run.getCyclerDatafileName());
        //Allow the user to change the directory and/or name
        File file = null;
        JFileChooser fc = new JFileChooser();
        if (settingsDB.getLastExperimentDatabaseDirectory() != null) {
            try {
                file = new File(settingsDB.getLastExperimentDatabaseDirectory());
            } catch (NullPointerException ev) {
                file = null;
            }
            fc.setCurrentDirectory(file);
//            fc.setSelectedFile(saveToFile);
            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                //Reset the savedFile to the user selection
                file = fc.getSelectedFile();
                settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
//                try {
//                    out.write(run.getRunDataFile());
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                }
                try {
                    desktop.open(file);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
