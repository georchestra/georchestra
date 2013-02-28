package org.georchestra.extractorapp.ws.extractor.wcs;

import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.BBOX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.COVERAGE;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.CRS;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.EXTENT;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.FORMAT;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MAXX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MAXY;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MINX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MINY;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.PASSWORD;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.REMOTE_REPROJECT;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RES;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RESULT_IMAGE_PARAMS;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RESX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RESY;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.USERNAME;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.USE_COMMANDLINE_GDAL;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.USE_POST;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.VERSION;
import static org.geotools.referencing.CRS.lookupEpsgCode;

import java.io.IOException;
import java.net.URL;

import org.georchestra.extractorapp.ws.ExtractorException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A convenient class that represents a GetCoverageRequest.  The result from getParameters can be passed to a
 * {@link WcsCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])} method
 * 
 * test server : http://gws2.pcigeomatics.com/wcs1.0.0/wcs?SERVICE=wcs&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=demo/wcs_l7_ms.pix&CRS=EPSG:4326&BBOX=-117.88341239280106,33.707704191028995,-117.65485697866967,33.89850474983795&WIDTH=700&HEIGHT=700&FORMAT=GeoTIFF
 * 
 * Use {@link WcsReaderRequestFactory} to create instances of {@link WcsReaderRequestFactory}
 * 
 * TODO When reading images you need to pass in the parameters for the type of coverage requested.  So if you are reading
 *      a geotiff you may need geotiff specific parameters for both reading and writing
 * @author jeichar
 */
public class WcsReaderRequest {
    public static final String DEFAULT_CRS = "EPSG:4326";
    public static final String DEFAULT_VERSION = "1.0.0";
    
    public final String             version;
    public final String             coverage;
    public final ReferencedEnvelope requestBbox;

    public final double groundResolutionX;

    /*
     * Not required for geOrchestra
     * 
     * public final String timeStart, timeEnd; public final int resx, resy;
     * public final _responseCrs;
     */
    public final String format;
    public final boolean usePost;
    public final CoordinateReferenceSystem responseCRS;
	protected final String username;
	protected final String password;
    public final boolean remoteReproject;
    public final boolean useCommandLineGDAL;

    /**
     * Use {@link WcsReaderRequestFactory} to create instances of {@link WcsReaderRequestFactory}
     * @param version WCS version to use.  Can use {@link #DEFAULT_VERSION}
     * @param coverage the layer/coverage name of the coverage to access
     * @param bbox the bbox of the desired area in the coverage
     * @param resolution width of the returned image
     * @param height height of returned image
     * @param format format of the image
     * @param usePost if true post will be used for making requests
     * @param remoteReproject 
     * @param password 
     * @param username 
     */
    protected WcsReaderRequest (String version, String coverage, ReferencedEnvelope bbox, CoordinateReferenceSystem responseCRS, double resolution,
            String format, boolean usePost, boolean remoteReproject, boolean useCommandLineGDAL, String username, String password) {
        if(resolution <= 0) {
            throw new IllegalArgumentException("resolution must be greater than 0");
        }
        this.version = version;
        this.coverage = coverage;
        this.responseCRS = responseCRS;
        this.groundResolutionX = resolution;
        this.format = format.toLowerCase ();
        this.usePost = usePost;
        this.remoteReproject = remoteReproject;
        this.useCommandLineGDAL = useCommandLineGDAL;
        this.requestBbox = bbox;
        this.username = username;
        this.password = password;
        try {
            lookupEpsgCode (this.responseCRS, false);
        } catch (FactoryException e) {
            throw new IllegalArgumentException ("Could not find EPSG code for responseCRS",e);
        }
    }
    
    protected WcsReaderRequest (WcsReaderRequest request) {
        this(request.version, request.coverage, request.requestBbox, request.responseCRS, request.groundResolutionX, request.format, request.usePost, request.remoteReproject, request.useCommandLineGDAL, request.username, request.password);
    }

    /**
     * Create a new request based on the current request but with a new format
     */
    public WcsReaderRequest withFormat (String newFormat) {
        return new WcsReaderRequest (version, coverage, requestBbox, responseCRS, groundResolutionX, newFormat, usePost, remoteReproject, useCommandLineGDAL, username, password);
    }
    
    /**
     * Create a new request based on the current request but with a new CRS.  The bbox
     * is reprojected as required
     */
    public WcsReaderRequest withCRS(String code) {
        try {
            CoordinateReferenceSystem newCrs = org.geotools.referencing.CRS.decode(code);
            return new WcsReaderRequest(version, coverage, requestBbox, newCrs, groundResolutionX, format, usePost, remoteReproject, useCommandLineGDAL, username, password);
        } catch (FactoryException e) {
            throw new ExtractorException(e);
        }
    }

    public String getResponseEpsgCode() {
		try {
			return org.geotools.referencing.CRS.lookupIdentifier(responseCRS,true);
		} catch (FactoryException e) {
			throw new ExtractorException(e);
		}
	}
    
    /**
     * Convert this request to Geotools parameters.  These are required to make a request with the WcsCoverageReader,
     * unless {@link #execute(WcsCoverageReader)} is used instead
     */
    public GeneralParameterValue[] getParameters() {
        ParameterValue<Boolean> usePost = USE_POST.createValue ();
        usePost.setValue (this.usePost);

        ParameterValue<Boolean> remoteReproject = REMOTE_REPROJECT.createValue ();
        remoteReproject.setValue (this.remoteReproject);
        
        ParameterValue<Boolean> useCommandLineGDAL = USE_COMMANDLINE_GDAL.createValue ();
        useCommandLineGDAL.setValue (this.useCommandLineGDAL);

        ParameterValue<String> version = VERSION.createValue ();
        version.setValue (this.version);

        ParameterValue<String> coverage = COVERAGE.createValue ();
        coverage.setValue (this.coverage);

        ParameterValue<String> format = FORMAT.createValue ();
        format.setValue (this.format);
        
        ParameterValue<String> username = USERNAME.createValue ();
        username.setValue (this.username);
        
        ParameterValue<String> password = PASSWORD.createValue ();
        password.setValue (this.password);
        
        ParameterValue<String> crs = CRS.createValue ();
        Integer epsg = epsg (responseCRS);
        crs.setValue ("EPSG:"+epsg);
        
        ParameterValueGroup size = RES.createValue ();
        
        size.parameter (RESX.getName ().getCode ()).setValue(groundResolutionX);
        size.parameter (RESY.getName ().getCode ()).setValue(groundResolutionX);
        
        ParameterValueGroup bbox = BBOX.createValue ();
        bbox.parameter (MINX.getName ().getCode ()).setValue (this.requestBbox.getMinX ());
        bbox.parameter (MINY.getName ().getCode ()).setValue (this.requestBbox.getMinY ());
        bbox.parameter (MAXX.getName ().getCode ()).setValue (this.requestBbox.getMaxX ());
        bbox.parameter (MAXY.getName ().getCode ()).setValue (this.requestBbox.getMaxY ());
        bbox.parameter (CRS.getName ().getCode ()).setValue ("EPSG:"+epsg(requestBbox.getCoordinateReferenceSystem()));

        ParameterGroup extent = new ParameterGroup (EXTENT, new ParameterValueGroup[]{bbox});
        ParameterGroup resultImageParams = new ParameterGroup (RESULT_IMAGE_PARAMS, new ParameterValueGroup[]{size});
        
        return new GeneralParameterValue[]{usePost, remoteReproject, useCommandLineGDAL, version, coverage, format, crs, extent, resultImageParams, username, password};
    }

    /**
     * Make this request with the reader provided
     */
    public GridCoverage execute(WcsCoverageReader reader) throws IOException {
        return reader.read(getParameters());
    }
    
    public BoundWcsRequest bind (URL wcsUrl) {
        return new BoundWcsRequest (wcsUrl, this);
    }

    /* -------------------  Package level support methods  ----------------------------------------*/
    /**
     * determins the type of extension the resulting image file should have.
     */
    String fileExtension () {
        if(Formats.isGeotiff(format)){
            return "tif";
        }
        if(Formats.isJPEG2000(format)){
            return "jp2";
        }
        return format;
    }
    
    /**
     * Used by this class and tests for getting the parameter data from a geotools parameter
     */
    static <T> T getValue (GeneralParameterValue param, Class<T> required) {
        Object value = ((ParameterValue<?>)param).getValue ();
        return required.cast (value);
    }

    Integer epsg (CoordinateReferenceSystem crs) throws Error {
        Integer epsg;
        try {
            epsg = lookupEpsgCode (crs, true);
        } catch (FactoryException e) {
            throw new Error(e);
        }
        return epsg;
    }

    /* -------------------  Support methods  ----------------------------------------*/
    
    static void assertNotNull (String paramName, Object value) {
       if(value==null){
           throw new IllegalArgumentException (paramName+" is a required parameter");
       }
   }

    

}
