package org.lreqpcr.data_export_provider;

import com.google.common.collect.Maps;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.data_export_services.DataExportServices;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import jxl.write.WriteException;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
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
            map.put(run.getName(), run.getAverageProfileList());
        }
        exportAverageSampleProfiles(map);
    }

    public void exportReplicateSampleProfilesFromRuns(List<Run> runList) {
//Defer to HashMap exportation of replicate sample profiles in which only the run name is used from the Run
        HashMap<String, List<AverageSampleProfile>> map = new HashMap<String, List<AverageSampleProfile>>();

        for (Run run : runList){
            map.put(run.getName(), run.getAverageProfileList());
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
            ExcelAveragSampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void exportReplicateSampleProfiles(HashMap<String, List<AverageSampleProfile>> groupList) {
        try {
            SampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
