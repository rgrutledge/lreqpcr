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

package org.lreqpcr.core.data_objects;

/**
 * For unknown reasons, DB4O (ver 7.4) cannot store either java.awt.Image objects
 * or ArrayList<Byte[]> for unknown reasons. However, it was found that DB4O can
 * store byte[] in an ArrayList if they are first wrapped in an object
 * and thus the reason for this class. This allows images to be stored in an
 * ArrayList<ByteWrapper>, although of course any byte[] can be stored in this
 * fashion.
 *
 * @author Bob Rutledge
 */
public class ByteWrapper {
    // TODO decide whether to import the image viewer from the LRE Suite
    private byte[] image;

    public ByteWrapper(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }


}
