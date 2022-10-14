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
public class WcsReaderRequestTest {

    private WcsReaderRequest createRequest() throws NoSuchAuthorityCodeException, FactoryException {
        return WcsReaderRequestFactory.create("1.0", "myCov", 0, 0, 1, 1, CRS.decode("EPSG:4326"),
                CRS.decode("EPSG:2154"), 1, "GeoTiff", true, true, true, "scott", "tiger");
    }

    @Test
    public void testExtractorLayerRequest() throws Exception {

        WcsReaderRequest rq = createRequest();

        WcsReaderRequest rqShp = rq.withFormat("shp");
        assertEquals(rq.format, "geotiff");
        assertEquals(rqShp.format, "shp");

        WcsReaderRequest rqCrs = rq.withCRS("EPSG:4326");
        assertEquals(rq.responseCRS, org.geotools.referencing.CRS.decode("EPSG:2154"));
        assertEquals(rqCrs.responseCRS, org.geotools.referencing.CRS.decode("EPSG:4326"));
        assertEquals("EPSG:4326", rqCrs.getResponseEpsgCode());

        double resMetric = rq.crsResolution();
        double resLatLon = rqCrs.crsResolution();
        assertEquals(1, resMetric, 0.0001);

    }

    @Test
    public void testGetParameters() throws Exception {
        WcsReaderRequest rq = createRequest();
        GeneralParameterValue[] params = rq.getParameters();
        for (int i = 0; i < params.length; i++) {
            GeneralParameterValue param = params[i];

            String name = param.getDescriptor().getName().getCode();
            if (name.equals(REMOTE_REPROJECT.getName().getCode())) {
                Parameter<?> p = (Parameter<?>) param;
                assertEquals(p.getValue(), rq.remoteReproject);
            } else if (name.equals(USE_COMMANDLINE_GDAL.getName().getCode())) {
                Parameter<?> p = (Parameter<?>) param;
                assertEquals(p.getValue(), rq.useCommandLineGDAL);
            } else if (name.equals(VERSION.getName().getCode())) {
                Parameter<?> p = (Parameter<?>) param;
                assertEquals(p.getValue(), rq.version);
                assertEquals(WcsReaderRequest.getValue(param, String.class), rq.version);
            } else if (name.equals(COVERAGE.getName().getCode())) {
                Parameter<?> p = (Parameter<?>) param;
                assertEquals(p.getValue(), rq.coverage);
            } else if (name.equals(COVERAGE.getName().getCode())) {
                Parameter<?> p = (Parameter<?>) param;
                assertEquals(p.getValue(), rq.coverage);
            } else if (name.equals(FORMAT.getName().getCode())) {
                Parameter<?> p = (Parameter<?>) param;
                assertEquals(p.getValue(), rq.format);
            } else if (name.equals(RES.getName().getCode())) {
                ParameterValueGroup g = (ParameterValueGroup) param;
                assertEquals(g.parameter(RESX.getName().getCode()), rq.groundResolutionX);
                assertEquals(g.parameter(RESY.getName().getCode()), rq.groundResolutionX);
            } else if (name.equals(BBOX.getName().getCode())) {
                ParameterValueGroup g = (ParameterValueGroup) param;
                assertEquals(g.parameter(MINX.getName().getCode()), rq.requestBbox.getMinX());
                assertEquals(g.parameter(MINY.getName().getCode()), rq.requestBbox.getMinY());
                assertEquals(g.parameter(MAXX.getName().getCode()), rq.requestBbox.getMaxX());
                assertEquals(g.parameter(MAXY.getName().getCode()), rq.requestBbox.getMaxY());
            }
        }
        assertTrue(true);
    }

    @Test
    public void testFileExtension() throws Exception {
        WcsReaderRequest rq = createRequest();
        WcsReaderRequest rqJp2 = rq.withFormat("jpeg2000");
        WcsReaderRequest rqShp = rq.withFormat("shp");

        assertEquals("tif", rq.fileExtension());
        assertEquals("jp2", rqJp2.fileExtension());
        assertEquals("shp", rqShp.fileExtension());

    }

    @Test
    public void testEpsg() throws Exception {
        WcsReaderRequest rq = createRequest();
        Integer epsg = rq.epsg(rq.responseCRS);
        assertEquals(2154, epsg.intValue());
    }

}
