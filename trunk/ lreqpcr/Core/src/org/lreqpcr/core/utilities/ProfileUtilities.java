/*
 * Copyright (C) 2013  Bob Rutledge
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
package org.lreqpcr.core.utilities;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.lreqpcr.core.data_objects.DatabaseInfo;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.Run;
import org.lreqpcr.core.database_services.DatabaseServices;

/**
 * Static utility methods
 * @author Bob Rutledge
 */
public class ProfileUtilities {

    /**
     * Generates an average Fc dataset from the provided list of 
     * profiles. Excluded profiles are ignored.
     * Returns null if the list is empty or if all the
     * provided Profiles are excluded. 
     * 
     * @param replicates ArrayList of the replicate Profiles
     * @return the average Fc dataset
     */
    public static double[] generateAverageFcDataset(List<? extends Profile> replicates) {
        if (replicates.isEmpty()) {
            return null;
        }

        int numberOfCycles = replicates.get(0).getRawFcReadings().length;
        double[] newAvFcDataset = new double[numberOfCycles];

        //Remove excluded profiles and profiles which do not have a LRE window from the average
        ArrayList<Profile> profileList = new ArrayList<Profile>();
        for (Profile profile : replicates) {
            if (!profile.isExcluded() && profile.hasAnLreWindowBeenFound()) {
                profileList.add(profile);
            }
        }
        if (profileList.isEmpty()) {
            return null;
        }
        if (profileList.size() > 1) {
            int numberOfProfiles = profileList.size();
            //Place the double[] Fc raw readings into a double[profile][raw Fc dataset]
            double[][] fcArray = new double[numberOfProfiles][numberOfCycles];
            for (int i = 0; i < numberOfProfiles; i++) {
                System.arraycopy(profileList.get(i).getRawFcReadings(), 0, fcArray[i], 0, numberOfCycles);
            }
            //Calculate average Fc for each cycle and put it into a new double[]
            for (int i = 0; i < numberOfCycles; i++) {
                double fcSum = 0;
                for (int j = 0; j < numberOfProfiles; j++) {
                    fcSum += fcArray[j][i];
                }
                newAvFcDataset[i] = fcSum / numberOfProfiles;
            }
        } else {//Only one Profile is present so just return its raw Fc readings
            newAvFcDataset = profileList.get(0).getRawFcReadings();
        }
        return newAvFcDataset;
    }//End of generate average Fc dataset
    
    public static void calcAvFmaxForAllRuns(DatabaseServices db) {
        List l = db.getAllObjects(DatabaseInfo.class);
        DatabaseInfo dbInfo = (DatabaseInfo) l.get(0);
        List<Run> runList = db.getAllObjects(Run.class);
        ArrayList<Double> fmaxList = Lists.newArrayList();//Used to determine SD
        double fmaxSum = 0;
        for (Run run : runList) {
            //Sum Fmax averages from all runs
            fmaxSum += run.getAverageFmax();
            fmaxList.add(run.getAverageFmax());
        }
        if (fmaxList.size() >= 1 && fmaxSum > 0) {
            double avRunFmax = fmaxSum / fmaxList.size();
            dbInfo.setAvRunFmax(avRunFmax);
            //Calculate the avFmax CV is there is more than one value
            if (fmaxList.size() > 1) {
                double avRunFmaxCV = MathFunctions.calcStDev(fmaxList) / avRunFmax;
                dbInfo.setAvRunFmaxCV(avRunFmaxCV);
            }
            db.saveObject(dbInfo);
        }
    }//End of calculate average run Fmax
}
