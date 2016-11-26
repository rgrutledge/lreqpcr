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

package org.lreqpcr.core.utilities;

/**
 * Static methods for formating
 */
public class FormatingUtilities {

    /**
     * Provides a decimal format string pattern for the supplied number.
     * Returns ###,### for >1,000, ### for 100-1000, ##.0 10-100, #.0000 <1
     * @param number the number to be displayed
     * @return string pattern based on decimal format
     */
    public static String decimalFormatPattern(double number) {
        double absNumber = Math.abs(number);
        if (absNumber >= 1000.0) {
            return "###,###";
        }
        if (absNumber >= 100.0 && absNumber <= 1000.0) {
            return "###";
        }
        if (absNumber >= 10.0 && absNumber <= 100.0) {
            return "##.0";
        }
        if (absNumber <= 10.0 && absNumber >= 1.0) {
            return "##.00";
        }
        if (absNumber <=1.0 && absNumber >= 0.1 ){
            return "0.000";
        }if (absNumber <=0.1 && absNumber >= 0.01){
            return "0.0000";
        }if (absNumber <= 0.01 && absNumber >= 0.001){
            return "0.00000";
        }else{//Number must be <1.0
            return "0.0000000";
        }
    }
}
