/**
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
package org.lreqpcr.ab7500ver2import;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.core.utilities.IOUtilities;
import org.lreqpcr.core.utilities.WellLabelToWellNumber;
import org.lreqpcr.data_import_services.DataImportType;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = RunImportService.class)
public class AB7500Ver2ImportProvider extends RunImportService {

    @Override
    @SuppressWarnings(value = "unchecked")
    public RunImportData constructRunImportData() {
        //Retrieve the ABI Ver2 export xls file
        File ver2ExcelImportFile = IOUtilities.openImportExcelFile("ABI7500 Version 2 Data Import");
        if (ver2ExcelImportFile == null) {
            return null;
        }
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(ver2ExcelImportFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BiffException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (workbook == null) {
            String msg = "The selected file (" + ver2ExcelImportFile.getName() + " could not be opened";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Unable to open the selected file " + ver2ExcelImportFile.getName(),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        Sheet resultSheet = null;
        Sheet ampDataSheet = null;
        resultSheet = workbook.getSheet("Results");
        ampDataSheet = workbook.getSheet("Amplification Data");
        if (resultSheet == null || ampDataSheet == null){
            String msg = "Either the \"Results\" or \"Amplification Data\" "
                    + "worksheet was not present or has been renamed.\n"
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        DateCell date;
        try {
            date = (DateCell) resultSheet.getCell(1, 3);
        } catch (Exception e) {
            String msg = "The Run Date appears to be invalid. Manually entry the "
                    + "run date in the \"Results\" sheet (B4), "
                    + "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Run Date",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
//All of the following is to deal with the ability of the user to change the column order
        int columnAbsent = -1;//Default is absent == -1
        //Setup column identity assignment
        int wellLabelCol = columnAbsent;//Well label
        int sampleNameCol = columnAbsent;
        int ampliconNameCol = columnAbsent;
        int taskCol = columnAbsent;//Designates a calibrator (STANDARD) from a sample (UNKNOWN) profile
        int ctCol = columnAbsent;//Cycle threshold
        int ftCol = columnAbsent;//Fluorescence threshold
        int calibratorQuantityCol = columnAbsent;//Quantity of the lambda calibrator in pigograms
        int tmCol = columnAbsent;//Amplicon melting temperature (Tm) "Tm1"
        int resultCol = 0;
        int resultColCount = resultSheet.getColumns();//Width of Results sheet
        while (resultCol < resultColCount) {
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Well")) {
                wellLabelCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Sample Name")) {
                sampleNameCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Target Name")) {
                ampliconNameCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Task")) {
                taskCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Cт")) {
                ctCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Ct Threshold")) {
                ftCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Quantity")) {
                calibratorQuantityCol = resultCol;
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Quantity")) {
                calibratorQuantityCol = resultCol;//Assumes it is the first Quanity col!!
            }
            if (resultSheet.getCell(resultCol, 7).getContents().equals("Tm1")) {
                tmCol = resultCol;
            }
            resultCol++;
        }
        //Check to see if all of the compusory columns were found
        if (wellLabelCol == columnAbsent) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The \"Well\" column was not found in the Results sheet (sheet #1)"
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "No Well column",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (ampliconNameCol == columnAbsent) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The \"Target Name\" column was not found in the Results sheet (sheet #1). "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "No Target Name column",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (sampleNameCol == columnAbsent) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The \"Sample Name\" column was not found in the Results sheet (sheet #1). "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "No Sample Name column",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (taskCol == columnAbsent) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The \"Task\" column was not found in the Results sheet (sheet #2)"
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "No Task column",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        //Setup the Run and determine run date
        String runName = ver2ExcelImportFile.getName();
        //Remove the file extension
        runName = runName.substring(0, runName.indexOf("."));
        Date runDate = RunImportUtilities.importExcelDate(date);
        //Import the data
        List<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();
        List<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
        //Determine the strandedness of the Targets
        TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
        NumberFormat numFormat = NumberFormat.getInstance();

        //Cycle down A1-H12 (96 rows) using Sample Name to denote a profile
        int resultRow = 8;//Starting row in the Result sheet
        int ampRow = 8;//Starting row in the Amplification Data sheet
        boolean reachedTheBottom = false;
        while (!reachedTheBottom) {
            //Assume that if sample name is blank, this is a blank well
            if (!resultSheet.getCell(ampliconNameCol, resultRow).getContents().equals("")) {
                Profile profile = null;
                //Determine if this is a calibration profile
                if (resultSheet.getCell(taskCol, resultRow).getContents().equals("STANDARD")) {
                    profile = new CalibrationProfile();
                    CalibrationProfile calbnProfile = (CalibrationProfile) profile;
                    if (calibratorQuantityCol != columnAbsent) {
                        try {
                            Number value = numFormat.parse(resultSheet.getCell(calibratorQuantityCol, resultRow).getContents());
                            calbnProfile.setLambdaMass(value.doubleValue());
                        } catch (Exception e) {
                            calbnProfile.setLambdaMass(0);
                        }
                    }
                } else {//Must be a Sample Profile
                    profile = new SampleProfile();
                    profile.setTargetStrandedness(targetStrandedness);
                }

                profile.setWellLabel(resultSheet.getCell(wellLabelCol, resultRow).getContents());
                WellLabelToWellNumber.labelToNumber96Well_AB7900(profile);
                profile.setSampleName(resultSheet.getCell(sampleNameCol, resultRow).getContents());
                profile.setAmpliconName(resultSheet.getCell(ampliconNameCol, resultRow).getContents());
                profile.setName(profile.getAmpliconName() + "@" + profile.getSampleName());

                if (ctCol != columnAbsent) {
                    try {
                        profile.setCt(Double.parseDouble(resultSheet.getCell(ctCol, resultRow).getContents()));
                    } catch (Exception e) {
                    }
                }
                if (ftCol != columnAbsent) {
                    try {
                        profile.setFt(Double.parseDouble(resultSheet.getCell(ftCol, resultRow).getContents()));
                    } catch (Exception e) {
                    }
                }
                if (tmCol != columnAbsent) {
                    try {
                        profile.setAmpTm(Double.parseDouble(resultSheet.getCell(tmCol, resultRow).getContents()));
                    } catch (Exception e) {
                    }
                }

                //Retrieve and store the Fc dataset
                //Find the start of the Fc dataset via well name
                while (!ampDataSheet.getCell(0, ampRow).getContents().equals(profile.getWellLabel())) {
                    ampRow++;
                }//Assume no errors
                //Retrieve the raw Fc readings (Rn)
                ArrayList<Double> fcDataSet = new ArrayList<Double>();
//This is necessary to catch moving the ampRow one row beyond the bottom of the dataSheet
                try {
//This will throw an OutOfBounds error if ampRow goes beyond the bottom of the datasheet
                    while (ampDataSheet.getCell(0, ampRow).getContents().equals(profile.getWellLabel())) {
//This is necessary to avoid reading null Fc reads caused by excluding a well from the dataset
                        try {
                            //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                            Number value = numFormat.parse(ampDataSheet.getCell(3, ampRow).getContents());
                            fcDataSet.add(value.doubleValue());
                        } catch (Exception e) {
                        }
                        ampRow++;

                    }
                } catch (Exception e) {
                }
                if (!fcDataSet.isEmpty()) {
                    double[] fcArray = new double[fcDataSet.size()];
                    for (int j = 0; j < fcDataSet.size(); j++) {
                        fcArray[j] = fcDataSet.get(j);
                    }
                    profile.setRawFcReadings(fcArray);
                }

                //This is necessary to eliminate empty Fc datasets
//                if (profile.getRawFcReadings() != null) {
                    if (CalibrationProfile.class.isAssignableFrom(profile.getClass())) {
                        CalibrationProfile calProfile = (CalibrationProfile) profile;
                        calbnProfileList.add(calProfile);
                    } else {
                        SampleProfile sampleProfile = (SampleProfile) profile;
                        sampleProfileList.add(sampleProfile);
                    }
//                }
            }//End of If not blank
                    try {
                        resultRow++;
                        resultSheet.getCell(0, resultRow).getContents();
                    } catch (Exception e) {
                        reachedTheBottom = true;
                    }
        }//End of While

        RunImportData importData = new RunImportData(DataImportType.STANDARD, runDate, runName);
        importData.setCalibrationProfileList(calbnProfileList);
        importData.setSampleProfileList(sampleProfileList);
        return importData;
    }
}
