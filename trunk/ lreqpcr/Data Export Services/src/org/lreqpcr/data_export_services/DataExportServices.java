package org.lreqpcr.data_export_services;

import org.lreqpcr.core.data_objects.Run;
import java.util.HashMap;
import java.util.List;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.SampleProfile;

/**
 * Service interface for exporting profiles. This service needs to be better implemented.
 * It is therefore anticipated that changes will need to be made. Note also that
 * amplicon data is exported by a dedicated module.
 * 
 * @author Bob Rutledge
 */
public interface DataExportServices {

    /**
     * Average sample profile export sorted by run.
     * @param runList list containing the runs to be exported
     */
    public void exportAverageSampleProfilesFromRuns(List<Run> runList);

    /**
     * Replicate sample profile export sorted by run.
     * @param runList list containing the runs to be exported
     */
    public void exportReplicateSampleProfilesFromRuns(List<Run> runList);

    /**
     * Average calibration profile export.
     * @param profileList list containing the average calibration profile to be exported
     */
    public void exportAverageCalibrationProfiles(List<AverageCalibrationProfile> profileList);

    /**
     * Data export of a lists of average sample profiles based on sorting by Run, Amplicon or
     * Sample. The resulting excel workbook will
     * contain a worksheet for each parent, with the parent name as the worksheet
     * name. 
     * 
     * @param groupList Map with the key being the name of the element (run, amplicon or sample used to
     * sort the average sample profiles (e.g. the name of the
     * Run, Amplicon or Sample)
     */
    public void exportAverageSampleProfiles(HashMap<String, List<AverageSampleProfile>> groupList);

    /**
     * Data export of a lists of repliProfiles based on sorting by Run, Amplicon or
     * Sample. The resulting excel workbook will
     * contain a worksheet for each parent, with the parent name as the worksheet
     * name.
     *
     * @param groupList Map with the key being the name of the element (run, amplicon or sample used to
     * sort the sample profiles (e.g. the name of the Run, Amplicon or Sample)
     */
    public void exportReplicateSampleProfiles(HashMap<String, List<AverageSampleProfile>> groupList);
}
