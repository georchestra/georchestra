package org.georchestra.security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class ProxyTest {
    private Proxy proxy;
    private BasicHttpResponse response;
    private boolean executed = true;
    private HttpRequestBase builtRequest;
    private MockHttpServletRequest request;
    private MockHttpServletResponse httpResponse;

    private Map<String, String> targets;

    @Before
    public void setUp() throws Exception {
        response = null;
        executed = false;
        proxy = new Proxy() {
            @Override
            protected HttpResponse executeHttpRequest(CloseableHttpAsyncClient httpclient,
                    HttpRequestBase proxyingRequest) throws IOException {
                executed = true;
                return response;
            }

            protected @Override HttpRequestBase makeRequest(HttpServletRequest request, String sURL)
                    throws IOException {
                HttpRequestBase req = super.makeRequest(request, sURL);
                builtRequest = req;
                return req;
            }
        };
        DataSource ogcStatsDataSource = Mockito.mock(DataSource.class);
        proxy.setOgcStatsDataSource(ogcStatsDataSource);
        proxy.setProxyPermissionsFile("default-permissions.xml");
        proxy.init();

        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        this.request = new MockHttpServletRequest("GET", "/proxy/");
        this.request.setServerName("localhost");
        this.request.setServerPort(80);
        this.httpResponse = new MockHttpServletResponse();

        targets = Maps.newHashMap();
        targets.put("geonetwork", "http://www.google.com/geonetwork-private");
        targets.put("extractorapp", "http://localhost/extractorapp-private");
        targets.put("nextcloud", "http://localhost/nextcloud");
        proxy.setTargets(targets);

        proxy.setDefaultTarget("/mapfishapp/");

    }

    @Test
    public void testGetUrlLegalUrl() throws Exception {
        proxy.handleUrlParamRequest(request, httpResponse, "http://localhost:8080/path");
        assertTrue(executed);
    }

    /**
     * All url proxy requests (http://server/proxy?url=...) that try to directly
     * access protected services like extractorapp should be rejected. In the cases
     * where the protected services are required, the url parameter should be the
     * public url of the protected service (not the private one).
     */
    @Test
    public void testGetUrlTarget() throws Exception {
        proxy.handleUrlParamRequest(request, httpResponse, "http://localhost:8080/geonetwork-private");
        assertFalse(executed);

        this.httpResponse = new MockHttpServletResponse();
        proxy.handleUrlParamRequest(request, httpResponse, "http://localhost:8080/extractorapp");
        assertFalse(executed);
    }

    /**
     * Ensure that when the proxy form: http://server.com/geonetwork/srv/eng/home is
     * used to access a protected service, the request should always be allowed no
     * matter the proxy permissions.
     */
    @Test
    public void testGetUrlLegalUrlButTarget() throws Exception {
        request = new MockHttpServletRequest("GET", "http://localhost:8080/geonetwork-private");
        proxy.handleRequest(request, httpResponse);
        assertFalse(executed);

        this.httpResponse = new MockHttpServletResponse();
        request = new MockHttpServletRequest("GET", "/geonetwork/srv/eng/something");
        proxy.handleRequest(request, httpResponse);
        assertTrue(executed);

        this.httpResponse = new MockHttpServletResponse();
        executed = false;
        request = new MockHttpServletRequest("GET", "/extractorapp/home");
        proxy.handleRequest(request, httpResponse);
        assertTrue(executed);

        this.httpResponse = new MockHttpServletResponse();
        executed = false;
        request = new MockHttpServletRequest("GET", "/unmapped/x");
        proxy.handleRequest(request, httpResponse);
        assertFalse(executed);
    }

    @Test
    public void testDefaultTarget() throws Exception {
        request = new MockHttpServletRequest("GET", "http://localhost:8080/");
        proxy.handleDefaultRequest(request, httpResponse);

        assertTrue(httpResponse.getRedirectedUrl().equals("/mapfishapp/"));

    }

    @Test
    public void testGetUrlIllegalUrl() throws Exception {
        proxy.handleUrlParamRequest(request, httpResponse, "http://www.google.com:8080/path");
        assertFalse(executed);
    }

    @Test
    public void testBuildUri() throws Exception {
        Proxy proxy = new Proxy();

        URI ret = proxy.buildUri("https://dev.pigma.org/geonetwork/srv/fre/xml.keyword.get?"
                + "thesaurus=local.theme.pigma&id=http://ids.pigma.org/themes%23Eau&multiple=false&transformation=to-iso19139-keyword");
        assertTrue(ret.toString().contains("id=http://ids.pigma.org/themes%23Eau"));

        ret = proxy
                .buildUri("https://sdi.georchestra.org/console/account/recover?email=psc%2Btestuser%40georchestra.org");
        assertTrue(ret.toString().contains("email=psc%2Btestuser%40georchestra.org"));

        // defunct URLs:
        // http://localhost:8080/geonetwork/srv/api/0.1/standards/iso19139/codelists/gmd%3ADS_InitiativeTypeCode
        // SP will re-encode %3A to %253A
        // see
        // https://github.com/georchestra/georchestra/issues/2020#issuecomment-402449307
        ret = proxy.buildUri(
                "http://localhost:8080/geonetwork/srv/api/0.1/standards/iso19139/codelists/gmd%3ADS_InitiativeTypeCode");
        assertTrue(!ret.toString().contains("%253ADS"));
    }

    @Test
    public void testRedirectPassThru() throws Exception {
        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.FOUND.value(), "Temporarily moved");
        final String expected = "http://acme.com";
        response.setHeader("Location", expected);

        proxy.handleUrlParamRequest(request, httpResponse, "http://localhost:8080/path");
        assertTrue(executed);
        assertEquals(HttpStatus.FOUND.value(), httpResponse.getStatus());
        assertTrue(httpResponse.getHeaderNames().contains("Location"));
        List<String> values = httpResponse.getHeaders("Location");
        assertEquals("Location header should have a single value, got: " + values, 1, values.size());
        assertEquals(expected, values.get(0));
    }

    @Test
    public void testMovedPermanentlyPassThru() throws Exception {
        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.MOVED_PERMANENTLY.value(),
                "Temporarily moved");
        final String expected = "http://acme.com";
        response.setHeader("Location", expected);

        proxy.handleUrlParamRequest(request, httpResponse, "http://localhost:8080/path");
        assertTrue(executed);
        assertEquals(HttpStatus.MOVED_PERMANENTLY.value(), httpResponse.getStatus());
        assertTrue(httpResponse.getHeaderNames().contains("Location"));
        List<String> values = httpResponse.getHeaders("Location");
        assertEquals("Location header should have a single value, got: " + values, 1, values.size());
        assertEquals(expected, values.get(0));
    }

    @Test
    public void testRedirectLocationIsProxified() throws Exception {
        // when calling /geonetwork/somepath...
        request = new MockHttpServletRequest("GET", "/geonetwork/somepath");
        // then server returns a redirect to
        // "http://www.google.com/geonetwork-private/privatepath?param1=A&param2=B"
        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.FOUND.value(), "Temporarily moved");
        response.setHeader("Location", "http://www.google.com/geonetwork-private/privatepath?param1=A&param2=B");

        // and proxy should override the Location response header as instructed in the
        // mappings
        final String expected = "/geonetwork/privatepath?param1=A&param2=B";

        proxy.handleUrlParamRequest(request, httpResponse, "http://localhost:8080/path");
        assertTrue(executed);
        assertEquals(HttpStatus.FOUND.value(), httpResponse.getStatus());
        assertTrue(httpResponse.getHeaderNames().contains("Location"));
        List<String> values = httpResponse.getHeaders("Location");
        assertEquals("Location header should have a single value, got: " + values, 1, values.size());
        assertEquals(expected, values.get(0));
    }

    @Test
    public void filterOne() {
        Proxy toTest = new Proxy();

        String[] filtered = toTest.filter(new String[] { "", "here", "", "the", "", "fish" });

        assertArrayEquals(new String[] { "here", "the", "fish" }, filtered);
    }

    @Test
    public void isRecursiveCallToProxy() {
        Proxy toTest = new Proxy();

        assertFalse(toTest.isRecursiveCallToProxy("/a/b/c", "/a/b/c/d"));
        assertFalse(toTest.isRecursiveCallToProxy("/a/b/c", "/a/b/d"));
        assertFalse(toTest.isRecursiveCallToProxy("", "a"));
        assertTrue(toTest.isRecursiveCallToProxy("/a/b/c/d", ""));
        assertTrue(toTest.isRecursiveCallToProxy("a/b/c", "/a/b/c"));
        assertTrue(toTest.isRecursiveCallToProxy("a/b/c/d", "/a/b/c"));
    }

    @Test
    public void charsetForName() {
        assertEquals("US-ASCII", Charset.forName("us-ascii").displayName());

        boolean thrown = false;
        try {
            Charset dummy = Charset.forName(null);
        } catch (Throwable t) {
            thrown = true;
        }

        assertTrue(thrown);

        thrown = false;
        try {
            Charset dummy = Charset.forName("momo");
        } catch (Throwable t) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testVerb() {

        for (RequestMethod e : RequestMethod.values()) {
            request = new MockHttpServletRequest(e.toString(), "/extractorapp/home");
            response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.OK.value(), e.toString() + " worked");
            response.setHeader("X-Test-Header", e.toString() + " worked");
            httpResponse = new MockHttpServletResponse();

            proxy.handleRequest(request, httpResponse);

            assertEquals(HttpStatus.OK.value(), httpResponse.getStatus());
            assertEquals(e.toString() + " worked", httpResponse.getHeader("X-Test-Header"));
        }
    }

    @Test
    public void testRequestEntityVerbs() throws Exception {
        testRequestEntity(RequestMethod.POST);
        testRequestEntity(RequestMethod.PUT);
        testRequestEntity(RequestMethod.PATCH);
    }

    @Test
    public void testUserDetails() throws Exception {
        Authentication auth = new AnonymousAuthenticationToken("anonymous", "anonymousUser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        final String rawResponseBody = "{\"roles\":[\"ROLE_ANONYMOUS\"],\"username\":\"anonymousUser\"}";

        request = new MockHttpServletRequest(RequestMethod.GET.toString(), "/userdetails");
        String content = proxy.getUserDetails(request);

        assertEquals(rawResponseBody, content);
    }

    private void testRequestEntity(RequestMethod method) throws Exception {
        executed = false;
        builtRequest = null;

        final String rawRequestBody = "{\"payload\": \"test\"}";
        final String rawResponseBody = "{\"response\": \"success\"}";

        request = new MockHttpServletRequest(method.toString(), "/extractorapp/home");
        request.setContentType("application/json");
        request.setContent(rawRequestBody.getBytes(StandardCharsets.UTF_8));

        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.OK.value(), method.toString() + " worked");
        response.setHeader("Content-Type", "application/json");
        response.setEntity(
                new ByteArrayEntity(rawResponseBody.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON));

        httpResponse = new MockHttpServletResponse();

        proxy.handleRequest(request, httpResponse);
        assertTrue(executed);
        assertNotNull(builtRequest);

        assertEquals(HttpStatus.OK.value(), httpResponse.getStatus());
        assertEquals("application/json", httpResponse.getHeader("Content-Type"));
        String content = httpResponse.getContentAsString();
        assertEquals(rawResponseBody, content);

        assertTrue(builtRequest instanceof HttpEntityEnclosingRequestBase);
        HttpEntityEnclosingRequestBase tr = (HttpEntityEnclosingRequestBase) builtRequest;
        HttpEntity entity = tr.getEntity();
        assertNotNull(entity);
        assertTrue(entity.getContentLength() > 0);
        byte[] responseArray = ByteStreams.toByteArray(entity.getContent());
        assertEquals(rawRequestBody, new String(responseArray));
    }

    @Test
    public void testWebDavVerb() {

        for (String webDavVerb : Arrays.asList("PROPFIND", "SEARCH")) {
            request = new MockHttpServletRequest(webDavVerb, "/nextcloud/plop");
            response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.OK.value(), webDavVerb + " worked");
            response.setHeader("X-Test-Header", webDavVerb + " worked");
            httpResponse = new MockHttpServletResponse();

            proxy.handleRequest(request, httpResponse);

            assertEquals(HttpStatus.OK.value(), httpResponse.getStatus());
            assertEquals(webDavVerb + " worked", httpResponse.getHeader("X-Test-Header"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingVerb() {
        request = new MockHttpServletRequest("NONSTANDARDVERB", "/nextcloud/plop");
        response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.OK.value(), "NONSTANDARDVERB worked");
        response.setHeader("X-Test-Header", "NONSTANDARDVERB worked");
        httpResponse = new MockHttpServletResponse();

        proxy.handleRequest(request, httpResponse);

    }
}
