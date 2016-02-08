/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ogcservstatistics.calculations;

import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.georchestra.ogcservstatistics.OGCServStatisticsException;
import org.georchestra.ogcservstatistics.dataservices.DataServicesConfiguration;
import org.georchestra.ogcservstatistics.dataservices.QueryCommand;
import org.georchestra.ogcservstatistics.dataservices.RetrieveAllCommand;
import org.georchestra.ogcservstatistics.dataservices.RetrieveLayerConnectionsForUserCommand;
import org.georchestra.ogcservstatistics.dataservices.RetrieveMostActiveUsers;
import org.georchestra.ogcservstatistics.dataservices.RetrieveMostConsultedLayers;
import org.georchestra.ogcservstatistics.dataservices.RetrieveUserConnectionForLayerCommand;


/**
 * This is a facade which provides a set of convenient methods to retrieve 
 * statistic information about OGC Services.
 *  
 * @author Mauricio Pazos
 *
 */
public final class OGCServiceStatistics {
	
	private final static DataServicesConfiguration dsConfiguration = DataServicesConfiguration.getInstance();

	private OGCServiceStatistics(){
		// utility class
	}

	/**
	 * The data required to configure must be specified in the log4j.properties file
	 * 
	 * @param log4jPropertiesFile
	 */
	public static void configure(final String log4jPropertiesFile){
		PropertyConfigurator.configure(log4jPropertiesFile);
	}

	/**
	 * Lists all ogc services log
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * 
	 * @throws OGCServStatisticsException 
	 */
	public static List<Map<String,Object>> list() throws OGCServStatisticsException {
		
		RetrieveAllCommand query = new RetrieveAllCommand();
		return execute(query);
	}
	
	/**
	 *  For each user : list of layers and number of connections
	 * 
	 * Returns a list of map with the following structure:
	 * <pre>
	 * key: layer
	 * value: aLayer
	 * 
	 * key: user
	 * value: aUser
	 * 
	 * key: connections
	 * value: aLong Value
	 * </pre>
	 * 
	 * @param year
	 * @param month
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * @throws IllegalArgumentException, OGCServStatisticsException 
	 */
	public static List<Map<String, Object> > retrieveConnectionsForLayer(final int year, final int month) 
			throws IllegalArgumentException, OGCServStatisticsException{

		RetrieveLayerConnectionsForUserCommand cmd = new RetrieveLayerConnectionsForUserCommand();

		return execute(cmd,year,month);
	}
	
	/**
	 *  For each user : list of layers and number of connections
	 * @param year
	 * @return List of pairs (fieldName, fieldValue)
	 * @throws IllegalArgumentException
	 * @throws OGCServStatisticsException
	 */
	public static List<Map<String, Object> > retrieveConnectionsForLayer(final int year) 
			throws IllegalArgumentException, OGCServStatisticsException{

		RetrieveLayerConnectionsForUserCommand cmd = new RetrieveLayerConnectionsForUserCommand();

		return execute(cmd,year);
	}
	
	/**
	 * For each layer : list of users and number of connections
	 * <pre>
	 * 
	 * key: user
	 * value: aUser
	 * 
	 * key: layer
	 * value: aLayer
	 * 
	 * key: connections
	 * value: aLong Value
	 * </pre> 
	 * 
	 * @param year
	 * @param month
	 * 
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * 
	 * @throws IllegalArgumentException, OGCServStatisticsException 
	 */
	public static List<Map<String, Object>> retrieveUserConnectionsForLayer(final int year, final int month) 
			throws IllegalArgumentException, OGCServStatisticsException {

		RetrieveUserConnectionForLayerCommand cmd = new RetrieveUserConnectionForLayerCommand();
		return execute(cmd, year, month);
	}	

	/**
	 * For each layer : list of users and number of connections
	 * @param year
	 * @return List of pairs (fieldName, fieldValue)
	 * @throws IllegalArgumentException
	 * @throws OGCServStatisticsException
	 */
	public static List<Map<String, Object>> retrieveUserConnectionsForLayer(final int year) 
			throws IllegalArgumentException, OGCServStatisticsException {

		RetrieveUserConnectionForLayerCommand cmd = new RetrieveUserConnectionForLayerCommand();
		return execute(cmd, year);
	}	
	/**
	 * List of the N most active users.
	 * Returns a list of map with the following structure:
	 * <pre>
	 * 
	 * key: user
	 * value: aUser
	 * 
	 * key: connections
	 * value: aLong Value
	 * </pre>
	 * 
	 * @param year	year
	 * @param month month
	 * @param limit N user most active
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * 
	 * @throws IllegalArgumentException, OGCServStatisticsException 
	 */
	public static List<Map<String, Object>> retrieveMostActiveUsers(final int year, final int month, final int limit) 
			throws IllegalArgumentException, OGCServStatisticsException {
		
		RetrieveMostActiveUsers cmd = new RetrieveMostActiveUsers();

		List<Map<String, Object>> result = executeUsingLimit(cmd, year, month, limit);

		assert result.size() <= limit;

		return result;
	}	

	public static List<Map<String, Object>> retrieveMostActiveUsersForYear(final int year, final int month, final int limit) 
			throws IllegalArgumentException, OGCServStatisticsException {
		
		RetrieveMostActiveUsers cmd = new RetrieveMostActiveUsers();

		List<Map<String, Object>> result = executeUsingLimit(cmd, year,  limit);

		assert result.size() <= limit;

		return result;
	}	

	/**
	 * List of the N most consulted layers on month 
	 * Returns a list of map with the following structure:
	 * <pre>
	 * key: layer
	 * value: aLayer
	 * 
	 * key: connections
	 * value: aLong Value
	 * </pre>
	 * 
	 * @param year year
	 * @param month month
	 * @param limit the n most consulted
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * 
	 * @throws OGCServStatisticsException, IllegalArgumentException 
	 */
	public static List<Map<String, Object>> retrieveMostConsultedLayers(final int year, final int month, final int limit) 
			throws IllegalArgumentException, OGCServStatisticsException {

		RetrieveMostConsultedLayers cmd = new RetrieveMostConsultedLayers();

		List<Map<String, Object>> result = executeUsingLimit(cmd, year, month, limit);

		assert result.size() <= limit;

		return result;
	}	

	/**
	 * List of the N most consulted layers on the year
	 * 
	 * @param year
	 * @param limit
	 * @return
	 * @throws IllegalArgumentException
	 * @throws OGCServStatisticsException
	 */
	public static List<Map<String, Object>> retrieveMostConsultedLayers(final int year, final int limit) 
			throws IllegalArgumentException, OGCServStatisticsException {

		RetrieveMostConsultedLayers cmd = new RetrieveMostConsultedLayers();

		List<Map<String, Object>> result = executeUsingLimit(cmd, year, limit);

		assert result.size() <= limit;

		return result;
	}	
	/**
	 * List of the N most consulted layers
	 * @param cmd
	 * @param year
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * @throws OGCServStatisticsException, IllegalArgumentException
	 */
	private static List<Map<String, Object>> execute(QueryCommand cmd, final int year) throws OGCServStatisticsException, IllegalArgumentException{
		if(year <= 0){
			throw new IllegalArgumentException("year must be greater than 0");
		}

		cmd.setYear(year);
		
		return execute(cmd);
	}

	/**
	 * Executes the specified QueryCommand
	 * 
	 * @param cmd 
	 * @param year
	 * @param month
	 * @return List of pairs (fieldName, fieldValue)
	 * @throws OGCServStatisticsException, IllegalArgumentException
	 */
	private static List<Map<String, Object>> execute(QueryCommand cmd, final int year, final int month) throws IllegalArgumentException, OGCServStatisticsException{
		if(year < 1){
			throw new IllegalArgumentException("year must be greater than 0");
		}
		if(month < 1 || month > 12){
			throw new IllegalArgumentException("1 <= month <= 12 is expected");
		}
		cmd.setYear(year);
		cmd.setMonth(month);
		
		return execute(cmd);
	}
	/**
	 * Executes the specified QueryCommand filtering by Year and Month
	 * 
	 * @param cmd
	 * @param year
	 * @param month
	 * @param limit
	 * @return List of pairs (fieldName, fieldValue)
	 * @throws OGCServStatisticsException, IllegalArgumentException
	 */
	private static List<Map<String, Object>> executeUsingLimit(QueryCommand cmd, final int year, final int month, final int limit) 
			throws IllegalArgumentException, OGCServStatisticsException{
		
		if(year < 1){
			throw new IllegalArgumentException("year must be greater than 0");
		}
		if(month < 1 || month > 12){
			throw new IllegalArgumentException("1 <= month <= 12 is expected");
		}
		if(limit < 1){
			throw new IllegalArgumentException("limit must be greater than 0");
		}
		cmd.setYear(year);
		cmd.setMonth(month);
		cmd.setLimit(limit);
		
		return execute(cmd);
	}
	/**
	 * Executes the specified QueryCommand filtering by Year
	 * @param cmd
	 * @param year
	 * @param limit
	 * @return
	 * @throws IllegalArgumentException
	 * @throws OGCServStatisticsException
	 */
	private static List<Map<String, Object>> executeUsingLimit(QueryCommand cmd, final int year, final int limit) 
			throws IllegalArgumentException, OGCServStatisticsException{
		
		if(year < 1){
			throw new IllegalArgumentException("year must be greater than 0");
		}
		if(limit < 1){
			throw new IllegalArgumentException("limit must be greater than 0");
		}
		cmd.setYear(year);
		cmd.setLimit(limit);
		
		return execute(cmd);
	}

	/**
	 * Executes the specified QueryCommand
	 * 
	 * @param cmd
	 * 
	 * @return List of pairs (fieldName, fieldValue)
	 * 
	 * @throws OGCServStatisticsException
	 */
	private static List<Map<String, Object>> execute(QueryCommand cmd) 
			throws OGCServStatisticsException{
		try {
			cmd.setConnection(dsConfiguration.getConnection());

			cmd.execute();

			List<Map<String, Object>> result = cmd.getResult();
			
			return result;
			
		} catch (Exception e) {
			throw new OGCServStatisticsException(e);
		}
		
	}
}
