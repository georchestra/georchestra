/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

/**
 * Utility class which contains useful method to prepare the http response.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
final class ResponseUtil {

	
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
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
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
