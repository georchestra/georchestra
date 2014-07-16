package org.geowebcache.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeorchestraURLManglerTest {

    @Test
    public void testGeorchestraURLMangler() {
        GeorchestraURLMangler gum = new GeorchestraURLMangler("http://sdi.georchestra.org", "geowebcache");

        String finalUrl = gum.buildURL("http://localhost:8080", "geowebcache-private", "/service/wmts");
        assertTrue(finalUrl.equals("http://sdi.georchestra.org/geowebcache/service/wmts"));

        // a little fuzzier case
        gum = new GeorchestraURLMangler(null, null);
        finalUrl = gum.buildURL("http://localhost:8080////", "/geowebcache-private/", "///service/wmts///");
        assertTrue(finalUrl.equals("null/null/service/wmts///"));


        // Testing empty strings
        gum = new GeorchestraURLMangler("", "");
        finalUrl = gum.buildURL("http://localhost:8080////", "/geowebcache-private/", "///service/wmts///");
        assertTrue(finalUrl.equals("//service/wmts///"));

        // Tests stripping of the parameters
        gum = new GeorchestraURLMangler("////https://sdi.georchestra.org:8443////", "////geo/web/cache/////");
        finalUrl = gum.buildURL("http://localhost:8080////", "/geowebcache-private/", "///service/wmts///");
        assertTrue(finalUrl.equals("https://sdi.georchestra.org:8443/geo/web/cache/service/wmts///"));
    }
}
