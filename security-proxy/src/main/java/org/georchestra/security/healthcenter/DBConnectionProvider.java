package org.georchestra.security.healthcenter;

import java.net.ConnectException;
import java.sql.Connection;

interface DBConnectionProvider {


	/**
	 * <pre>
	 * 
	 * <b>Usage:</b>
	 * <code>
	 * Connection conn = null;
	 * try{
	 *		DBConnectionProvider connProvider = DBConnectionProvider.getInstance();
	 *		connection = connProvider.getConnection();
	 *      ...
	 *
	 * } catch(...){
	 *   ....
	 * } finally{
	 *  	if(conn != null) conn.close();
	 * }
	 * </code>	 
	 * <pre>
	 * 
	 * @return {@link Connection}
	 * @throws ConnectException 
	 */
	public abstract Connection getConnection() throws ConnectException;

}