package org.georchestra.extractorapp.ws.extractor.csw;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.georchestra.extractorapp.ws.extractor.ExtractorLayerRequest;
import org.georchestra.extractorapp.ws.extractor.FileUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


/**
 * Extracts the metadata document
 * 
 * <p>
 * It is responsible of request the metadata associated to a layer the from Catalog Service and saves it in the temporal directory
 * which will be used to build the zip file. 
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
public class CSWExtractor {
	
	protected static final Log LOG = LogFactory.getLog(CSWExtractor.class.getPackage().getName());
	
	private File _basedir;
	private String _adminPassword;
	private String _secureHost;
	private String _adminUserName;
	
	/**
	 * CSWExtractor
	 * 
	 * @param layerDirectory directory that contains the extracted layer
	 * @param adminUserName 
	 * @param adminPassword
	 * @param secureHost 
	 */
    public CSWExtractor (final File layerDirectory, final String adminUserName, final String adminPassword, final String secureHost) {
    	
        this._basedir = layerDirectory;
        this._adminPassword = adminPassword;
        this._adminUserName = adminUserName;
        this._secureHost = secureHost;
    }
	

    /**
     * checks the permissions to access to the CSW
     * 
     * @param request
     * @param username request user name
     * @param roles
     * 
     * @throws IOException
     */
	public void checkPermission(ExtractorLayerRequest request, String username, String roles)
		    throws IOException {

		InputStream content = null;
		try {
			HttpGet get = new HttpGet(request._isoMetadataURL.toURI());
			
	        if(_secureHost.equalsIgnoreCase(request._isoMetadataURL.getHost())
	                || "127.0.0.1".equalsIgnoreCase(request._isoMetadataURL.getHost())
	                || "localhost".equalsIgnoreCase(request._isoMetadataURL.getHost())) {
	        	LOG.debug(getClass().getName()+ ".checkPermission - Secured Server: adding username header and role headers to request for checkPermission");
	            if(username != null) get.addHeader("sec-username", username);
	            if(roles != null) get.addHeader("sec-roles", roles);
	        } else {
	        	LOG.debug("WfsExtractor.checkPermission - Non Secured Server");
	        }

	        // checks whether it is a metadata
        	DefaultHttpClient httpclient = new DefaultHttpClient();
			content = httpclient.execute(get).getEntity().getContent();

			String metadata = FileUtils.asString(content);
			Pattern regex = Pattern.compile("<gmd:MD_Metadata>*");
			boolean isMetadata = regex.matcher(metadata).find();
			
			if(!isMetadata){
	            throw new SecurityException("The metadata is not available: "+request._isoMetadataURL);
			}
		} catch (Exception e) {
				
			throw new IOException(e);

		} finally {

			if (content != null)
				content.close();
		}
		
		
	}

	/**
	 * Requests to the CSW for a metadata the content will be stored in the temporl dir as xml document.
	 *  
	 * @param request
	 * 
	 * @throws IOException
	 * @throws TransformException
	 * @throws FactoryException
	 */
    public void extract (final URL metadataURL) throws IOException {
        assert metadataURL != null: metadataURL + "must be provided";
        

        CSWRequest cswRequest = new CSWRequest();
        cswRequest.setURL(metadataURL);
        cswRequest.setTimeout(Integer.valueOf(60000));

        if(_secureHost.equalsIgnoreCase(metadataURL.getHost())
                || "127.0.0.1".equalsIgnoreCase(metadataURL.getHost())
                || "localhost".equalsIgnoreCase(metadataURL.getHost())) {
        	LOG.debug("CswExtractor.extract - Secured Server: Adding extractionUserName to connection params");

            // to access the secure host it uses the administrator credential
        	cswRequest.setUser( _adminUserName);  
        	cswRequest.setPassword(_adminPassword); 
        } else {
        	LOG.debug("Non Secured Server");        	
        }

        MetadataEntity metadata = MetadataEntity.create(cswRequest);
        
        metadata.save(this._basedir.getAbsolutePath()+ File.separatorChar + "metadata.xml"); 
        
    }


}
