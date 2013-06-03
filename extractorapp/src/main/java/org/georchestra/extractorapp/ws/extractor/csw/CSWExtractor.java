package org.georchestra.extractorapp.ws.extractor.csw;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
	private String _password;
	private String _secureHost;
	private String _username;
	
	
    public CSWExtractor (File basedir, String userName, String password, String secureHost) {
    	
        this._basedir = basedir;
        this._password = password;
        this._username = userName;
        this._secureHost = secureHost;
    }
	

    /**
     * checks the permissions to access to the CSW
     * @param request
     * @param secureHost
     * @param username
     * @param roles
     * @throws MalformedURLException
     * @throws IOException
     */
	public void checkPermission(ExtractorLayerRequest request, String secureHost, String username, String roles)
		    throws MalformedURLException, IOException {
		
		URL capabilitiesURL = request.capabilitiesURL("CSW", null);
		        
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(capabilitiesURL.toExternalForm());
		if(secureHost.equalsIgnoreCase(request._url.getHost())
			|| "127.0.0.1".equalsIgnoreCase(request._url.getHost())
		    || "localhost".equalsIgnoreCase(request._url.getHost())) {
		    LOG.debug(getClass().getSimpleName()+".checkPermission - Secured Server: adding username header and role headers to request for checkPermission");
		    if(username != null) get.addHeader("sec-username", username);
		    if(roles != null) get.addHeader("sec-roles", roles);
		} else {
			LOG.debug(getClass().getSimpleName()+"checkPermission - Non Secured Server");
		}
// FIXME what should be the strategy to check if this user as access to the matadata catalog?
//		        String capabilities = FileUtils.asString(httpclient.execute(get).getEntity().getContent());
//		        Pattern regex = Pattern.compile("(?m)<Layer[^>]*>(\\\\n|\\s)*<Name>\\s*"+Pattern.quote(request._layerName)+"\\s*</Name>");
//		        boolean permitted = regex.matcher(capabilities).find();
//		        
//		        if(!permitted) {
//		            throw new SecurityException("User does not have sufficient privileges to access the Layer: "+request._layerName+". \n\nCapabilties:  "+capabilities);
//		        }
		    }

	/**
	 * Requests to the CSW for a metadata the content will be stored in the temporl dir as xml document.
	 *  
	 * @param request
	 * @return the xml file that contains the metadata
	 * 
	 * @throws IOException
	 * @throws TransformException
	 * @throws FactoryException
	 */
    public File[] extract (final URL metadataURL) throws IOException {
        assert metadataURL != null: metadataURL + "must be provided";
        

        CSWRequest cswRequest = new CSWRequest();
        cswRequest.setURL(metadataURL);
        cswRequest.setTimeOut(Integer.valueOf(60000));

        if(_secureHost.equalsIgnoreCase(metadataURL.getHost())
                || "127.0.0.1".equalsIgnoreCase(metadataURL.getHost())
                || "localhost".equalsIgnoreCase(metadataURL.getHost())) {
        	LOG.debug("CswExtractor.extract - Secured Server: Adding extractionUserName to connection params");
        	
        	cswRequest.setUser( _username);  
        	cswRequest.setPassword(_password); 
        } else {
        	LOG.debug("Non Secured Server");        	
        }

        MetadataEntity metadata = MetadataEntity.create(cswRequest);
        
        String fileName = FileUtils.toSafeFileName("MD_"+"MOCK_ID");// FIXME
        
        metadata.save(this._basedir.getAbsolutePath()+ File.separatorChar + fileName +".xml"); // FIXME maybe it is better the metadata id as filename metadata_id.xml
        
        return this._basedir.listFiles();
    }


}
