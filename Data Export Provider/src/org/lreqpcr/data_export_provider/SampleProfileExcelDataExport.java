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

import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import org.openide.windows.WindowManager;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
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
public class SampleProfileExcelDataExport {

    /**
     * Generic data export for both replicate and average profiles
     */
    @SuppressWarnings("unchecked")
    public static void exportProfiles(HashMap<String, List<SampleProfile>> groupList) throws IOException, WriteException {
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
        List<String> nameArray = new ArrayList<>(groupList.keySet());
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
            label = new Label(3, startRow, "No", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(4, startRow, "Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(5, startRow, "C1/2", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(6, startRow, "Fmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(7, startRow, "Run AvFmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(8, startRow, "OCF", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(9, startRow, "Amp Size", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(10, startRow, "Amp Tm", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(11, startRow, "Well", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(12, startRow, "Notes", centerBoldUnderline);
            sheet.addCell(label);

            List<SampleProfile> profileList = groupList.get(pageName);
            //Setup the workbook name label
            String name;
            if (profileList.get(0) instanceof AverageProfile) {
                name = pageName + "  (Average Profiles)";
            } else {
                name = pageName + "  (Replicate Profiles)";
            }
            label = new Label(2, 0, name);
            sheet.addCell(label);
            Collections.sort(profileList);
            int row = startRow + 1;
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            Number number;
            for (SampleProfile sampleProfile : profileList) {
                if (sampleProfile.isTargetQuantityNormalizedToFmax()) {
                    String avFmaxMessage = "Target quantities are normalized to their Run's average Fmax";
                    label = new Label(2, 1, avFmaxMessage, boldLeft);
                    sheet.addCell(label);
                }
                WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
                dateFormat.setAlignment(Alignment.CENTRE);
                DateTime dateCell = new DateTime(0, row, sampleProfile.getRunDate(), dateFormat);
                sheet.addCell(dateCell);
                label = new Label(1, row, sampleProfile.getAmpliconName());
                sheet.addCell(label);
                label = new Label(2, row, sampleProfile.getSampleName(), center);
                sheet.addCell(label);
                double no = sampleProfile.getNo();
                if (no >= 0 && !sampleProfile.isExcluded()) {
                    number = new Number(3, row, no, integerFormat);
                    sheet.addCell(number);
                } else {//Must be -1 signifying that this is an invalid profile\
                    //This will also trigger a note to be added to this Profile in the Notes column
                    label = new Label(3, row, "nd", center);
                    sheet.addCell(label);
                }
                if (sampleProfile.getEmax() != -1) {
                    number = new Number(4, row, sampleProfile.getEmax(), percentFormat);
                    sheet.addCell(number);
                }
                if (!(sampleProfile.getMidC() <= 0)) {
                    number = new Number(5, row, sampleProfile.getMidC(), floatFormat);
                    sheet.addCell(number);
                }
                if (sampleProfile.getFmax() != -1) {
                    double fmax = (sampleProfile.getFmax());
                    number = new Number(6, row, fmax, floatFormat);
                    sheet.addCell(number);
                }
                number = new Number(7, row, sampleProfile.getRun().getAverageFmax(), floatFormat);
                sheet.addCell(number);
                number = new Number(8, row, sampleProfile.getOCF(), floatFormat);
                sheet.addCell(number);
                number = new Number(9, row, sampleProfile.getAmpliconSize(), integerFormat);
                sheet.addCell(number);
                if (sampleProfile.getAmpliconTm() != -1) {
                    number = new Number(10, row, sampleProfile.getAmpliconTm(), floatFormat);
                    sheet.addCell(number);
                }
                label = new Label(11, row, sampleProfile.getWellLabel(), center);
                sheet.addCell(label);
                String note = "";
                if (sampleProfile.getLongDescription() != null) {
                    note = sampleProfile.getLongDescription();
                }
                if (sampleProfile.isExcluded()) {
                    note = "[Excluded] " + note;
                } else {
                    if (sampleProfile instanceof AverageProfile) {
                        AverageProfile avPrf = (AverageProfile) sampleProfile;
                        if (avPrf.isTheReplicateAverageNoLessThan10Molecules()) {
                            note = "[<10 molecules] " + note;
                        } else {
                            //Check to see if this is a valid Average Profile
                            if (!avPrf.areTheRepProfilesSufficientlyClustered()) {
                                note = "[Scattered Replicate Profiles] " + note;
                            }
                        }
                    }
                }
                label = new Label(12, row, note);
                sheet.addCell(label);
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
