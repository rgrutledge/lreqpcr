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
package org.lreqpcr.ab7900Ver2_3Import;

import java.net.URL;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.data_objects.SampleProfile;
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
import org.lreqpcr.core.utilities.WellNumberToLabel;
import org.lreqpcr.data_import_services.ImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 * AB 7900 Version 2.3 data import
 * In this version, no well labels are provided which must be
 * translated from the well number. Also the data structure 
 * is fixed so there is no need to check for the presence of 
 * specific columns, unlike the 7500 data import
 * 
 * @author Bob Rutledge
 */
public class AB7900Ver2_3ImportProvider extends RunImportService {

    public String getRunImportServiceName() {
        return "AB7900 Ver 2.3";
    }

    public URL getHelpFile() {
        try {
            try {
                // TODO setup the 7900 import help file
                return new URI("file:HelpFiles/ab7900ver2_3.html").toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @SuppressWarnings(value = "unchecked")
    public ImportData importRunData() {

        //Retrieve the export xls file
        File ver2_3ExcelImportFile = IOUtilities.openImportExcelFile("AB 7900 Version 2.3 Data Import");
        if (ver2_3ExcelImportFile == null) {
            return null;
        }
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(ver2_3ExcelImportFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BiffException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (workbook == null) {
            String msg = "The selected file (" + ver2_3ExcelImportFile.getName() + " could not be opened";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Unable to open the selected file " + ver2_3ExcelImportFile.getName(),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

         Sheet resultSheet = null;
         Sheet fcSheet = null;
        try {
            resultSheet = workbook.getSheet(0);
            fcSheet = workbook.getSheet(1);
        } catch (Exception e) {
            String msg = "Either the \"Results\" or \"Rn\" worksheet could not be imported. "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        DateCell date = null;
        try {
            date = (DateCell) resultSheet.getCell(1, 4);
        } catch (Exception e) {
            String msg = "The Run Date appears to be invalid. Manually replace "
                    + "the run date in the Results sheet (B5), "
                    + "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Run Date",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        boolean is96WellPlate;
        Object[] plates = {"96 Well", "384 Well"};
        int n = JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(),
                "Which type of plate was used",
                "Designate the type of plate",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                plates,
                plates[0]);
        if (n == 0) {
            is96WellPlate = true;
        } else {
            is96WellPlate = false;
        }

        //Setup the Run and determine run date
        RunImpl run = new RunImpl();
        String runName = ver2_3ExcelImportFile.getName();
        //This has been deactivated as it can cause long delays
        //Import the excel workbook
//        RunImportUtilities.importExcelImportFile(run, ver2_3ExcelImportFile);
        runName = runName.substring(0, runName.indexOf(".xls"));
        run.setName(runName);
        run.setRunDate(RunImportUtilities.importExcelDate(date));

        //Import the data
        ArrayList<Profile> sampleProfileList = new ArrayList<Profile>();
        ArrayList<Profile> calbnProfileList = new ArrayList<Profile>();
        //Determine the strandedness of the Targets
        TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
        NumberFormat numFormat = NumberFormat.getInstance();

        int resultRow = 11;//Starting row in the Result sheet
        int fcRow = 2;//Starting row in the Fc Data sheet
//Cycle down the rows until an out of bounds exception is encounter, signifying the bottom of the sheet
        try {//This is necessary for the 96 well import
            //"Slope" is present at the end of the resulte in the 384 well export but not the 96 well export??
            while (!resultSheet.getCell(0, resultRow).getContents().equals("Slope")) {
                Profile profile = null;
                //Determine if this is a calibration profile
                if (resultSheet.getCell(4, resultRow).getContents().equals("Standard")) {
                    profile = new CalibrationProfile();//Target strandedness is set to double during instantiation
                    CalibrationProfile calbnProfile = (CalibrationProfile) profile;
                    try {
                        Number value = numFormat.parse(resultSheet.getCell(6, resultRow).getContents());
                        calbnProfile.setLambdaMass(value.doubleValue());
                    } catch (Exception e) {
                        calbnProfile.setLambdaMass(0);
                    }
                } else {//Must be a Sample Profile
                    profile = new SampleProfile();
                    profile.setTargetStrandedness(targetStrandedness);
                }
                profile.setWellNumber(Integer.parseInt(resultSheet.getCell(0, resultRow).getContents()));
                //Extract the well label from the well number
                if (is96WellPlate) {
                    WellNumberToLabel.indexToLabel96WelL_AB7900(profile);
                } else {
                    WellNumberToLabel.indexToLabel384Well_AB7900(profile);
                }

                profile.setSampleName(resultSheet.getCell(1, resultRow).getContents());
                profile.setAmpliconName(resultSheet.getCell(2, resultRow).getContents());
                profile.setName(profile.getSampleName() + " @ " + profile.getAmpliconName());

                try {
                    profile.setCt(Double.parseDouble(resultSheet.getCell(5, resultRow).getContents()));
                } catch (Exception e) {
                }
                try {
                    profile.setFt(Double.parseDouble(resultSheet.getCell(16, resultRow).getContents()));
                } catch (Exception e) {
                }
                //Tm is not exported!!!

                //Retrieve and store the Fc dataset
                //Assumes one to one relationship with the rows within the result sheet
                //Retrieve the raw Fc readings (Rn)
                ArrayList<Double> fcDataSet = new ArrayList<Double>();
                int fcCol = 3;
                //Cycle until reach "Delta Rn" with denotes the end of the profile
                while (!fcSheet.getCell(fcCol, 1).getContents().equals("Delta Rn")) {
                    try {
                        //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                        Number value = numFormat.parse(fcSheet.getCell(fcCol, fcRow).getContents());
                        fcDataSet.add(value.doubleValue());
                    } catch (Exception e) {
                    }
                    fcCol++;
                }

                if (!fcDataSet.isEmpty()) {
                    double[] fcArray = new double[fcDataSet.size()];
                    for (int j = 0; j < fcDataSet.size(); j++) {
                        fcArray[j] = fcDataSet.get(j);
                    }
                    profile.setRawFcReadings(fcArray);


                }
                if (CalibrationProfile.class.isAssignableFrom(profile.getClass())) {
                    calbnProfileList.add(profile);
                } else {
                    sampleProfileList.add(profile);
                }
                resultRow++;
                fcRow++;
            }
        } catch (Exception e) {
        }
        ImportData importData = new ImportData();
        importData.setRun(run);
        importData.setCalibrationProfileList(calbnProfileList);
        importData.setSampleProfileList(sampleProfileList);
        return importData;
    }

    public int compareTo(RunImportService o) {
        return getRunImportServiceName().compareToIgnoreCase(o.getRunImportServiceName());
    }
}
