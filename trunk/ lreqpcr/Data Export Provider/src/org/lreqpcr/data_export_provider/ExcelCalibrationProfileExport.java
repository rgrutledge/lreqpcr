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
package org.lreqpcr.data_export_provider;

import com.google.common.collect.Lists;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.utilities.IOUtilities;
import org.lreqpcr.core.utilities.MathFunctions;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;

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
        ArrayList<AverageCalibrationProfile> profileList = new ArrayList<AverageCalibrationProfile>(prfList);
        Collections.sort(profileList);
        //Calculate the average OCF and CV
        double ocfSum = 0;
        ArrayList<Double> ocfArray = Lists.newArrayList();
        for (int i = 0; i < profileList.size(); i++) {
            Profile profile = (Profile) profileList.get(i);
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
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(selectedFile);
        } catch (Exception e) {
            String msg = "The file '" + selectedFile.getName()
                    + "' could not be opened, possibly because it is already open.";
            JOptionPane.showMessageDialog(null, msg, "Unable to open " + selectedFile.getName(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Setup cell formatting
        WritableFont arialBold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableFont arial = new WritableFont(WritableFont.ARIAL, 10);
        WritableCellFormat boldRight = new WritableCellFormat(arialBold);
        boldRight.setAlignment(Alignment.RIGHT);
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
        Label label = new Label(1, 1, "Average OCF:", boldRight);
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
        label = new Label(4, 3, "avFo CV", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(5, 3, "C1/2", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(6, 3, "Fmax", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(7, 3, "Amp Size", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(8, 3, "Lam-fg", centerBoldUnderline);
        sheet.addCell(label);

        label = new Label(9, 3, "Notes", centerBoldUnderline);
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
            if (profile.getEmax() > 1.0){
                number = new Number(2, row, profile.getAdjustedOCF(), floatFormat);
            } else {
                number = new Number(2, row, profile.getOCF(), floatFormat);
            }
            sheet.addCell(number);
            number = new Number(3, row, profile.getEmax(), percentFormat);
            sheet.addCell(number);
            number = new Number(4, row, profile.getAvFoCV(), percentFormat);
            sheet.addCell(number);
            number = new Number(5, row, profile.getMidC(), floatFormat);
            sheet.addCell(number);
            double fmax = (profile.getEmax() / profile.getDeltaE()) * -1;
            number = new Number(6, row, fmax, floatFormat);
            sheet.addCell(number);
            number = new Number(7, row, profile.getAmpliconSize(), integerFormat);
            sheet.addCell(number);
            number = new Number(8, row, profile.getLambdaMass() * 1000000d, integerFormat);
            sheet.addCell(number);
            String notes = "";
            if (profile.isExcluded()) {
                if (profile.getLongDescription() != null) {
                    notes = "EXCLUDED " + profile.getLongDescription();
                } else {
                    notes = "EXCLUDED ";
                }
            } else {
                notes = profile.getLongDescription();
            }
            label = new Label(9, row, notes);
            sheet.addCell(label);
            number = new Number(9, row, profile.getDeltaE(), exponentialFormat);
            sheet.addCell(number);
            row++;
        }

        //Add formulas
        String equation = "AVERAGE(D5:D" + String.valueOf(row) + ")";
        Formula formula = new Formula(3, row + 1, equation, percentFormat);
        sheet.addCell(formula);
        equation = "STDEV(D5:D" + String.valueOf(row) + ")";
        formula = new Formula(3, row + 2, equation, percentFormat);
        sheet.addCell(formula);
        equation = "AVERAGE(E5:E" + String.valueOf(row) + ")";
        formula = new Formula(4, row + 1, equation, percentFormat);
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
