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

package org.lreqpcr.amplicon_ui.amplicon_io;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.IOUtilities;

/**
 *
 * @author Bob Rutledge
 */
public class AmpliconExcelExport {

    @SuppressWarnings(value = "unchecked")
    public static void exportAmplicons(DatabaseServices ampliconDB) throws IOException, WriteException{
        File selectedFile = IOUtilities.newExcelFile();
        if(selectedFile != null){
            WritableWorkbook workbook = Workbook.createWorkbook(selectedFile);
            //Setup cell formatting
            WritableFont arial = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
            WritableCellFormat centerArial = new WritableCellFormat(arial);
            WritableFont arialBold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            WritableCellFormat centerUnderline = new WritableCellFormat(arialBold);
            centerUnderline.setAlignment(Alignment.CENTRE);
            centerUnderline.setBorder(Border.BOTTOM, BorderLineStyle.DOUBLE, Colour.BLACK);
            //Construct the sheet
            WritableSheet sheet = workbook.createSheet("Amplicon Export" , 0);
            Label label = new Label(0, 0, "Name", centerUnderline);
            sheet.addCell(label);
            label = new Label(1, 0, "Size", centerUnderline);
            sheet.addCell(label);
            label = new Label(2, 0, "5' Primer", centerUnderline);
            sheet.addCell(label);
            label = new Label(3, 0, "3' Primer", centerUnderline);
            sheet.addCell(label);
            label = new Label(4, 0, "Short Description", centerUnderline);
            sheet.addCell(label);
            label = new Label(5, 0, "Notes", centerUnderline);
            sheet.addCell(label);
            int row = 1;//Starting row
            List<Amplicon> ampList = ampliconDB.getAllObjects(Amplicon.class);
            for (Amplicon amp : ampList){
                label = new Label(0, row, amp.getName(), centerArial);
                sheet.addCell(label);
                label = new Label(1, row, String.valueOf(amp.getAmpliconSize()), centerArial);
                sheet.addCell(label);
                label = new Label(2, row, amp.getUpPrimer(), centerArial);
                sheet.addCell(label);
                label = new Label(3, row, amp.getDownPrimer(), centerArial);
                sheet.addCell(label);
                label = new Label(4, row, amp.getShortDescription(), centerArial);
                sheet.addCell(label);
                label = new Label(5, row, amp.getLongDescription(), centerArial);
                sheet.addCell(label);
                row++;
            }
            //That's it!!!!!
            workbook.write();
            workbook.close();

            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            desktop.open(selectedFile);
        } else {
            // TODO show an error dialog
        }
    }

}
