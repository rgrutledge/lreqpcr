/**
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
package org.lreqpcr.mxpver3_4import;

import java.net.URL;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.utilities.IOUtilities;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class MxpVer3_4ImportProvider extends RunImportService {

    public String getRunImportServiceName() {
        return "Mx3000P Ver 3.4";
    }

    public URL getHelpFile() {
        try {
            try {
                return new URI("file:HelpFiles/mxpVer4.html").toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public RunImportData importRunData() {
        //Retrieve the export xls file
        File mxpExcelImportFile = IOUtilities.openImportExcelFile("MXP5000 Version 3.4 Data Import");
        if (mxpExcelImportFile == null) {
            return null;
        }
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(mxpExcelImportFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BiffException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (workbook == null) {
            String msg = "The selected file (" + mxpExcelImportFile.getName() + " could not be opened";
            JOptionPane.showMessageDialog(null, msg, "Unable to open the selected file " + mxpExcelImportFile.getName(), JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Sheet chartSheet = null;
        Sheet reportSheet = null;
        chartSheet = workbook.getSheet(0);
        reportSheet = workbook.getSheet(1);
        try {
        } catch (Exception e) {
            String msg = "One of the worksheets could not be loaded."
                    + "Be sure that \nthe first sheet contains the Fc"
                    + "datasets and the second\n sheet contains the Report Data.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        DateCell date = null;
        try {
            date = (DateCell) chartSheet.getCell(0, 0);
        } catch (Exception e) {
            String msg = "The Run Date appears to be invalid. Manually entry the\n "
                    + "run date in the \"Chart Data\" sheet (sheet #1) in cell A1,\n "
                    + "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(null, msg,
                    "Invalid Run Date", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        //This is taken from the 7500 Ver2 service provider
        //Setup column identity assignment
        int columnAbsent = -1;//Default is absent == -1
        int wellLabelCol = columnAbsent;//Well label
        int wellNameCol = columnAbsent;
        int wellType = columnAbsent;//Designates a calibrator (STANDARD) from a sample (UNKNOWN) profile
        int ctCol = columnAbsent;//Cycle threshold
        int ftCol = columnAbsent;//Fluorescence threshold
        int quantityCol = columnAbsent;//Quantity of the lambda calibrator in pigograms
        int tmCol = columnAbsent;//Amplicon melting temperature (Tm) "Tm1"
        int reportCol = 0;
        int resultColCount = reportSheet.getColumns();//Width of Results sheet
        while (reportCol < resultColCount) {
            if (reportSheet.getCell(reportCol, 0).getContents().equals("Well")) {
                wellLabelCol = reportCol;
            }
            if (reportSheet.getCell(reportCol, 0).getContents().equals("Well Name")) {
                wellNameCol = reportCol;
            }
            if (reportSheet.getCell(reportCol, 0).getContents().equals("Well Type")) {
                wellType = reportCol;
            }
            if (reportSheet.getCell(reportCol, 0).getContents().equals("Ct (dR)")) {
                ctCol = reportCol;
            }
            if (reportSheet.getCell(reportCol, 0).getContents().equals("Threshold (dR)")) {
                ftCol = reportCol;
            }
            if (reportSheet.getCell(reportCol, 0).getContents().startsWith("Quantity")) {
                quantityCol = reportCol;
            }
            if (reportSheet.getCell(reportCol, 0).getContents().equals("Tm Product 1 (-R'(T))")) {
                tmCol = reportCol;
            }
            reportCol++;
        }
        //Check to see if all of the compusory columns were found
        if (wellLabelCol == columnAbsent) {
            String msg = "The \"Well\" column was not found in the Text Report sheet (sheet #1). "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(null, msg,
                    "No Well column", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (wellNameCol == columnAbsent) {
            String msg = "The \"Sample Name\" column was not found in the Text Report sheet (sheet #1). "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(null, msg,
                    "No Well column", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (wellType == columnAbsent) {
            String msg = "The \"Well Type\" column was not found in the Text Report sheet (sheet #1). "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(null, msg,
                    "No Well Type column", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        //Setup the Run and determine run date
        RunImpl run = new RunImpl();
        String runName = mxpExcelImportFile.getName();
//        RunImportUtilities.importExcelImportFile(run, mxpExcelImportFile);
        runName = runName.substring(0, runName.indexOf("."));
        run.setName(runName);
        run.setRunDate(RunImportUtilities.importExcelDate(date));

        //Import the data
        ArrayList<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();
        ArrayList<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
        //Determine the strandedness of the Targets
        TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
        NumberFormat numFormat = NumberFormat.getInstance();

        int reportRow = 1;//Starting row in the Report sheet
        int chartRow = 3;//Starting row in the Chart Data sheet
        boolean reachedTheBottom = false;
        while (!reachedTheBottom) {
            Profile profile = null;
            //Determine if this is a calibration profile
            if (reportSheet.getCell(wellType, reportRow).getContents().equals("Standard")) {
                //This is a calibration profile
                profile = new CalibrationProfile();//Target strandedness is set to double during instantiation
                CalibrationProfile calbnProfile = (CalibrationProfile) profile;
                if (quantityCol != columnAbsent) {
                    try {
                        Number value = numFormat.parse(reportSheet.getCell(quantityCol, reportRow).getContents());
                        calbnProfile.setLambdaMass(value.doubleValue());
                    } catch (Exception e) {
                        calbnProfile.setLambdaMass(0);
                    }
                }
            } else {//Must be a Sample Profile
                profile = new SampleProfile();
                profile.setTargetStrandedness(targetStrandedness);
            }

            profile.setWellLabel(reportSheet.getCell(wellLabelCol, reportRow).getContents());
            //Parse and set the sample and amplicon names (separated by a comma)
            String wellName = reportSheet.getCell(wellNameCol, reportRow).getContents();
            String[] names = RunImportUtilities.parseAmpSampleNames(wellName);
            if (names[0] == null) {
                //Only one String was in the well label
                profile.setSampleName(names[0]);
                profile.setAmpliconName("none");
            } else {
                profile.setSampleName(names[1]);
                profile.setAmpliconName(names[0]);
            }
            profile.setName(profile.getAmpliconName() + " @ " + profile.getSampleName());
            if (ctCol != columnAbsent) {
                try {
                    profile.setCt(Double.parseDouble(reportSheet.getCell(ctCol, reportRow).getContents()));
                } catch (Exception e) {
                }
            }
            if (ftCol != columnAbsent) {
                try {
                    profile.setFt(Double.parseDouble(reportSheet.getCell(ftCol, reportRow).getContents()));
                } catch (Exception e) {
                }
            }
            if (tmCol != columnAbsent) {
                try {
                    profile.setAmpTm(Double.parseDouble(reportSheet.getCell(tmCol, reportRow).getContents()));
                } catch (Exception e) {
                }
            }
            reportRow++;
            //Retrieve and store the Fc dataset
            ArrayList<Double> fcDataSet = new ArrayList<Double>();
            //This assumes identical order of the wells in the report and the chart sheet
            if (!reachedTheBottom) {
                fcRead:
                while (!chartSheet.getCell(2, chartRow).getContents().contains("Fluorescence")) {
                    try {
                        //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                        Number value = numFormat.parse(chartSheet.getCell(2, chartRow).getContents());
                        fcDataSet.add(value.doubleValue());
                    } catch (Exception e) {
                    }
                    chartRow++;
                    try {
                        chartSheet.getCell(2, chartRow).getContents();
                    } catch (Exception e) {
                        reachedTheBottom = true;
                        break fcRead;
                    }
                }
                //Move over the label
                chartRow++;
                //Assume no errors
                double[] fcArray = new double[fcDataSet.size()];
                for (int k = 0; k < fcDataSet.size(); k++) {
                    fcArray[k] = fcDataSet.get(k);
                }
                profile.setRawFcReadings(fcArray);

                if (CalibrationProfile.class.isAssignableFrom(profile.getClass())) {
                    CalibrationProfile calProfile = (CalibrationProfile) profile;
                    calbnProfileList.add(calProfile);
                } else {
                    SampleProfile sampleProfile = (SampleProfile) profile;
                    sampleProfileList.add(sampleProfile);
                }
            }
        }
        RunImportData importData = new RunImportData();
        importData.setRun(run);
        importData.setCalibrationProfileList(calbnProfileList);
        importData.setSampleProfileList(sampleProfileList);
        return importData;
    }
}
