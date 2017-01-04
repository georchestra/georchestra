package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;
import org.xml.sax.SAXParseException;

public class WMCDocServiceTest {


    @Test
    public void testXEEOnExtractRealFileName() {
        assumeTrue("file /etc/passwd does not exist, which is unlikely if you are running the testsuite under linux. Skipping test",
                new File("/etc/passwd").exists());

        final String xeeVuln = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                + "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n"
                +"<wmc><Title>&xxe;</Title></wmc>";


        WMCDocService wmcds = new WMCDocService("xml", null) {};

        Method erfn = ReflectionUtils.findMethod(wmcds.getClass(), "extractRealFileName", InputStream.class);
        erfn.setAccessible(true);

        String ret = (String) ReflectionUtils.invokeMethod(erfn, wmcds,
                new ByteArrayInputStream(xeeVuln.getBytes()));

        // If no resolution, filename should be null
        assertTrue("title is not null, XEE attack vulnerable ?",
               StringUtils.isEmpty(ret));
    }

    @Test
    public void testWMCParse() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/default.wmc");

        WMCDocService wmcds = new WMCDocService("xml", null);
        JSONObject res = wmcds.extractsStandardSpecificEntries(is);

        // Check title and abstract
        assertTrue("Missing or invalid title", res.getString("title").equals("Default context (OSM Géobretagne)"));
        assertTrue("Missing or invalid abstract", res.getString("abstract").equals("This is the default context provided for geOrchestra, loading a layer kindly provided by GéoBretagne, data issued from OpenStreetMap and contributors"));

        // Check keywords
        JSONArray keywords = res.getJSONArray("keywords");
        List<String> keywordsAsList = new LinkedList<String>();
        for (int i = 0; i < keywords.length(); i++)
            keywordsAsList.add(keywords.getString(i));

        assertTrue("Invalid keywords count", keywordsAsList.size() == 2);
        assertTrue("keywords does not contains 'OSM'", keywordsAsList.contains("OSM"));
        assertTrue("keywords does not contains 'Géobretagne'", keywordsAsList.contains("Géobretagne"));

        // Check srs
        assertTrue("Missing or invalid SRS", res.getString("srs").equals("EPSG:3857"));
        // bbox
        double[] bbox = {-1363722.41004360002, 5166003.89129989967, 1994613.05072530010, 6777907.07312569954};
        JSONArray rawExtractedBbox = res.getJSONArray("bbox");
        double[] extractedBbox = {rawExtractedBbox.getDouble(0), rawExtractedBbox.getDouble(1),
                rawExtractedBbox.getDouble(2), rawExtractedBbox.getDouble(3)};
        Assert.assertArrayEquals("Missing or invalid BBOX", bbox, extractedBbox, 0.00000000001);

    }

    @Test(expected=SAXParseException.class)
    public void testInvalidWMC() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/default-invalid-doc.wmc");
        WMCDocService wmcds = new WMCDocService("xml", null);
        wmcds.extractsStandardSpecificEntries(is);
    }


    @Test
    public void testNoTitle() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/default_without_title.wmc");
        WMCDocService wmcds = new WMCDocService("xml", null);
        JSONObject res = wmcds.extractsStandardSpecificEntries(is);

        // Check title and abstract
        assertTrue("Title should not appear in result", !res.has("title"));
        assertTrue("Valid abstract", res.getString("abstract").equals("This is the default context provided for geOrchestra, loading a layer kindly provided by GéoBretagne, data issued from OpenStreetMap and contributors"));
    }

    @Test(expected=NumberFormatException.class)
    public void testInvalidBBox() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/default_with_invalid_bbox.wmc");
        WMCDocService wmcds = new WMCDocService("xml", null);
        wmcds.extractsStandardSpecificEntries(is);
    }


    @Test
    public void testNoKeyword() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/default_without_keyword.wmc");
        WMCDocService wmcds = new WMCDocService("xml", null);
        JSONObject res = wmcds.extractsStandardSpecificEntries(is);

        // Check keywords
        assertTrue("Keyword should not appear in result", !res.has("keywords"));
    }

    @Test
    public void testNoKeywordList() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/default_without_keywordlist.wmc");
        WMCDocService wmcds = new WMCDocService("xml", null);
        JSONObject res = wmcds.extractsStandardSpecificEntries(is);

        // Check keywords
        assertTrue("Keyword should not appear in result", !res.has("keywords"));
    }


}
