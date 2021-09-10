package org.georchestra.console.ws;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

public class AreaControllerTest {
    private AreaController ctrl;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setupTest() {
        ctrl = new AreaController();
    }

    @Test
    public void testAreasUrlIsActuallyAUrl() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", "https://example.com");
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "");
        assertEquals(response.getRedirectedUrl(), "https://example.com");
        assertEquals(response.getStatus(), 302);
    }

    @Test
    public void testInvalidFileInDatadir() throws IOException {
        String datadir = tempFolder.getRoot().toString();
        File areaJson = tempFolder.newFile("xyz.json");
        FileUtils.writeStringToFile(areaJson, "hello world", "UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", areaJson.toString());
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "{\"error\": \"specifed file (area.geojson) could not be parsed server side\"}");
        assertEquals(response.getStatus(), 500);
    }

    @Test
    public void testAreaUrlInDatadir() throws IOException {
        JSONObject expectedJSON = new JSONObject(
                "{ \"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [125.6, 10.1]  },\"properties\": {\"name\": \"Dinagat Islands\"}}");
        String datadir = tempFolder.getRoot().toString();
        File areaJson = tempFolder.newFile("xyz.json");
        FileUtils.writeStringToFile(areaJson, expectedJSON.toString(), "UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", areaJson.toString());
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        // instead of comparing string (which might fail, order etc...). Comparing
        // objects.
        assertEquals(new JSONObject(ret).toMap(), expectedJSON.toMap());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testAreaUrlInDatadirConsole() throws IOException {
        JSONObject expectedJSON = new JSONObject(
                "{ \"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [125.6, 10.1]  },\"properties\": {\"name\": \"Dinagat Islands\"}}");
        String datadir = tempFolder.getRoot().toString();
        tempFolder.newFolder("console");
        File areaJson = tempFolder.newFile("console/xyz.json");
        FileUtils.writeStringToFile(areaJson, expectedJSON.toString(), "UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", areaJson.toString());
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(new JSONObject(ret).toMap(), expectedJSON.toMap());
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testAreaFileDoesntExist() throws IOException {
        String datadir = tempFolder.getRoot().toString();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", "i_dont_exists.json");
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "{\"error\": \"area.geojson not found\"}");
        assertEquals(response.getStatus(), 404);

    }
}
