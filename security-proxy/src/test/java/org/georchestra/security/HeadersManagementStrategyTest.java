package org.georchestra.security;

import static org.georchestra.security.HeaderNames.COOKIE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Jesse on 4/24/2014.
 */
public class HeadersManagementStrategyTest {

    /**
     * Show that by default the headers are removed
     */
    @Test
    public void testConfigureRequestHeaders_RemoveSecHeaders_DefaultBehaviour() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();

        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();

        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, false, null);

        assertFalse(hasHeader("sec-username", proxyRequest));
        assertFalse(hasHeader("sec-roles", proxyRequest));

        assertFalse(hasHeader("imp-username", proxyRequest));
        assertFalse(hasHeader("imp-roles", proxyRequest));

        assertFalse(hasHeader("sec-org", proxyRequest));
        assertFalse(hasHeader("sec-email", proxyRequest));
        assertFalse(hasHeader("sEc-capitalize", proxyRequest));
        assertFalse(hasHeader("sec-capitalize", proxyRequest));

        assertTrue(hasHeader("other_header", proxyRequest));
    }

    private MockHttpServletRequest createTestRequest() {
        MockHttpServletRequest originalRequest = new MockHttpServletRequest("get", "http://georchestra.org/geonetwork");
        originalRequest.setRemoteHost("someserver.com");
        originalRequest.addHeader("sec-username", "jeichar");
        originalRequest.addHeader("sec-roles", "ROLE_GN_ADMIN");
        originalRequest.addHeader("imp-username", "imp_user");
        originalRequest.addHeader("imp-roles", "ROLE_GN_IMP");
        originalRequest.addHeader("other_header", "value");
        originalRequest.addHeader("sec-org", "value");
        originalRequest.addHeader("sec-email", "value");
        originalRequest.addHeader("sEc-capitalize", "value");
        return originalRequest;
    }

    private boolean hasHeader(String headerName, HttpRequestBase proxyRequest) {
        return proxyRequest.getHeaders(headerName).length > 0;
    }

    @Test
    public void testHeaderForProxy() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        headerManagement
                .setHeaderProviders(Collections.<HeaderProvider>singletonList(new SecurityRequestHeaderProvider()));

        HttpRequestBase proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();
        Authentication auth = new UsernamePasswordAuthenticationToken("jeichar", "random");
        SecurityContextHolder.getContext().setAuthentication(auth);
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, true, null);
        assertTrue(hasHeader("sec-username", proxyRequest));
        assertEquals(proxyRequest.getHeaders("sec-username")[0].getValue(), "jeichar");

        proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, false, null);
        assertFalse(hasHeader("sec-username", proxyRequest));

        proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, true, null);
        assertTrue(hasHeader("sec-username", proxyRequest));
        assertEquals(proxyRequest.getHeaders("sec-username")[0].getValue(), "jeichar");
    }

    @Test
    public void testHandleRequestCookies() throws URISyntaxException {
        String cookie = "JSESSIONID=node0aaaaddddddazaaaadudududu.node0; _ga=GA1.3.1524586053.1570800882; _gid=GA1.3.1833230840.1570800882; _gat=1";
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        headerManagement
                .setHeaderProviders(Collections.<HeaderProvider>singletonList(new SecurityRequestHeaderProvider()));
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.addHeader(COOKIE_ID, cookie);
        HttpRequestBase proxyRequest = Mockito.mock(HttpRequestBase.class);
        Mockito.when(proxyRequest.getURI()).thenReturn(new URI("https://www.georchestra.org/console/newPassword"));

        headerManagement.handleRequestCookies(originalRequest, proxyRequest, new StringBuilder());
    }

    @Test
    public void testHandleResponseCookiesSeveralParameters() {
        String setCookie = "JSESSIONID=node0aaaaaaaaaaaaaaaaaaaaaaaa.node0;Path=/console;Secure;HttpOnly";
        String requestUri = "/console/newPassword";
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        HttpSession session = new MockHttpSession();
        Header setCookieHeader = new BasicHeader(HeaderNames.SET_COOKIE_ID, setCookie);

        headerManagement.handleResponseCookies(requestUri, new MockHttpServletResponse(),
                new Header[] { setCookieHeader }, session);

        Map<String, String> jSessionIds = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
        assertTrue("Unexpected JsessionId map in session",
                jSessionIds.get("/console").contentEquals("JSESSIONID=node0aaaaaaaaaaaaaaaaaaaaaaaa.node0"));
    }

    @Test
    public void testHandleResponseCookiesNoParams() {
        String setCookie = "JSESSIONID=node0aaaaaaaaaaaaaaaaaaaaaaaa.node0";
        String requestUri = "/console/newPassword";
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        HttpSession session = new MockHttpSession();
        Header setCookieHeader = new BasicHeader(HeaderNames.SET_COOKIE_ID, setCookie);

        headerManagement.handleResponseCookies(requestUri, new MockHttpServletResponse(),
                new Header[] { setCookieHeader }, session);

        Map<String, String> jSessionIds = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
        assertTrue("jSessionIds map expected to be unset", jSessionIds == null);
    }

    @Test
    public void testHandleResponseCookiesOnlyPath() {
        String setCookie = "JSESSIONID=node0aaaaaaaaaaaaaaaaaaaaaaaa.node0;Path=/console";
        String requestUri = "/console/newPassword";
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        HttpSession session = new MockHttpSession();
        Header setCookieHeader = new BasicHeader(HeaderNames.SET_COOKIE_ID, setCookie);

        headerManagement.handleResponseCookies(requestUri, new MockHttpServletResponse(),
                new Header[] { setCookieHeader }, session);

        Map<String, String> jSessionIds = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
        assertTrue("Unexpected JsessionId map in session",
                jSessionIds.get("/console").contentEquals("JSESSIONID=node0aaaaaaaaaaaaaaaaaaaaaaaa.node0"));

    }

    @Test
    public void testHandleResponseCookiesPathDoesNotMatch() {
        String setCookie = "custom_key=custom_value;Path=/myconsole_is_elsewhere";
        String setCookie2 = "JSESSIONID=aaaaaa.node0;Path=/myconsole_is_elsewhere";
        String requestUri = "/console/newPassword";
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        HttpSession session = new MockHttpSession();
        Header setCookieHeader = new BasicHeader(HeaderNames.SET_COOKIE_ID, setCookie);
        Header setCookieHeader2 = new BasicHeader(HeaderNames.SET_COOKIE_ID, setCookie2);
        MockHttpServletResponse response = new MockHttpServletResponse();

        headerManagement.handleResponseCookies(requestUri, response, new Header[] { setCookieHeader, setCookieHeader2 },
                session);

        // In the session map, the JSESSIONID should still be indexed by the original
        // path
        Map<String, String> jSessionIds = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
        assertTrue("Unexpected JsessionId map in session",
                jSessionIds.get("/myconsole_is_elsewhere").contentEquals("JSESSIONID=aaaaaa.node0"));
        // The other custom cookie should be rewritten so that the path corresponds
        // to the "visible" path configured in the SP's targets-mappings.properties
        // file.
        String setCookieReceived = (String) response.getHeaderValue(HeaderNames.SET_COOKIE_ID);
        // The other cookie should be rewritten before being sent to the client
        // with the actual path (here: /console)
        assertTrue("Unexpected Set-Cookie header in the actual response",
                setCookieReceived.equals("custom_key=custom_value;Path=/console"));
    }

}