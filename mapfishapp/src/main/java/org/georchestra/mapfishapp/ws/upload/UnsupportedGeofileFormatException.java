/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;


/**
 * This exception is thrown if the geographic file format is not  supported. 
 * 
 * @author Mauricio Pazos
 *
 */
public final class UnsupportedGeofileFormatException extends Exception{

	private static final long serialVersionUID = -9152924743908998721L;

	public UnsupportedGeofileFormatException(String msg) {
		super(msg);
	}
	
}
