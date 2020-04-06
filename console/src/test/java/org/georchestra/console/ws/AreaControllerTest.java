package org.georchestra.console.ws;

import org.apache.commons.io.FileUtils;
import org.georchestra.console.bs.ExpiredTokenCleanTask;
import org.georchestra.console.bs.ExpiredTokenManagement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

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
    public void testAreaUrlInDatadir() throws IOException {
        String datadir = tempFolder.getRoot().toString();
        final File areaJson = tempFolder.newFile("xyz.json");
        FileUtils.writeStringToFile(areaJson, "hello world", "UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", areaJson.toString());
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "hello world");
    }

    @Test
    public void testAreaUrlInDatadirConsole() throws IOException {
        String datadir = tempFolder.getRoot().toString();
        tempFolder.newFolder("console");
        final File areaJson = tempFolder.newFile("console/xyz.json");
        FileUtils.writeStringToFile(areaJson, "hello world", "UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", areaJson.toString());
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "hello world");
    }

    @Test
    public void testAreaFileDoesntExist() throws IOException {
        String datadir = tempFolder.getRoot().toString();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(ctrl, "areasUrl", "i_dont_exists.json");
        ReflectionTestUtils.setField(ctrl, "datadir", datadir);
        String ret = ctrl.serveArea(response);
        assertEquals(ret, "{\"error\": \"area.json not found\"}");
        assertEquals(response.getStatus(), 404);

    }

}
