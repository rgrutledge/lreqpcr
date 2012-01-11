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
package org.lreqpcr.core.db4o_provider;

import org.lreqpcr.core.data_objects.Family;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_objects.AverageProfile;
import org.lreqpcr.core.data_objects.Run;
import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.query.Query;
import com.db4o.reflect.jdk.JdkReflector;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.ReactionSetupImpl;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;

/**
 * The primary database service provider
 * based on DB4O.
 *
 * @author Bob Rutledge
 */
public abstract class Db4oServices implements DatabaseServices {

    private ObjectContainer db4o;
    private Configuration config = Db4o.newConfiguration();
    private File databaseFile;

    public Db4oServices() {
        config.reflectWith(new JdkReflector(LreObject.class.getClassLoader()));
        config.optimizeNativeQueries(false);
//Indexing greatly increases speed of retrieval of all profiles holding a specific amplicon or sample
        config.objectClass(AverageProfile.class).objectField("ampliconName").indexed(true);
        config.objectClass(AverageProfile.class).objectField("sampleName").indexed(true);
//        Likely redundant...
        config.objectClass(Profile.class).objectField("ampliconName").indexed(true);
        config.objectClass(Profile.class).objectField("sampleName").indexed(true);
        config.activationDepth(2);//This greatly increases display performance within NetBeans
    }

    public void saveObject(Object object) {
        if (!isDatabaseOpen()) {
            return;
        }
        db4o.store(object);
        //Conducting a commit here dramatically reduces response time!!!
    }

    /**
     * Object-specific deletion, based on
     * a deletion depth of 1, which is necessary in order
     * to preserve linked data objects (e.g. a Run object linked to
     * a Profile). This however requires that other types
     * such as ListArrays be manually deleted. Unfortunately,
     * this leads to a dreaded "if" cascade.
     *
     * @param object
     */
    @SuppressWarnings(value = "unchecked")
    public void deleteObject(Object object) {
        if (object instanceof AverageProfile) {
            AverageProfile replicateProfile = (AverageProfile) object;
            deleteAverageProfile(replicateProfile);
        } else if (object instanceof Run) {
            Run run = (Run) object;
            //Delete all of the Replicate Profiles within this run
            if (run.getAverageProfileList() != null) {
                ArrayList<? extends AverageProfile> avProfileList =
                        (ArrayList<? extends AverageProfile>) run.getAverageProfileList();
                for (AverageProfile avPrf : avProfileList) {
                    deleteAverageProfile(avPrf);
                }
            }
            db4o.delete(run);
        } else if (object instanceof ReactionSetupImpl){
            deleteReactionSetup((ReactionSetupImpl) object);
        }else {
            db4o.delete(object);
        }
    }

    private void deleteAverageProfile(AverageProfile averageProfile) {
        //Delete the replicate Profiles
        if (averageProfile != null) {
            List<? extends Profile> profileList = averageProfile.getReplicateProfileList();
            for (Profile prf : profileList) {
                db4o.delete(prf);
            }
            db4o.delete(averageProfile);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void deleteReactionSetup(ReactionSetupImpl setup){
        List<Profile> l = (List<Profile>) getChildren(setup, AverageProfile.class);
        for (Profile prf : l){
            AverageProfile p = (AverageProfile) prf;
            deleteAverageProfile(p);
        }
        db4o.delete(setup);
    }

    public void commitChanges() {
        if (db4o != null) {
            db4o.commit();
        }
    }

    /**
     * Retrieves all objects of clazz, or if a database file is not open
     * returns a empty list. 
     * @param clazz
     * @return List of all objects or an empty list if a database file is not open
     */
    @SuppressWarnings(value = "unchecked")
    public List getAllObjects(Class clazz) {
        if(isDatabaseOpen()){
            ObjectSet list = db4o.query(clazz);
        return list;
        } else return Collections.EMPTY_LIST;
    }

    public void closeDatabase() {
        if (db4o != null) {
            while (!db4o.ext().isClosed()) {
                //Pre NetBeans Platform implementation
                //Purging does not remove db4o instantiated objects from memory!!
//                db4o.commit();
//                db4o.ext().purge();
                //Closing the database file does not remove db4o instantiated objects from memory!!
                //db4o accepts queries and returns objects even if the database files is closed!!
                //That is, a active db4o object remains in memory even when the database file is closed!!
                //Note also that closing and reopening the same database file can produce duplicates
                //Version 7.12 (vs. 7.4) appears (but not confirmed) to have the same problem
                db4o.close();
                //Porting to the NetBeans windowing system appears to prevent duplicate object creation
                //and so the problem has been resolved, at least for version 7.4.106. Attempting to upgrade
                //to version 7.4.155 produces an IllegalArgumentException when attempting to open a second database file.
                //This appears to be undocumented and does not occur with ver 106
            }
        }
    }

    public void openDatabase(File file) {
        databaseFile = file;
        if (db4o != null) {
            while (!db4o.ext().isClosed()) {
                db4o.close();
            }
        }
        try {
 //This throws an IllegalArguentException for ver 7.4.155, which is not documented!!
            db4o = Db4o.openFile(config, databaseFile.getAbsolutePath());
        } catch (Exception e) {
            String msg = "The database file " + databaseFile.getName() 
                    + " could not be opened due to the error: \""
                    + e.getClass().getSimpleName() + "\"";
            JOptionPane.showMessageDialog(null, msg, "Unable to open the database file",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    @SuppressWarnings(value = "unchecked")
    public List<? extends Family> getChildren(Family member, Class childClass) {
        Query query = db4o.query();
        if (query == null) return Collections.EMPTY_LIST;
        query.constrain(childClass);
        query.descend("parent").constrain(member);
        List children = query.execute();
        return children;
    }

    public boolean isDatabaseOpen() {
        if (db4o == null) {
            return false;
        } else {
            return !db4o.ext().isClosed();
        }
    }

    public List retrieveUsingFieldValue(Class clazz, String fieldName, Object fieldValue) {
        Query query = db4o.query();
        query.constrain(clazz);
        query.descend(fieldName).constrain(fieldValue);
        List list = query.execute();
        return list;
    }

    /*
     * Each database type must implement these methods to generate
     * type-specific database files
     */
    /**
     * Allows a database-specific file to be created
     * Return false if the new file creation was canceled
     *
     * @return indicates whether a new database file was created
     */
    public abstract boolean createNewDatabase();

    /**
     * Allows an existing database-specific file to be selected.
     * Returns false if the file selection was canceled.
     * 
     * @return indicates whether a database file was opened
     */
    public abstract boolean openDatabase();

    /**
     * Allows the database type to be determined based on the enum DatabaseType
     * 
     * @return the database type of this database service
     */
    public abstract DatabaseType getDatabaseType();
}
