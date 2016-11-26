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

package org.lreqpcr.ui_components;

import org.lreqpcr.core.data_objects.ByteWrapper;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.openide.util.ImageUtilities;

/**
 * NOT IMPLEMENTED
 * 
 * @author Bob Rutledge
 */
public class ImageTabbedView {

    public static JTabbedPane getImageViewer(ArrayList<ByteWrapper> imageList) {
        JTabbedPane tabbedPane = new JTabbedPane();
        for (ByteWrapper wapper : imageList) {
            ObjectScene scene = new ObjectScene();
            JComponent view = scene.createView();
            JScrollPane panel = new JScrollPane(view);
            tabbedPane.addTab("Image", panel);
            LayerWidget mainLayer = new LayerWidget(scene);
            scene.addChild(mainLayer);
            scene.getActions().addAction(ActionFactory.createZoomAction());
            ImageIcon icon = new ImageIcon(wapper.getImage());
            Image image = ImageUtilities.icon2Image(icon);
            ImageWidget wigdet = new ImageWidget(scene, image);
            wigdet.getActions().addAction(scene.createObjectHoverAction());
            wigdet.getActions().addAction(scene.createSelectAction());
            mainLayer.addChild(wigdet);
            scene.validate();
        }
        return tabbedPane;
    }
}
