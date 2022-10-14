/**
 *
 */
package org.georchestra.extractorapp.ws.extractor.wcs;

import static org.junit.Assert.*;

import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.BBOX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.COVERAGE;
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

/**
 * @author fgravin
 *
 */
public class WcsReaderRequestFactoryTest {

    private WcsReaderRequest createRequest() throws NoSuchAuthorityCodeException, FactoryException {
        return WcsReaderRequestFactory.create("1.0", "myCov", 0, 0, 1, 1, CRS.decode("EPSG:4326"),
                CRS.decode("EPSG:2154"), 1, "GeoTiff", true, true, true, "scott", "tiger");
    }

    @Test
    public void testCreate() throws Exception {

        WcsReaderRequest rq = createRequest();
        GeneralParameterValue[] params = rq.getParameters();
        WcsReaderRequest rqP = WcsReaderRequestFactory.create(params);

        assertEquals(rq.format, rqP.format);
        assertEquals(rq.usePost, rqP.usePost);
        assertEquals(rq.responseCRS, rqP.responseCRS);
        assertEquals(rq.username, rqP.username);
        assertEquals(rq.remoteReproject, rqP.remoteReproject);
        assertEquals(rq.useCommandLineGDAL, rqP.useCommandLineGDAL);
        assertEquals(rq.version, rqP.version);
        assertEquals(rq.coverage, rqP.coverage);
        assertEquals(rq.requestBbox, rqP.requestBbox);
    }
}
