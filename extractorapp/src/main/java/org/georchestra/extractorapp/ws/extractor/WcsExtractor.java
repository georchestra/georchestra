package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.georchestra.extractorapp.ws.extractor.wcs.WcsCoverageReader;
import org.georchestra.extractorapp.ws.extractor.wcs.WcsFormat;
import org.georchestra.extractorapp.ws.extractor.wcs.WcsReaderRequest;
import org.georchestra.extractorapp.ws.extractor.wcs.WcsReaderRequestFactory;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * Extracts Coverages from WCS
 *
 * @author jeichar
 */
public class WcsExtractor {

    private final File _basedir;
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

        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setUserAgent(requestConfig.userAgent);

        HttpClientContext localContext = HttpClientContext.create();
        final HttpHost httpHost = new HttpHost(capabilitiesURL.getHost(), capabilitiesURL.getPort(),
                capabilitiesURL.getProtocol());
        HttpGet get = new HttpGet(capabilitiesURL.toExternalForm());
        if (username != null && (secureHost.equalsIgnoreCase(request._url.getHost())
                || "127.0.0.1".equalsIgnoreCase(request._url.getHost())
                || "localhost".equalsIgnoreCase(request._url.getHost()))) {
            LOG.debug(getClass().getSimpleName()
                    + ".checkPermission - Secured Server: adding username header and role headers to "
                    + "request for checkPermission");

            WfsExtractor.addImpersonateUserHeaders(username, roles, get);

            String extractorAppUsername = requestConfig.adminCredentials.getUserName();
            String extractorAppPassword = requestConfig.adminCredentials.getPassword();

            WfsExtractor.enablePreemptiveBasicAuth(capabilitiesURL, httpClientBuilder, localContext, httpHost,
                    extractorAppUsername, extractorAppPassword);

        } else {
            LOG.debug(getClass().getSimpleName() + "checkPermission - Non Secured Server");
        }

        final CloseableHttpClient httpclient = httpClientBuilder.build();
        String capabilities = FileUtils
                .asString(httpclient.execute(httpHost, get, localContext).getEntity().getContent());

        String queriedLayer = request._layerName;

        boolean permitted = isLayerPresent(capabilities, queriedLayer);

        if (!permitted) {
            throw new SecurityException("User does not have sufficient privileges to access the Layer: " + queriedLayer
                    + ". " + "\n\nCapabilities:  " + capabilities);
        }
    }

    /**
     * Checks if the user has access to the requested layer
     *
     * @param getCapabilitiesDocument the GetCapabilities response of the remote
     *                                server
     * @return true if the current user has access, else false.
     */
    private boolean isLayerPresent(String getCapabilitiesDocument, String layerName) {
        // if the layer name is prefixed, then remove it, the regexp should take care of
        // it.
        String queriedLayer = layerName;
        if (queriedLayer.contains(":")) {
            String[] tmpLayer = queriedLayer.split(":");
            queriedLayer = tmpLayer[tmpLayer.length - 1];
        }

        Pattern regex = Pattern.compile(
                "(?m)<Layer[^>]*>(\\\\n|\\s)*<Name>\\s*([\\w_0-9-]*:)?" + Pattern.quote(queriedLayer) + "\\s*</Name>");
        return regex.matcher(getCapabilitiesDocument).find();
    }

    /**
     * Creates a directory where the layer is extracted.
     *
     * @param request
     * @return directory that contains the layers
     *
     * @throws IOException
     * @throws TransformException
     * @throws FactoryException
     */
    public File extract(ExtractorLayerRequest request) throws IOException, TransformException, FactoryException {
        if (request._owsType != OWSType.WCS) {
            throw new IllegalArgumentException(request._owsType + "must be WCS for the WcsExtractor");
        }

        WcsCoverageReader reader = _format.getReader(request._url);

        File basedir = request.createContainingDir(_basedir);

        CoordinateReferenceSystem requestCRS = CRS.decode(request._epsg);
        String username;
        String password;
        // HACK I want unrestricted access to layers.
        // Security check takes place in ExtractorThread
        if (requestConfig.adminCredentials != null && (requestConfig.secureHost.equalsIgnoreCase(request._url.getHost())
                || "127.0.0.1".equalsIgnoreCase(request._url.getHost())
                || "localhost".equalsIgnoreCase(request._url.getHost()))) {
            LOG.debug("WcsExtractor.extract - Secured Server: Adding extractionUserName to connection params");
            username = requestConfig.adminCredentials.getUserName();
            password = requestConfig.adminCredentials.getPassword();
        } else {
            LOG.debug("WcsExtractor.extract - Non Secured Server");
            username = null;
            password = null;
        }

        WcsReaderRequest readerRequest = WcsReaderRequestFactory.create(WcsReaderRequest.DEFAULT_VERSION,
                request._layerName, request._bbox, requestCRS, request._resolution, request._format, false,
                requestConfig.remoteReproject, requestConfig.useCommandLineGDAL, username, password);

        String safeFileName = FileUtils.toSafeFileName(request._layerName);
        reader.readToFile(basedir, safeFileName, readerRequest.getParameters());

        return basedir;
    }

}
