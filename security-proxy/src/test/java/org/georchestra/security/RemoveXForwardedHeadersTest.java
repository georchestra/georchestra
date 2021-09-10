package org.georchestra.security;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;

import com.google.common.collect.Lists;

import junit.framework.TestCase;

public class RemoveXForwardedHeadersTest extends TestCase {

    @Test
    public void testFilterIncludes() throws Exception {
        final RemoveXForwardedHeaders headers = new RemoveXForwardedHeaders();
        headers.checkConfiguration();

        headers.setIncludes(Lists.newArrayList(".*.service=wms.*"));

        headers.checkConfiguration();

        assertFalse(headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("extractorapp")));
        assertFalse(headers.filter(RemoveXForwardedHeaders.FOR, null, createProxyRequest("extractorapp")));
        assertFalse(headers.filter("header", null, createProxyRequest("extractorapp")));
        assertFalse(headers.filter("header", null, createProxyRequest("geoserver/wms?SERVICE=WMS")));
        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("geoserver/wms?SERVICE=WMS")));
        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("geoserver/wms?Service=wms")));
        assertTrue(headers.filter(RemoveXForwardedHeaders.FOR, null, createProxyRequest("geoserver/wms?Service=wms")));
    }

    @Test
    public void testMultipleFilterIncludes() throws Exception {
        final RemoveXForwardedHeaders headers = new RemoveXForwardedHeaders();
        headers.setIncludes(Lists.newArrayList(".*geo.admin.ch.*,.*rolnp.fr.*"));
        assertEquals(2, headers.getIncludes().size());
        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST, null,
                createProxyRequest("wmts100.geo.admin.ch", "extractorapp")));

        assertFalse(
                headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("example.com", "extractorapp")));
    }

    @Test
    public void testFilterExcludes() throws Exception {
        final RemoveXForwardedHeaders headers = new RemoveXForwardedHeaders();
        headers.checkConfiguration();

        headers.setExcludes(Lists.newArrayList(".*.service=wms.*"));

        headers.checkConfiguration();

        assertTrue(headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("extractorapp")));
        assertTrue(headers.filter(RemoveXForwardedHeaders.FOR, null, createProxyRequest("extractorapp")));
        assertFalse(headers.filter("header", null, createProxyRequest("extractorapp")));
        assertFalse(headers.filter("header", null, createProxyRequest("geoserver/wms?SERVICE=WMS")));
        assertFalse(
                headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("geoserver/wms?SERVICE=WMS")));
        assertFalse(
                headers.filter(RemoveXForwardedHeaders.HOST, null, createProxyRequest("geoserver/wms?Service=wms")));
        assertFalse(headers.filter(RemoveXForwardedHeaders.FOR, null, createProxyRequest("geoserver/wms?Service=wms")));
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

    private HttpRequestBase createProxyRequest(String uriFragment) throws URISyntaxException {
        return createProxyRequest("http://localhost:8080/", uriFragment);
    }

    private HttpRequestBase createProxyRequest(String uri, String uriFragment) throws URISyntaxException {
        return new HttpGet(new URI(uri + uriFragment));
    }
}