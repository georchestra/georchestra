package org.georchestra.extractorapp.ws.extractor.csw;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Request builder. 
 * 
 * This class is responsibe to build the request required by the metadata catalog (CSW).
 * 
 * @author Mauricio Pazos
 *
 */
class CSWRequest {

	private URL url = null;
	private Integer timeout = Integer.valueOf(60000);
	private String user;
	private String password;

	public String build(){
		return null; // TODO
	}

	public void setURL(URL url) {
		
		this.url = url;
	}

	public void setTimeOut(Integer timeOut) {

		this.timeout = timeOut;
	}
	

	public void setUser(String userName) {

		this.user = userName;
	}

	public void setPassword(String password) {

		this.password = password;
	}

	public URI buildURI() throws URISyntaxException {
		return this.url.toURI();
	}
	
	



}
