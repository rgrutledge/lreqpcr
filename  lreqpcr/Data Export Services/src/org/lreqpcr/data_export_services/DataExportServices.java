package org.lreqpcr.data_export_services;

import org.lreqpcr.core.data_objects.Run;
import java.util.HashMap;
import java.util.List;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;

/**
 * Service interface for exporting profiles. This service needs to be better implemented.
 * It is therefore anticipated that changes will need to be made. Note also that
 * amplicon data is exported by a dedicated module.
 * 
 * @author Bob Rutledge
 */
public interface DataExportServices {

    /**
     * Average sample profile export organized by run.
     * @param runList list containing the runs to be exported
     */
    public void exportAverageSampleProfiles(List<Run> runList);
    
    /**
     * Replicate sample profile export organized by run.
     * @param runList list containing the runs to be exported
     */
    public void exportReplicateSampleProfiles(List<Run> runList);

   /**
    * Average calibration profile export.
    * @param profileList list containing the average calibration profile to be exported
    */
    public void exportAverageCalibrationProfiles(List<AverageCalibrationProfile> profileList);

    /**
     * Data export of a lists of Profiles based on sorting by Run, Amplicon or
     * Sample. The resulting excel workbook will
     * contain a worksheet for each parent, with the parent name as the worksheet
     * name. 
     * 
     * @param groupList Map with the key being the name of the element (run, amplicon or sample used to
     * sort the average sample profiles (e.g. the name of the
     * Run, Amplicon or Sample)
     */
    public void exportAverageProfiles(HashMap<String, List<AverageSampleProfile>> groupList);

}
