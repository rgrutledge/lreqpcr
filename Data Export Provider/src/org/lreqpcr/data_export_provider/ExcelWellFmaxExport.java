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

import com.google.common.collect.Lists;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.utilities.IOUtilities;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

/**
 *
 * @author Bob Rutledge
 */
public class ExcelWellFmaxExport {//For testing only

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
        int col = 0;
        while (it.hasNext()) {
            String runName = it.next();
            WritableSheet sheet = workbook.createSheet(runName, 0);

            Label label = new Label(0, 0, "Well:", boldRight);
            sheet.addCell(label);
            
            //Need a list of all SampleProfiles in each run
            List<AverageSampleProfile> avProfileList = groupList.get(runName);
            ArrayList<SampleProfile> profileList = Lists.newArrayList();
            for (AverageSampleProfile avProfile : avProfileList){
                for (SampleProfile profile : avProfile.getReplicateProfileList()){
                    profileList.add(profile);
                }
            }
            
            //Need a well name iterator
            Comparator<SampleProfile> wellNameComparator = new Comparator<SampleProfile>() {

                public int compare(SampleProfile profile1, SampleProfile profile2) {
                    return profile1.getWellLabel().compareTo(profile2.getWellLabel());
                }
            };
            //Sort by well name
            Collections.sort(profileList, wellNameComparator);

            int row = 0;
            for (SampleProfile profile : profileList) {
                //Export well name and Fmax for each SampleProfile in two adjacent colums
                label = new Label(col, row, profile.getWellLabel());
                    sheet.addCell(label);
                    label = new Label(col +1, row, profile.getWellLabel());
                    sheet.addCell(label);
                row++;
            }
//            col++
                    }
                
              
            pageCounter++;
        }
//        workbook.write();
//        workbook.close();
//        Desktop desktop = null;
//        if (Desktop.isDesktopSupported()) {
//            desktop = Desktop.getDesktop();
//            desktop.open(selectedFile);
//        }
//    }
}//End of exportProfiles
