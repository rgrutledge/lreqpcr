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

package org.lreqpcr.data_export_provider;

import java.util.List;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.SampleProfile;

/**
 *
 * @author Bob Rutledge
 */
public class LightCyclerUtilities {

    public LightCyclerUtilities() {
    }
    
    public static void lightCyclerWellLabelParsing(List<AverageSampleProfile> avProfileList){
        
        for (AverageSampleProfile avProfile : avProfileList) {
            for(SampleProfile profile : avProfile.getReplicateProfileList()){
                String wellName = profile.getSampleName();//Well label: sample name
                //parse out the well label and retrieve the well number, setting both in the profile
                profile.setWellLabel(parseWellLabel(wellName));
                //Set the well number
//                int wellNumber = 
            }
        }
    }
    
    private static String parseWellLabel(String wellName){
        //Well label: sample name
//        String wellLabel = wellName;//Parse here
        int colonIndex = wellName.indexOf(':');
        String wellLabel = wellName.substring(0, colonIndex);
        return wellLabel;
    }

}
