package org.georchestra.extractorapp.ws.extractor.csw;

import com.google.common.io.Files;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.georchestra.extractorapp.ws.extractor.AbstractTestWithServer;
import org.georchestra.extractorapp.ws.extractor.ExtractorLayerRequest;
import org.georchestra.extractorapp.ws.extractor.TestResourceUtils;
import org.georchestra.extractorapp.ws.extractor.WfsExtractor1_0_0Test;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSWExtractorTest extends AbstractTestWithServer {
    private static final String MD_UUID = "18664427-9cd0-4acc-928c-e4071b828206";
    private static final String CONTEXT = "/geonetwork/srv/eng/csw";
    public static final String EMPTY_UUID = "Empty";
    public static final String DENIED_UUID = "denied";
    private boolean serverWasCalled = false;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void before() {
        this.serverWasCalled = false;
    }

    @Test
    public void testCheckPermission_no_auth_permitted() throws Exception {
        new CSWExtractor(folder.getRoot(), null, null, "localhost", null).checkPermission(createExtractionRequest(MD_UUID), null, "localhost");

        assertTrue(serverWasCalled);
    }

    @Test(expected = SecurityException.class)
    public void testCheckPermission_no_auth_denied() throws Exception {
        new CSWExtractor(folder.getRoot(), null, null, "localhost", null).checkPermission(createExtractionRequest(DENIED_UUID), null, "localhost");

        assertTrue(serverWasCalled);
    }

    @Test(expected = SecurityException.class)
    public void testCheckPermission_no_auth_denied2() throws Exception {
        new CSWExtractor(folder.getRoot(), null, null, "localhost", null).checkPermission(createExtractionRequest(EMPTY_UUID), null, "localhost");

        assertTrue(serverWasCalled);
    }

    @Test
    public void testCheckPermission_auth() throws Exception {
        final String username = "username";
        final String password = "password";
        final String impUser = "impUser";
        setServerContext(CONTEXT, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                serverWasCalled = true;
                final Headers requestHeaders = httpExchange.getRequestHeaders();
                final String authorization = requestHeaders.getFirst("Authorization");
                String authStringEnc = WfsExtractor1_0_0Test.encodeUserNameAndPassForBasicAuth(username, password);
                if ( authorization != null && authorization.equals("Basic " + authStringEnc) && requestHeaders.getFirst("imp-username").equals(impUser)) {
                    respondWithMetadataDocument(httpExchange);
                } else {
                    final byte[] bytes = "Illegal Auth".getBytes("UTF-8");
                    httpExchange.sendResponseHeaders(401, bytes.length);
                    httpExchange.getResponseBody().write(bytes);
                }

            }
        });
        new CSWExtractor(folder.getRoot(), username, password, "localhost", null).checkPermission(createExtractionRequest(MD_UUID), impUser,
                "localhost");
        assertTrue(serverWasCalled);
    }

    @Test
    public void testExtract() throws Exception {
        final CSWExtractor extractor = new CSWExtractor(folder.getRoot(), null, null, "localhost", null);
        final URL url = createExtractionRequest(MD_UUID)._isoMetadataURL;
        extractor.extract(url);

        assertTrue(serverWasCalled);

        final File[] list = this.folder.getRoot().listFiles();

        assertEquals(1, list.length);

        final String metadata = Files.toString(list[0], Charset.forName("UTF-8"));

        assertTrue(metadata.contains("michel.quinquis@swisstopo.ch"));
        assertTrue(metadata.contains("<gmd:MD_Metadata"));
    }

    private void respondWithMetadataDocument(HttpExchange httpExchange) throws IOException {
        byte[] response = TestResourceUtils.getResourceAsBytes(CSWExtractorTest.class,
                "/csw/csw-GetById-18664427-9cd0-4acc-928c-e4071b828206.xml");
        writeResponse(httpExchange, response);
    }
    private void respondWithEmptyMetadataDocument(HttpExchange httpExchange) throws IOException {
        byte[] response = TestResourceUtils.getResourceAsBytes(CSWExtractorTest.class,
                "/csw/csw-GetById-Empty.xml");
        writeResponse(httpExchange, response);
    }

    @Override
    protected void configureContext(final HttpServer server) {
        server.createContext(CONTEXT, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                serverWasCalled = true;
                final String query = httpExchange.getRequestURI().getQuery();
                if (query.contains(MD_UUID)) {
                    respondWithMetadataDocument(httpExchange);
                } else if (query.contains(EMPTY_UUID)) {
                    respondWithEmptyMetadataDocument(httpExchange);
                } else {
                    sendError(httpExchange, 404, "Unable to find metadata with id: " + query);
                }
            }

        });
    }

    private ExtractorLayerRequest createExtractionRequest(String mdUuid) throws JSONException, FactoryException, MalformedURLException {
        int port = getServerPort();
        JSONObject layerJson = new JSONObject();
        layerJson.put(ExtractorLayerRequest.URL_KEY, "http://localhost:" + port + "/geoserver/wfs");
        layerJson.put(ExtractorLayerRequest.PROJECTION_KEY, "EPSG:26713");
        layerJson.put(ExtractorLayerRequest.TYPE_KEY, "WFS");
        layerJson.put(ExtractorLayerRequest.FORMAT_KEY, "shp");
        layerJson.put(ExtractorLayerRequest.LAYER_NAME_KEY, "layerName");
        JSONObject bbox = new JSONObject();
        bbox.put(ExtractorLayerRequest.BBOX_SRS_KEY, "EPSG:26713");
        JSONArray bboxValue = new JSONArray("[589851.4376666048,4914490.882968263,608346.4603107043,4926501.8980334345]");
        bbox.put(ExtractorLayerRequest.BBOX_VALUE_KEY, bboxValue);
        layerJson.put(ExtractorLayerRequest.BBOX_KEY, bbox);
        layerJson.put(ExtractorLayerRequest.ISO_METADATA_URL_KEY, "http://localhost:" + getServerPort() +
                                                                  "/geonetwork/srv/eng/csw?request=GetRecordById&service=CSW&version=2" +
                                                                  ".0.2&elementSetName=full&id=" + mdUuid + "&outputSchema=IsoRecord");
        JSONObject globalJson = new JSONObject();
        JSONArray emails = new JSONArray();

        return new ExtractorLayerRequest(layerJson, globalJson, emails);
    }
}