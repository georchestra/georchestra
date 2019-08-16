package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.georchestra.mapfishapp.ws.upload.GeotoolsFeatureReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

public class UploadGeoFileControllerTest {

    private UpLoadGeoFileController controller;

    public @Rule TemporaryFolder tmpFolder = new TemporaryFolder();

    private MockHttpServletResponse response;

    public static @BeforeClass void setUpGeoToolsReferencing() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    public @Before void before() {
        controller = new UpLoadGeoFileController();
        controller.setDocTempDir(tmpFolder.getRoot().getAbsolutePath());
        controller.setTempDirectory(tmpFolder.getRoot());
        controller.setAllowFileProtocol(true);
        response = new MockHttpServletResponse();
    }

    public @Test void testUploadUnsupportedFileType() throws Exception {
        URL url = fileURL("pigma_regions_POLYGON.dat");
        controller.toGeoJsonFromURL(response, url, null, null);

        String responseBody = response.getContentAsString();
        assertEquals("application/json", response.getContentType());
        assertEquals(responseBody, 500, response.getStatus());

        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, false, jsonresponse.get("success"));
        assertEquals(responseBody, "unsupported file type", jsonresponse.get("msg"));
        assertEquals(responseBody, "fileupload_error_unsupportedFormat", jsonresponse.get("error"));
    }

    public @Test void testUploadGeoJSONFromURL() throws Exception {
        URL url = fileURL("geojson_mixed_feautre_types.geojson");
        controller.toGeoJsonFromURL(response, url, null, null);

        String responseBody = response.getContentAsString();
        assertEquals("application/json", response.getContentType());
        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, "true", jsonresponse.get("success"));
        assertEquals(responseBody, 200, response.getStatus());
        assertFeatureCollection(jsonresponse, "EPSG:4326", 2);
    }

    public @Test void testUploadGeoJSONReproject() throws Exception {
        URL url = fileURL("geojson_mixed_feautre_types.geojson");
        controller.toGeoJsonFromURL(response, url, "EPSG:3857", null);

        String responseBody = response.getContentAsString();
        assertEquals("application/json", response.getContentType());
        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, "true", jsonresponse.get("success"));
        assertEquals(responseBody, 200, response.getStatus());
        assertFeatureCollection(jsonresponse, "EPSG:3857", 2);
    }

    public @Test void testUploadKML22FromURL() throws Exception {
        URL url = fileURL("recettage.kml");
        controller.toGeoJsonFromURL(response, url, null, null);

        String responseBody = response.getContentAsString();
        assertEquals("application/json", response.getContentType());
        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, "true", jsonresponse.get("success"));
        assertEquals(responseBody, 200, response.getStatus());
        assertFeatureCollection(jsonresponse, null, 5);
    }

    public @Test void testUploadKML22FromURLReproject() throws Exception {
        URL url = fileURL("recettage.kml");
        controller.toGeoJsonFromURL(response, url, "EPSG:3857", null);

        String responseBody = response.getContentAsString();
        System.err.println(responseBody);
        assertEquals("application/json", response.getContentType());
        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, "true", jsonresponse.get("success"));
        assertEquals(responseBody, 200, response.getStatus());
        assertFeatureCollection(jsonresponse, "EPSG:3857", 5);
    }

    public @Test void testUploadKML21FromURL() throws Exception {
        URL url = fileURL("recettage_v21.kml");
        controller.toGeoJsonFromURL(response, url, null, null);

        String responseBody = response.getContentAsString();
        assertEquals("application/json", response.getContentType());
        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, "true", jsonresponse.get("success"));
        assertEquals(responseBody, 200, response.getStatus());
        assertFeatureCollection(jsonresponse, null, 5);
    }

    public @Test void testResponseContentTypeWorkaround() throws Exception {
        URL url = fileURL("recettage.kml");
        HttpHeaders headers = new HttpHeaders();
        headers.put("Accept", Arrays.asList("text/html", "application/xhtml+xml", "application/xml;q=0.9", "image/webp",
                "image/apng", "*/*;q=0.8", "application/signed-exchange;v=b3"));
        
        controller.toGeoJsonFromURL(response, url, null, headers);

        assertEquals("text/html", response.getContentType());
        String responseBody = response.getContentAsString();
        JSONObject jsonresponse = (JSONObject) new JSONParser().parse(responseBody);
        assertEquals(responseBody, "true", jsonresponse.get("success"));
        assertEquals(responseBody, 200, response.getStatus());
        assertFeatureCollection(jsonresponse, null, 5);
    }

    private void assertFeatureCollection(JSONObject response, String expectedSRS, int expectedFeatureCount) {
        JSONObject featureCollection = (JSONObject) response.get("geojson");
        assertNotNull(response.toString(), featureCollection);
        assertEquals("FeatureCollection", featureCollection.get("type"));
        if (expectedSRS != null) {
            JSONObject crs = (JSONObject) featureCollection.get("crs");
            assertNotNull(crs);
            assertEquals(response.toString(), expectedSRS, ((JSONObject) crs.get("properties")).get("name"));
        }
        JSONArray features = (JSONArray) featureCollection.get("features");
        if (expectedFeatureCount > 0) {
            assertNotNull(features);
            assertEquals(expectedFeatureCount, features.size());
        }
    }

    private URL fileURL(String fileName) throws Exception {
        Class<?> base = GeotoolsFeatureReader.class;
        try (InputStream in = base.getResourceAsStream(fileName)) {
            assertNotNull(in);// would've thrown an NPE if the resource does not exist
        }
        URL url = base.getResource(fileName);
        return url;
    }
}
