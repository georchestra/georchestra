package org.georchestra.security;

import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHttpResponse;
import org.georchestra.security.permissions.Permissions;
import org.georchestra.security.permissions.UriMatcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProxyTest {
    private Proxy proxy;
    private BasicHttpResponse response;
    private boolean executed = true;
    private MockHttpServletRequest request;
    private MockHttpServletResponse httpResponse;

    @Before
    public void setUp() throws Exception {
        response = null;
        executed = false;
        proxy = new Proxy() {
            @Override
            protected HttpResponse executeHttpRequest(HttpClient httpclient, HttpRequestBase proxyingRequest) throws IOException {
                executed = true;
                return response;
            }
        };
        proxy.setProxyPermissions(new Permissions().
                setAllowed(Collections.singletonList(new UriMatcher().setHost("localhost"))).
                setDenied(Collections.singletonList(new UriMatcher().setHost("google.com"))));

        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        this.request = new MockHttpServletRequest("GET", "/sec/proxy/");
        this.request.setServerName("localhost");
        this.request.setServerPort(80);
        this.httpResponse = new MockHttpServletResponse();

        Map<String, String> targets = Maps.newHashMap();
        targets.put("geonetwork", "http://www.google.com/geonetwork-private");
        targets.put("extractorapp", "http://localhost/extractorapp-private");
        proxy.setTargets(targets);

    }

    @Test
    public void testGetUrlLegalUrl() throws Exception {
        proxy.handleUrlGETRequest(request, httpResponse, "http://localhost:8080/path");
        assertTrue(executed);
    }

    /**
     * All url proxy requests (http://server/sec/proxy?url=...) that try to directly access protected services like extractorapp should
     * be rejected.  In the cases where the protected services are required, the url parameter should be the public url of the
     * protected service (not the private one).
     */
    @Test
    public void testGetUrlTarget() throws Exception {
        proxy.handleUrlGETRequest(request, httpResponse, "http://localhost:8080/geonetwork-private");
        assertFalse(executed);

        this.httpResponse = new MockHttpServletResponse();
        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        proxy.handleUrlGETRequest(request, httpResponse, "http://localhost:8080/extractorapp");
        assertTrue(executed);
    }

    /**
     * Ensure that when the proxy form: http://server.com/sec/geonetwork/srv/eng/home is used to access a protected service,
     * the request should always be allowed no matter the proxy permissions.
     */
    @Test
    public void testGetUrlLegalUrlButTarget() throws Exception {
        request = new MockHttpServletRequest("GET", "http://localhost:8080/geonetwork-private");
        proxy.handleGETRequest(request, httpResponse);
        assertFalse(executed);

        this.httpResponse = new MockHttpServletResponse();
        request = new MockHttpServletRequest("GET", "/geonetwork/srv/eng/something");
        proxy.handleGETRequest(request, httpResponse);
        assertTrue(executed);

        this.httpResponse = new MockHttpServletResponse();
        executed = false;
        request = new MockHttpServletRequest("GET", "/extractorapp/home");
        proxy.handleGETRequest(request, httpResponse);
        assertTrue(executed);

        this.httpResponse = new MockHttpServletResponse();
        executed = false;
        request = new MockHttpServletRequest("GET", "/unmapped/x");
        proxy.handleGETRequest(request, httpResponse);
        assertFalse(executed);
    }

    @Test
    public void testGetUrlIllegalUrl() throws Exception {
        proxy.handleUrlGETRequest(request, httpResponse, "http://www.google.com:8080/path");
        assertFalse(executed);
    }

    @Test
    public void testLoadPermissions() throws Exception {
        proxy = new Proxy();
        proxy.setProxyPermissionsFile("test-permissions.xml");
        proxy.init();

        final Permissions proxyPermissions = proxy.getProxyPermissions();
        assertTrue(proxyPermissions.isAllowByDefault());
        assertEquals(1, proxyPermissions.getAllowed().size());
        assertEquals(1, proxyPermissions.getDenied().size());

        assertEquals("localhost", proxyPermissions.getAllowed().get(0).getHost());
        assertEquals(-1, proxyPermissions.getAllowed().get(0).getPort());
        assertEquals(null, proxyPermissions.getAllowed().get(0).getPath());

        assertEquals("localhost", proxyPermissions.getDenied().get(0).getHost());
        assertEquals(433, proxyPermissions.getDenied().get(0).getPort());
        assertEquals("/geonetwork/", proxyPermissions.getDenied().get(0).getPath());

        assertTrue(proxyPermissions.isInitialized());
    }

    @Test
    public void testLoadEmptyPermissions() throws Exception {
        proxy = new Proxy();
        proxy.setProxyPermissionsFile("empty-permissions.xml");
        proxy.init();

        // no exception? good
    }
}