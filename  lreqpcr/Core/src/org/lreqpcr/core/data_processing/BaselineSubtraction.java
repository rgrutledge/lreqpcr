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
package org.lreqpcr.core.data_processing;

import org.lreqpcr.core.data_objects.Profile;

/**
 * Methods for analyzing the Profile baseline fluorescence.
 * Future functions could include detection of basline drift.
 * 
 * @author Bob Rutledge
 */
public class BaselineSubtraction {

    /**
     * This is a very crude method based on cycles 4-9. An Analysis Service
     * Provider are welcome to use their own baseline subtraction methodology.
     *
     * It is very important that the earliest (bottom) cycles of the profile
     * are no earlier than cycle 10. Also note that any loss in reaction 
     * fluorescence during cycles 1-3 is assumed not to continue into cycle 4.
     *
     * @param profile the Profile to be processed
     */
    public static void baselineSubtraction(Profile profile) {
        if(profile.getRawFcReadings().length == 0){
            //All replicates have been excluded
            return;
        }
        // TODO consider upgrading this to a user selectable Fb window
        //Start with 6 cycle Fb window
        double[] rawFc = profile.getRawFcReadings();
        //The initial Fb start and end cycles
        int start = 4;
        int end = 9;
//        int start = 7;
//        int end = 12;
        int fbWindow = (end - start) + 1;
        double fb = 0d;//The fluorescence background
        //Start with a 6 cycle average from cycle 4-8
        for (int i = start; i < end + 1; i++) {
            fb = fb + rawFc[i - 1];//List starts at 0
        }
        fb = fb / fbWindow;
        profile.setFbStart(start);
        profile.setFbWindow(end - start + 1);
        profile.setFb(fb);
        //Subtract this initial Fb from the raw Fc readings
        double[] fc = new double[rawFc.length];//The background subtracted Fc dataset
        for (int i = 0; i < fc.length; i++) {
            fc[i] = rawFc[i] - fb;
        }
        profile.setFcReadings(fc);
    }
}
