/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lreqpcr.experiment_ui.components;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.Number;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.IOUtilities;

/**
 *
 * @author Bob Rutledge
 */
public class FcExport {

    public static void exportSampleProfileFcReadings(DatabaseServices exptDB) throws WriteException, IOException {
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile != null) {
            WritableWorkbook workbook = Workbook.createWorkbook(selectedFile);
            //Setup cell formatting
            WritableFont arialBold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            WritableFont arial = new WritableFont(WritableFont.ARIAL, 10);
            WritableCellFormat boldRight = new WritableCellFormat(arialBold);
            boldRight.setAlignment(Alignment.RIGHT);
            WritableCellFormat center = new WritableCellFormat(arial);
            center.setAlignment(Alignment.CENTRE);
            WritableCellFormat right = new WritableCellFormat(arial);
            right.setAlignment(Alignment.RIGHT);
            WritableCellFormat leftYellow = new WritableCellFormat(arial);
            leftYellow.setAlignment(Alignment.LEFT);
            leftYellow.setBackground(Colour.YELLOW);
            WritableCellFormat centerUnderline = new WritableCellFormat(arial);
            centerUnderline.setAlignment(Alignment.CENTRE);
            centerUnderline.setBorder(Border.BOTTOM, BorderLineStyle.DOUBLE, Colour.BLACK);
            //Construct the sheet
            WritableSheet sheet = workbook.createSheet("LRE Sample Template", 0);
            Label label = new Label(1, 0, "Run Date(required):", boldRight);
            sheet.addCell(label);
            label = new Label(1, 1, "Run Name:", boldRight);
            sheet.addCell(label);
            label = new Label(2, 1, "Run 1");
            sheet.addCell(label);
            label = new Label(1, 2, "Profile Name(optnl):", boldRight);
            sheet.addCell(label);
//            label = new Label(4, 0, "Note that Amplicon Name, Amplicon Size and Sample Name must be provided for each Fc dataset."
//                    + " Profile Name is optional.");
//            sheet.addCell(label);
            label = new Label(1, 3, "Amplicon Name:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 4, "Amplicon Size:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 5, "Sample Name:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 6, "Is Target dsDNA:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 7, "Paste Fc datasets starting with cell C10 ");
            sheet.addCell(label);
            label = new Label(1, 8, "Cycle", centerUnderline);
            sheet.addCell(label);
            for (int i = 1; i < 183; i++) {
                label = new Label(i + 1, 8, "Fc" + String.valueOf(i), centerUnderline);
                sheet.addCell(label);
            }
//            label = new Label(184, 8, "Do Not paste data beyond this point");
//            sheet.addCell(label);
//            label = new Label(2, 9, "Paste Here", leftYellow);
//            sheet.addCell(label);
            for (int i = 1; i < 71; i++) {
                label = new Label(1, i + 8, String.valueOf(i), center);
                sheet.addCell(label);
            }
            Date now = Calendar.getInstance().getTime();
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
            dateFormat.setAlignment(Alignment.CENTRE);
            DateTime dateCell = new DateTime(2, 0, now, dateFormat);
            sheet.addCell(dateCell);

            List<SampleProfile> prfList = new ArrayList<SampleProfile>(exptDB.getAllObjects(SampleProfile.class));
            int row = 1;//Starting row
            int col = 2;//Starting column
            for (SampleProfile prf : prfList) {
                label = new Label(col, row, prf.getRun().getName());
                sheet.addCell(label);
                row++;
//                label = new Label(col, row, prf.getName());
//                sheet.addCell(label);
                row++;
                label = new Label(col, row, prf.getAmpliconName());
                sheet.addCell(label);
                row++;
                Number number = new jxl.write.Number(col, row, prf.getAmpliconSize());
                sheet.addCell(number);
                row++;
                label = new Label(col, row, prf.getSampleName());
                row++;
                sheet.addCell(label);
                label = new Label(col, row, "no");
                sheet.addCell(label);
                row = row + 2;
                double[] fc = prf.getRawFcReadings();
                for (int j = 0; j < fc.length; j++) {
                    row++;
                    number = new jxl.write.Number(col, row, fc[j]);
                    sheet.addCell(number);
                }
                col++;
                row = 1;//Starting row
            }
            //That's it!!!!!
            workbook.write();
            workbook.close();

            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(selectedFile);
            }
        }
    }
}
