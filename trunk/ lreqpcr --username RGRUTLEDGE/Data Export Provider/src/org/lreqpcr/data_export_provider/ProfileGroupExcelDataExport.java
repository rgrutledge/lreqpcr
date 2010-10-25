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
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.Profile;
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

/**
 *
 * @author Bob Rutledge
 */
public class ProfileGroupExcelDataExport {

    @SuppressWarnings("unchecked")
    public static void exportProfileGroups(HashMap<String, List<Profile>> groupList) throws WriteException, IOException {
        //Set the workbook file from the user
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile == null) {
            return;
        }
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(selectedFile);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            String msg = "The file '" + selectedFile.getName() +
                    "' could not be opened, possibly because it is already open.";
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
        center.setAlignment(Alignment.RIGHT);
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

        int i = 0;
        Set<String> groupNames = groupList.keySet();
        Iterator<String> it = groupNames.iterator();
        while (it.hasNext()) {
            String parentName = it.next();
            WritableSheet sheet = workbook.createSheet(parentName, i);

            Label label = new Label(0, 3, "Run Date", centerBoldUnderline);
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
            label = new Label(10, 3, "OCF", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(11, 3, "Notes", centerBoldUnderline);
            sheet.addCell(label);

            List<Profile> profileList = groupList.get(parentName);
            //Collections.sort cannot use a List
            ArrayList<Profile> profileArray = new ArrayList<Profile>(profileList);
            Collections.sort(profileArray);
            DateFormat customDateFormat = new DateFormat("ddMMMyy");
            WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
            dateFormat.setAlignment(Alignment.CENTRE);
            int row = 4;
            for (Profile profile : profileList) {
                AverageProfile avProfile = (AverageProfile) profile;
                double avTm = 0;
//                double avCt = 0;
//                double ctSD = 0;//Ct standard deviation
                ArrayList<Double> ctArray = Lists.newArrayList();
                int tmCnt = 0;
//                int ctCnt = 0;
                for (Profile sampleProfile : avProfile.getReplicateProfileList()) {
                    if (sampleProfile.getAmpTm() != 0) {
                        avTm += sampleProfile.getAmpTm();
                        tmCnt++;
                    }
//                    if (sampleProfile.getCt() != 0) {
//                        avCt += sampleProfile.getCt();
//                        ctArray.add(sampleProfile.getCt());//For SD
//                        ctCnt++;
//                    }
                }
                if (avTm != 0) {
                    avTm = avTm / tmCnt;
                }
//                if (avCt != 0) {
//                    avCt = avCt / ctCnt;
//                    ctSD = MathFunctions.calcStDev(ctArray);
//                }
                DateTime dateCell = new DateTime(0, row, profile.getRunDate(), dateFormat);
                sheet.addCell(dateCell);
                label = new Label(1, row, profile.getAmpliconName());
                sheet.addCell(label);
                label = new Label(2, row, profile.getSampleName());
                sheet.addCell(label);
                Number number = new Number(3, row, profile.getNo(), commaSep);
                sheet.addCell(number);
                number = new Number(4, row, profile.getEmax(), percentFormat);
                sheet.addCell(number);
                number = new Number(5, row, profile.getAvFoCV(), percentFormat);
                sheet.addCell(number);
                number = new Number(6, row, profile.getMidC(), floatFormat);
                sheet.addCell(number);
                double fmax = (profile.getEmax() / profile.getDeltaE()) * -1;
                number = new Number(7, row, fmax, floatFormat);
                sheet.addCell(number);
                if (avTm != 0) {
                    number = new Number(8, row, avTm, floatFormat);
                    sheet.addCell(number);
                }
                number = new Number(9, row, profile.getAmpliconSize(), integerFormat);
                sheet.addCell(number);
//                if (avCt != 0) {
//                    number = new Number(13, row, avCt, floatFormat);
//                    sheet.addCell(number);
//                    number = new Number(14, row, ctSD, floatFormat);
//                    sheet.addCell(number);
//                }
//                number = new Number(15, row, avProfile.getReplicateProfileList().get(0).getFt(), floatFormat);
//                sheet.addCell(number);
                double ocf = 0;
                //If run OCF != 0 then this value was used to calculate No
                if(profile.getRunOCF() != 0){
                    ocf = profile.getRunOCF();
                }else {
                    ocf = profile.getOCF();
                }
                number = new Number(10, row, ocf, floatFormat);
                sheet.addCell(number);
                label = new Label(11, row, profile.getLongDescription());
                sheet.addCell(label);
                row++;
            }
            i++;//Run counter
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
