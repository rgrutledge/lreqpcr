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
package org.lreqpcr.amplicon_ui.amplicon_io;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.PanelMessages;
import org.lreqpcr.core.utilities.IOUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * This import function if based on the AmpliconImportExcelTemplate format
 *
 * @author Bob Rutledge
 */
public class AmpliconImport {

    public static void importAmplicon(DatabaseServices ampliconDB) {
        if (!ampliconDB.isDatabaseOpen()) {
            String msg = "An Amplicon database has not been opened." +
                    "Data import will be terminated.";
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    msg,
                    "No Amplicon database is available",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        //Retrieve the Excel sample profile import file
        File excelImportFile = IOUtilities.openImportExcelFile("Amplicon Import");
        if (excelImportFile == null) {
            return;
        }
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(excelImportFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BiffException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (workbook == null) {
            String msg = "The Excel import file could not be opened";
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Unable to open the Excel file ",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Sheet sheet = workbook.getSheet(0);
        //Check if this is an LRE Template sheet
        if (sheet.getName().compareTo("Amplicon Import Template") != 0) {
            String msg = "This appears not to be a Amplicon import template file. Note " +
                    "that the Excel sheet name must be \"Amplicon Import Template\"";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
//        int colCount = sheet.getColumns();
        int rowCount = sheet.getRows();
        int row = 1;//Start column
        while (row < rowCount) {
            Amplicon amp = new Amplicon();
            amp.setName(sheet.getCell(0, row).getContents());
            try {
                amp.setAmpliconSize(Integer.valueOf(sheet.getCell(1, row).getContents()));
            } catch (NumberFormatException e) {
                String msg = "The size of " + amp.getName() + " does not appear to be an integer." +
                        "An amplicon size of zero will be entered instead.";
                JOptionPane.showMessageDialog(
                        WindowManager.getDefault().getMainWindow(),
                        msg,
                        "Amplicon size is not an integer",
                        JOptionPane.ERROR_MESSAGE);
                amp.setAmpliconSize(0);
            }
            amp.setUpPrimer(sheet.getCell(2, row).getContents());
            amp.setDownPrimer(sheet.getCell(3, row).getContents());
            amp.setAmpSequence(sheet.getCell(4, row).getContents());
            amp.setShortDescription(sheet.getCell(5, row).getContents());
            amp.setUniGene(sheet.getCell(6, row).getContents());
            amp.setLongDescription(sheet.getCell(7, row).getContents());
            ampliconDB.saveObject(amp);
            row++;
        }
        ampliconDB.commitChanges();
        UniversalLookup.getDefault().fireChangeEvent(PanelMessages.UPDATE_AMPLICON_PANELS);
    }
}
