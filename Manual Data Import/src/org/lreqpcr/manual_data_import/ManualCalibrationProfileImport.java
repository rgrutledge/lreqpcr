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

import java.awt.Desktop;
import java.awt.Toolkit;
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
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.IOUtilities;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.data_import_services.DataImportType;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class ManualCalibrationProfileImport extends RunImportService {

    /**
     * Creates an Excel calibration template file.
     *
     * @throws jxl.write.WriteException
     */
    public static void createCalbnProfileTemplate() throws WriteException, IOException {
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
            center.setAlignment(Alignment.RIGHT);
            WritableCellFormat leftYellow = new WritableCellFormat(arial);
            leftYellow.setAlignment(Alignment.LEFT);
            leftYellow.setBackground(Colour.YELLOW);
            WritableCellFormat centerUnderline = new WritableCellFormat(arial);
            centerUnderline.setAlignment(Alignment.CENTRE);
            centerUnderline.setBorder(Border.BOTTOM, BorderLineStyle.DOUBLE, Colour.BLACK);
            //Construct the sheet
            WritableSheet sheet = workbook.createSheet("LRE Calibration Template", 0);
            Label label = new Label(4, 0, "Note that Run Date, Amplicon Name, Amplicon Size and fg lambda must be provided for each Fc dataset.");
            sheet.addCell(label);
            label = new Label(4, 1, "Run Name, Well Label and Amplicon Tm are optional.");
            sheet.addCell(label);
            label = new Label(1, 0, "Run Date:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 1, "Run Name (optnl):", boldRight);
            sheet.addCell(label);
            label = new Label(1, 2, "Well Label (optnl):", boldRight);
            sheet.addCell(label);
            label = new Label(1, 3, "Amplicon Name:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 4, "Amplicon Size:", boldRight);
            sheet.addCell(label);
            label = new Label(1, 5, "Lambda gDNA (fg):", boldRight);
            sheet.addCell(label);
            label = new Label(1, 6, "Amplicon Tm(optnl):", boldRight);
            sheet.addCell(label);
            label = new Label(1, 7, "Cycle", centerUnderline);
            sheet.addCell(label);
            Date now = Calendar.getInstance().getTime();
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
            dateFormat.setAlignment(Alignment.CENTRE);
            DateTime dateCell = new DateTime(2, 0, now, dateFormat);
            sheet.addCell(dateCell);
            for (int i = 1; i < 183; i++) {
                label = new Label(i + 1, 7, "", centerUnderline);
                sheet.addCell(label);
            }
            label = new Label(184, 7, "Do Not paste data beyond this point");
            sheet.addCell(label);
            for (int i = 1; i < 71; i++) {
                label = new Label(1, i + 7, String.valueOf(i), center);
                sheet.addCell(label);
            }
            workbook.write();
            workbook.close();

            Desktop desktop;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(selectedFile);
            }
        }
    }

    /**
     * Manual import of calibration profiles using an Excel template.
     *
     * @return import data object ready for processing
     */
    @Override
    public RunImportData constructRunImportData() {
        //Determine if a Calibration database is open
        UniversalLookup uLookup = UniversalLookup.getDefault();
        if (uLookup.containsKey(DatabaseType.CALIBRATION)) {
            DatabaseServices calbnDB = (DatabaseServices) uLookup.getAll(DatabaseType.CALIBRATION).get(0);
            if (!calbnDB.isDatabaseOpen()) {
                String msg = "A Calibration database is not open. \n"
                        + "Data import will be terminated.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        msg, "Calibration database not open",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        //Retrieve the Excel sample profile import file
        File excelImportFile = IOUtilities.openImportExcelFile("Manual Calibration Profile Data Import");
        if (excelImportFile == null) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The Calibration Excel data import file could not be opened.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Unable to open the Calibration import datafile",
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
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg,
                    "Unable to open the selected file " + excelImportFile.getName(), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        Sheet sheet = workbook.getSheet(0);
        //Check if this is an LRE Calibration Template sheet
        if (sheet.getName().compareTo("LRE Calibration Template") != 0) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "Based on the worksheet name, this does not appear to be a calibration profile template file.\n"
                    + "Note that an empty calibration profile import template can be created within the\n\'Manual Data Entry\' menu item";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg, "Invalid calibration profile import template",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        DateCell date;
        try {
            date = (DateCell) sheet.getCell(2, 0);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The Run Date appears to be invalid. Manually enter "
                    + "the run date C1),\n"
                    + "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg,
                    "Invalid Run Date", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        //Import the data
        List<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
        NumberFormat numFormat = NumberFormat.getInstance();
        Date runDate = RunImportUtilities.importExcelDate(date);
        String runName = (sheet.getCell(2, 1).getContents());

        int colCount = sheet.getColumns();
        int rowCount = sheet.getRows();
        int col = 2;//Start column
        while (col < colCount && sheet.getCell(col, 2).getType() != CellType.EMPTY) {
            CalibrationProfile calbnProfile = new CalibrationProfile();
            calbnProfile.setWellLabel(sheet.getCell(col, 2).getContents());
            calbnProfile.setAmpliconName(sheet.getCell(col, 3).getContents());
            calbnProfile.setSampleName(sheet.getCell(col, 5).getContents() + " fg");
            try {
                //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                Number value = numFormat.parse(sheet.getCell(col, 5).getContents());
                calbnProfile.setLambdaMass(value.doubleValue());
            } catch (Exception e) {
                calbnProfile.setLambdaMass(0.0);
            }
            try {
                calbnProfile.setAmpliconSize(Integer.valueOf(sheet.getCell(col, 4).getContents()));
            } catch (NumberFormatException e) {
//Do nothing... Run intialization service will try to retrieve Amplicon size if an Amplicon database is open
            }
            if (!sheet.getCell(col, 6).getContents().equals("")) {
                try {
                    calbnProfile.setAmpTm(Double.valueOf(sheet.getCell(col, 6).getContents()));
                } catch (Exception e) {
                }
            }
            DecimalFormat df = new DecimalFormat("###,###");
            calbnProfile.setName(calbnProfile.getAmpliconName() + "-" + df.format(calbnProfile.getLambdaMass() * 1000000));
            //Move down the column to collect Fc readings until null cell is reached
            int row = 8;
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

        RunImportData importData = new RunImportData(DataImportType.MANUAL_CALIBRATION_PROFILE, runDate, runName);
        importData.setCalibrationProfileList(calbnProfileList);
        return importData;
    }
}
