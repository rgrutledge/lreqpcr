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

import java.awt.Toolkit;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.openide.windows.WindowManager;

/**
 * NOT USED
 * @author Bob Rutledge
 */
public class ProfileWorksheetCreator {

    @SuppressWarnings("unchecked")
    public static void createRunWorksheet(WritableWorkbook workbook, Run run, int sheetNumber) throws WriteException {
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
        NumberFormat nf = new NumberFormat("###,###");
        WritableCellFormat commaSep = new WritableCellFormat(nf);
        NumberFormat nfOCF = new NumberFormat("###,###.000");
        WritableCellFormat ocfFormat = new WritableCellFormat(nfOCF);

        //Test whether the run name is >30 characters. If so abort import of the run
        String runName = run.getName();
        if (runName.length() > 30) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The Run ''" + runName + "'' name is longer that 30 characters.\n"
                    + "The will cause the worksheet name to be truncated."
                    + "\nNote also that identical run names will generate an Excel error."
                    + "\nIf this occurs, select ''Yes'' in the resulting dialog box.";
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    msg,
                    "Run name is too long",
                    JOptionPane.WARNING_MESSAGE);
        }
        WritableSheet sheet = workbook.createSheet(run.getName(), sheetNumber);

        Label label = new Label(0, 0, "Run Name:", boldRight);
        sheet.addCell(label);
        label = new Label(0, 1, "Run Date:", boldRight);
        sheet.addCell(label);
        label = new Label(3, 1, "Average OCF:", boldRight);
        sheet.addCell(label);
        label = new Label(6, 1, "Run Specific OCF:", boldRight);
        sheet.addCell(label);
        label = new Label(1, 3, "Amplicon", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(2, 3, "Sample", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(3, 3, "No", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(4, 3, "Emax", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(5, 3, "avFo CV", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(6, 3, "C1/2", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(7, 3, "Fmax", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(8, 3, "Av. Amp Tm", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(9, 3, "Amp Size", centerBoldUnderline);
        sheet.addCell(label);
        label = new Label(10, 3, "Notes", centerBoldUnderline);
        sheet.addCell(label);
        int row = 4;
        label = new Label(1, 0, run.getName());
        sheet.addCell(label);
        DateFormat customDateFormat = new DateFormat("ddMMMyy");
        WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
        dateFormat.setAlignment(Alignment.CENTRE);
        DateTime dateCell = new DateTime(1, 1, run.getRunDate(), dateFormat);
        sheet.addCell(dateCell);
        List<AverageSampleProfile> profileList = run.getAverageProfileList();
        Collections.sort(profileList);
        Number number = new Number(4, 1, profileList.get(0).getOCF(), ocfFormat);
        sheet.addCell(number);
        if (profileList.get(0).getRun().getRunOCF() == 0) {
            label = new Label(7, 1, "Not Applied");
            sheet.addCell(label);
        } else {
            number = new Number(7, 1, profileList.get(0).getRun().getRunOCF(), ocfFormat);
            sheet.addCell(number);
        }

//Calculate averages for the amplicon Tm and Ct taken from the replicate profiles
        //Export of Ct values has been decapricated
        for (AverageSampleProfile avProfile : profileList) {
            double avTm = 0;
//                    double avCt = 0;
//                    double ctSD = 0;//Ct standard deviation
//                    ArrayList<Double> ctArray = Lists.newArrayList();
            int tmCnt = 0;
            for (Profile sampleProfile : avProfile.getReplicateProfileList()) {
                if (sampleProfile.getAmpTm() != 0) {
                    avTm += sampleProfile.getAmpTm();
                    tmCnt++;
                }
//                        if (sampleProfile.getCt() != 0) {
//                            avCt += sampleProfile.getCt();
//                            ctArray.add(sampleProfile.getCt());//For SD
//                            ctCnt++;
//                        }
            }
            if (avTm != 0) {
                avTm = avTm / tmCnt;
            }
//                    if (avCt != 0) {
//                        avCt = avCt / ctCnt;
//                        ctSD = MathFunctions.calcStDev(ctArray);
//                    }

            label = new Label(1, row, avProfile.getAmpliconName());
            sheet.addCell(label);
            label = new Label(2, row, avProfile.getSampleName());
            sheet.addCell(label);
            if (avProfile.isExcluded()) {
                //All replicate profiles have been excluded
                label = new Label(3, row, "nd", center);
                sheet.addCell(label);
            } else {
                if (avProfile.getNo() < 0) {
                    label = new Label(3, row, "<0");
                } else {
                    number = new Number(3, row, avProfile.getNo(), commaSep);
                    sheet.addCell(number);
                }
            }
            if (avProfile.getEmax() != 0) {
                number = new Number(4, row, avProfile.getEmax(), percentFormat);
                sheet.addCell(number);
            }
            if (avProfile.getAvFoCV() != 0) {
                number = new Number(5, row, avProfile.getAvFoCV(), percentFormat);
                sheet.addCell(number);
            }
            if (avProfile.getMidC() != 0) {
                number = new Number(6, row, avProfile.getMidC(), floatFormat);
                sheet.addCell(number);
            }
            if (avProfile.getEmax() != 0) {
                double fmax = (avProfile.getEmax() / avProfile.getDeltaE()) * -1;
                number = new Number(7, row, fmax, floatFormat);
                sheet.addCell(number);
            }
            if (avTm != 0) {
                number = new Number(8, row, avTm, floatFormat);
                sheet.addCell(number);
            }
            number = new Number(9, row, avProfile.getAmpliconSize(), integerFormat);
            sheet.addCell(number);
            label = new Label(10, row, avProfile.getLongDescription());
            sheet.addCell(label);
            row++;
        }
    }
}
