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
package org.lreqpcr.manual_data_import;

import java.awt.Desktop;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.data_objects.RunImpl;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.data_objects.TargetStrandedness;
import org.lreqpcr.data_import_services.RunImportService;
import org.openide.util.Exceptions;

/**
 *
 * @author Bob Rutledge
 */
public class SampleProfileTemplateDataImport extends RunImportService {

    /**
     * Creates an Excel template xls file in a directory selected by the user.
     *
     * @return the template xls file
     * @throws jxl.write.WriteException
     */
    public static void createSampleProfileImportTemplate() throws WriteException, IOException {
        File selectedFile = IOUtilities.newExcelFile();
        if(selectedFile != null){
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
            label = new Label(4, 0, "Note that Amplicon Name, Amplicon Size and Sample Name must be provided for each Fc dataset." +
                    " Profile Name is optional.");
            sheet.addCell(label);
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
            label = new Label(184, 8, "Do Not paste data beyond this point");
            sheet.addCell(label);
            label = new Label(2, 9, "Paste Here", leftYellow);
            sheet.addCell(label);
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
            //That's it!!!!!
            workbook.write();
            workbook.close();

            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            desktop.open(selectedFile);
        } else {
            String msg = "File Error";
            JOptionPane.showMessageDialog(null, msg,
                    "The file could not be opened", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Imports data using the SAMPLE Excel template.
     *
     * @param execelFile the Excel file
     * @return the Run of Excel type
     */
    @Override
    public RunImportData importRunData() {
        //Retrieve the Excel sample profile import file
        File excelImportFile = IOUtilities.openImportExcelFile("Sample Template Data Import");
        if (excelImportFile == null) {
            return null;
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
            JOptionPane.showMessageDialog(null, msg, "Unable to open the Excel file ",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        Sheet sheet = workbook.getSheet(0);
        //Check if this is an LRE Template sheet
        if (sheet.getName().compareTo("LRE Sample Template") != 0) {
            String msg = "This appears not to be a LRE sample template file. Note " +
                    "that the Excel sheet name must be \"LRE Sample Template\"";
            JOptionPane.showMessageDialog(null, msg, "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        DateCell date = null;
        try {
            date = date = (DateCell) sheet.getCell(2, 0);
        } catch (Exception e) {
            String msg = "The Run Date appears to be invalid. Manually replace " +
                    "the run date in the Results sheet (B8), " +
                    "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(null, msg,
                    "Invalid Run Date", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        RunImpl run = new RunImpl();
        run.setName(sheet.getCell(2, 1).getContents());
        run.setRunDate(RunImportUtilities.importExcelDate(date));

        //Import the data
        List<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();
        List<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();//Empty
        NumberFormat numFormat = NumberFormat.getInstance();

        int colCount = sheet.getColumns();
        int rowCount = sheet.getRows();
        int col = 2;//Start column
        int wellNumber = 1;//Used to preserve ordering of the profiles
        while (col < colCount && sheet.getCell(col, 3).getType() != CellType.EMPTY) {
            SampleProfile profile = new SampleProfile();
            profile.setWellNumber(wellNumber);
            profile.setRunDate(run.getRunDate());
            profile.setName(sheet.getCell(col, 2).getContents());
            profile.setAmpliconName(sheet.getCell(col, 3).getContents());
            try {
                profile.setAmpliconSize(Integer.valueOf(sheet.getCell(col, 4).getContents()));

            } catch (NumberFormatException e) {
//Do nothing... Run intialization service will try to retrieve Amplicon size if an Amplicon database is open
            }
            profile.setSampleName(sheet.getCell(col, 5).getContents());
            String ssDNA = sheet.getCell(col, 6).getContents();
            if (ssDNA.equalsIgnoreCase("Yes")) {
                profile.setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
            } else{
                profile.setTargetStrandedness(TargetStrandedness.SINGLESTRANDED);
            }
            //If Profile name is empty, generate a name based on amp and sample names
            if (profile.getName().equals("")) {
                profile.setName(profile.getAmpliconName() + " @ " + profile.getSampleName());
            }
            //Move down the column to collect Fc readings until null cell reached
            int row = 9;
            ArrayList<Double> fcReadings = new ArrayList<Double>();
            while (row < rowCount && sheet.getCell(col, row).getType() != CellType.EMPTY) {
                try {
                    //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                    Number value = numFormat.parse(sheet.getCell(col, row).getContents());
                    fcReadings.add(value.doubleValue());
                } catch (Exception e) {
                }
                row++;
            }
            double[] d = new double[fcReadings.size()];
            for (int i = 0; i < d.length; i++) {
                d[i] = fcReadings.get(i);
            }
            profile.setRawFcReadings(d);
            sampleProfileList.add(profile);
            col++;
            wellNumber++;
        }
        workbook.close();

        RunImportData importData = new RunImportData();
        importData.setRun(run);
        importData.setCalibrationProfileList(calbnProfileList);
        importData.setSampleProfileList(sampleProfileList);
        return importData;
    }
}
