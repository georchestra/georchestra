/**
 * 
 */
package org.georchestra.ogcservstatistics.dataservices;

import java.sql.Connection;

/**
 * @author Mauricio Pazos
 *
 */
public interface DataCommand {

	/**
	 * Database connection
	 * @param connection
	 */
	public void setConnection(Connection connection);

	/**
	 * Execute the sql command specified
	 * @throws DataCommandException
	 */
	public void execute() throws DataCommandException;

}
