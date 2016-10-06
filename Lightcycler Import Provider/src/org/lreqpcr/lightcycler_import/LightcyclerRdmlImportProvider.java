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
public class LightcyclerRdmlImportProvider extends RunImportService {

    private NodeList sampleNodeList;

    public String getRunImportServiceName() {
        return "Lightcycler";
    }

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
    public RunImportData constructRunImportData() {
        //Retrieve the xml file
        //For development purposes, just use the example RDML file       
//        File lcRdmlFile = IOUtilities.openXmlFile("Lightcycler XML Data Import");
//        if (lcRdmlFile == null) {
//            return null;
//        }
        //Instanciate the DOM document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setIgnoringComments(true);
        docFactory.setIgnoringElementContentWhitespace(true);
        docFactory.setValidating(false);
        DocumentBuilder docBuilder;
        Document xmlDoc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            xmlDoc = docBuilder.parse(new InputSource("./rdml_example.xml"));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        //Get the all of the Run nodes
        NodeList runNodeList = xmlDoc.getElementsByTagName("run");
        sampleNodeList = xmlDoc.getElementsByTagName("sample");
//        Boolean typePresent = sampleElement.hasAttribute("type");

//        Date dateMade = new Date(dateString);
//        int numberOfRuns = listOfRuns.getLength();
        ArrayList<Run> runList = new ArrayList<Run>();
        for (int i = 0; i < runNodeList.getLength(); i++) {
            Run run = new RunImpl();
            run.setRunDate(new Date());//Set the default to the current date
            Element runElement = (Element) runNodeList.item(i);
            //If a run date is provided, reset the Run's runDate
            if (runElement.hasAttribute("runDate")) {
                //TODO learn how to import the dateTime element for RDML run date
                //Test how to extract a date using dateMade element
                //Wow, this is complex...need to cast to Element, then get the child Node
                NodeList dateMadeList = xmlDoc.getElementsByTagName("dateMade");
                Element dateMadeElement = (Element) dateMadeList.item(0);
                NodeList dateElementList = dateMadeElement.getChildNodes();
                String dateString = dateElementList.item(0).getNodeValue();
                //However, constructing a Date using a string has been disallowed
            }
            //Set the run name
            run.setName(runElement.getAttribute("id"));
            //Get ready to import the run data
            ArrayList<SampleProfile> sampleProfileList = new ArrayList<SampleProfile>();
            ArrayList<CalibrationProfile> calbnProfileList = new ArrayList<CalibrationProfile>();
            //Determine the strandedness of the Targets
            TargetStrandedness targetStrandedness = RunImportUtilities.isTheTargetSingleStranded();
            //Get all of the reactions elements
            NodeList listOfReactions = runElement.getElementsByTagName("react");
            for (int j = 0; j < listOfReactions.getLength(); j++) {
                Element reactionElement = (Element) listOfReactions.item(j);
                //TODO determine if the reactions uses a calibrator or a sample
                String reactionName = reactionElement.getAttribute("id");
                //Get sample name of which there is only one
                Element sampleElement = (Element) reactionElement.getElementsByTagName("sample").item(0);
                String sampleName = sampleElement.getAttribute("id");
                
                //Get target name
                Element targetElement = (Element) reactionElement.getElementsByTagName("tar").item(0);
                String targetName = targetElement.getAttribute("id");
                //Construct the Fc dataset
                NodeList listOfFcReadings = reactionElement.getElementsByTagName("adp");
                for (int k = 0; k < listOfFcReadings.getLength(); k++) {
                }
                int a = 0;
            }

        }
        Node firstRunNode = runNodeList.item(1);
        String runName = firstRunNode.getNodeValue();

        int j = 0;

        return null;
    }
//       private void getSampeType(String sampleName){
//           //Need to scroll through the enter
//           Element sampleNodeList.
//       }
}
