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

package org.georchestra.ldapadmin.ws.backoffice.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

/**
 * Utility class which contains useful method to prepare the http response.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
final public class ResponseUtil {

	
	private ResponseUtil(){
		//utility class pattern
	}
	/**
	 * Build the success message
	 * @return success message
	 */
	public static String buildSuccessMessage(){
		return buildResponseMessage(Boolean.TRUE, null);
	}

	public static String buildResponseMessage(Boolean status){
		return buildResponseMessage(status, null);
	}
	
	public static String buildResponseMessage(Boolean status, String errorMessage){
		
		String error;
		if(errorMessage == null){
			error = "{ \"success\": "+ status.toString() +" }";
		} else {
			error = "{ \"success\": "+ status.toString() +" , \"error\": \"" +errorMessage+ "\" }";
		}
		return error;
	}


	public static void buildResponse(HttpServletResponse response, String jsonData, int sc) throws IOException {
		
		response.setContentType("application/json");
		response.setStatus(sc);

		PrintWriter out = response.getWriter();
		try {
			out.println(jsonData);
			
		} finally {
			out.close();
		}
	}
	
	public static void writeSuccess(HttpServletResponse response) throws IOException {
		
		buildResponse(response, ResponseUtil.buildSuccessMessage() , HttpServletResponse.SC_OK);
	}
	
	public static void writeError(HttpServletResponse response, String message) throws IOException{
		
		buildResponse(response, ResponseUtil.buildResponseMessage(Boolean.FALSE, message), HttpServletResponse.SC_NOT_FOUND);		
	}
	
}
