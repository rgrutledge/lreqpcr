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
package org.lreqpcr.ab7900Ver2_3Import;

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
import org.lreqpcr.core.utilities.WellNumberToLabel;
import org.lreqpcr.data_import_services.DataImportType;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
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
@ServiceProvider(service = RunImportService.class)
public class AB7900Ver2_3ImportProvider extends RunImportService {

    @Override
    @SuppressWarnings(value = "unchecked")
    public RunImportData constructRunImportData() {

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

        Sheet resultSheet;
        Sheet fcSheet;
        try {
            resultSheet = workbook.getSheet(1);
            fcSheet = workbook.getSheet(0);
        } catch (Exception e) {
            String msg = "Either the \"Results\" or \"Rn\" worksheet could not be imported. "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        DateCell date;
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

        String runName = ver2_3ExcelImportFile.getName();
        runName = runName.substring(0, runName.indexOf(".xls"));
        Date runDate = RunImportUtilities.importExcelDate(date);

        //Import the data
        List<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();
        List<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
        //Determine the strandedness of the Targets
        TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
        NumberFormat numFormat = NumberFormat.getInstance();

        int resultRow = 0;//Start at the top of the Result sheet; assumes no well data in first row
        int fcRow = 0;//Starting row in the Fc Data sheet
        boolean reachedTheBottom = false;

        reachBottom:
        while (!reachedTheBottom) {
            //Find the next well or the bottom of the sheet
            boolean foundWell = false;
            while (!foundWell) {
                resultRow++;
                //Is this the bottom of the sheet?
                try {
                    resultSheet.getCell(0, resultRow).getContents();
                } catch (Exception e) {
                    //Reached the bottom of the sheet
                    reachedTheBottom = true;
                    break reachBottom;
                }
                //Test to see if this cell holds a well number designated by an integer
                try {
                    Integer.valueOf(resultSheet.getCell(0, resultRow).getContents());
                    foundWell = true;
                } catch (Exception e) {
                    resultRow++;
                }
            }
            Profile profile = null;
            //Determine if this is a calibration profile
            if (resultSheet.getCell(4, resultRow).getContents().equals("Standard")) {
                profile = new CalibrationProfile();
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
            profile.setName(profile.getSampleName() + "@" + profile.getAmpliconName());

            try {
                profile.setCycleThreshold(Double.parseDouble(resultSheet.getCell(5, resultRow).getContents()));
            } catch (Exception e) {
            }
            try {
                profile.setFluorescenceThreshold(Double.parseDouble(resultSheet.getCell(16, resultRow).getContents()));
            } catch (Exception e) {
            }
            //Tm is not exported!!!

            //Retrieve and store the Fc dataset
            //Retrieve the raw Fc readings (Rn)

            ArrayList<Double> fcDataSet = new ArrayList<Double>();
            int fcCol = 3;
            //Move to the row containing the corresponding well based on well number
            //This assumes that a corresponding well does exist
            while (!fcSheet.getCell(0, fcRow).getContents().equals(String.valueOf(profile.getWellNumber()))) {
                fcRow++;
            }
            //Move across the row until a blank cell is reached, signifying the end of te Rn Fc data
            while (!fcSheet.getCell(fcCol, fcRow).getContents().equals("")) {
                try {
                    //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                    Number value = numFormat.parse(fcSheet.getCell(fcCol, fcRow).getContents());
                    fcDataSet.add(value.doubleValue());
                } catch (Exception e) {
                    //Not sure if this will ever happen
                }
                fcCol++;
            }
            //Reset the row
            fcRow = 0;
            if (!fcDataSet.isEmpty()) {
                double[] fcArray = new double[fcDataSet.size()];
                for (int j = 0; j < fcDataSet.size(); j++) {
                    fcArray[j] = fcDataSet.get(j);
                }
                profile.setRawFcReadings(fcArray);
            }
            //This is necessary to eliminate empty Fc datasets
            // TODO present an error dialog if the Fc dataset is null
            if (profile.getRawFcReadings() != null) {
                if (CalibrationProfile.class.isAssignableFrom(profile.getClass())) {
                    CalibrationProfile calProfile = (CalibrationProfile) profile;
                    calbnProfileList.add(calProfile);

                } else {
                    SampleProfile sampleProfile = (SampleProfile) profile;
                    sampleProfileList.add(sampleProfile);
                }
            }
        }
    RunImportData importData = new RunImportData(DataImportType.STANDARD, runDate, runName);
    importData.setCalibrationProfileList (calbnProfileList);
    importData.setSampleProfileList (sampleProfileList);
    return importData ;
}

}
