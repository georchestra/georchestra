package com.camptocamp.ogcservstatistics.log4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 
 * This class provides a set of helper methods which allow to format a message that it
 * can be recognized by the OGCServicesAppender.
 * <p>
 * The message format does have the following structure:
 * </p>
 * <pre>
 *  
 * userName|yyyy-MM-dd|ogcRequest
 *   
 * </pre>
 * 
 * @see format
 * @author Mauricio Pazos
 */
public class OGCServiceMessageFormatter {
	
	public static final String SEPARATOR = "|";
	public static final String DATE_FORMAT = "yyyy/MM/dd";

	private OGCServiceMessageFormatter(){
		// utility class
	}

	public static String format(final String user, final String request){
			return format(user, new Date(), request);
	}
	/**
	 * Builds a formated string that can be recognized by the OGCServicesAppender.
	 * <pre>
	 * Produced format:
	 * 
	 * user|yyyy-MM-dd|request
	 * 
	 * </pre>
	 * @param user
	 * @param date
	 * @param request
	 * 
	 * @return The ogcservice message
	 */
	public static String format(final String user, final Date date, final String request){
		
		if((user == null)|| "".equals(user) ){
			throw new IllegalArgumentException("user cannot be null");
		}
		if( date== null ){
			throw new IllegalArgumentException("date cannot be null");
		}
		if((request == null)|| "".equals(request) ){
			throw new IllegalArgumentException("request cannot be null");
		}
		
		// appends user
		StringBuilder ogcService = new StringBuilder(user);
		ogcService.append(SEPARATOR);
		
		// appends date
		DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		ogcService.append(formatter.format(date));
		ogcService.append(SEPARATOR);
		 
		// appends ogc service request
		ogcService.append(request);
		
		return ogcService.toString();
	}

}
