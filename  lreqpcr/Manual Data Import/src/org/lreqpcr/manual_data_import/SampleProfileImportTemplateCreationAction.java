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
package org.lreqpcr.manual_data_import;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import jxl.write.WriteException;
import org.openide.util.Exceptions;

public final class SampleProfileImportTemplateCreationAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            SampleProfileTemplateImport.createSampleProfileImportTemplate();
        } catch (WriteException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}