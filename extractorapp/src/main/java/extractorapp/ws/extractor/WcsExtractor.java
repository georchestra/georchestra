package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import extractorapp.ws.extractor.wcs.WcsCoverageReader;
import extractorapp.ws.extractor.wcs.WcsFormat;
import extractorapp.ws.extractor.wcs.WcsReaderRequest;
import extractorapp.ws.extractor.wcs.WcsReaderRequestFactory;

/**
 * Extracts Coverages from WCS
 * 
 * @author jeichar
 */
public class WcsExtractor {

    private final File      _basedir;
    private final WcsFormat _format;
    private RequestConfiguration requestConfig;

    public WcsExtractor(File requestBaseDir, RequestConfiguration requestConfig) {
        this._basedir = requestBaseDir;
        this._format = new WcsFormat(requestConfig.maxCoverageExtractionSize);
        this.requestConfig = requestConfig;
    }
    protected static final Log LOG = LogFactory.getLog(WcsExtractor.class.getPackage().getName());

	public void checkPermission(ExtractorLayerRequest request, String secureHost, String username, String roles)
		    throws MalformedURLException, IOException {
		        URL capabilitiesURL = request.capabilitiesURL("WMS", null);
		        
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

		        String capabilities = FileUtils.asString(httpclient.execute(get).getEntity().getContent());
		        Pattern regex = Pattern.compile("(?m)<Layer[^>]*>(\\\\n|\\s)*<Name>\\s*"+Pattern.quote(request._layerName)+"\\s*</Name>");
		        boolean permitted = regex.matcher(capabilities).find();
		        
		        if(!permitted) {
		            throw new SecurityException("User does not have sufficient privileges to access the Layer: "+request._layerName);
		        }
		    }
    public File[] extract (ExtractorLayerRequest request) throws IOException, TransformException, FactoryException {
        if (request._owsType != OWSType.WCS) {
            throw new IllegalArgumentException (request._owsType + "must be WCS for the WcsExtractor");
        }

        WcsCoverageReader reader = _format.getReader(request._url);

        File basedir = request.createContainingDir(_basedir);
        basedir.mkdirs();

        CoordinateReferenceSystem requestCRS = CRS.decode(request._epsg);
        String username;
        String password;
        // HACK  I want unrestricted access to layers. 
        // Security check takes place in ExtractorThread
        if(requestConfig.secureHost.equalsIgnoreCase(request._url.getHost())
                || "127.0.0.1".equalsIgnoreCase(request._url.getHost())
                || "localhost".equalsIgnoreCase(request._url.getHost())) {
        	LOG.debug("WcsExtractor.extract - Secured Server: Adding extractionUserName to connection params");
            username = requestConfig.adminCredentials.getUserName();
            password = requestConfig.adminCredentials.getPassword();
        } else {
        	LOG.debug("WcsExtractor.extract - Non Secured Server");        	
            username = null;
            password = null;
        }

        WcsReaderRequest readerRequest = WcsReaderRequestFactory.create(WcsReaderRequest.DEFAULT_VERSION, request._layerName,
                request._bbox, requestCRS, request._resolution, request._format, false, requestConfig.remoteReproject, username, password);
        
        String safeFileName = FileUtils.toSafeFileName(request._layerName);
        reader.readToFile(basedir, safeFileName, readerRequest.getParameters());
        
        return basedir.listFiles();
    }
    
}
