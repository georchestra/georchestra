package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.NodeList;

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

    public WcsExtractor (File basedir, WcsFormat format) {
        this._basedir = basedir;
        this._format = format;
    }
    public void checkPermission(ExtractorLayerRequest request, String secureHost, String username, String roles)
    throws MalformedURLException, IOException {
        URL capabilitiesURL = request.capabilitiesURL("WMS", null);
        HttpURLConnection connection = (HttpURLConnection) capabilitiesURL.openConnection();
        if(secureHost.equalsIgnoreCase(request._url.getHost())
                || "127.0.0.1".equalsIgnoreCase(request._url.getHost())
                || "localhost".equalsIgnoreCase(request._url.getHost())) {
                    if(username != null) connection.addRequestProperty("sec-username", username);
                    if(roles != null) connection.addRequestProperty("sec-roles", roles);
        }
        
        String capabilities = FileUtils.asString(connection.getInputStream());
        String xpathExpression = String.format("//Layer[Name='%s']", request._layerName);
        NodeList select = XmlUtils.select(xpathExpression, capabilities, XmlUtils.WMS_NAMESPACE_CONTEXT);
        boolean permitted = select.getLength() == 1;
        
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
        WcsReaderRequest readerRequest = WcsReaderRequestFactory.create(WcsReaderRequest.DEFAULT_VERSION, request._layerName,
                request._bbox, requestCRS, request._resolution, request._format, false);
        
        String safeFileName = FileUtils.toSafeFileName(request._layerName);
        reader.readToFile(basedir, safeFileName, readerRequest.getParameters());
        
        return basedir.listFiles();
    }
    
}
