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
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import jxl.write.Number;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServiceFactory;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.openide.util.Lookup;

/**
 * Not complete
 * @author Bob Rutledge
 */
public class ExcelReplicateSampleProfileDataExport {

    /**
     * Note that any average profiles will not be included into the export data
     * 
     * @param runList list of all runs to be exported
     * @throws IOException
     * @throws WriteException
     */
    @SuppressWarnings("unchecked")
    public static void exportAllSampleProfiles(List<Run> runList) throws IOException, WriteException {
        //Select the workbook file from the user
        File selectedFile = IOUtilities.newExcelFile();
        if (selectedFile == null) {
            return;
        }
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(selectedFile);
        } catch (Exception e) {
            String msg = "The file '" + selectedFile + "' could not be opened";
            JOptionPane.showMessageDialog(null, msg, "Unable to open file" + selectedFile.getName(), JOptionPane.ERROR_MESSAGE);
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
        NumberFormat nfOCF = new NumberFormat("###,###.000");
        WritableCellFormat ocfFormat = new WritableCellFormat(nfOCF);
        WritableCellFormat percentFormat = new WritableCellFormat(NumberFormats.PERCENT_FLOAT);
        WritableCellFormat exponentialFormat = new WritableCellFormat(NumberFormats.EXPONENTIAL);
        NumberFormat nf = new NumberFormat("###,###");
        WritableCellFormat commaSep = new WritableCellFormat(nf);

        int i = 0;
        for (Run run : runList) {
            //Construct the sheet
            WritableSheet sheet = workbook.createSheet("LRE Profile Export", i);

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
            label = new Label(0, 0, "Average OCF:", boldRight);
            sheet.addCell(label);
            label = new Label(0, 3, "Run Date", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(1, 3, "Sample", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(2, 3, "No", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(3, 3, "Emax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(4, 3, "C1/2", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(5, 3, "Amplicon", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(6, 3, "Amp Size", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(7, 3, "deltaE", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(8, 3, "Fo", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(9, 3, "Fo CV", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(10, 3, "Fb Drift r2", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(11, 3, "Fmax", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(12, 3, "Amp Tm", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(13, 3, "Well", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(14, 3, "Ct", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(15, 3, "Ft", centerBoldUnderline);
            sheet.addCell(label);
            label = new Label(16, 3, "Run OCF", centerBoldUnderline);
            sheet.addCell(label);

        //Get all Runs from the profile database
        DatabaseServices experimentDB = Lookup.getDefault().
                lookup(DatabaseServiceFactory.class).createDatabaseService(DatabaseType.EXPERIMENT);
        // TODO present an error dialog
        if (!experimentDB.isDatabaseOpen()) {
            return;
        }
//        List<Profile> runList =
//                (List<Profile>) experimentDB.getAllObjects(SampleProfile.class);
//            //Prepare a list of all replicate sample profiles
//            ArrayList<Profile> profileArray = new ArrayList<Profile>();
//            for (AverageProfile avPrf : run.getAverageProfileList()){
//                for(Profile samplePrf : avPrf.getReplicateProfileList()){
//                    profileArray.add(samplePrf);
//                }
//            }
//
//            Collections.sort(profileArray);
//            //Assumes that a single OCF is applied across all Runs
//            Profile firstProfile = profileArray.get(0);
//            Number number = new Number(1, 0, firstProfile.getAverageOCF(), commaSep);
//            sheet.addCell(number);
//            int row = 4;
//            for (Profile profile : profileArray) {
//                if (AverageSampleProfile.class.isAssignableFrom(profile.getClass())) {
//                    continue;
//                }
//                Number number = new Number(4, 1, profileList.get(0).getAverageOCF(), ocfFormat);
//            sheet.addCell(number);
//            if (profileList.get(0).getRunOCF() == 0) {
//                label = new Label(7, 1, "Not Applied");
//                sheet.addCell(label);
//            } else {
//                number = new Number(7, 1, profileList.get(0).getRunOCF(), ocfFormat);
//                sheet.addCell(number);
//            }
//
//                DateFormat customDateFormat = new DateFormat("ddMMMyy");
//                WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
//                dateFormat.setAlignment(Alignment.CENTRE);
//                DateTime dateCell = new DateTime(0, row, profile.getRunDate(), dateFormat);
//                sheet.addCell(dateCell);
//                label = new Label(1, row, profile.getSampleName());
//                sheet.addCell(label);
//                number = new Number(2, row, profile.getNo(), commaSep);
//                sheet.addCell(number);
//                number = new Number(3, row, profile.getEmax(), percentFormat);
//                sheet.addCell(number);
//                number = new Number(4, row, profile.getMidC(), floatFormat);
//                sheet.addCell(number);
//                label = new Label(5, row, profile.getAmpliconName());
//                sheet.addCell(label);
//                number = new Number(6, row, profile.getAmpliconSize(), integerFormat);
//                sheet.addCell(number);
//                number = new Number(7, row, profile.getDeltaE(), exponentialFormat);
//                sheet.addCell(number);
//                number = new Number(8, row, profile.getAvFo(), exponentialFormat);
//                sheet.addCell(number);
//                number = new Number(9, row, profile.getAvFoCV(), percentFormat);
//                sheet.addCell(number);
//                number = new Number(10, row, profile.getFbR2(), floatFormat);
//                sheet.addCell(number);
//                double fmax = (profile.getEmax() / profile.getDeltaE()) * -1;
//                number = new Number(11, row, fmax, floatFormat);
//                sheet.addCell(number);
//                number = new Number(12, row, profile.getAmpTm(), floatFormat);
//                sheet.addCell(number);
//                label = new Label(13, row, profile.getWellLabel());
//                sheet.addCell(label);
//                number = new Number(14, row, profile.getCt(), floatFormat);
//                sheet.addCell(number);
//                number = new Number(15, row, profile.getFt(), floatFormat);
//                sheet.addCell(number);
//                if (profile.getRunOCF() == 0) {
//                    label = new Label(16, row, "Not Applied");
//                    sheet.addCell(label);
//                } else {
//                    number = new Number(16, row, profile.getRunOCF(), ocfFormat);
//                    sheet.addCell(number);
//                }
//                row++;
//            }
//            i++;
        }

        workbook.write();
        workbook.close();

        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        desktop.open(selectedFile);
    }
}
