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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.CalibrationProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
import org.openide.windows.WindowManager;

import com.google.common.collect.Lists;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *
 * @author Bob Rutledge
 */
public class ExcelCalibrationProfileExport {

    @SuppressWarnings("unchecked")
    public static void exportCalibrationProfiles(List<AverageCalibrationProfile> prfList) throws WriteException, IOException {
        if (prfList == null) {
            return;
        }
        //Sort the profile list
        //Construction of an ArrayList avoids an exception when attempting to sort DB4O lists
        ArrayList<AverageCalibrationProfile> profileList = new ArrayList<>(prfList);
        Collections.sort(profileList);
        //Calculate the average OCF and CV
        double ocfSum = 0;
        ArrayList<Double> ocfArray = Lists.newArrayList();
        for (int i = 0; i < profileList.size(); i++) {
            CalibrationProfile profile = (CalibrationProfile) profileList.get(i);
            if (!profile.isExcluded()) {
                ocfSum = ocfSum + profile.getOCF();
                ocfArray.add(profile.getOCF());
            }
        }
        double averageOCF = ocfSum / ocfArray.size();
        double sd = MathFunctions.calcStDev(ocfArray);
        double cv = sd / averageOCF;
        //Get the workbook file from the user
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile == null) {
            return;
        }
        WritableWorkbook workbook;
        try {
            workbook = Workbook.createWorkbook(selectedFile);
        } catch (Exception e) {
            String msg = "The file '" + selectedFile.getName()
                    + "' could not be opened, possibly because it is already open.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
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

        //Construct the sheet
        WritableSheet sheet = workbook.createSheet("Average Calbn Profile Export", 0);
        //Column labels
        Label label;
        if (profileList.get(0).isOcfNormalizedToFmax()) {
            label = new Label(1, 0, "*OCF is normalized to the run's average Fmax", boldLeft);
            sheet.addCell(label);
        }
        label = new Label(1, 1, "Average OCF:", boldRight);
        sheet.addCell(label);
        Number number = new Number(2, 1, averageOCF, floatFormat);
        sheet.addCell(number);
        label = new Label(3, 1, "OCF CV:", boldRight);
        sheet.addCell(label);
        number = new Number(4, 1, cv, percentFormat);
        sheet.addCell(number);
        label = new Label(0, 3, "Run Date", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(1, 3, "Amplicon", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(2, 3, "OCF", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(3, 3, "Emax", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(4, 3, "LRE-Emax", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(5, 3, "C1/2", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(6, 3, "Fmax", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(7, 3, "Amp Size", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(8, 3, "Lam-fg", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(9, 3, "#Molecs", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(10, 3, "Notes", centerBoldUnderline);
        sheet.addCell(label);

        //Column values
        int row = 4;
        for (AverageCalibrationProfile profile : profileList) {
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
            dateFormat.setAlignment(Alignment.CENTRE);
            DateTime dateCell = new DateTime(0, row, profile.getRunDate(), dateFormat);
            sheet.addCell(dateCell);
            label = new Label(1, row, profile.getAmpliconName(), center);
            sheet.addCell(label);
            if (profile.isExcluded()) {
                //All replicate profiles have been excluded
                label = new Label(2, row, "nd", center);
                sheet.addCell(label);
                String s;
                if (profile.getLongDescription() != null) {
                    s = "EXCLUDED... " + profile.getLongDescription();
                } else {
                    s = "EXCLUDED ";
                }
                label = new Label(10, row, s, boldLeft);
                sheet.addCell(label);
                row++;
                continue;
            } else {
                number = new Number(2, row, profile.getOCF(), floatFormat);
                sheet.addCell(number);
                label = new Label(3, row, "LRE-derived", center);
                sheet.addCell(label);
                number = new Number(4, row, profile.getMaxEfficiency(), percentFormat);
                sheet.addCell(number);
                number = new Number(5, row, profile.getMidC(), floatFormat);
                sheet.addCell(number);
                double fmax = (profile.getMaxEfficiency() / profile.getChangeInEfficiency()) * -1;
                number = new Number(6, row, fmax, floatFormat);
                sheet.addCell(number);
                number = new Number(7, row, profile.getAmpliconSize(), integerFormat);
                sheet.addCell(number);
                number = new Number(8, row, profile.getLambdaMass() * 1000000d, integerFormat);
                sheet.addCell(number);
                double numberOfGenomes = profile.getLambdaMass() * 1000000d * 18.762;
                number = new Number(9, row, numberOfGenomes, integerFormat);
                sheet.addCell(number);
                String notes;
                String note = "";
                notes = note;
                label = new Label(10, row, notes);

                sheet.addCell(label);
                row++;
            }
        }

        //Add formulas
        label = new Label(3, row + 1, "Average:", boldRight);
        sheet.addCell(label);
        label = new Label(3, row + 2, "SD:", boldRight);
        sheet.addCell(label);
        String equation = "AVERAGE(E5:E" + String.valueOf(row) + ")";
        Formula formula = new Formula(4, row + 1, equation, percentFormat);
        sheet.addCell(formula);
        equation = "STDEV(E5:E" + String.valueOf(row) + ")";
        formula = new Formula(4, row + 2, equation, percentFormat);
        sheet.addCell(formula);
        equation = "AVERAGE(F5:F" + String.valueOf(row) + ")";
        formula = new Formula(5, row + 1, equation, floatFormat);
        sheet.addCell(formula);
        equation = "STDEV(F5:F" + String.valueOf(row) + ")";
        formula = new Formula(5, row + 2, equation, floatFormat);
        sheet.addCell(formula);
        equation = "AVERAGE(G5:G" + String.valueOf(row) + ")";
        formula = new Formula(6, row + 1, equation, floatFormat);
        sheet.addCell(formula);
        equation = "STDEV(G5:G" + String.valueOf(row) + ")/G" + String.valueOf(row + 2);
        formula = new Formula(6, row + 2, equation, percentFormat);
        sheet.addCell(formula);

        workbook.write();
        workbook.close();

        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        desktop.open(selectedFile);
    }
}
