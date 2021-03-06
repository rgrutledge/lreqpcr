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
package org.lreqpcr.data_export_provider;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.utilities.FormatingUtilities;
import org.lreqpcr.core.utilities.IOUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bob Rutledge
 */
public class CalibrationProfileExcelDataExport {

    /**
     * Generic data export for both replicate and average profiles
     */
    @SuppressWarnings("unchecked")
    public static void exportProfiles(HashMap<String, List<CalibrationProfile>> groupList) throws IOException, WriteException {
        DecimalFormat df = new DecimalFormat();
        //Setup the the workbook based on the file choosen by the user
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile == null) {
            return;
        }
        WritableWorkbook workbook;
        try {
            workbook = Workbook.createWorkbook(selectedFile);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The file '" + selectedFile.getName()
                    + "' could not be opened, possibly because it is already open.";
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Unable to open " + selectedFile.getName(),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Setup cell formatting
        WritableFont arialBold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableFont arial = new WritableFont(WritableFont.ARIAL, 10);
        WritableCellFormat boldRight = new WritableCellFormat(arialBold);
        boldRight.setAlignment(Alignment.RIGHT);
        WritableCellFormat boldLeft = new WritableCellFormat(arialBold);
        boldLeft.setAlignment(Alignment.LEFT);
        WritableCellFormat center = new WritableCellFormat(arial);
        center.setAlignment(Alignment.CENTRE);
        WritableCellFormat leftYellow = new WritableCellFormat(arial);
        leftYellow.setAlignment(Alignment.LEFT);
        leftYellow.setBackground(Colour.YELLOW);
        WritableCellFormat centerBoldUnderline = new WritableCellFormat(arialBold);
        centerBoldUnderline.setAlignment(Alignment.CENTRE);
        centerBoldUnderline.setBorder(Border.BOTTOM, BorderLineStyle.DOUBLE, Colour.BLACK);

        WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);
        integerFormat.setAlignment(Alignment.CENTRE);
        WritableCellFormat floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
        floatFormat.setAlignment(Alignment.CENTRE);
        WritableCellFormat percentFormat = new WritableCellFormat(NumberFormats.PERCENT_FLOAT);
        WritableCellFormat exponentialFormat = new WritableCellFormat(NumberFormats.EXPONENTIAL);
        NumberFormat nf = new NumberFormat("###,###");
        WritableCellFormat commaSep = new WritableCellFormat(nf);
        NumberFormat nfOCF = new NumberFormat("###,###.000");
        WritableCellFormat ocfFormat = new WritableCellFormat(nfOCF);

        int pageCounter = 0;
        //This is to sort the resulting worksheets
        List<String> nameArray = new ArrayList<String>(groupList.keySet());
        Collections.sort(nameArray);
        for (String pageName : nameArray) {
//Test whether the run name is >30 characters as this can create identical page names
            if (pageName.length() > 30) {
                Toolkit.getDefaultToolkit().beep();
                String msg = "The Parent name ''" + pageName + "'' is longer that 30 characters.\n"
                        + "The will cause the worksheet name to be truncated."
                        + "\nNote also that identical run names will generate an Excel error."
                        + "\nIf this occurs, select ''Yes'' in the resulting dialog box.";
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        msg,
                        "Parent name is too long",
                        JOptionPane.WARNING_MESSAGE);
            }
            WritableSheet sheet = workbook.createSheet(pageName, pageCounter);

            int startRow = 3;
            Label label = new Label(1, 0, "Name:", boldRight);
            sheet.addCell(label);
            label = new Label(0, startRow, "Run Date", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(1, startRow, "Amplicon", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(2, startRow, "Sample", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(3, startRow, "OCF", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(4, startRow, "Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(5, startRow, "C1/2", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(6, startRow, "Fmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(7, startRow, "Run AvFmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(8, startRow, "Amp Size", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(9, startRow, "Amp Tm", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(10, startRow, "Well", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(11, startRow, "Notes", centerBoldUnderline);
            sheet.addCell(label);


            List<CalibrationProfile> profileList = groupList.get(pageName);
            //Setup the workbook name label
            String name;
            if (profileList.get(0) instanceof AverageProfile) {
                name = pageName + "  (Average Profiles)";
            } else {
                name = pageName + "  (Replicate Profiles)";
            }
            label = new Label(2, 0, name);
            sheet.addCell(label);

            int row = startRow + 1;
            Number number;
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            Collections.sort(profileList);
            for (CalibrationProfile calibrationProfile : profileList) {
                if (calibrationProfile.isOcfNormalizedToFmax()) {
                    double avRunFmax = calibrationProfile.getRun().getAverageFmax();
                    df.applyPattern(FormatingUtilities.decimalFormatPattern(avRunFmax));
                    String avFmaxMessage = "OCF values are normalized to their Run's average Fmax";
                    label = new Label(2, 1, avFmaxMessage, boldLeft);
                    sheet.addCell(label);
                }
                WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
                dateFormat.setAlignment(Alignment.CENTRE);
                DateTime dateCell = new DateTime(0, row, calibrationProfile.getRunDate(), dateFormat);
                sheet.addCell(dateCell);
                label = new Label(1, row, calibrationProfile.getAmpliconName());
                sheet.addCell(label);
                label = new Label(2, row, calibrationProfile.getSampleName());
                sheet.addCell(label);
                if (calibrationProfile.isExcluded()) {
                    //All replicate profiles have been excluded
                    label = new Label(3, row, "nd", center);
                    sheet.addCell(label);
                    label = new Label(10, row, calibrationProfile.getWellLabel());
                    sheet.addCell(label);
                    String s;
                    if (calibrationProfile.getLongDescription() != null) {
                        s = "EXCLUDED " + calibrationProfile.getLongDescription();
                    } else {
                        s = "EXCLUDED ";
                    }
                    label = new Label(11, row, s);
                    sheet.addCell(label);
                    row++;
                    continue;
                } else {
                    number = new Number(3, row, calibrationProfile.getOCF(), integerFormat);
                    sheet.addCell(number);
                }
                number = new Number(4, row, calibrationProfile.getEmax(), percentFormat);
                sheet.addCell(number);
                if (calibrationProfile.getMidC() != 0) {
                    number = new Number(5, row, calibrationProfile.getMidC(), floatFormat);
                    sheet.addCell(number);
                }
                if (calibrationProfile.getFmax() != 0) {
                    double fmax = calibrationProfile.getFmax();
                    number = new Number(6, row, fmax, floatFormat);
                    sheet.addCell(number);
                }
                number = new Number(7, row, calibrationProfile.getRun().getAverageFmax(), floatFormat);
                sheet.addCell(number);
                number = new Number(8, row, calibrationProfile.getAmpliconSize(), integerFormat);
                sheet.addCell(number);
                if (calibrationProfile.getAmpTm() != 0) {
                    number = new Number(9, row, calibrationProfile.getAmpTm(), floatFormat);
                    sheet.addCell(number);
                }
                if (calibrationProfile instanceof AverageProfile) {
                    label = new Label(10, row, "Multiple", center);
                    sheet.addCell(label);
                } else {
                    label = new Label(10, row, calibrationProfile.getWellLabel(), center);
                    sheet.addCell(label);
                }
                if (calibrationProfile.getLongDescription() != null) {
                    label = new Label(11, row, calibrationProfile.getLongDescription());
                    sheet.addCell(label);
                }
                row++;
            }
            pageCounter++;
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
