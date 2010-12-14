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

package org.lreqpcr.core.database_services;

import org.lreqpcr.core.data_objects.Family;
import java.io.File;
import java.util.List;

/**
 * The database services interface.
 * Note that is a DB4O-centric interface, in which the Family interface is used
 * to retrieve various Objects from the database.
 *
 * @author Bob Rutledge
 */
public interface DatabaseServices {

    /**
     * Create a new database
     * @return indicates whether creation of the new database was successful
     */
    public boolean createNewDatabase();

    /**
     * Open a database. Note that this assumes that a file chooser dialog will
     * be provided by this method in order to allow a database file to be specified.
     * @return indicates whether the database was opened successfully
     */
    public boolean openDatabase();

    /**
     * The enum DatabaseType is used to specify the identity of each database
     * @return the database type
     */
    public DatabaseType getDatabaseType();

    /**
     * Returns all stored objects of the specified Class
     *
     * @param clazz the Class of the objects to be returned
     * @return a list of all stored objects of the type clazz
     */
    public List getAllObjects(Class clazz);

    /**
     * Returns the children of this Member, or null if it does not have children.
     *
     * @param member the Family member
     * @param childClass the child class as specified by the Family interface
     * @return a List containing all Member's children
     */
    public List<? extends Family> getChildren(Family member, Class childClass);

    /**
     * An extended version of "getChildren" that retrieves all instances of a specified
     * class that contains a specified value within the specified field. For example,
     * all Profiles holding the specified amplicon name within the ampliconName field.
     * In this case, all Profiles containing a specified amplicon would be retrieved.
     *
     * @param clazz the Class of the objects to be retrieve
     * @param fieldName the name of the field within the Class
     * @param fieldValue the field value to base the search on
     * @return a List of all retrieved Objects
     */
    public List retrieveUsingFieldValue(Class clazz, String fieldName, Object fieldValue);

    /**
     * Save the supplied Object to the database
     *
     * @param object the object to be saved
     */
    public void saveObject(Object object);

    /**
     * Delete the supplied object from the database.
     *
     * @param object the object to delete
     */
    public void deleteObject(Object object);

    /**
     * Commits changes to the database file to disk.
     */
    public void commitChanges();

    /**
     * Open the database specified by the file. Note that it is the responsiblity
     * of the database service provider to close the previous database file,
     * if one is already open.
     *
     * @param file the database to be opened
     */
    public void openDatabase(File file);

    /**
     * Close the database file.
     * 
     */
    public void closeDatabase();

    /**
     * The database file. This should only be used for retrieving information 
     * about the current database file, such as the file name, path or size.
     * @return the database file or null if no file is open
     */
    public File getDatabaseFile();

    /**
     * Used to determine whether a database file is currently open.
     * 
     * @return returns true if a database file is open
     */
    public boolean isDatabaseOpen();

}
