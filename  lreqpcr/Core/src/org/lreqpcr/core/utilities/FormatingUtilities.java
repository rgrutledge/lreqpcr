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

package org.lreqpcr.core.utilities;

/**
 * Static methods for formating
 * @author Bob Rutledge
 */
public class FormatingUtilities {

    /**
     * Provides a decimal format string pattern for the supplied number. 
     * Returns ###,### for >1,000, ### for 100-1000, ##.0 10-100, #.0000 <1
     * @param number the number to be displayed
     * @return string pattern based on decimal format
     */
    public static String decimalFormatPattern(double number) {
        if (number >= 1000.0) {
            return "###,###";
        }
        if (number >= 100.0 && number <= 1000.0) {
            return "###";
        }
        if (number >= 10.0 && number <= 100.0) {
            return "##.0";
        }
        if (number <= 10.0 && number >= 1.0) {
            return "##.00";
        } else{//Number must be <1.0
            return "0.0000";
        }
    }
}