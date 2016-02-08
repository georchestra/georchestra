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

package org.georchestra.ogcservstatistics.log4j;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
	
	private static Log LOGGER = LogFactory.getLog(OGCServiceMessageFormatter.class.getSimpleName());
	
	public static final String SEPARATOR = "|";
	public static final String DATE_FORMAT = "yyyy/MM/dd";

	private OGCServiceMessageFormatter(){
		// utility class
	}

	public static String format(final String user, final String request, final String org,String [] roles){
			return format(user, new Date(), request, org,roles);
	}
	/*
	public static String format(final String user,final String request, String org) { // roles null  ???
		
			return format(user, new Date(), request, org,null);

	}

	*/
	/**
	 * Builds a formated string that can be recognized by the OGCServicesAppender.
	 * <pre>
	 * Produced format:
	 * 
	 * user|yyyy-MM-dd|request|roles
	 * 
	 * </pre>
	 * @param user
	 * @param date
	 * @param request
	 * @param roles
	 * 
	 * @return The ogcservice message
	 */
	public static String format(final String user, final Date date, final String request, final String org,final String [] roles){
		
		if((user == null)|| "".equals(user) ){
			throw new IllegalArgumentException("user cannot be null");
		}
		if( date== null ){
			throw new IllegalArgumentException("date cannot be null");
		}
		if((request == null)|| "".equals(request) ){
			throw new IllegalArgumentException("request cannot be null");
		}
		if(roles == null){
			roles[0] = "";
		}
		// org can be null
		
		// appends user

		
		StringBuilder ogcLogBuilder = new StringBuilder(user);
		ogcLogBuilder.append(SEPARATOR);
		
		// appends date
		DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		ogcLogBuilder.append(formatter.format(date));
		ogcLogBuilder.append(SEPARATOR);
		 
		// appends ogc service request
		ogcLogBuilder.append(request);
		ogcLogBuilder.append(SEPARATOR);
		
		// appends ogc service org
		ogcLogBuilder.append(org);
		ogcLogBuilder.append(SEPARATOR);
		
		// appends ogc service roles
		StringBuilder rolesBuilder = new StringBuilder();
		int i=0;
		for(String s : roles) {
		   if (i > 0) {
			   rolesBuilder.append(",");
		   }
		   rolesBuilder.append(s);
		   i++;
		}
		
		ogcLogBuilder.append(rolesBuilder.toString());

	    final String ogcStatisticLog = ogcLogBuilder.toString();
	    
	    // log additional information to test the system
	    log(ogcLogBuilder);
		
		return ogcStatisticLog;
	}
	
	private static void log(final StringBuilder logBuilder) {

		StringBuilder debugLog = new StringBuilder("REQUEST: ");
		debugLog.append(logBuilder);

		final long divisor = 1048576;//Gb 1073741824 // Mb 1048576
		final String unit = " Mb ";
		
		debugLog.append(SEPARATOR);
		
		debugLog.append("MEM -");
		// available memory
		long totalMem = Runtime.getRuntime().totalMemory();
		debugLog.append("Current available: ");
		debugLog.append(totalMem / divisor ).append(unit);
		debugLog.append(" - ");

		// available memory
		long maxMem = Runtime.getRuntime().maxMemory();
		debugLog.append("Max: ");
		debugLog.append(maxMem / divisor).append(unit);
		debugLog.append(" - ");

		// free memory
		long freeMem = Runtime.getRuntime().freeMemory();
		debugLog.append("Free: ");
		debugLog.append(freeMem / divisor).append(unit);
		debugLog.append(SEPARATOR);
		 
		 // cpu usage
		 debugLog.append("CPU - ");
		 OperatingSystemMXBean so =   ManagementFactory.getOperatingSystemMXBean();
		 debugLog.append("Load Average: ").append(so.getSystemLoadAverage());
		 debugLog.append(" - ");
		 debugLog.append("Available Processors: ").append(so.getAvailableProcessors());
		 debugLog.append(SEPARATOR);

		 // disk usage
		 File[] roots = File.listRoots();
		 for (File root : roots) {
			 debugLog.append(" DISK (").append(root.toString()).append(")");
			 debugLog.append(" - Total: ").append(root.getTotalSpace()/divisor).append(unit);
			 debugLog.append(" - Usable: ").append(root.getUsableSpace()/divisor).append(unit);
			 debugLog.append(" - Free: ").append(root.getFreeSpace()/divisor).append(unit);
			 debugLog.append(SEPARATOR);
		 }

		 
//		 Properties p = System.getProperties();   
//		    p.list(System.out); 
//		    
//		    System.out.print("Total CPU:");
//		    System.out.println(Runtime.getRuntime().availableProcessors());
//		    System.out.println("os.name=" + System.getProperty("os.name"));
		    
		    
		 LOGGER.debug(debugLog);	
	}

}
