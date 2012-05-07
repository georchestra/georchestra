package com.camptocamp.ogcservstatistics.log4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import com.camptocamp.ogcservstatistics.OGCServStatisticsException;
import com.camptocamp.ogcservstatistics.dataservices.DataServicesConfiguration;
import com.camptocamp.ogcservstatistics.dataservices.InsertCommand;

/**
 * This appender is responsible to record the OGC services in the configured
 * database table.
 * <p>
 * <b>Usage:</b>
 * </p>
 * To configure this module you must to include this appender in the log4j.properties file.
 * The example shows how to configure the appender to work with postgres database: 
 * <pre>
 * 
 * log4j.rootCategory= INFO, OGCSERVICES
 * log4j.appender.OGCSERVICES=com.camptocamp.ogcservstatistics.log4j.OGCServicesAppender
 * log4j.appender.OGCSERVICES.activated=true
 * log4j.appender.OGCSERVICES.jdbcURL=jdbc:postgresql://localhost:5432/testdb
 * log4j.appender.OGCSERVICES.databaseUser=postgres
 * log4j.appender.OGCSERVICES.databasePassword=postgres
 * log4j.appender.OGCSERVICES.bufferSize=1
 * 
 * </pre>
 * <p>
 * Note: you could improve the performance increasing the <b>bufferSize</b> value.
 * </p>
 * 
 * <p>
 * To load the configuration you should include the following code:
 * </p>
 * <pre>
 * 
 *	static{
 *		final String file = "[install-dir]/log4j.properties";
 *		PropertyConfigurator.configure(file);
 *	}
 * </pre>
 * <p>
 * To log a message you should use the following idiom:
 * </p>
 * <pre>
 * final Date time = calendar.getTime();
 * final String user = getUserName();
 * final String request = "http://ns383241.ovh.net:80/geoserver/wfs/...";
 * String ogcServiceMessage = OGCServiceMessageFormatter.format(user, time,request);
 * LOGGER.info(ogcServiceMessage); 
 * </pre>
 * 
 * 
 * @author Mauricio Pazos
 */
public class OGCServicesAppender extends AppenderSkeleton {

	protected String databaseUser = "";

	protected String databasePassword = "";

	private String jdbcURL = "";

	protected String databaseName = "";

	protected String databaseHost = "";

	protected String databasePort = "";

	/**
	 * size of LoggingEvent buffer before writing to the database. 
	 * Default is 1.
	 */
	protected int bufferSize = 1;

	/**
	 * ArrayList holding the buffer of Logging Events.
	 */
	protected ArrayList<Map<String, Object>> buffer;

	/**
	 * Activated 
	 * true: 	it log ogc services 
	 * false: 	it does not log ogc services
	 */
	protected boolean activated = false;

	private DataServicesConfiguration dataServiceConfiguration = DataServicesConfiguration.getInstance();


	public OGCServicesAppender() {
		super();
		this.buffer = new ArrayList<Map<String, Object>>(this.bufferSize);
	}

	
	public String getDatabaseName() {
		return databaseName;
	}


	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getJdbcURL(){
		return this.jdbcURL;
	}
	
	public void setJdbcURL(String jdbcURL){
		this.jdbcURL = jdbcURL;
	}

	public String getDatabaseHost() {
		return databaseHost;
	}


	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}


	public String getDatabasePort() {
		return databasePort;
	}


	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
	}


	public String getDatabaseUser() {
		return databaseUser;
	}
	
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
	
	public String getDatabasePassword() {
		return databasePassword;
	}
	
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int newBufferSize) {
		this.bufferSize = newBufferSize;
		this.buffer.ensureCapacity(this.bufferSize);
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * This hook method called after set all appender properties.
	 * In this case the configuration is set.
	 *  
	 */
	@Override
	public void activateOptions() {
		
		this.dataServiceConfiguration.setUser(getDatabaseUser());
		this.dataServiceConfiguration.setPassword(getDatabasePassword());
		this.dataServiceConfiguration.setJdbcURL(getJdbcURL());
	}


	/**
	 * Appends the OGC Service in the table.
	 * 
	 * The string present in buffer is parsed, if it is an interesting OGC
	 * service then extracts the data required to insert a row in the table.
	 */
	@Override
	protected void append(LoggingEvent event) {

		if (!this.activated)
			return;

		try {

			if (OGCServiceParser.isOGCService(event)) {

				String msg = event.getRenderedMessage();

				List<Map<String, Object>> logList = OGCServiceParser.parseLog(msg);
				
				for (Map<String, Object> log : logList) {
					this.buffer.add(log);
					if (this.buffer.size() >= this.bufferSize) {
						flushBuffer();
					}
				} 
			} 
		} catch (Exception ex) {
			errorHandler.error("Failed to insert the ogc service reocrd", ex,
					ErrorCode.WRITE_FAILURE);
		}
	}


	/**
	 * Inserts in the database table the OGC Service logs maintained in the appender buffer 
	 * @throws OGCServStatisticsException 
	 */
	private void flushBuffer() {
		
		ArrayList<Map<String, Object>> removed = new ArrayList<Map<String,Object>>(this.buffer.size());
		for (Map<String,Object> log: this.buffer) {

			insert(log);
			removed.add(log);
		}
		this.buffer.removeAll(removed);
	}


	private void insert(Map<String, Object> ogcServiceRecord)  {

		try {
			InsertCommand cmd = new InsertCommand();
			cmd.setConnection(this.dataServiceConfiguration .getConnection());
			cmd.setRowValues( ogcServiceRecord);
			cmd.execute();

		} catch (Exception e) {

			errorHandler.error("Failed to insert the log", e,
					ErrorCode.WRITE_FAILURE);
		}
		
	}
	
	@Override
	public void finalize() {
		close();
	}

	/**
	 * Release all the allocated resources
	 */
	@Override
	public void close() {
		try {

			flushBuffer();
			this.dataServiceConfiguration.closeConnection();
			
		} catch (SQLException e) {
			this.errorHandler.error("Error closing connection", e, ErrorCode.GENERIC_FAILURE);
		} finally {
		    this.closed = true;
		}
		
	}

	@Override
	public boolean requiresLayout() {
		return false; // does not require layout configuration
	}


}
