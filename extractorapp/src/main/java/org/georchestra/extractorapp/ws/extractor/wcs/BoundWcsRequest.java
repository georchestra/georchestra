/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor.wcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ProxySelector;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.georchestra.extractorapp.ws.ExtractorException;
import org.georchestra.extractorapp.ws.extractor.FileUtils;
import org.georchestra.extractorapp.ws.extractor.OversizedCoverageRequestException;
import org.georchestra.extractorapp.ws.extractor.XmlUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a {@link WcsReaderRequest} that has been bound to a URL and will
 * lazily download and cache different requests like describeCoverage and
 * getCapabilities
 *
 * @author jeichar
 */
class BoundWcsRequest extends WcsReaderRequest {
    private static final String GET_CAPABILITIES = "GetCapabilities";
    private static final String DESCRIBE_COVERAGE = "DescribeCoverage";
    private static final String GET_COVERAGE = "GetCoverage";
    private static final Map<String, Set<String>> FORMAT_ALIASES;

    private static final Log LOG = LogFactory.getLog(BoundWcsRequest.class.getPackage().getName());
    private static final String XML_ERROR_TYPE = "application/vnd.ogc.se_xml";
    private int bands = -1;

    static {
        HashMap<String, Set<String>> tmp = new HashMap<String, Set<String>>();
        tmp.put("jpeg", set("jpg"));
        tmp.put("jpg", set("jpeg"));
        tmp.put("tiff", set("tif"));
        tmp.put("tif", set("tiff"));
        FORMAT_ALIASES = Collections.unmodifiableMap(tmp);
    }

    private static HashSet<String> set(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private final URL _wcsUrl;
    private String _describeCoverage;
    private String _capabilities;
    private Set<String> formats;
    private Set<String> responseCrss, requestCrss, nativeCRSs;

    private HttpClient httpClient = new DefaultHttpClient();

    public void setHttpClient(HttpClient c) {
        this.httpClient = c;
    }

    BoundWcsRequest(URL wcsUrl, WcsReaderRequest request) {
        super(request);
        _wcsUrl = wcsUrl;
    }

    private BoundWcsRequest(String version, String coverage, ReferencedEnvelope bbox,
            CoordinateReferenceSystem responseCRS, double resx, String format, boolean usePost, boolean remoteReproject,
            boolean useCommandLineGDAL, String username, String password, URL wcsUrl, String capabilities,
            String describeCoverage) {
        super(version, coverage, bbox, responseCRS, resx, format, usePost, remoteReproject, useCommandLineGDAL,
                username, password);
        this._wcsUrl = wcsUrl;
        this._describeCoverage = describeCoverage;
        this._capabilities = capabilities;
    }

    /**
     * Returns all formats that can be exported by the server. All formats are
     * lowercase
     */
    public Set<String> getSupportedFormats() throws IOException {
        Set<String> unaliasedFormats = getUnaliasedFormats();
        Set<String> allFormats = new HashSet<String>(unaliasedFormats);
        for (String string : unaliasedFormats) {
            Set<String> set = FORMAT_ALIASES.get(string);
            if (set != null) {
                allFormats.addAll(set);
            }
        }
        return allFormats;
    }

    /**
     * Returns all Crss that can be exported by the server. All CRS are uppercase
     */
    public Set<String> getSupportedResponseCRSs() throws IOException {
        if (responseCrss == null) {
            NodeList nodes = select(
                    "//wcs:requestResponseCRSs/text()|//wcs:responseCRSs/text()|//wcs:nativeCRSs/text()",
                    getDescribeCoverage());
            responseCrss = new HashSet<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                responseCrss.add("" + nodes.item(i).getNodeValue().trim().toUpperCase());
            }
        }

        return responseCrss;
    }

    /**
     * Returns all Crss that can be exported by the server. All CRS are uppercase
     */
    public int numBands() throws IOException {
        if (bands < 0) {
            NodeList nodes = select("//wcs:AxisDescription", getDescribeCoverage());
            bands = nodes.getLength();
        }

        return bands;
    }

    /**
     * Returns all Crss that can be handled by the server as requests. All CRS are
     * uppercase
     */
    public Set<String> getSupportedRequestCRSs() throws IOException {
        if (requestCrss == null) {
            NodeList nodes = select("//wcs:requestResponseCRSs/text()|//wcs:requestCRSs/text()|//wcs:nativeCRSs/text()",
                    getDescribeCoverage());
            requestCrss = new HashSet<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node item = nodes.item(i);
                requestCrss.add("" + item.getNodeValue().trim().toUpperCase());
            }
        }

        return requestCrss;
    }

    /**
     * Returns all Crss that can be handled by the server as requests. All CRS are
     * uppercase
     */
    public Set<String> getNativeCRSs() throws IOException {
        if (nativeCRSs == null) {
            NodeList nodes = select("//wcs:nativeCRSs/text()", getDescribeCoverage());
            nativeCRSs = new HashSet<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                nativeCRSs.add(nodes.item(i).getNodeValue().trim().toUpperCase());
            }

            nodes = select("//wcs:spatialDomain/*/@srsName", getDescribeCoverage());
            for (int i = 0; i < nodes.getLength(); i++) {
                nativeCRSs.add(nodes.item(i).getNodeValue().trim().toUpperCase());
            }

        }

        return nativeCRSs;
    }

    /**
     * Download describeCoverage document and return it in string form.
     *
     * Downloading only occurs once and is cached so a new instance will be required
     * to redownload
     */
    public String getDescribeCoverage() throws ProtocolException, MalformedURLException, IOException {
        if (_describeCoverage == null) {
            InputStream stream = makeRequest(DESCRIBE_COVERAGE, _wcsUrl, false, 3000);

            _describeCoverage = toString(stream);
        }

        return _describeCoverage;
    }

    /**
     * Download getCapabilities document and return it in string form.
     *
     * Downloading only occurs once and is cached so a new instance will be required
     * to redownload
     */
    public String getCapabilities() throws ProtocolException, MalformedURLException, IOException {
        if (_capabilities == null) {
            InputStream stream = makeRequest(GET_CAPABILITIES, _wcsUrl, false, 3000);

            _capabilities = toString(stream);
        }

        return _capabilities;
    }

    /**
     * Get the inputStream for the request. This is NOT cached
     */
    public InputStream getCoverage() throws ProtocolException, MalformedURLException, IOException {
        return makeRequest(GET_COVERAGE, _wcsUrl, true, Integer.MAX_VALUE);
    }

    /*
     * ------------------------- Request Update Methods
     * -----------------------------------
     */

    @Override
    public BoundWcsRequest withFormat(String newFormat) {
        return new BoundWcsRequest(version, coverage, requestBbox, responseCRS, groundResolutionX, newFormat, usePost,
                remoteReproject, useCommandLineGDAL, username, password, _wcsUrl, this._capabilities,
                this._describeCoverage);
    }

    @Override
    public BoundWcsRequest withCRS(String code) {
        try {
            CoordinateReferenceSystem newCrs = CRS.decode(code);
            return new BoundWcsRequest(version, coverage, requestBbox, newCrs, groundResolutionX, format, usePost,
                    remoteReproject, useCommandLineGDAL, username, password, _wcsUrl, _capabilities, _describeCoverage);
        } catch (FactoryException e) {
            throw new ExtractorException(e);
        }
    }

    /*
     * ------------------------- Support methods -----------------------------------
     */

    private Set<String> getUnaliasedFormats() throws IOException {
        if (formats == null) {
            // NOTE: seems only available in v1.0.0 of WCS
            NodeList nodes = select("//wcs:formats/text()", getDescribeCoverage());
            formats = new HashSet<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node curNode = nodes.item(i);
                String format = curNode.getNodeValue();

                formats.add(format.trim().toLowerCase());
            }
            formats = Collections.unmodifiableSet(formats);
        }

        return formats;
    }

    private String toString(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] bytes = new byte[8192];

        int read = stream.read(bytes);
        while (read != -1) {
            out.write(bytes, 0, read);
            read = stream.read(bytes);
        }
        return new String(out.toByteArray());
    }

    /**
     *
     * @param resolveFormat Currently all parameters are sent in all requests.
     *                      (Format parameter is sent for a describeLayer request
     *                      which is unnecessary, but should ignored). Because of
     *                      this I had an infinite loop. The describeLayer request
     *                      required for getting the supported formats was trying to
     *                      resolve the aliases in order to make the request.
     */
    private InputStream makeRequest(String request, URL wcsUrl, boolean resolveFormat, int timeout)
            throws IOException, ProtocolException, MalformedURLException {
        httpClient.getParams().setParameter("http.socket.timeout", new Integer(timeout));
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);

        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                httpClient.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault());
        if (httpClient instanceof AbstractHttpClient) {
            ((AbstractHttpClient) httpClient).setRoutePlanner(routePlanner);
        }
        HttpRequestBase httpRequest;
        if (usePost && false) {
            HttpPost httpPost = new HttpPost(request);
            httpRequest = httpPost;
            String params = params(request, "\n", resolveFormat);
            LOG.debug("making POST request to " + wcsUrl + " with post: \n" + params + "\n\n");
            HttpEntity bytes = new ByteArrayEntity(params.getBytes());
            httpPost.setEntity(bytes);
        } else {
            String spec = wcsUrl.toExternalForm();
            if (spec.contains("?")) {
                spec += params(request, "&", resolveFormat);
            } else {
                spec += "?" + params(request, "&", resolveFormat);
            }
            LOG.info("making GET request to " + spec);
            httpRequest = new HttpGet(spec);
        }

        BasicHttpContext localcontext = new BasicHttpContext();
        if (username != null) {
            AuthScope authScope = new AuthScope(wcsUrl.getHost(), wcsUrl.getPort());
            Credentials credentials = new UsernamePasswordCredentials(username, password);

            if (httpClient instanceof AbstractHttpClient)
                ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(authScope, credentials);

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(new HttpHost(wcsUrl.getHost(), wcsUrl.getPort(), wcsUrl.getProtocol()), basicAuth);

            // Add AuthCache to the execution context
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

            ArrayList<String> authpref = new ArrayList<String>();
            authpref.add(AuthPolicy.BASIC);
            authpref.add(AuthPolicy.DIGEST);
            httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);
        }

        HttpResponse response = httpClient.execute(httpRequest, localcontext);
        // check for an error response from the server
        int statusCode = response.getStatusLine().getStatusCode();
        LOG.debug("WCS response status : " + statusCode);
        if (statusCode != 200) {
            throw new ExtractorException(
                    "Error from server while fetching coverage: Response Satus Code not valid -> " + statusCode);
        }
        if (hasContentType(response, XML_ERROR_TYPE)) {
            String error = FileUtils.asString(response.getEntity().getContent());
            throw new ExtractorException("Error from server while fetching coverage:" + error);
        } else {
            return response.getEntity().getContent();
        }
    }

    private boolean hasContentType(HttpResponse response, String contentType) {
        HeaderElement[] types = response.getEntity().getContentType().getElements();
        for (HeaderElement headerElement : types) {
            if (headerElement.getValue() != null && headerElement.getValue().equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * concatenate all the params into a string separated by the provided string
     * 
     * @param resolveFormat
     * @throws IOException
     */
    private String params(String request, String separator, Boolean resolveFormat) throws IOException {
        StringBuilder params = new StringBuilder("SERVICE=WCS");
        params.append(separator);
        params.append("VERSION=" + version);
        params.append(separator);
        String resolvedformat = format;
        if (resolveFormat) {
            resolvedformat = resolveFormat(format);
        }
        params.append("FORMAT=" + resolvedformat);
        params.append(separator);
        params.append("REQUEST=" + request);
        params.append(separator);
        params.append("COVERAGE=" + coverage);
        params.append(separator);
        params.append("RESPONSE_CRS=EPSG:" + epsg(responseCRS));
        params.append(separator);
        params.append("CRS=EPSG:" + epsg(requestBbox.getCoordinateReferenceSystem()));
        params.append(separator);
        params.append("BBOX=" + bboxString());
        params.append(separator);
        double resx = crsResolution();
        params.append("RESX=" + resx);
        params.append(separator);
//        double resy = resx * requestBbox.getHeight()/requestBbox.getWidth();
        double resy = resx; // keeping two resolutions the same because the users were getting unexpected
                            // results
        params.append("RESY=" + resy);
        params.append(separator);

        return params.toString();
    }

    private String resolveFormat(String format) throws IOException {
        if (getUnaliasedFormats().contains(format)) {
            return format;
        } else if (getSupportedFormats().contains(format)) {
            for (String f : getUnaliasedFormats()) {
                Set<String> aliases = FORMAT_ALIASES.get(f);
                if (aliases != null && aliases.contains(format)) {
                    return f;
                }
            }
            throw new Error("Programming error.  " + format + " was not resolved as one of " + getUnaliasedFormats());
        } else {
            throw new IllegalArgumentException(format + " is not a supported format: " + getUnaliasedFormats());
        }
    }

    private String bboxString() {
        return requestBbox.getMinX() + "," + requestBbox.getMinY() + "," + requestBbox.getMaxX() + ","
                + requestBbox.getMaxY();
    }

    private NodeList select(String xpathExpression, String data)
            throws ProtocolException, MalformedURLException, IOException, AssertionError {
        return XmlUtils.select(xpathExpression, data, XmlUtils.WCS_NAMESPACE_CONTEXT);
    }

    public BoundWcsRequest withRequestBBox(ReferencedEnvelope newBBox) {
        return new BoundWcsRequest(version, coverage, newBBox, responseCRS, groundResolutionX, format, usePost,
                remoteReproject, useCommandLineGDAL, username, password, _wcsUrl, _capabilities, _describeCoverage);
    }

    public void assertLegalSize(long maxSize) throws IOException {
        double xmin = requestBbox.getMinX();
        double xmax = requestBbox.getMaxX();
        double ymin = requestBbox.getMinY();
        double ymax = requestBbox.getMaxY();
        double size = ((xmax - xmin) / groundResolutionX) * ((ymax - ymin) / groundResolutionX) * numBands();

        LOG.debug("Raster to extract => xSize : " + (xmax - xmin) / groundResolutionX + " - ySize : "
                + (ymax - ymin) / groundResolutionX + " - nbBands : " + (double) numBands() + " - SIZE : " + size
                + " (size max set to " + maxSize + ")");
        if (size > maxSize) {
            throw new OversizedCoverageRequestException(coverage);
        }
    }
}
