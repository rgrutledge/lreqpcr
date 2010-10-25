package org.lreqpcr.data_export_services;

import java.util.ArrayList;
import org.lreqpcr.core.data_objects.Run;
import java.util.HashMap;
import java.util.List;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;

/**
 * Interface for exporting data
 * 
 * @author Bob Rutledge
 */
public interface DataExportServices {

    public  void exportAverageSampleProfiles(List<Run> runList);
    
    public void exportReplicateSampleProfiles(List<Run> runList);

    public void exportAverageCalibrationProfiles(List<AverageCalibrationProfile> profileList);

    /**
     * Data export of a list of Profiles derived by a parent,
     * such as a Run, Amplicon or Sample. The resulting excel workbook will
     * contain a worksheet for each parent, with the parent name as the worksheet
     * name. 
     * 
     * @param groupList Map with worksheet name as the key (e.g. the name of the
     * Run, Amplicon or Sample)
     */
    public void exportAverageProfiles(HashMap<String, ArrayList<AverageSampleProfile>> groupList);

}
