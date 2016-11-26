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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.SettingsServices;
import org.lreqpcr.core.ui_elements.LreNode;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * DEACTIVATED due long delays during file import
 * @author Bob Rutledge
 */
public class SaveCyclerDatafileAction extends AbstractAction {

    private ExplorerManager mgr;

    public SaveCyclerDatafileAction(ExplorerManager mgr) {
        this.mgr = mgr;
        putValue(NAME, "Save Cycler Datafile");
    }

    public void actionPerformed(ActionEvent arg0) {
        Node[] nodes = mgr.getSelectedNodes();
        LreNode selectedNode = (LreNode) nodes[0];
        DatabaseServices db = selectedNode.getDatabaseServices();
        SettingsServices settingsDB = Lookup.getDefault().lookup(SettingsServices.class);
        Run run = selectedNode.getLookup().lookup(Run.class);
        JFileChooser fc = new JFileChooser();
        File file;
        byte[] dataFile;
        if (settingsDB.getLastExperimentDatabaseDirectory() != null) {
            try {
                file = new File(settingsDB.getLastExperimentDatabaseDirectory());
            } catch (NullPointerException ev) {
                file = null;
            }
            fc.setCurrentDirectory(file);
            int returnVal = fc.showOpenDialog(null);
            File newFile;
            FileInputStream in;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                newFile = fc.getSelectedFile();
                settingsDB.setLastExperimentDatabaseDirectory(fc.getCurrentDirectory().getAbsolutePath());
                dataFile = new byte[(int) newFile.length()];
                try {
                    in = new FileInputStream(newFile);
                    int c;
                    int i = 0;
                    while ((c = in.read()) != -1) {
                        dataFile[i] = (byte) c;
                        i++;
                    }
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, null, ex);
                }
            }
//            run.setRunDataFile(dataFile);
//            run.setCyclerDatafileName(newFile.getName());
            db.saveObject(run);
            db.commitChanges();
        }
    }
}
