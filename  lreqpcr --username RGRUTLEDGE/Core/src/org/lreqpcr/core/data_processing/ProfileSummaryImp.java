/*
 * Copyright (C) 2010  Bob Rutledge
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
 * Basic ProfileSummary implementation
 * 
 * @author Bob Rutledge
 */
public class ProfileSummaryImp extends ProfileSummary {

    public ProfileSummaryImp(Profile profile) {
        this.profile = profile;
    }
}
