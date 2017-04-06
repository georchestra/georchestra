package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URISyntaxException;
import java.net.URL;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;
import org.xml.sax.SAXParseException;

public class ContextControllerTest {

    @Test
    public void testGetXmlInfo() throws Exception {

        URL defaultWmc = this.getClass().getResource("/default.wmc");
        File defaultWmcF = new File(defaultWmc.toURI());

        assertTrue("resource test default.wmc not found", defaultWmcF.exists());

        ContextController ctxCtrl = new ContextController();

        Method prvMethod = ctxCtrl.getClass().getDeclaredMethod("getXmlInfos", File.class);
        prvMethod.setAccessible(true);
        Object ret = ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, defaultWmcF);

        assertTrue("Returned object is not a JSONObject", ret instanceof JSONObject);

        JSONObject jsRet = (JSONObject) ret;
        assertTrue("Unexpected label", jsRet.get("label").equals("Default context (OSM Géobretagne)"));
        assertTrue("Unexpected tip", jsRet.get("tip").equals("This is the default context provided for geOrchestra, loading a layer "
                                                            +"kindly provided by GéoBretagne, data issued from OpenStreetMap and contributors"));
        assertTrue("Unexpected keywords: does not contain \"OSM\"", jsRet.getJSONArray("keywords").toString().contains("OSM"));
        assertTrue("Unexpected keywords: does not contain \"Géobretagne\"", jsRet.getJSONArray("keywords").toString().contains("Géobretagne"));

    }

    @Test
    public void testgetRolesForContext() throws NoSuchMethodException, SecurityException, URISyntaxException {
        URL defaultCtxUrl = this.getClass().getResource("/default.wmc");
        assumeNotNull(new Object[] {defaultCtxUrl});

        ContextController ctxCtrl = new ContextController();
        Method prvMethod = ctxCtrl.getClass().getDeclaredMethod("getRolesForContext", String.class, String.class);
        prvMethod.setAccessible(true);

        File resourceDir = new File(this.getClass().getResource("/").toURI());

        Object ret = ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, resourceDir.getPath(), "default");
        assertTrue(((JSONArray) ret).length() == 2);
        assertTrue(((JSONArray) ret).toString().contains("ROLE_ADMINISTRATOR"));
    }

    @Test
    public void testParseRoleXmlFile() throws URISyntaxException, NoSuchMethodException, SecurityException {
        URL u1 = this.getClass().getResource("roles/several_roles.xml");
        URL u2 = this.getClass().getResource("roles/one_role.xml");
        URL u3 = this.getClass().getResource("roles/empty_role.xml");
        assumeNotNull(new Object[] { u1, u2, u3 });

        File f1 = new File(u1.toURI());
        File f2 = new File(u2.toURI());
        File f3 = new File(u3.toURI());

        ContextController ctxCtrl = new ContextController();
        Method prvMethod = ctxCtrl.getClass().getDeclaredMethod("parseRoleXmlFile", File.class);
        prvMethod.setAccessible(true);

        Object ret = ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, f1);
        assertTrue(((JSONArray) ret).length() == 4);
        assertTrue(((JSONArray) ret).toString().contains("ROLE_ADMINISTRATOR"));

        ret = ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, f2);
        assertTrue(((JSONArray) ret).length() == 1);

        ret = ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, f3);
        assertTrue(((JSONArray) ret).length() == 0);
    }

    @Test
    public void testGetContextInfo() throws Exception {
        URL defaultWmc = this.getClass().getResource("/default.wmc");
        File defaultWmcF = new File(defaultWmc.toURI());

        assertTrue("resource test default.wmc not found", defaultWmcF.exists());

        ContextController ctxCtrl = new ContextController();

        Method prvMethod = ctxCtrl.getClass().getDeclaredMethod("getContextInfo", File.class);
        prvMethod.setAccessible(true);
        Object ret = ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, defaultWmcF);

        assertTrue("Returned object is not a JSONObject", ret instanceof JSONObject);
        JSONObject jsRet = (JSONObject) ret;

        assertTrue("Unexpected thumbnail", jsRet.get("thumbnail").equals("context/image/default.png"));
        assertTrue("Unexpected wmc", jsRet.get("wmc").equals("context/default.wmc"));

    }

    @Test(expected=SAXParseException.class)
    public void testGetContextInfoInvalidWmc() throws Throwable {
        File invalidWmc = new File(this.getClass().getResource("/default-invalid-doc.wmc").toURI());

        ContextController ctxCtrl = new ContextController();
        Method prvMethod = ctxCtrl.getClass().getDeclaredMethod("getContextInfo", File.class);
        prvMethod.setAccessible(true);

        try {
            ReflectionUtils.invokeMethod(prvMethod, ctxCtrl, invalidWmc);
        } catch (UndeclaredThrowableException e) {
            throw e.getUndeclaredThrowable();
        }
    }

    /**
     * This test ensures that the retrieved contexts are correctly sorted in alphabetical order.
     *
     * @throws Exception
     */
    @Test
    public void testGetContexts() throws Exception {
        URL testPathUrl = this.getClass().getResource(".");
        assumeTrue("testPathUrl does not exist, skipping test", testPathUrl != null);
        GeorchestraConfiguration georConfig = Mockito.mock(GeorchestraConfiguration.class);
        String testPath = new File(testPathUrl.toURI()).toString();
        Mockito.when(georConfig.getContextDataDir()).thenReturn(testPath);

        ContextController ctxCtrl = new ContextController();
        ctxCtrl.setGeorchestraConfiguration(georConfig);
        JSONArray ret = ctxCtrl.getContexts();

        assertTrue(ret.getJSONObject(0).getString("label").equals("2.wmc"));
        assertTrue(ret.getJSONObject(1).getString("label").equals("a.wmc"));
        assertTrue(ret.getJSONObject(2).getString("label").equalsIgnoreCase("Z.wmc"));
    }

    @Test
    public void testNonExistingContextDirectory() throws Exception {
        File pathNonExisting = new File("/this/path/does/not/exist/");
        assumeTrue(! pathNonExisting.exists());

        ContextController cc = new ContextController();
        GeorchestraConfiguration gc = Mockito.mock(GeorchestraConfiguration.class);
        Mockito.when(gc.getContextDataDir()).thenReturn(pathNonExisting.toString());
        Field f = ReflectionUtils.findField(cc.getClass(), "georchestraConfiguration");
        f.setAccessible(true);
        f.set(cc, gc);

        JSONArray ret = cc.getContexts();

        assertTrue("expected an empty array", ret.length() == 0);
    }


}
