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
package org.lreqpcr.ab7500Ver1import;

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
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 * This is an early attempt to decouple import service providers which can be
 * implemented individually via META-INF.services. This would generate a list
 * of available import services.
 * 
 * @author Bob Rutledge
 */
public class Ab7500Ver1ImportProvider extends RunImportService {

    public String getRunImportServiceName() {
        return "AB7500 Ver 1";
    }

    public URL getHelpFile() {
        try {
            try {
                return new URI("file:HelpFiles/ab7500ver1.html").toURL();
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
        File ver1ExcelImportFile = IOUtilities.openImportExcelFile("AB 7500 Version 1 Data Import");
        if (ver1ExcelImportFile == null) {
            return null;
        }
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(ver1ExcelImportFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BiffException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (workbook == null) {
            String msg = "The selected file (" + ver1ExcelImportFile.getName() + " could not be opened";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Unable to open the selected file " + ver1ExcelImportFile.getName(),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Sheet deltaRnSheet = null;
        Sheet resultSheet = null;
        try {
            deltaRnSheet = workbook.getSheet(0);
            resultSheet = workbook.getSheet(1);
        } catch (Exception e) {
            String msg = "Either the \"Results\" or \"Delta Rn\" worksheet could not be imported. "
                    + "Data import will be terminated.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Excel import file",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        DateCell date = null;
        try {
            date = (DateCell) resultSheet.getCell(1, 7);
        } catch (Exception e) {
            String msg = "The Run Date appears to be invalid. Manually replace "
                    + "the run date in the Results sheet (B8), "
                    + "save the file, and try importing the xls file again.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Invalid Run Date",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }


        //Setup the Run and determine run date
        RunImpl run = new RunImpl();
        String runName = ver1ExcelImportFile.getName();
        runName = runName.substring(0, runName.indexOf(".xls"));
        run.setName(runName);
        run.setRunDate(RunImportUtilities.importExcelDate(date));

        //Import the data
        ArrayList<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();
        ArrayList<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
        //Determine the strandedness of the Targets
        TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
        NumberFormat numFormat = NumberFormat.getInstance();

        int resultRow = 15;//Starting row in the Result sheet
        int dRnRow = 1;//Starting row in the delta Rn sheet
        int resultsRowCount = resultSheet.getRows();
        int dRnColCount = deltaRnSheet.getColumns();
        for (int i = 15; i < resultsRowCount; i++, resultRow++) {
            Profile profile = null;
            //Determine if this is a calibration profile
            if (resultSheet.getCell(3, resultRow).getContents().equals("Standard")) {
                profile = new CalibrationProfile();//Target strandedness is set to double during instantiation
                CalibrationProfile calbnProfile = (CalibrationProfile) profile;
                try {
                    calbnProfile.setLambdaMass(Double.valueOf(resultSheet.getCell(6, resultRow).getContents()));
                } catch (Exception e) {
                    calbnProfile.setLambdaMass(0);
                }

            } else {//Must be a Sample Profile
                profile = new SampleProfile();
                profile.setTargetStrandedness(targetStrandedness);
            }

            profile.setWellLabel(resultSheet.getCell(0, resultRow).getContents());
            String wellName = resultSheet.getCell(1, resultRow).getContents();
            String[] names = RunImportUtilities.parseAmpSampleNames(wellName);
            if (names[1] == null) {
                profile.setSampleName(names[0]);
            } else {
                profile.setAmpliconName(names[0]);
                profile.setSampleName(names[1]);
            }
            profile.setName(profile.getAmpliconName() + "@" + profile.getSampleName());
            try {
                profile.setAmpTm(Double.valueOf(resultSheet.getCell(10, resultRow).getContents()));
            } catch (Exception e) {
            }

            //Retrieve and store the Fc dataset
            //This assumes that the dRn rows always correlate exactly with the Results rows
            //Retrieve the raw Fc readings (Rn)
            ArrayList<Double> fcDataSet = new ArrayList<Double>();
            for (int j = 3; j < dRnColCount; j++) {
                try {
                    //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                    Number value = numFormat.parse(deltaRnSheet.getCell(j, dRnRow).getContents());
                    fcDataSet.add(value.doubleValue());
                } catch (Exception e) {
                }
            }
            dRnRow++;

            double[] fcArray = new double[fcDataSet.size()];
            for (int j = 0; j < fcDataSet.size(); j++) {
                fcArray[j] = fcDataSet.get(j);
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
        RunImportData importData = new RunImportData();
        importData.setRun(run);
        importData.setCalibrationProfileList(calbnProfileList);
        importData.setSampleProfileList(sampleProfileList);
        return importData;
    }
}
