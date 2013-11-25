/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lreqpcr.abstepone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "org.lreqpcr.abstepone.AbStepOnePlusAction")
@ActionRegistration(
        displayName = "#CTL_AbStepOnePlusAction")
@ActionReference(path = "Menu/Import", position = 370)
@Messages("CTL_AbStepOnePlusAction=AB StepOne Version 2")
public final class AbStepOnePlusAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        new StepOnePlusImportProvider();
    }
}