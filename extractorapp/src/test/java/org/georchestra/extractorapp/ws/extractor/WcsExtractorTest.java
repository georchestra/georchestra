package org.georchestra.extractorapp.ws.extractor;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.georchestra.extractorapp.ws.ExtractorException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.referencing.FactoryException;

import com.google.common.io.Files;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WcsExtractorTest extends AbstractTestWithServer {

    private boolean serverWasCalled = false;

    @Rule
    public TemporaryFolder testDir = new TemporaryFolder();

    @Before
    public void before() {
        this.serverWasCalled = false;

    }

    @Test(expected = SecurityException.class)
    public void testCheckPermission_Illegal_Layer() throws Exception {
        RequestConfiguration requestConfig = createRequestConfiguration(null, null);
        WcsExtractor wcsExtractor = new WcsExtractor(testDir.getRoot(), requestConfig);
        final String layerName = "layerName";

        ExtractorLayerRequest request = createLayerRequestObject(layerName, "geotiff");
        wcsExtractor.checkPermission(request, "localhost", null, null);

        assertTrue(this.serverWasCalled);
    }

    @Test
    public void testCheckPermission_Legal_Layer() throws Exception {
        RequestConfiguration requestConfig = createRequestConfiguration(null, null);
        WcsExtractor wcsExtractor = new WcsExtractor(testDir.getRoot(), requestConfig);

        ExtractorLayerRequest request = createLayerRequestObject("nurc:Arc_Sample", "geotiff");
        wcsExtractor.checkPermission(request, "localhost", null, null);
        assertTrue(this.serverWasCalled);
    }

    @Test
    public void testCheckPermission_Username_and_Password() throws Exception {
        final String impUser = "impUser";
        final String extractorappUsername = "extractorapUsername";
        final String extractorappPassword = "password";
        setServerContext("/geoserver/wcs", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                serverWasCalled = true;
                final Headers requestHeaders = httpExchange.getRequestHeaders();
                final String authorization = requestHeaders.getFirst("Authorization");
                String authStringEnc = WfsExtractor1_0_0Test.encodeUserNameAndPassForBasicAuth(extractorappUsername,
                        extractorappPassword);
                if (authorization.equals("Basic " + authStringEnc) && requestHeaders.getFirst("imp-username").equals(impUser)) {
                    respondWith1_0_0CapabiltiesDocument(httpExchange);
                } else {
                    sendError(httpExchange, 401, "Illegal Auth");
                }

            }
        });

        RequestConfiguration requestConfig = createRequestConfiguration(extractorappUsername, extractorappPassword);
        WcsExtractor wcsExtractor = new WcsExtractor(testDir.getRoot(), requestConfig);

        ExtractorLayerRequest request = createLayerRequestObject("nurc:Arc_Sample", "geotiff");
        wcsExtractor.checkPermission(request, "localhost", impUser, "ROLE_SV_ADMIN");

        assertTrue(this.serverWasCalled);
    }

    @Test
    public void testExtract_Geotiff() throws Exception {
        try {
            Class.forName("org.gdal.gdal.gdal");
        } catch (ClassNotFoundException e) {
            Assume.assumeNoException("GDAL JNI could not be found. Skipping test.", e);
        }
        RequestConfiguration requestConfig = createRequestConfiguration(null, null);
        WcsExtractor wcsExtractor = new WcsExtractor(testDir.getRoot(), requestConfig);

        ExtractorLayerRequest request = createLayerRequestObject("nurc:Arc_Sample", "geotiff");
        final File extract = wcsExtractor.extract(request);
        assertTrue(this.serverWasCalled);

        final String[] fileNames = extract.list();
        assertEquals(1, fileNames.length);
        assertTrue(fileNames[0], fileNames[0].endsWith(".tif"));

        for (File file : extract.listFiles()) {
            assertTrue(file.length() > 0);
        }
    }
    @Test
    public void testExtract_TIF() throws Exception {
        RequestConfiguration requestConfig = createRequestConfiguration(null, null);
        WcsExtractor wcsExtractor = new WcsExtractor(testDir.getRoot(), requestConfig);

        ExtractorLayerRequest request = createLayerRequestObject("nurc:Arc_Sample", "tif");
        File extract = null;
        try {
            extract = wcsExtractor.extract(request);
        } catch (RuntimeException e)
        {
            if (!( e instanceof ExtractorException))
                Assume.assumeNoException("RuntimeException occured, please check gdal/ogr native bindings setup.", e);
        }
        assertTrue(this.serverWasCalled);

        final String[] fileNames = extract.list();
        assertEquals(Arrays.toString(fileNames), 4, fileNames.length);

        Set<String> extensions = new HashSet<String>();
        for (String fileName : fileNames) {
            extensions.add(Files.getFileExtension(fileName));
        }

        assertTrue(Arrays.toString(fileNames), extensions.containsAll(Arrays.asList("tif", "tfw", "prj", "tab")));

        for (File file : extract.listFiles()) {
            assertTrue(file.length() > 0);
        }
    }

    public RequestConfiguration createRequestConfiguration(String extractorappUsername, String extractorappPassword) {
        UsernamePasswordCredentials adminCredentials = null;
        if (extractorappUsername != null) {
            adminCredentials = new UsernamePasswordCredentials(extractorappUsername, extractorappPassword);
        }
        return new RequestConfiguration(null, null, null, null, true, null, null, adminCredentials,
                "localhost", this.testDir.getRoot().toString(), 10000000, true, false, null);
    }

    @Override
    protected void configureContext(HttpServer server) {
        server.createContext("/geoserver/wcs", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                final String query = httpExchange.getRequestURI().getQuery().toUpperCase();
                serverWasCalled = true;
                System.out.println("WCS Request: " + httpExchange.getRequestURI().getQuery());
                if (query.contains("REQUEST=GETCAPABILITIES")) {
                    respondWith1_0_0CapabiltiesDocument(httpExchange);
                } else if (query.contains("REQUEST=DESCRIBECOVERAGE")) {
                    respondWith1_0_0DescribeCoverageDocument(httpExchange);
                } else if (query.contains("REQUEST=GETCOVERAGE") && query.contains("FORMAT=GEOTIFF")) {
                    respondWithGeotiff(httpExchange);
                } else if (query.contains("REQUEST=GETCOVERAGE") && query.contains("FORMAT=TIFF")) {
                    respondWithGeotiff(httpExchange);
                } else {
                    sendError(httpExchange, 404, "Not a recognized request: " + httpExchange.getRequestURI());
                }
            }
        });
    }

    private void respondWithGeotiff(HttpExchange httpExchange) throws IOException {
        byte[] response = TestResourceUtils.getResourceAsBytes(WfsExtractor1_0_0Test.class, "/wcs/wcs.tiff");

        httpExchange.getResponseHeaders().put("Content-Type", Arrays.asList("image/geotiff"));
        writeResponse(httpExchange, response);
    }

    private void respondWith1_0_0DescribeCoverageDocument(HttpExchange httpExchange) throws IOException {
        String capabilities = TestResourceUtils.getResourceAsString(WfsExtractor1_0_0Test.class, "/wcs/wcs_1_0_0_describeCoverage.xml");

        capabilities = capabilities.replace("@@port@@", valueOf(getServerPort()));
        byte[] response = capabilities.getBytes("UTF-8");
        httpExchange.getResponseHeaders().put("Content-Type", Arrays.asList("application/xml"));
        writeResponse(httpExchange, response);
    }

    private void respondWith1_0_0CapabiltiesDocument(HttpExchange httpExchange) throws IOException {
        String capabilities = TestResourceUtils.getResourceAsString(WfsExtractor1_0_0Test.class, "/wms/wms-1.1.1-capabilities.xml");

        capabilities = capabilities.replace("@@port@@", valueOf(getServerPort()));
        byte[] response = capabilities.getBytes("UTF-8");
        httpExchange.getResponseHeaders().put("Content-Type", Arrays.asList("application/xml"));
        writeResponse(httpExchange, response);

    }


    private ExtractorLayerRequest createLayerRequestObject(String layerName, String formatType) throws JSONException, FactoryException,
            MalformedURLException {
        int port = getServerPort();
        JSONObject layerJson = new JSONObject();
        layerJson.put(ExtractorLayerRequest.URL_KEY, "http://localhost:" + port + "/geoserver/wcs");
        layerJson.put(ExtractorLayerRequest.PROJECTION_KEY, "CRS:84");
        layerJson.put(ExtractorLayerRequest.TYPE_KEY, "WCS");
        layerJson.put(ExtractorLayerRequest.FORMAT_KEY, formatType);
        layerJson.put(ExtractorLayerRequest.LAYER_NAME_KEY, layerName);
        layerJson.put(ExtractorLayerRequest.RESOLUTION_KEY, 1000);
        JSONObject bbox = new JSONObject();
        bbox.put(ExtractorLayerRequest.BBOX_SRS_KEY, "CRS:84");
        JSONArray bboxValue = new JSONArray("[0,0,45.0,45.0]");
        bbox.put(ExtractorLayerRequest.BBOX_VALUE_KEY, bboxValue);
        layerJson.put(ExtractorLayerRequest.BBOX_KEY, bbox);
        JSONObject globalJson = new JSONObject();
        JSONArray emails = new JSONArray();

        return new ExtractorLayerRequest(layerJson, globalJson, emails);
    }

}