/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lreqpcr.experiment_ui.components;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.utilities.IOUtilities;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *
 * @author Bob Rutledge
 */
public class SampleFcExport {

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

            //Export based on Run with each run within a worksheet
            List<Run> runList = new ArrayList<>(exptDB.getAllObjects(Run.class));
            int runNum = 0;
            for (Run run : runList) {
                //Construct the sheet
                //Create the worksheet name based on run date and run name if present
                //Retrieve the Run data
                SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
                String sheetName = "";
                if (run.getName() != null) {
                    sheetName = run.getName() + "-" + sdf.format(run.getRunDate());
                } else {
                    sheetName = sdf.format(run.getRunDate());
                }
                WritableSheet sheet = workbook.createSheet(sheetName, runNum);
                Label label = new Label(1, 0, "Run Date:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 1, "Run Name:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 2, "Well Label:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 3, "Amplicon Name:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 4, "Amplicon Size:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 5, "Sample Name:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 6, "Is Target dsDNA:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 7, "Amplicon Tm:", boldRight);
                sheet.addCell(label);
                label = new Label(1, 8, "Cycle", centerUnderline);
                sheet.addCell(label);
                for (int i = 1; i < 183; i++) {
                    label = new Label(i + 1, 8, "", centerUnderline);
                    sheet.addCell(label);
                }
                for (int i = 1; i < 71; i++) {
                    label = new Label(1, i + 8, String.valueOf(i), center);
                    sheet.addCell(label);
                }
                Date runDate = run.getRunDate();
                DateFormat customDateFormat = new DateFormat("ddMMMyy");
                WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
                dateFormat.setAlignment(Alignment.CENTRE);
                DateTime dateCell = new DateTime(2, 0, runDate, dateFormat);
                sheet.addCell(dateCell);
                if (run.getName() != null) {
                    label = new Label(2, 1, run.getName(), center);
                    sheet.addCell(label);
                }

                int row = 2;//Starting row
                int col = 2;//Starting column
                List<AverageProfile> avPrfList = run.getAverageProfileList();
                for (AverageProfile avPrf : avPrfList) {
                    //Only export replicate profile Fc datasets
                    for (Profile prf : avPrf.getReplicateProfileList()) {
                        if (prf.getWellLabel() != null) {
                            label = new Label(col, row, prf.getWellLabel(), center);
                            sheet.addCell(label);
                        }
                        row++;
                        label = new Label(col, row, prf.getAmpliconName(), center);
                        sheet.addCell(label);
                        row++;
                        Number number = new jxl.write.Number(col, row, prf.getAmpliconSize());
                        sheet.addCell(number);
                        row++;
                        label = new Label(col, row, prf.getSampleName(), center);
                        sheet.addCell(label);
                        row++;
                        String dsDNA = "no";
                        if (prf.getTargetStrandedness() == TargetStrandedness.DOUBLESTRANDED ){
                            dsDNA = "yes";
                        }
                        label = new Label(col, row, dsDNA, center);
                        sheet.addCell(label);
                        row++;
                        if (prf.getAmpliconTm() != 0.0d) {
                            number = new jxl.write.Number(col, row, prf.getAmpliconTm());
                            sheet.addCell(number);
                        }
                        row++;
                        double[] fc = prf.getRawFcReadings();
                        for (int j = 0; j < fc.length; j++) {
                            row++;
                            number = new jxl.write.Number(col, row, fc[j]);
                            sheet.addCell(number);
                        }
                        col++;
                        row = 2;//Starting row
                    }//End of replicate profile loop
                }//End of Average Profile loop
                runNum++;
            }//End of Run loop
            workbook.write();
            workbook.close();

            Desktop desktop;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(selectedFile);
            }
        }
    }
}
