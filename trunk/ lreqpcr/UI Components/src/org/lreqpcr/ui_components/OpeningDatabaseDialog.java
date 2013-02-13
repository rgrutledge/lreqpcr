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

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class OpeningDatabaseDialog extends JPanel{
//     private JProgressBar progressBar;

    public OpeningDatabaseDialog() {
        super(new BorderLayout());

//        progressBar = new JProgressBar(0, 100);
//        progressBar.setIndeterminate(true);
        
        JTextArea text = new JTextArea("Loading Database File...                         ");
        
        JPanel panel = new JPanel();
//        panel.add(progressBar);
        panel.add(text);
        add(panel, BorderLayout.PAGE_START);      
    }

    public static JFrame makeDialog(){
        JFrame frame = new JFrame("Opening Database");
        frame.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        frame.setResizable(false);
        JComponent newContentPane = new OpeningDatabaseDialog();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

}
