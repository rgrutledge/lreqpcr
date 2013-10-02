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
package org.lreqpcr.profile_editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.lreqpcr.ui_components.PanelMessages;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.lrerqpcr.profile_viewer//ProfileEditor//EN",
autostore = false)
public final class ProfileEditorTopComponent extends TopComponent
        implements UniversalLookupListener, PropertyChangeListener {

    private static ProfileEditorTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ProfileEditorTopComponent";

    public ProfileEditorTopComponent() {
        initComponents();
//        setName(NbBundle.getMessage(ProfileEditorTopComponent.class, "CTL_ProfileEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(ProfileEditorTopComponent.class, "HINT_ProfileEditorTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        setName("Profile Editor");
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(this);
        UniversalLookup.getDefault().addListner(PanelMessages.NEW_DATABASE, this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        profileEditor1 = new org.lreqpcr.ui_components.ProfileEditor();

        profileEditor1.setPreferredSize(new java.awt.Dimension(630, 640));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(profileEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, 638, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(profileEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.lreqpcr.ui_components.ProfileEditor profileEditor1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized ProfileEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new ProfileEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ProfileEditorTopComponent instance. Never call
     * {@link #getDefault} directly!
     */
    public static synchronized ProfileEditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ProfileEditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ProfileEditorTopComponent) {
            return (ProfileEditorTopComponent) win;
        }
        Logger.getLogger(ProfileEditorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    //this is necessary to override the mode menu acess which allows the user to close the window
@Override
    public boolean canClose(){
        return false;
    }

    @Override
    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.NEW_DATABASE) {//Open, New or Close database file change
            DatabaseServices db = (DatabaseServices) UniversalLookup.getDefault().getAll(PanelMessages.NEW_DATABASE).get(0);
            if (db != null) {
                if (db.getDatabaseType() == DatabaseType.CALIBRATION || db.getDatabaseType() == DatabaseType.EXPERIMENT) {
                    requestVisible();
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //Maybe a new TC window has been selected but selection of any node causes 2-3 events...
        //Be sure to never include anything that is time intensive
        TopComponent tc = getRegistry().getActivated();
        if (tc instanceof DatabaseProvider) {
            DatabaseProvider dbProvider = (DatabaseProvider) tc;
            DatabaseType type = dbProvider.getDatabaseServices().getDatabaseType();
            if (type == DatabaseType.EXPERIMENT || type == DatabaseType.CALIBRATION) {
                requestVisible();
            }
        }
    }
}
