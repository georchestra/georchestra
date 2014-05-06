package org.georchestra.extractorapp.ws.extractor;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.codec.binary.Base64;
import org.geotools.data.FileDataStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WfsExtractor1_0_0Test {
    @Rule
    public TemporaryFolder testDir = new TemporaryFolder();

    private WFSDataStoreFactory factory;
    private HttpServer server;

    private AtomicInteger portInc = new AtomicInteger(23878);
    private boolean usesVersion1_0_0;
    private boolean serverWasCalled = false;


    @Before
    public void before() throws IOException {
        usesVersion1_0_0 = true;
        this.factory = new WFSDataStoreFactory();
        this.server = HttpServer.create(new InetSocketAddress(portInc.incrementAndGet()), 0);
        this.server.createContext("/geoserver/wfs", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                final String query = httpExchange.getRequestURI().getQuery().toUpperCase();
                serverWasCalled = true;
                usesVersion1_0_0 &= query.contains("VERSION=1.0.0");
                if (query.contains("REQUEST=GETCAPABILITIES")) {
                    respondWith1_0_0CapabiltiesDocument(httpExchange);
                } else if (query.contains("REQUEST=DESCRIBEFEATURETYPE")) {
                    respondWith1_0_0DescribeFeatureDocument(httpExchange);
                } else if (query.contains("REQUEST=GETFEATURE")) {
                    respondWith1_0_0GetFeatureDocument(httpExchange);
                } else {
                    final byte[] response = ("Not a recognized request: " + httpExchange.getRequestURI()).getBytes("UTF-8");
                    httpExchange.sendResponseHeaders(404, response.length);
                    httpExchange.getResponseBody().write(response);
                }
            }
        });
        this.server.start();
    }

    @After
    public void after() {
        this.server.stop(0);
    }

    @Test(expected = SecurityException.class)
    public void testCheckPermission_Illegal_Layer() throws Exception {
        WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot(), factory);
        final String layerName = "layerName";

        ExtractorLayerRequest request = createLayerRequestObject(layerName);
        wfsExtractor.checkPermission(request, "localhost", null, null);

        assertTrue(this.usesVersion1_0_0);
        assertTrue(this.serverWasCalled);
    }

    @Test
    public void testCheckPermission_Legal_Layer() throws Exception {
        WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot(), factory);

        ExtractorLayerRequest request = createLayerRequestObject("sf:archsites");
        wfsExtractor.checkPermission(request, "localhost", null, null);
        assertTrue(this.usesVersion1_0_0);
        assertTrue(this.serverWasCalled);
    }

    @Test
    public void testCheckPermission_Username_and_Password() throws Exception {
        final String impUser = "impUser";
        final String extractorappUsername = "extractorapUsername";
        final String extractorappPassword = "password";
        this.server.removeContext("/geoserver/wfs");
        this.server.createContext("/geoserver/wfs", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                serverWasCalled = true;
                final Headers requestHeaders = httpExchange.getRequestHeaders();
                final String authorization = requestHeaders.getFirst("Authorization");
                String authString = extractorappUsername + ":" + extractorappPassword;
                byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                String authStringEnc = new String(authEncBytes);
                if (authorization.equals("Basic " + authStringEnc) && requestHeaders.getFirst("imp-username").equals(impUser)) {
                    respondWith1_0_0CapabiltiesDocument(httpExchange);
                } else {
                    final byte[] bytes = "Illegal Auth".getBytes("UTF-8");
                    httpExchange.sendResponseHeaders(401, bytes.length);
                    httpExchange.getResponseBody().write(bytes);
                }

            }
        });

        WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot(), factory, extractorappUsername, extractorappPassword, "localhost");

        ExtractorLayerRequest request = createLayerRequestObject("sf:archsites");
        wfsExtractor.checkPermission(request, "localhost", impUser, "ROLE_SV_ADMIN");

        assertTrue(this.serverWasCalled);
    }

    @Test
    public void testExtract_1_0_0_ShapeOutput() throws Exception {

        WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot(), factory);
        ExtractorLayerRequest request = createLayerRequestObject("sf:archsites");

        final File extract = wfsExtractor.extract(request);

        assertTrue(this.usesVersion1_0_0);
        assertTrue(this.serverWasCalled);

        final File[] fileNames = extract.listFiles();
        assertEquals(8, fileNames.length);
        assertBoundingPolygon(extract);

        Collection<File> shapefiles = Collections2.filter(Arrays.asList(fileNames), new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                final String fileExtension = Files.getFileExtension(input.getName());
                return fileExtension.equalsIgnoreCase("shp");
            }
        });

        assertEquals(2, shapefiles.size());

        final ShapefileDataStoreFactory shapefileDataStoreFactory = new ShapefileDataStoreFactory();
        FileDataStore bounding = null, data = null;
        for (File shapefile : shapefiles) {
            if (shapefile.getName().startsWith("bounding_")) {
                bounding = shapefileDataStoreFactory.createDataStore(shapefile.toURI().toURL());
            } else {
                data = shapefileDataStoreFactory.createDataStore(shapefile.toURI().toURL());
            }
        }

        assertNotNull(bounding);
        assertNotNull(data);

        Envelope bounds = calculateBounds(bounding.getFeatureSource());
        assertEquals(23, data.getFeatureSource().getCount(Query.ALL));

        assertTrue(bounds.contains(calculateBounds(data.getFeatureSource())));
    }

    @Test
    public void testExtract_1_0_0_KmlOutput() throws Exception {

        WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot(), factory);
        ExtractorLayerRequest request = createLayerRequestObject("sf:archsites", "kml");

        final File extract = wfsExtractor.extract(request);

        assertTrue(this.usesVersion1_0_0);
        assertTrue(this.serverWasCalled);

        List<String> fileNames = Arrays.asList(extract.list());
        assertEquals(5, extract.listFiles().length);
        assertBoundingPolygon(extract);

        Collections2.filter(fileNames, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.toLowerCase().endsWith("kml");
            }
        });
    }

    private void assertBoundingPolygon(File extractDir) throws IOException {
        List<String> fileNames = Arrays.asList(extractDir.list());
        final String shapefileName = "bounding_POLYGON.shp";
        assertTrue(fileNames.containsAll(Arrays.asList(shapefileName, "bounding_POLYGON.shx", "bounding_POLYGON.dbf",
                "bounding_POLYGON.prj")));

        final ShapefileDataStoreFactory shapefileDataStoreFactory = new ShapefileDataStoreFactory();
        final FileDataStore dataStore = shapefileDataStoreFactory.createDataStore(new File(extractDir, shapefileName).toURI().toURL());

        assertEquals(1, dataStore.getFeatureSource().getCount(Query.ALL));
    }

    private ExtractorLayerRequest createLayerRequestObject(String layerName) throws JSONException, FactoryException,
            MalformedURLException {
        return createLayerRequestObject(layerName, "shp");
    }

    private ExtractorLayerRequest createLayerRequestObject(String layerName, String formatType) throws JSONException, FactoryException,
            MalformedURLException {
        int port = this.server.getAddress().getPort();
        JSONObject layerJson = new JSONObject();
        layerJson.put(ExtractorLayerRequest.URL_KEY, "http://localhost:" + port + "/geoserver/wfs");
        layerJson.put(ExtractorLayerRequest.PROJECTION_KEY, "EPSG:26713");
        layerJson.put(ExtractorLayerRequest.TYPE_KEY, "WFS");
        layerJson.put(ExtractorLayerRequest.FORMAT_KEY, formatType);
        layerJson.put(ExtractorLayerRequest.LAYER_NAME_KEY, layerName);
        JSONObject bbox = new JSONObject();
        bbox.put(ExtractorLayerRequest.BBOX_SRS_KEY, "EPSG:26713");
        JSONArray bboxValue = new JSONArray("[589851.4376666048,4914490.882968263,608346.4603107043,4926501.8980334345]");
        bbox.put(ExtractorLayerRequest.BBOX_VALUE_KEY, bboxValue);
        layerJson.put(ExtractorLayerRequest.BBOX_KEY, bbox);
        JSONObject globalJson = new JSONObject();
        JSONArray emails = new JSONArray();

        return new ExtractorLayerRequest(layerJson, globalJson, emails);
    }

    private Envelope calculateBounds(SimpleFeatureSource featureSource) throws IOException {
        Envelope bounds = new Envelope();
        final SimpleFeatureIterator features = featureSource.getFeatures().features();
        try {
            while (features.hasNext()) {
                SimpleFeature next = features.next();
                final Geometry defaultGeometry = (Geometry) next.getDefaultGeometry();
                bounds.expandToInclude(defaultGeometry.getEnvelopeInternal());
            }
        } finally {
            features.close();
        }

        return bounds;
    }

    private void respondWith1_0_0DescribeFeatureDocument(HttpExchange httpExchange) throws IOException {
        final byte[] response = TestResourceUtils.getResourceAsBytes(WfsExtractor1_0_0Test.class,
                "/wfs/wfs_1_0_0_describeFeatureType.xml");

        writeResponse(httpExchange, response);
    }

    private void respondWith1_0_0GetFeatureDocument(HttpExchange httpExchange) throws IOException {
        final byte[] response = TestResourceUtils.getResourceAsBytes(WfsExtractor1_0_0Test.class,
                "/wfs/wfs_1_0_0_getFeature.xml");

        writeResponse(httpExchange, response);
    }

    private void respondWith1_0_0CapabiltiesDocument(HttpExchange httpExchange) throws IOException {
        String capabilities = TestResourceUtils.getResourceAsString(WfsExtractor1_0_0Test.class, "/wfs/wfs_1_0_0_capabilities.xml");

        capabilities = capabilities.replace("@@port@@", valueOf(this.server.getAddress().getPort()));
        byte[] response = capabilities.getBytes("UTF-8");
        writeResponse(httpExchange, response);
    }


    private void writeResponse(HttpExchange httpExchange, byte[] response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().close();
    }

}