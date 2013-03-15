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

/**
 *
 * @author Bob Rutledge
 */
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
        HashMap<String, List<SampleProfile>> map = new HashMap<String, List<SampleProfile>>();
        for (Run run : runList){
            List<SampleProfile> sampleList = Lists.newArrayList();
            for (AverageProfile avProfile : run.getAverageProfileList()){
                for(Profile profile : avProfile){
                    SampleProfile sampleProfile = (SampleProfile) profile;
                    sampleList.add(sampleProfile);
                }
                AverageSampleProfile avSampleProfile = (AverageSampleProfile) avProfile;
                avSampleList.add(avSampleProfile);
            }
            map.put(run.getName(), avSampleList);
        }
        exportReplicateSampleProfiles(map);
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

    public void exportAverageSampleProfiles(HashMap<String, List<AverageSampleProfile>> groupList) {
        try {
            ExcelAverageSampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void exportReplicateSampleProfiles(HashMap<String, List<AverageSampleProfile>> groupList) {
        try {
            ExcelSampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
