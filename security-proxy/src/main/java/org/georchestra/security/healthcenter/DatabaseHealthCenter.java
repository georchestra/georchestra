package org.georchestra.security.healthcenter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Checks the database connection health.
 * 
 * @author Mauricio Pazos
 *
 */
public class DatabaseHealthCenter {
	
    protected static final Log LOGGER = LogFactory.getLog(CheckPostgresConnections.class.getPackage().getName());

    private static DatabaseHealthCenter THIS = new DatabaseHealthCenter();

	private String host;

	private Integer port;

	private String user;

	private String password;

	private String clientName;

	private String database;
    
	private DatabaseHealthCenter(){
		// singleton
	}
	public static synchronized DatabaseHealthCenter getInstance(String host, Integer port, String database, String user, String password, String clientName){

		THIS.host = host;
		THIS.port = port;
		THIS.database = database;
		THIS.user = user;
		THIS.password = password;
		THIS.clientName = clientName;
		
		return THIS;
		
	}
	/** 
	 * <p>
	 * checks that the connections live is under the maxConnection.
	 * If the 80% of the max value is overcome it is considerate 
	 * "configuration limit", so a warning will be log with
	 * the data of opened connections.
	 * </p>
	 * <p>
	 * If the max is reached the system is in unstable status, thus
	 * a fatal message will be log with the detail of all live connections.
	 * </p>
	 *  
	 * 
	 * @param maxConnections max connections allowed
	 * 
	 * @return true if the connection check pass, false in other case.
	 */
	public boolean checkConnections(final int maxConnections){
		
		if(maxConnections <= 0 ){
			throw new IllegalArgumentException("maxDatabaseConnection must be greater than 0 ");
		}
		
		boolean healthy = true;
		try {
			long healthLimit = Math.round( maxConnections * 0.8 );
			
			List<Map<String,Object>> listConnections = CheckPostgresConnections.findConnections(this.host, this.port, this.database,  this.user, this.password, this.clientName);
			final int liveConnections = listConnections.size();
			if( (liveConnections >= healthLimit) && (liveConnections < maxConnections) ){
				// the configuration is near to the limit, then log the connections status 
				warningReport(liveConnections, healthLimit, maxConnections, listConnections);
				healthy = false;
			} else if(liveConnections >= maxConnections){
			
				// the system is in unstable situation
				unstableReport(liveConnections, healthLimit, maxConnections, listConnections);
				healthy = false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return healthy;
		
		
	}
	/**
	 * The system has open all available connection. 
	 * The information of live connection will be log in order to provide a report
	 *  
	 * @param liveConnections
	 * @param healthLimit
	 * @param maxConnections
	 */
	private void unstableReport(
			final int liveConnections, 
			final long healthLimit,
			final int maxConnections,
			final List<Map<String,Object>> listConnections) {

		String msg = connectionStatusMessage(liveConnections, maxConnections);
		LOGGER.fatal(msg);

		for (Map<String, Object> connection : listConnections) {

			StringBuilder status = new StringBuilder(100);
			status.append("Connection Status UNSTABLE:  ").append(connection);
			LOGGER.fatal(status);
		}

	}
	private void warningReport(
			final int liveConnections, 
			final long healthLimit,
			final int maxConnections, 
			final List<Map<String,Object>> listConnections) {

		String msg = connectionStatusMessage(liveConnections, maxConnections);
		LOGGER.warn(msg);
		for (Map<String, Object> connection : listConnections) {

			StringBuilder status = new StringBuilder(100);
			status.append("Connection Status NEAR TO LIMIT:  ").append(
					connection);
			LOGGER.warn(status);
		}
	}
	
	private String connectionStatusMessage(final int liveConnections, final int maxConnections) {
		StringBuilder msg = new StringBuilder(50);
		
		msg.append("Database Connections: ")
			.append(liveConnections)
			.append(" Max:").append(maxConnections);
		
		return msg.toString();
	}

}
