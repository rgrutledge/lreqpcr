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

import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.openide.windows.WindowManager;
import java.lang.Boolean;

/**
 *
 * @author Bob Rutledge
 */
public class SampleProfileDataExport {

    /**
     * Exports the AverageSampleProfiles from a list of Runs. Each 
     * Run data is placed into a work sheet.
     * 
     * @param runList the list of Runs to be exported
     * @throws IOException Excel file read exception
     * @throws WriteException Excel file write exception
     */
    @SuppressWarnings("unchecked")
    public static void exportProfiles(HashMap<String, List<AverageSampleProfile>> groupList) throws IOException, WriteException {
        Boolean hasEmaxBeenOverridden = false;
        //Setup the the workbook based on the file choosen by the user
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile == null) {
            return;
        }
        WritableWorkbook workbook = null;
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
        Set<String> groupNames = groupList.keySet();
        Iterator<String> it = groupNames.iterator();
        while (it.hasNext()) {
//Test whether the run name is >30 characters as this can create identical page names
            String pageName = it.next();
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

            Label label = new Label(0, 0, "Name:", boldRight);
            sheet.addCell(label);
            label = new Label(0, 2, "Run Date", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(1, 2, "Amplicon", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(2, 2, "Sample", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(3, 2, "No", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(4, 2, "Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(5, 2, "LRE Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(6, 2, "C1/2", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(7, 2, "Fmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(8, 2, "Amp Tm", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(9, 2, "Amp Size", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(10, 2, "OCF", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(11, 2, "Well", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(12, 2, "Notes", centerBoldUnderline);
            sheet.addCell(label);
            int row = 3;
            label = new Label(1, 0, pageName);
            sheet.addCell(label);

            Number number = null;
            DateFormat customDateFormat = new DateFormat("ddMMMyy");

            List<AverageSampleProfile> profileList = groupList.get(pageName);
            Collections.sort(profileList);
            ArrayList<SampleProfile> belowTenMoleculeList = new ArrayList<SampleProfile>();

            for (AverageSampleProfile avProfile : profileList) {
                for (SampleProfile sampleProfile : avProfile.getReplicateProfileList()) {
                    if (sampleProfile.getNo() < 10 && !sampleProfile.isExcluded()) {
                        belowTenMoleculeList.add(sampleProfile);
                        continue;
                    }
                    WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
                    dateFormat.setAlignment(Alignment.CENTRE);
                    DateTime dateCell = new DateTime(0, row, sampleProfile.getRunDate(), dateFormat);
                    sheet.addCell(dateCell);
                    label = new Label(1, row, sampleProfile.getAmpliconName());
                    sheet.addCell(label);
                    label = new Label(2, row, sampleProfile.getSampleName());
                    sheet.addCell(label);
                    if (sampleProfile.isExcluded()) {
                        //All replicate profiles have been excluded
                        label = new Label(3, row, "nd", center);
                        sheet.addCell(label);
                        label = new Label(11, row, sampleProfile.getWellLabel());
                        sheet.addCell(label);
                        String s = "";
                        if (avProfile.getLongDescription() != null) {
                            s = "EXCLUDED " + sampleProfile.getLongDescription();
                        } else {
                            s = "EXCLUDED ";
                        }
                        label = new Label(12, row, s, boldLeft);
                        sheet.addCell(label);
                        row++;
                        continue;
                    } else {
                        number = new Number(3, row, sampleProfile.getNo(), integerFormat);
                        sheet.addCell(number);
                    }
                    String notes = "";
                    String note = "";
                    if (sampleProfile.getLongDescription() != null) {
                        note = sampleProfile.getLongDescription();
                    }
                    if (sampleProfile.isEmaxOverridden()) {
                        notes = "***Emax is fixed to " + String.valueOf(sampleProfile.getOverriddendEmaxValue() * 100) + "%... " + note;
                        hasEmaxBeenOverridden = true;
                        label = new Label(12, row, notes, boldLeft);
                        number = new Number(4, row, sampleProfile.getOverriddendEmaxValue(), percentFormat);
                    } else {
                        notes = note;
                        label = new Label(12, row, notes);
                        number = new Number(4, row, sampleProfile.getEmax(), percentFormat);
                    }
                    sheet.addCell(label);
                    sheet.addCell(number);
                    number = new Number(5, row, sampleProfile.getEmax(), percentFormat);
                    sheet.addCell(number);
                    if (sampleProfile.getMidC() != 0) {
                        number = new Number(6, row, sampleProfile.getMidC(), floatFormat);
                        sheet.addCell(number);
                    }
                    if (sampleProfile.getEmax() != 0) {
                        double fmax = (sampleProfile.getEmax() / sampleProfile.getDeltaE()) * -1;
                        number = new Number(7, row, fmax, floatFormat);
                        sheet.addCell(number);
                    }
                    if (sampleProfile.getAmpTm() != 0) {
                        number = new Number(8, row, sampleProfile.getAmpTm(), floatFormat);
                        sheet.addCell(number);
                    }
                    number = new Number(9, row, sampleProfile.getAmpliconSize(), integerFormat);
                    sheet.addCell(number);
                    number = new Number(10, row, sampleProfile.getOCF(), floatFormat);
                    sheet.addCell(number);
                    label = new Label(11, row, sampleProfile.getWellLabel());
                    sheet.addCell(label);
                    row++;
                }
            }

//List the replicate profiles from the average profiles with No <10
            if (!belowTenMoleculeList.isEmpty()) {
                //Setup the <10 target molecule section
                row++;
                label = new Label(0, row, "Samples with <10 target molecules", boldLeft);
                sheet.addCell(label);
                row++;
                label = new Label(0, row, "Run Date", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(1, row, "Amplicon", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(2, row, "Sample", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(3, row, "No", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(4, row, "Emax", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(5, row, "avFo CV", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(6, row, "C1/2", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(7, row, "Fmax", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(8, row, "Amp Tm", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(9, row, "Amp Size", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(10, row, "OCF", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(11, row, "Well", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(12, row, "LRE-Fo", centerBoldUnderline);
                sheet.addCell(label);
                label = new Label(13, row, "Notes", centerBoldUnderline);
                sheet.addCell(label);
                for (SampleProfile sampleProfile : belowTenMoleculeList) {
                    row++;
                    WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
                    dateFormat.setAlignment(Alignment.CENTRE);
                    DateTime dateCell = new DateTime(0, row, sampleProfile.getRunDate(), dateFormat);
                    sheet.addCell(dateCell);
                    label = new Label(1, row, sampleProfile.getAmpliconName());
                    sheet.addCell(label);
                    label = new Label(2, row, sampleProfile.getSampleName());
                    sheet.addCell(label);
                    if (sampleProfile.isExcluded()) {
                        label = new Label(3, row, "0", integerFormat);
                        sheet.addCell(label);
                        label = new Label(11, row, sampleProfile.getWellLabel());
                        sheet.addCell(label);
                        label = new Label(12, row, sampleProfile.getLongDescription());
                        sheet.addCell(label);
                        row++;
                        continue;
                    }
                    number = new Number(3, row, sampleProfile.getNo(), floatFormat);
                    sheet.addCell(number);
                    if (sampleProfile.getEmax() != 0) {
                        number = new Number(4, row, sampleProfile.getEmax(), percentFormat);
                        sheet.addCell(number);
                    }
                    if (sampleProfile.getAvFoCV() != 0) {
                        number = new Number(5, row, sampleProfile.getAvFoCV(), percentFormat);
                        sheet.addCell(number);
                    }
                    if (sampleProfile.getMidC() != 0) {
                        number = new Number(6, row, sampleProfile.getMidC(), floatFormat);
                        sheet.addCell(number);
                    }
                    if (sampleProfile.getEmax() != 0) {
                        double fmax = (sampleProfile.getEmax() / sampleProfile.getDeltaE()) * -1;
                        number = new Number(7, row, fmax, floatFormat);
                        sheet.addCell(number);
                    }
                    if (sampleProfile.getAmpTm() != 0) {
                        number = new Number(8, row, sampleProfile.getAmpTm(), floatFormat);
                        sheet.addCell(number);
                    }
                    number = new Number(9, row, sampleProfile.getAmpliconSize(), integerFormat);
                    sheet.addCell(number);
                    number = new Number(10, row, sampleProfile.getOCF(), floatFormat);
                    sheet.addCell(number);
                    label = new Label(11, row, sampleProfile.getWellLabel());
                    sheet.addCell(label);
                    number = new Number(12, row, sampleProfile.getAvFo(), exponentialFormat);
                    sheet.addCell(number);
                    label = new Label(13, row, sampleProfile.getLongDescription());
                    sheet.addCell(label);
                }
            }
            pageCounter++;
        }
        workbook.write();
        workbook.close();
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            desktop.open(selectedFile);
        }
    }
}
