package org.georchestra.console.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

public class AreaControllerTest {
    private AreaController ctrl;

    @TempDir
    public File tempFolder;

    @BeforeEach
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
        String datadir = tempFolder.toString();
        File areaJson = File.createTempFile("xyz.json", null, tempFolder);
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
        String datadir = tempFolder.toString();
        File areaJson = File.createTempFile("xyz.json", null, tempFolder);
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
        String datadir = tempFolder.toString();
        newFolder(tempFolder, "console");
        File areaJson = File.createTempFile("console/xyz.json", null, tempFolder);
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
        String datadir = tempFolder.toString();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", "i_dont_exists.json");
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "{\"error\": \"area.geojson not found\"}");
        assertEquals(response.getStatus(), 404);

    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
