package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.ReflectionUtils;

public class AddonControllerTest {

	public static @BeforeClass void init(){
		BasicConfigurator.configure();
	}
	
    @Test
    public void testConstructAddonsSpec() throws Exception {
        AddonController ac = new AddonController();
        String resDir = new File(this.getClass().getResource(".").toURI()).getPath();
        GeorchestraConfiguration gc = Mockito.mock(GeorchestraConfiguration.class);
        Mockito.when(gc.activated()).thenReturn(true);
        Mockito.when(gc.getContextDataDir()).thenReturn(resDir);
        ac.setGeorchestraConfiguration(gc);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        // for the tests, internal webapp directory and georchestra.datadir points
        // to the same directory
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(resDir);
        ac.setServletContext(servletContext);

        JSONArray ret = ac.constructAddonsSpec();

        boolean id0 =  (ret.getJSONObject(0).getString("id").equals("magnifier_0") ||
                ret.getJSONObject(0).getString("id").equals("annotation_0"));
        boolean id1 =  (ret.getJSONObject(1).getString("id").equals("magnifier_0") ||
                ret.getJSONObject(1).getString("id").equals("annotation_0"));

        assertTrue("Expected 2 elements, found " + ret.length(), ret.length() == 2);
        assertTrue("Unexpected key: " + ret.getJSONObject(0).getString("id"), id0);
        assertTrue("Unexpected key: " + ret.getJSONObject(1).getString("id"), id1);
    }

    @Test
    public void testGetAddonFileLFINoDatadir() throws Exception {
        // No datadir, testing avoid local file inclusion abuses
        AddonController ac = new AddonController();
        String resDir = new File(this.getClass().getResource(".").toURI()).getPath();
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(resDir);
        ac.setServletContext(servletContext);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // Gets back outside of /, FilenameUtils.normalize() will return null (hence the +2)
        int relPathToGetBack = StringUtils.countMatches(resDir, File.separator);
        String pathToEtcPasswd = StringUtils.repeat("../", relPathToGetBack + 2) + "etc/passwd";
        req.setPathInfo("/addons/" + pathToEtcPasswd);        
        ac.getAddonFile(req, resp);

        assertTrue("Expected bad request (400), received " + resp.getStatus(),
                resp.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
        
        // same but with a path pointing exactly to /etc/passwd
        relPathToGetBack = StringUtils.countMatches(resDir, File.separator);
        pathToEtcPasswd = StringUtils.repeat("../", relPathToGetBack) + "etc/passwd";
       
        req.setPathInfo("/addons/" + pathToEtcPasswd);
        
        ac.getAddonFile(req, resp);

        assertTrue("Expected bad request (400), received " + resp.getStatus(),
                resp.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testGetAddonFileNotFoundNoDatadir() throws Exception {
        // No datadir, testing legit file which does not exist
        AddonController ac = new AddonController();
        String resDir = new File(this.getClass().getResource(".").toURI()).getPath();
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(resDir);
        ac.setServletContext(servletContext);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        req.setPathInfo("/addons/notExistingFile");
        
        ac.getAddonFile(req, resp);

        assertTrue("Expected file not found (404), received " + resp.getStatus(),
                resp.getStatus() == HttpServletResponse.SC_NOT_FOUND);        
    }
    
    @Test
    public void testGetAddonNoDatadir() throws Exception {
        // No datadir, testing legit file which actually exists
        AddonController ac = new AddonController();
        String resDir = new File(this.getClass().getResource(".").toURI()).getPath();
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(resDir + "/addons");
        ac.setServletContext(servletContext);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        req.setPathInfo("/addons/annotation/config.json");
        
        ac.getAddonFile(req, resp);

        assertTrue("Expected status code OK (200), received " + resp.getStatus(),
                resp.getStatus() == HttpServletResponse.SC_OK);
        assertTrue("Expected config.json containing \"annotation_0\", not found in the response sent.",
                resp.getContentAsString().contains("annotation_0"));
    }

    @Test
    public void testUnexistingDatadir() throws Exception {
        File nonExistingPath = new File("/this/path/does/not/exist");
        assumeTrue(! nonExistingPath.exists());
        AddonController ac = new AddonController();
        Method m = ReflectionUtils.findMethod(ac.getClass(), "buildAddonSpecs", String.class);
        m.setAccessible(true);

        JSONArray ret = (JSONArray) ReflectionUtils.invokeMethod(m, ac, nonExistingPath.toString());

        assertTrue("Expected to get an empty array", ret.length() == 0);

    }
}
