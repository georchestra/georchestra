/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor.csw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.georchestra.extractorapp.ws.extractor.FileUtils;


/**
 * This class is responsible to maintain the metadata values in a file.
 * 
 * @author Mauricio Pazos
 *
 */
final class MetadataEntity {

	protected static final Log LOG = LogFactory.getLog(MetadataEntity.class.getPackage().getName());

	
	private CSWRequest request;

	/**
	 * a new instance of {@link MetadataEntity}.
	 * 
	 * @param cswRequest where the metadata is available
	 */
	private MetadataEntity(CSWRequest cswRequest) {
		
		this.request = cswRequest;
	}

	
	/**
	 * Crates a new instance of {@link MetadataEntity}. Its values will be retrieved
	 * from the Catalog service specified in the request parameter.
	 * 
	 * @param cswRequest where the metadata is available
	 */
	public static MetadataEntity create(final CSWRequest cswRequest) {
		
		return new MetadataEntity(cswRequest);
		
	}
	

		
	/**
	 * Stores the metadata retrieved from CSW using the request value.
	 * 
	 * @param fileName file name where the metadata must be saved.
	 * 
	 * @throws IOException
	 */
	public void save(final String fileName) throws IOException {
    	
        InputStream content = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
		try {
            writer = new PrintWriter( fileName, "UTF-8" );

        	HttpGet get = new HttpGet(this.request.buildURI() );
        	DefaultHttpClient httpclient = new DefaultHttpClient();
            content = httpclient.execute(get).getEntity().getContent();
            reader = new BufferedReader(new InputStreamReader(content));

            String line = reader.readLine();
            while(line!=null){
                
            	writer.println(line);
                
                line=reader.readLine();
            }
            
        } catch (Exception e ){
        	
        	final String msg = "The metadata could not be extracted";
        	LOG.error(msg, e);
        	
        	throw new IOException(e);
        	
        } finally {
        	
        	if(writer != null) writer.close();
        	
        	if(reader != null) reader.close();
        	
            if( content != null ) content.close();
        }
	}

	
}
