/**
 * Copyright (C) 2013 Bob Rutledge
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/> and open
 * the template in the editor.
 */
package org.lreqpcr.lightcycler_import;

import com.google.common.collect.Lists;
import java.net.URL;
import org.lreqpcr.core.data_objects.*;
import org.lreqpcr.data_import_services.RunImportUtilities;
import org.lreqpcr.core.utilities.IOUtilities;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.lreqpcr.data_import_services.DataImportType;
import org.lreqpcr.data_import_services.RunImportData;
import org.lreqpcr.data_import_services.RunImportService;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Rudimentary RDML import service that only imports profiles along with the
 * corresponding Run, which ignores all other RDML specified data
 * (www.rdml.org). A dedicated RDML import service would make this provider
 * redundant.
 *
 * @author Bob Rutledge
 */
public class LC480ImportProvider extends RunImportService {
    
    public String getRunImportServiceName() {
        return "Lightcycler";
    }
    
    //*********************************NOT IMPLEMENT YET*************************************************
    public URL getHelpFile() {
        return null;
//        try {
//            try {
//                return new URI("file:HelpFiles/mxpVer4.html").toURL();
//            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
//                return null;
//            }
//        } catch (URISyntaxException ex) {
//            Exceptions.printStackTrace(ex);
//            return null;
//        }
    }
    
    @Override
    @SuppressWarnings(value = "unchecked")
    public RunImportData importRunData() {
        //Instanciate the DOM document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setIgnoringComments(true);
        docFactory.setIgnoringElementContentWhitespace(true);
        docFactory.setValidating(false);
        DocumentBuilder docBuilder;
        Document xmlDoc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            xmlDoc = docBuilder.parse(new InputSource("./LC480.xml"));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        //Setup the objects need for the import
        //Setup the Run object
//            Run run = new RunImpl();
        //Set the run name and date
        Run run = new RunImpl();
        //Retrieve the xml file
        //For development purposes, just use an example xml file       
//        File lcRdmlFile = IOUtilities.openXmlFile("Lightcycler XML Data Import");
//        if (lcRdmlFile == null) {
//            return null;
//        }
        //Setup the profile arraylists
        ArrayList<SampleProfile> sampleProfileList = Lists.newArrayList();
        ArrayList<CalibrationProfile> calbnProfileList = Lists.newArrayList();
        //Determine the strandedness of the majority of the Targets...too bad that this is not provided by the instrument
        TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
        //Get the all of the profile nodes
        NodeList profileNodeList = xmlDoc.getElementsByTagName("series");
        //Cycle through all of the profile nodes
        for (int i = 0; i < profileNodeList.getLength(); i++) {
            //TODO determine whether this profile is sample or calibration
            Profile profile = createProfileType(run);
            Element profileElement = (Element) profileNodeList.item(i);
            String wellLabel = profileElement.getAttribute("title");
            //TODO parse the well label, sample and amplicon name from the wellLabel
            NodeList cycleList = profileElement.getElementsByTagName("point");
            //Collect and set the Fc reading 
            profile.setFcReadings(retrieveFcReadings(cycleList));
            if (CalibrationProfile.class.isAssignableFrom(profile.getClass())) {
                CalibrationProfile calProfile = (CalibrationProfile) profile;
                calbnProfileList.add(calProfile);
            } else {
                SampleProfile sampleProfile = (SampleProfile) profile;
                sampleProfileList.add(sampleProfile);
            }
        }
        return null;
    }

    //Deterimine if this is a sample or calibration profile
    private Profile createProfileType(Run run) {
        //will need to first import the excel data 
        return new SampleProfile();
    }
    
    private double[] retrieveFcReadings(NodeList cycleList) {
        //Check the the first cycle is cycle #1, else throw an error dialog
        Element firstCycleElement = (Element) cycleList.item(0);
        String firstCycleNumber = firstCycleElement.getAttribute("X");
        if (!firstCycleNumber.contains("1")) {
            //TODO present an error dialog and terminate import
            //...pretty harsh but this is essential for baseline subtraction
        }
        NumberFormat numFormat = NumberFormat.getInstance();//Need to convert comma decimal seperators to periods
        ArrayList<Double> fcDataSet = Lists.newArrayList();
        //Cycle through the cycles and collect the Fc readings
        for (int j = 0; j < cycleList.getLength(); j++) {
            //This assumes that the first cycle is cycle 1
            Element cycleElement = (Element) cycleList.item(j);
            try {
                //NumberFormat needed to prevent locale differences in numbers (e.g. comma vs period)
                Number value = numFormat.parse(cycleElement.getAttribute("Y"));
                fcDataSet.add(value.doubleValue());
            } catch (Exception e) {
            }
        }
        double[] fcArray = new double[fcDataSet.size()];
        for (int k = 0; k < fcDataSet.size(); k++) {
            fcArray[k] = fcDataSet.get(k);
        }
        return fcArray;
    }
    
    private class SampleInfo{
        
//        Enum profileType[CAL, SAMPLE];//should be an enum
        String sampleName;
    }
}
