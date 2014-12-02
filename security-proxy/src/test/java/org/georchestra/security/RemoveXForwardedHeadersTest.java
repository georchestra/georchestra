package org.georchestra.security;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class RemoveXForwardedHeadersTest extends TestCase {

    @Test
    public void testFilterIncludes() throws Exception {
        final RemoveXForwardedHeaders headers = new RemoveXForwardedHeaders();
        headers.checkConfiguration();

        headers.setIncludes(Lists.newArrayList(".*.service=wms.*"));

        headers.checkConfiguration();

        assertFalse(headers.filter(RemoveXForwardedHeaders.HOST,
                new MockHttpServletRequest("GET", "extractorapp"), null));
        assertFalse(headers.filter(RemoveXForwardedHeaders.FOR,
                new MockHttpServletRequest("GET", "extractorapp"), null));
        assertFalse(headers.filter("header",
                new MockHttpServletRequest("GET", "extractorapp"), null));
        assertFalse(headers.filter("header",
                new MockHttpServletRequest("GET", "geoserver/wms?SERVICE=WMS"), null));
        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST,
                new MockHttpServletRequest("GET", "geoserver/wms?SERVICE=WMS"), null));
        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST,
                new MockHttpServletRequest("GET", "geoserver/wms?Service=wms"), null));
        assertTrue(headers.filter(RemoveXForwardedHeaders.FOR,
                new MockHttpServletRequest("GET", "geoserver/wms?Service=wms"), null));
    }

    @Test
    public void testFilterExcludes() throws Exception {
        final RemoveXForwardedHeaders headers = new RemoveXForwardedHeaders();
        headers.checkConfiguration();

        headers.setExcludes(Lists.newArrayList(".*.service=wms.*"));

        headers.checkConfiguration();

        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST,
                new MockHttpServletRequest("GET", "extractorapp"), null));
        assertTrue(headers.filter(RemoveXForwardedHeaders.FOR,
                new MockHttpServletRequest("GET", "extractorapp"), null));
        assertFalse(headers.filter("header",
                new MockHttpServletRequest("GET", "extractorapp"), null));
        assertFalse(headers.filter("header",
                new MockHttpServletRequest("GET", "geoserver/wms?SERVICE=WMS"), null));
        assertFalse(headers.filter(RemoveXForwardedHeaders.HOST,
                new MockHttpServletRequest("GET", "geoserver/wms?SERVICE=WMS"), null));
        assertFalse(headers.filter(RemoveXForwardedHeaders.HOST,
                new MockHttpServletRequest("GET", "geoserver/wms?Service=wms"), null));
        assertFalse(headers.filter(RemoveXForwardedHeaders.FOR,
                new MockHttpServletRequest("GET", "geoserver/wms?Service=wms"), null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBothIncludeAndExcludeSet() throws Exception {
        final RemoveXForwardedHeaders headers = new RemoveXForwardedHeaders();
        headers.setIncludes(Lists.newArrayList(".*.service=wfs.*"));
        headers.setExcludes(Lists.newArrayList(".*.service=wms.*"));
        try {
            headers.checkConfiguration();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // good
        }
    }
}