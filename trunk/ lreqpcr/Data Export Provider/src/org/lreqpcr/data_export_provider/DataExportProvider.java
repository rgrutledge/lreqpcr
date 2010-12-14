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

    public void exportAverageSampleProfiles(List<Run> runList) {
        HashMap<String, List<AverageSampleProfile>> map = Maps.newHashMap();
        for(Run run : runList){
            map.put(run.getName(), run.getAverageProfileList());
        }
        exportAverageProfiles(map);
    }

    public void exportReplicateSampleProfiles(List<Run> runList) {
        //Not implemented
//        try {
//            ExcelReplicateSampleProfileDataExport.exportAllSampleProfiles(runList);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (WriteException ex) {
//            Exceptions.printStackTrace(ex);
//        }
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

    public void exportAverageProfiles(HashMap<String, List<AverageSampleProfile>> groupList) {
        try {
            ExcelAveragSampleProfileDataExport.exportProfiles(groupList);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
