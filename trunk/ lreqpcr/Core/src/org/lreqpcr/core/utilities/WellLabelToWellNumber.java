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

import org.lreqpcr.core.data_objects.Profile;

/**
 * Converts the well label to well number. This is required for data
 * export that only provides a well label and
 * is based on the AB 7900 labeling scheme
 *
 * @author Bob Rutledge
 */
public class WellLabelToWellNumber {

    /**
     * Converts the well label to well number for the provided profile
     * @param profile the profile
     */
    public static void labelToNumber96Well_AB7900(Profile profile){
        String wellLabel = profile.getWellLabel();
        if(wellLabel.compareTo("A1") == 0){
            profile.setWellNumber(1);
            return;
        }
        if(wellLabel.compareTo("A2") == 0){
            profile.setWellNumber(2);
            return;
        }
        if(wellLabel.compareTo("A3") == 0){
            profile.setWellNumber(3);
            return;
        }
        if(wellLabel.compareTo("A4") == 0){
            profile.setWellNumber(4);
            return;
        }
        if(wellLabel.compareTo("A5") == 0){
            profile.setWellNumber(5);
            return;
        }
        if(wellLabel.compareTo("A6") == 0){
            profile.setWellNumber(6);
            return;
        }
        if(wellLabel.compareTo("A7") == 0){
            profile.setWellNumber(7);
            return;
        }
        if(wellLabel.compareTo("A8") == 0){
            profile.setWellNumber(8);
            return;
        }
        if(wellLabel.compareTo("A9") == 0){
            profile.setWellNumber(9);
            return;
        }
        if(wellLabel.compareTo("A10") == 0){
            profile.setWellNumber(10);
            return;
        }
        if(wellLabel.compareTo("A11") == 0){
            profile.setWellNumber(11);
            return;
        }
        if(wellLabel.compareTo("A12") == 0){
            profile.setWellNumber(12);
            return;
        }
        if(wellLabel.compareTo("B1") == 0){
            profile.setWellNumber(13);
            return;
        }
        if(wellLabel.compareTo("B2") == 0){
            profile.setWellNumber(14);
            return;
        }
        if(wellLabel.compareTo("B3") == 0){
            profile.setWellNumber(15);
            return;
        }
        if(wellLabel.compareTo("B4") == 0){
            profile.setWellNumber(16);
            return;
        }
        if(wellLabel.compareTo("B5") == 0){
            profile.setWellNumber(17);
            return;
        }
        if(wellLabel.compareTo("B6") == 0){
            profile.setWellNumber(18);
            return;
        }
        if(wellLabel.compareTo("B7") == 0){
            profile.setWellNumber(19);
            return;
        }
        if(wellLabel.compareTo("B8") == 0){
            profile.setWellNumber(20);
            return;
        }
        if(wellLabel.compareTo("B9") == 0){
            profile.setWellNumber(21);
            return;
        }
        if(wellLabel.compareTo("B10") == 0){
            profile.setWellNumber(22);
            return;
        }
        if(wellLabel.compareTo("B11") == 0){
            profile.setWellNumber(23);
            return;
        }
        if(wellLabel.compareTo("B12") == 0){
            profile.setWellNumber(24);
            return;
        }
        if(wellLabel.compareTo("C1") == 0){
            profile.setWellNumber(25);
            return;
        }
        if(wellLabel.compareTo("C2") == 0){
            profile.setWellNumber(26);
            return;
        }
        if(wellLabel.compareTo("C3") == 0){
            profile.setWellNumber(27);
            return;
        }
        if(wellLabel.compareTo("C4") == 0){
            profile.setWellNumber(28);
            return;
        }
        if(wellLabel.compareTo("C5") == 0){
            profile.setWellNumber(29);
            return;
        }
        if(wellLabel.compareTo("C6") == 0){
            profile.setWellNumber(30);
            return;
        }
        if(wellLabel.compareTo("C7") == 0){
            profile.setWellNumber(31);
            return;
        }
        if(wellLabel.compareTo("C8") == 0){
            profile.setWellNumber(32);
            return;
        }
        if(wellLabel.compareTo("C9") == 0){
            profile.setWellNumber(33);
            return;
        }
        if(wellLabel.compareTo("C10") == 0){
            profile.setWellNumber(34);
            return;
        }
        if(wellLabel.compareTo("C11") == 0){
            profile.setWellNumber(35);
            return;
        }
        if(wellLabel.compareTo("C12") == 0){
            profile.setWellNumber(36);
            return;
        }
        if(wellLabel.compareTo("D1") == 0){
            profile.setWellNumber(37);
            return;
        }
        if(wellLabel.compareTo("D2") == 0){
            profile.setWellNumber(38);
            return;
        }
        if(wellLabel.compareTo("D3") == 0){
            profile.setWellNumber(39);
            return;
        }
        if(wellLabel.compareTo("D4") == 0){
            profile.setWellNumber(40);
            return;
        }
        if(wellLabel.compareTo("D5") == 0){
            profile.setWellNumber(41);
            return;
        }
        if(wellLabel.compareTo("D6") == 0){
            profile.setWellNumber(42);
            return;
        }
        if(wellLabel.compareTo("D7") == 0){
            profile.setWellNumber(43);
            return;
        }
        if(wellLabel.compareTo("D8") == 0){
            profile.setWellNumber(44);
            return;
        }
        if(wellLabel.compareTo("D9") == 0){
            profile.setWellNumber(45);
            return;
        }
        if(wellLabel.compareTo("D10") == 0){
            profile.setWellNumber(46);
            return;
        }
        if(wellLabel.compareTo("D11") == 0){
            profile.setWellNumber(47);
            return;
        }
        if(wellLabel.compareTo("D12") == 0){
            profile.setWellNumber(48);
            return;
        }
        if(wellLabel.compareTo("E1") == 0){
            profile.setWellNumber(49);
            return;
        }
        if(wellLabel.compareTo("E2") == 0){
            profile.setWellNumber(50);
            return;
        }
        if(wellLabel.compareTo("E3") == 0){
            profile.setWellNumber(51);
            return;
        }
        if(wellLabel.compareTo("E4") == 0){
            profile.setWellNumber(52);
            return;
        }
        if(wellLabel.compareTo("E5") == 0){
            profile.setWellNumber(53);
            return;
        }
        if(wellLabel.compareTo("E6") == 0){
            profile.setWellNumber(54);
            return;
        }
        if(wellLabel.compareTo("E7") == 0){
            profile.setWellNumber(55);
            return;
        }
        if(wellLabel.compareTo("E8") == 0){
            profile.setWellNumber(56);
            return;
        }
        if(wellLabel.compareTo("E9") == 0){
            profile.setWellNumber(57);
            return;
        }
        if(wellLabel.compareTo("E10") == 0){
            profile.setWellNumber(58);
            return;
        }
        if(wellLabel.compareTo("E11") == 0){
            profile.setWellNumber(59);
            return;
        }if(wellLabel.compareTo("E12") == 0){
            profile.setWellNumber(60);
            return;
        }
        if(wellLabel.compareTo("F1") == 0){
            profile.setWellNumber(61);
            return;
        }
        if(wellLabel.compareTo("F2") == 0){
            profile.setWellNumber(62);
            return;
        }
        if(wellLabel.compareTo("F3") == 0){
            profile.setWellNumber(63);
            return;
        }
        if(wellLabel.compareTo("F4") == 0){
            profile.setWellNumber(64);
            return;
        }
        if(wellLabel.compareTo("F5") == 0){
            profile.setWellNumber(65);
            return;
        }
        if(wellLabel.compareTo("F6") == 0){
            profile.setWellNumber(66);
            return;
        }
        if(wellLabel.compareTo("F7") == 0){
            profile.setWellNumber(67);
            return;
        }
        if(wellLabel.compareTo("F8") == 0){
            profile.setWellNumber(68);
            return;
        }
        if(wellLabel.compareTo("F9") == 0){
            profile.setWellNumber(69);
            return;
        }
        if(wellLabel.compareTo("F10") == 0){
            profile.setWellNumber(70);
            return;
        }
        if(wellLabel.compareTo("F11") == 0){
            profile.setWellNumber(71);
            return;
        }
        if(wellLabel.compareTo("F12") == 0){
            profile.setWellNumber(72);
            return;
        }
        if(wellLabel.compareTo("G1") == 0){
            profile.setWellNumber(73);
            return;
        }
        if(wellLabel.compareTo("G2") == 0){
            profile.setWellNumber(74);
            return;
        }
        if(wellLabel.compareTo("G3") == 0){
            profile.setWellNumber(75);
            return;
        }
        if(wellLabel.compareTo("G4") == 0){
            profile.setWellNumber(76);
            return;
        }
        if(wellLabel.compareTo("G5") == 0){
            profile.setWellNumber(77);
            return;
        }
        if(wellLabel.compareTo("G6") == 0){
            profile.setWellNumber(78);
            return;
        }
        if(wellLabel.compareTo("G7") == 0){
            profile.setWellNumber(79);
            return;
        }
        if(wellLabel.compareTo("G8") == 0){
            profile.setWellNumber(80);
            return;
        }
        if(wellLabel.compareTo("G9") == 0){
            profile.setWellNumber(81);
            return;
        }
        if(wellLabel.compareTo("G10") == 0){
            profile.setWellNumber(82);
            return;
        }
        if(wellLabel.compareTo("G11") == 0){
            profile.setWellNumber(83);
            return;
        }
        if(wellLabel.compareTo("G12") == 0){
            profile.setWellNumber(84);
            return;
        }
        if(wellLabel.compareTo("H1") == 0){
            profile.setWellNumber(85);
            return;
        }
        if(wellLabel.compareTo("H2") == 0){
            profile.setWellNumber(86);
            return;
        }
        if(wellLabel.compareTo("H3") == 0){
            profile.setWellNumber(87);
            return;
        }
        if(wellLabel.compareTo("H4") == 0){
            profile.setWellNumber(88);
            return;
        }
        if(wellLabel.compareTo("H5") == 0){
            profile.setWellNumber(89);
            return;
        }
        if(wellLabel.compareTo("H6") == 0){
            profile.setWellNumber(90);
            return;
        }
        if(wellLabel.compareTo("H7") == 0){
            profile.setWellNumber(91);
            return;
        }
        if(wellLabel.compareTo("H8") == 0){
            profile.setWellNumber(92);
            return;
        }
        if(wellLabel.compareTo("H9") == 0){
            profile.setWellNumber(93);
            return;
        }
        if(wellLabel.compareTo("H10") == 0){
            profile.setWellNumber(94);
            return;
        }
        if(wellLabel.compareTo("H11") == 0){
            profile.setWellNumber(95);
            return;
        }
        if(wellLabel.compareTo("H12") == 0){
            profile.setWellNumber(96);
            return;
        }
    }

}
