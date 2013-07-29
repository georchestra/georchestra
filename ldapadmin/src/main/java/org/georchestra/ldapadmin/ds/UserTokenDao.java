package org.georchestra.ldapadmin.ds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Maintains the tokens generated when the "Lost password use case" is executed.
 * 
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
public class UserTokenDao {
	
	private static final Log LOG = LogFactory.getLog(UserTokenDao.class.getName());
	
	private Connection connection = null;
	
	private String databaseUser;
	private String databasePassword;
	private String jdbcURL;

	private String databaseName = "postgres" ;
	
	public UserTokenDao() {
		
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

	public String getJdbcURL() {
		return jdbcURL;
	}


	public void setJdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL;
	}


	/**
	 * Inserts the new association uid-token.
	 * 
	 * @param uid user identifier
	 * @param token token
	 * @throws DataServiceException
	 */
	public void insertToken(String uid, String token) throws DataServiceException {
		
		if(LOG.isDebugEnabled()){
			String msg = "DatabaseUser: " + databaseUser +"; " + "DatabasePassword: " + databasePassword +"; " + "JdbcURL: " + jdbcURL;
			LOG.debug(msg);
		}
		try {
			InsertUserTokenCommand cmd = new InsertUserTokenCommand();
			cmd.setConnection(getConnection());
			
			Map<String, Object> row = new HashMap<String, Object>(3);
			row.put(DatabaseSchema.UID_COLUMN, uid);
			row.put(DatabaseSchema.TOKEN_COLUMN,  token);
			
			
			Calendar cal = Calendar.getInstance();
			Date date = cal.getTime();
			Timestamp currentDay = new Timestamp(date.getTime());
			row.put(DatabaseSchema.CREATEION_DATE_COLUMN,  currentDay);
			
			cmd.setRowValues( row );
			cmd.execute();

		} catch (Exception e) {

			LOG.error("Failed to insert the uid,token", e);
			
			throw new DataServiceException(e);
		}
	}
		

	/**
	 * Searches the user_token association which match with the provided token.
	 *  
	 * @param token
	 * @return uid 
	 * 
	 * @throws DataServiceException
	 * @throws NotFoundException
	 */
	public String findUserByToken(String token)  throws DataServiceException, NotFoundException{
		try {
			QueryByTokenCommand cmd = new QueryByTokenCommand();
			
			cmd.setConnection(getConnection());

			cmd.setToken(token);
			cmd.execute();

			List<Map<String, Object>> result = cmd.getResult();
			
			if(result.isEmpty() ){
				throw new NotFoundException("the token " + token+ " wasn't found.");
			}

			String uid = (String) result.get(0).get(DatabaseSchema.UID_COLUMN);
			
			return uid;
			
		} catch (Exception e) {
			throw new DataServiceException(e);
		}
	}


	
	public List<Map<String, Object>> findBeforeDate(Date expired) throws DataServiceException {
		try {
			QueryUserTokenExpiredCommand cmd = new QueryUserTokenExpiredCommand();
			
			cmd.setConnection(getConnection());

			cmd.setBeforeDate(expired);
			cmd.execute();

			List<Map<String, Object>> result = cmd.getResult();
			
			return result;
			
		} catch (Exception e) {
			throw new DataServiceException(e);
		}
	}
	
	
	public boolean exist(String uid) throws DataServiceException {
		
		try {
			QueryByUidCommand cmd = new QueryByUidCommand();
			
			cmd.setConnection(getConnection());

			cmd.setUid(uid);
			cmd.execute();

			List<Map<String, Object>> result = cmd.getResult();
			
			return !result.isEmpty();
			
		} catch (Exception e) {
			throw new DataServiceException(e);
		}
		
	}

	public void delete(String uid) throws DataServiceException{
		try {
			DeleteUserTokenCommand cmd = new DeleteUserTokenCommand();

			cmd.setConnection(getConnection());
			
			cmd.setUid( uid);
			
			cmd.execute();

		} catch (Exception e) {

			LOG.error("Failed to insert the uid,token", e);
			
			throw new DataServiceException(e);
		}
	}
	

	private Connection getConnection() throws  DataServiceException {

		// TODO talk about what is the connection strategy in georchestra 
		try {
			if(this.connection == null || this.connection.isClosed() ){
				
				try{
					Class.forName("org.postgresql.Driver");
					
					String url = "jdbc:postgresql://localhost:5432/"+this.databaseName;
					Properties props = new Properties();
					props.setProperty("user","postgres");
					props.setProperty("password","postgres");
					props.setProperty("ssl","true");
					props.setProperty("applicationName", "ldapAdmin");
					this.connection = DriverManager.getConnection(url, props);
					
				} catch(Exception e) {
					LOG.error(e.getMessage());
					throw new DataServiceException("cannot open the connection " + this.databaseName  + "."+ e.getMessage());
					
				}
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage());
			throw new DataServiceException("cannot open the connection " + this.databaseName  + "."+ e.getMessage());
		}
		
		return this.connection;
		
	}
	

}
