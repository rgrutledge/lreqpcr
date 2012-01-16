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
import java.awt.Toolkit;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.utilities.IOUtilities;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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
import org.lreqpcr.data_import_services.RunImportService;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class CalibrationProfileTemplateDataImport extends RunImportService {

     /**
     * Creates a calibration Excel template xls file in a directory
     * selected by the user.
     *
     * @return the template xls file
     * @throws jxl.write.WriteException
     */
    public static void createCalbnProfileTemplate() throws WriteException, IOException {
        // TODO implement the new Run import severvice

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
            center.setAlignment(Alignment.RIGHT);
            WritableCellFormat leftYellow = new WritableCellFormat(arial);
            leftYellow.setAlignment(Alignment.LEFT);
            leftYellow.setBackground(Colour.YELLOW);
            WritableCellFormat centerUnderline = new WritableCellFormat(arial);
            centerUnderline.setAlignment(Alignment.CENTRE);
            centerUnderline.setBorder(Border.BOTTOM, BorderLineStyle.DOUBLE, Colour.BLACK);
            //Construct the sheet
            WritableSheet sheet = workbook.createSheet("LRE Calibration Template", 0);
            Label label = new Label(1, 0, "Run Date, Amplicon Name, Amplicon Size and fg lambda must be provided for each Fc dataset ");
            sheet.addCell(label);
            label = new Label(1, 1, "Run Date:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 2, "Amplicon Name:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 3, "Amplicon Size:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 4, "Lambda gDNA (fg):", boldRight);
            sheet.addCell(label);
            label = new Label(1, 5, "Cycle", centerUnderline);
            sheet.addCell(label);
            label = new Label(2, 5, "Fc", centerUnderline);
            sheet.addCell(label);
            label = new Label(3, 5, "Fc", centerUnderline);
            sheet.addCell(label);
            label = new Label(4, 5, "Fc", centerUnderline);
            sheet.addCell(label);
            label = new Label(5, 5, "etc.", centerUnderline);
            sheet.addCell(label);
            label = new Label(1, 6, "1", center);
            sheet.addCell(label);
            label = new Label(1, 7, "2", center);
            sheet.addCell(label);
            label = new Label(1, 8, "3", center);
            sheet.addCell(label);
            label = new Label(1, 9, "etc.", center);
            sheet.addCell(label);
            Date now = Calendar.getInstance().getTime();
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
            dateFormat.setAlignment(Alignment.CENTRE);
            DateTime dateCell = new DateTime(2, 1, now, dateFormat);
            sheet.addCell(dateCell);
            //That's it!!!!!
            workbook.write();
            workbook.close();

            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(selectedFile);
            }
        }else {
            //TODO present an error dialog
        }
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public RunImportData importRunData() {
        //Retrieve the Excel sample profile import file
        File excelImportFile = IOUtilities.openImportExcelFile("***Calibration Template Data Import");
        if (excelImportFile == null) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The calibration Excel data file could not be opened.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "Unable to open the Calibration datafile",
                    JOptionPane.ERROR_MESSAGE);
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
            Toolkit.getDefaultToolkit().beep();
            String msg = "The selected file (" + excelImportFile.getName() + " could not be opened";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "Unable to open the selected file " + excelImportFile.getName(), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        Sheet sheet = workbook.getSheet(0);
        //Check if this is an LRE Calibration Template sheet
        if (sheet.getName().compareTo("LRE Calibration Template") != 0) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "This appears not to be a calibration template file. Note " +
                    "that the Excel sheet name must be 'LRE Calibration Template'";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, "Unable to import data " + excelImportFile.getName(), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        int colCount = sheet.getColumns();
        int rowCount = sheet.getRows();
        int col = 2;//Start column
        DateCell date = null;
        try {
            date = date = (DateCell) sheet.getCell(col, 1);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The Run Date appears to be invalid. Manually enter " +
                    "the run date in the Calibration template import sheet (C2), " +
                    "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg,
                    "Invalid Run Date", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        //Import the data
        List<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();//Not used
        List<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
        NumberFormat numFormat = NumberFormat.getInstance();

        while (col < colCount && sheet.getCell(col, 2).getType() != CellType.EMPTY) {
            CalibrationProfile calbnProfile = new CalibrationProfile();
            calbnProfile.setTargetStrandedness(TargetStrandedness.DOUBLESTRANDED);
            calbnProfile.setRunDate(RunImportUtilities.importExcelDate(date));
            calbnProfile.setAmpliconName(sheet.getCell(col, 2).getContents());
            calbnProfile.setSampleName(sheet.getCell(col, 4).getContents() + " fg");
            try {
                calbnProfile.setAmpliconSize(Integer.valueOf(sheet.getCell(col, 3).getContents()));
            } catch (NumberFormatException e) {
//Do nothing... Run intialization service will try to retrieve Amplicon size if an Amplicon database is open
            }
            try {
                //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                Number value = numFormat.parse(sheet.getCell(col, 4).getContents());
                calbnProfile.setLambdaMass(value.doubleValue());
            } catch (Exception e) {
                calbnProfile.setLambdaMass(0.0);
            }
            DecimalFormat df = new DecimalFormat("###,###");
            calbnProfile.setName(calbnProfile.getAmpliconName() + "-" + df.format(calbnProfile.getLambdaMass() * 1000000));
            //Move down the column to collect Fc readings until null cell reached
            int row = 6;
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
            calbnProfile.setRawFcReadings(d);
            calbnProfileList.add(calbnProfile);
            col++;
        }
        workbook.close();

        RunImpl run = new RunImpl();
        run.setRunDate(RunImportUtilities.importExcelDate(date));
        
        RunImportData importData = new RunImportData();
        importData.setRun(run);
        importData.setCalibrationProfileList(calbnProfileList);
        importData.setSampleProfileList(sampleProfileList);
        return importData;
    }
}
