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
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import jxl.write.WriteException;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.data_export_services.DataExportServices;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service=DataExportServices.class)
public class DataExportProvider implements DataExportServices {

    public void exportAverageSampleProfilesFromRuns(List<Run> runList) {
//Defer to HashMap exportation of average profiles in which only the run name is used from the Run
        HashMap<String, List<AverageSampleProfile>> map = Maps.newHashMap();
        for(Run run : runList){
           //Must cast the Run AverageProfileList to AverageSampleProfiles
            List<AverageSampleProfile> avSampleList = Lists.newArrayList();
            for (AverageProfile avProfile : run.getAverageProfileList()){
                AverageSampleProfile avSampleProfile = (AverageSampleProfile) avProfile;
                avSampleList.add(avSampleProfile);
            }
            map.put(run.getName(), avSampleList);
        }
        exportAverageSampleProfiles(map);
    }

    public void exportReplicateSampleProfilesFromRuns(List<Run> runList) {
//Defer to HashMap exportation of replicate sample profiles in which only the run name is used from the Run
        //Run name, 
        HashMap<String, List<SampleProfile>> map = new HashMap<String, List<SampleProfile>>();
        for (Run run : runList){
            List<SampleProfile> samplePrfList = Lists.newArrayList();
            for (AverageProfile avProfile : run.getAverageProfileList()){
                for(Profile profile : avProfile.getReplicateProfileList()){
                    SampleProfile sampleProfile = (SampleProfile) profile;
                    samplePrfList.add(sampleProfile);
                }
            }
            map.put(run.getName(),samplePrfList);
        }
        exportReplicateSampleProfiles(map);
    }

    public void exportAverageCalibrationProfiles(HashMap<String, List<AverageCalibrationProfile>> groupList) {
        
    }

    public void exportAverageSampleProfiles(HashMap<String, List<AverageSampleProfile>> groupList) {
        try {
            ExcelAverageSampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void exportAverageCalibrationProfiles(List<AverageCalibrationProfile> profileList) {
        try {
            ExcelCalibrationProfileExport.exportCalibrationProfiles(profileList);
        } catch (WriteException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void exportReplicateSampleProfiles(HashMap<String, List<SampleProfile>> groupList) {
        try {
            ExcelSampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void exportReplicateCalibrationProfiles(List<AverageCalibrationProfile> profileList) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void exportReplicateCalibrationProfiles(HashMap<String, List<AverageCalibrationProfile>> profileList) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
