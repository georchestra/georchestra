package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import org.json.JSONObject;
import org.junit.Test;
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


}
