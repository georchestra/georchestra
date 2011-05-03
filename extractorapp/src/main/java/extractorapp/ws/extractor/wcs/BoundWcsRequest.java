package extractorapp.ws.extractor.wcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import extractorapp.ws.extractor.OversizedCoverageRequestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.NodeList;

import extractorapp.ws.ExtractorException;
import extractorapp.ws.extractor.FileUtils;
import extractorapp.ws.extractor.XmlUtils;

/**
 * Represents a {@link WcsReaderRequest} that has been bound to a URL and will
 * lazily download and cache different requests like describeCoverage and
 * getCapabilities
 * 
 * @author jeichar
 */
public class BoundWcsRequest extends WcsReaderRequest {
    private static final String              GET_CAPABILITIES      = "GetCapabilities";
    private static final String              DESCRIBE_COVERAGE     = "DescribeCoverage";
    private static final String                   GET_COVERAGE          = "GetCoverage";
    private static final Map<String, Set<String>> FORMAT_ALIASES;
    
    private static final Log       LOG = LogFactory.getLog(BoundWcsRequest.class.getPackage().getName());
    private static final String XML_ERROR_TYPE = "application/vnd.ogc.se_xml";
    private int bands = -1;

    static {
        HashMap<String, Set<String>> tmp = new HashMap<String, Set<String>> ();
        tmp.put ("jpeg", set ("jpg"));
        tmp.put ("jpg", set ("jpeg"));
        tmp.put ("tiff", set ("tif"));
        tmp.put ("tif", set ("tiff"));
        FORMAT_ALIASES = Collections.unmodifiableMap (tmp);
    }

    private static HashSet<String> set (String... values) {
        return new HashSet<String> (Arrays.asList (values));
    }

    private final URL                        _wcsUrl;
    private String                           _describeCoverage;
    private String                           _capabilities;
    private Set<String>                      formats;
    private Set<String>                      responseCrss, requestCrss, nativeCRSs;

    BoundWcsRequest (URL wcsUrl, WcsReaderRequest request) {
        super (request);
        _wcsUrl = wcsUrl;
    }

    private BoundWcsRequest (String version, String coverage, ReferencedEnvelope bbox, CoordinateReferenceSystem responseCRS, double resx, 
            String format, boolean usePost, URL wcsUrl, String capabilities, String describeCoverage) {
        super(version, coverage, bbox, responseCRS, resx, format, usePost);
        this._wcsUrl = wcsUrl;
        this._describeCoverage = describeCoverage;
        this._capabilities = capabilities;
    }

    /**
     * Returns all formats that can be exported by the server. All formats are
     * lowercase
     */
    public Set<String> getSupportedFormats () throws IOException {
        Set<String> unaliasedFormats = getUnaliasedFormats ();
        Set<String> allFormats = new HashSet<String> (unaliasedFormats);
        for (String string : unaliasedFormats) {
            Set<String> set = FORMAT_ALIASES.get (string);
            if (set != null) {
                allFormats.addAll (set);
            }
        }
        return allFormats;
    }

    /**
     * Returns all Crss that can be exported by the server. All CRS are
     * uppercase
     */
    public Set<String> getSupportedResponseCRSs () throws IOException {
        if (responseCrss == null) {
            NodeList nodes = select ("//wcs:requestResponseCRSs|//wcs:responseCRSs|//wcs:nativeCRSs", getDescribeCoverage());
            responseCrss = new HashSet<String> ();
            for (int i = 0; i < nodes.getLength (); i++) {
                responseCrss.add ("" + nodes.item (i).getTextContent ().trim ().toUpperCase ());
            }
        }

        return responseCrss;
    }

    /**
     * Returns all Crss that can be exported by the server. All CRS are
     * uppercase
     */
    public int numBands () throws IOException {
        if (bands < 0) {
            NodeList nodes = select ("//wcs:AxisDescription", getDescribeCoverage());
            bands = nodes.getLength();
        }

        return bands;
    }

    /**
     * Returns all Crss that can be handled by the server as requests. All CRS are
     * uppercase
     */
    public Set<String> getSupportedRequestCRSs () throws IOException {
        if (requestCrss == null) {
            NodeList nodes = select ("//wcs:requestResponseCRSs|//wcs:requestCRSs|//wcs:nativeCRSs", getDescribeCoverage());
            requestCrss = new HashSet<String> ();
            for (int i = 0; i < nodes.getLength (); i++) {
                requestCrss.add ("" + nodes.item (i).getTextContent ().trim ().toUpperCase ());
            }
        }

        return requestCrss;
    }

    /**
     * Returns all Crss that can be handled by the server as requests. All CRS are
     * uppercase
     */
    public Set<String> getNativeCRSs () throws IOException {
        if (nativeCRSs == null) {
            NodeList nodes = select ("//wcs:nativeCRSs", getDescribeCoverage());
            nativeCRSs = new HashSet<String> ();
            for (int i = 0; i < nodes.getLength (); i++) {
                nativeCRSs.add ("" + nodes.item (i).getTextContent ().trim ().toUpperCase ());
            }
        }

        return nativeCRSs;
    }

    /**
     * Download describeCoverage document and return it in string form.
     * 
     * Downloading only occurs once and is cached so a new instance will be
     * required to redownload
     */
    public String getDescribeCoverage () throws ProtocolException, MalformedURLException, IOException {
        if (_describeCoverage == null) {
            InputStream stream = makeRequest (DESCRIBE_COVERAGE, _wcsUrl, false, 3000);

            _describeCoverage = toString (stream);
        }

        return _describeCoverage;
    }

    /**
     * Download getCapabilities document and return it in string form.
     * 
     * Downloading only occurs once and is cached so a new instance will be
     * required to redownload
     */
    public String getCapabilities () throws ProtocolException, MalformedURLException, IOException {
        if (_capabilities == null) {
            InputStream stream = makeRequest (GET_CAPABILITIES, _wcsUrl, false, 3000);

            _capabilities = toString (stream);
        }

        return _capabilities;
    }

    /**
     * Get the inputStream for the request. This is NOT cached
     */
    public InputStream getCoverage () throws ProtocolException, MalformedURLException, IOException {
        return makeRequest (GET_COVERAGE, _wcsUrl, true, Integer.MAX_VALUE);
    }

    /* ------------------------- Request Update Methods -----------------------------------*/

    @Override
    public BoundWcsRequest withFormat (String newFormat) {
            return new BoundWcsRequest (version, coverage, requestBbox, responseCRS, groundResolutionX, newFormat, usePost, _wcsUrl, this._capabilities, this._describeCoverage);
    }

    @Override
	public BoundWcsRequest withCRS(String code) {
		try {
			CoordinateReferenceSystem newCrs = CRS.decode(code);
			return new BoundWcsRequest(version, coverage, requestBbox, newCrs, groundResolutionX, format, usePost, _wcsUrl, _capabilities, _describeCoverage);
		} catch (FactoryException e) {
			throw new ExtractorException(e);
		}
	}

    /* ------------------------- Support methods -----------------------------------*/

    private Set<String> getUnaliasedFormats () throws IOException {
        if (formats == null) {
            NodeList nodes = select ("//wcs:formats", getDescribeCoverage());
            formats = new HashSet<String> ();
            for (int i = 0; i < nodes.getLength (); i++) {
                String format = nodes.item (i).getTextContent ().trim ().toLowerCase ();
                formats.add (format);
            }
            formats = Collections.unmodifiableSet (formats);
        }

        return formats;
    }

    private String toString (InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream ();

        byte[] bytes = new byte[8192];

        int read = stream.read (bytes);
        while (read != -1) {
            out.write (bytes, 0, read);
            read = stream.read (bytes);
        }
        return new String (out.toByteArray ());
    }

	/**
	 * 
	 * @param resolveFormat
	 *            Currently all parameters are sent in all requests. (Format
	 *            parameter is sent for a describeLayer request which is
	 *            unnecessary, but should ignored). Because of this I had an
	 *            infinite loop. The describeLayer request required for getting
	 *            the supported formats was trying to resolve the aliases in
	 *            order to make the request. 
	 */
    private InputStream makeRequest (String request, URL wcsUrl, Boolean resolveFormat, int timeout) throws IOException, ProtocolException,
            MalformedURLException {
        InputStream in;
        HttpURLConnection connection;
        if (usePost && false) {
            // TODO POST does not work right now
            connection = (HttpURLConnection) wcsUrl.openConnection ();
            connection.setDoOutput (true);
            connection.setDoInput (true);
            connection.setRequestMethod ("POST");

            OutputStream out = connection.getOutputStream ();
            try {
                String params = params (request, "\n", resolveFormat);
                LOG.debug("making POST request to "+wcsUrl+" with post: \n"+params+"\n\n");
                out.write (params.getBytes ());
                in = connection.getInputStream ();
            } finally {
                out.close ();
            }
        } else {
            String spec = wcsUrl.toExternalForm ();
            if (spec.contains ("?")) {
                spec += params (request, "&", resolveFormat);
            } else {
                spec += "?" + params (request, "&", resolveFormat);
            }
            URL getURL = new URL (spec);
            LOG.debug("making GET request to "+getURL);
            connection = (HttpURLConnection) getURL.openConnection();
        }
        
        connection.setReadTimeout(timeout);
        
        // HACK  I want unrestricted access to layers. 
        // Security check takes place in ExtractorController
        connection.addRequestProperty("sec-username", "admin");
        connection.addRequestProperty("sec-roles", "ROLE_SV_ADMIN");
        
        // check for an error response from the server
        if(connection.getContentType().contains(XML_ERROR_TYPE)){
            String error = FileUtils.asString(connection.getInputStream());
            throw new ExtractorException("Error from server while fetching coverage:"+error);
        } else {
            in = connection.getInputStream();
            return in;            
        }
    }

    /**
     * concatenate all the params into a string separated by the provided string
     * @param resolveFormat
     * @throws IOException
     */
    private String params (String request, String separator, Boolean resolveFormat) throws IOException {
        StringBuilder params = new StringBuilder ("SERVICE=WCS");
        params.append (separator);
        params.append ("VERSION=" + version);
        params.append (separator);
        String resolvedformat = format;
        if (resolveFormat) {
            resolvedformat = resolveFormat (format);
        }
        params.append ("FORMAT=" + resolvedformat);
        params.append (separator);
        params.append ("REQUEST=" + request);
        params.append (separator);
        params.append ("COVERAGE=" + coverage);
        params.append (separator);
        params.append ("RESPONSE_CRS=EPSG:" + epsg (responseCRS));
        params.append (separator);
        params.append ("CRS=EPSG:" + epsg (requestBbox.getCoordinateReferenceSystem()));
        params.append (separator);
        params.append ("BBOX=" + bboxString ());
        params.append (separator);
        double resx = crsResolution();
        params.append ("RESX=" + resx);
        params.append (separator);
        double resy = resx * requestBbox.getHeight()/requestBbox.getWidth();
        params.append ("RESY=" + resy);
        params.append (separator);

        return params.toString ();
    }

    private double crsResolution() {
        // TODO check if interpolation is None if so then
        // request full resolution

        if (ScaleUtils.isLatLong(responseCRS)) {
            try {

                GeodeticCalculator calc = new GeodeticCalculator(responseCRS);
                ReferencedEnvelope bbox = requestBbox.transform(responseCRS, true);
                calc.setStartingPosition(bbox.getLowerCorner());
                
                calc.setDirection(0, groundResolutionX);
                return calc.getDestinationGeographicPoint().distance(calc.getStartingGeographicPoint());
            } catch (Exception e) {
                throw new ExtractorException(e);
            }
        } else {
            return ScaleUtils.fromMeterToCrs(groundResolutionX, responseCRS);
        }
    }

    private String resolveFormat (String format) throws IOException {
            if (getUnaliasedFormats ().contains (format)) {
                return format;
            } else if (getSupportedFormats ().contains (format)) {
                for (String f : getUnaliasedFormats ()) {
                    Set<String> aliases = FORMAT_ALIASES.get (f);
                    if (aliases != null && aliases.contains (format)) {
                        return f;
                    }
                }
                throw new Error ("Programming error.  " + format + " was not resolved as one of "
                        + getUnaliasedFormats ());
            } else {
                throw new IllegalArgumentException (format + " is not a supported format: " + getUnaliasedFormats ());
            }
    }

	private String bboxString () {
        return requestBbox.getMinX () + "," + requestBbox.getMinY () + "," + requestBbox.getMaxX () + "," + requestBbox.getMaxY ();
    }

    private NodeList select (String xpathExpression, String data) throws ProtocolException, MalformedURLException, IOException,
            AssertionError {
        return XmlUtils.select(xpathExpression, data, XmlUtils.WCS_NAMESPACE_CONTEXT);
    }


    public BoundWcsRequest withRequestBBox(ReferencedEnvelope newBBox) {
        return new BoundWcsRequest(version, coverage, newBBox, responseCRS, groundResolutionX, format, usePost, _wcsUrl, _capabilities, _describeCoverage);
    }

    public void assertLegalSize(long maxSize) throws IOException {
        double xmin = requestBbox.getMinX();
        double xmax = requestBbox.getMaxX();
        double ymin = requestBbox.getMinY();
        double ymax = requestBbox.getMaxY();
        double size = ((xmax - xmin) / groundResolutionX) * ((ymax - ymin) / groundResolutionX) * (double)numBands();

        if(size>maxSize) {
            throw new OversizedCoverageRequestException(coverage);
        }
    }
}
