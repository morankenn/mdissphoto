package org.mdissjava.commonutils.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * This Interface has the common methods to store files. Could be of various types
 * Database storer, file storer, cache storer...
 * 
 * @author MDISS Java team 2011-2012 University of Deusto
 *
 */
public interface DataStorer 
{
	/**
	 * Saves some data that gets in a InputStream and returns a ID that has been generated
	 * for the stored file/data
	 * 
	 * @param data InputStream with the data to store
	 * @return Returns the id that has been auto generated by the method when the data/file has been stored 
	 * @throws IOException If the data could not be saved then this exception will be thrown
	 */
	String saveData(InputStream data) throws IOException;
	
	/**
	 * Saves some data that gets in a InputStream and assigns the ID that has been passed through the
	 * id param
	 * 
	 * @param data InputStream with the data to store
	 * @param id The key that will be assigned to stored the data
	 * @throws IOException If the data could not be saved then this exception will be thrown
	 */
	void saveData(InputStream data, String id) throws IOException;
	
	/**
	 * Get the data previously stored with the id that is passed in the param
	 * 
	 * @param id The identifier that identifies the stored data
	 * @return Returns the retrieved data in an outputStream
	 * @throws IOException If the data could not be retrieved because of the connection, corrupt data... this exception will be thrown
	 */
	OutputStream getData(String id) throws IOException;
	
	/**
	 * Deletes the previously stored data with the id that is passed in the param
	 * 
	 * @param id The identifier that identifies the stored data
	 */
	void deleteData(String id) throws IOException;

}
