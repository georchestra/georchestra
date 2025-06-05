/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.security;

import static org.georchestra.commons.security.SecurityHeaders.IMP_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.IMP_USERNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.georchestra.security.HeaderNames.COOKIE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jesse on 4/24/2014.
 */
public class HeadersManagementStrategyTest {

    public @Rule TemporaryFolder tmpDatadir = new TemporaryFolder();

    /**
     * Show that by default the headers are removed
     */
    @Test
    public void testConfigureRequestHeaders_RemoveSecHeaders_DefaultBehaviour() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();

        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();

        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, false, null);

        assertFalse(hasHeader(SEC_USERNAME, proxyRequest));
        assertFalse(hasHeader(SEC_ROLES, proxyRequest));

        assertFalse(hasHeader("imp-username", proxyRequest));
        assertFalse(hasHeader("imp-roles", proxyRequest));

        assertFalse(hasHeader(SEC_ORG, proxyRequest));
        assertFalse(hasHeader(SEC_EMAIL, proxyRequest));
        assertFalse(hasHeader("sEc-capitalize", proxyRequest));
        assertFalse(hasHeader("sec-capitalize", proxyRequest));

        assertTrue(hasHeader("other_header", proxyRequest));
    }

    private MockHttpServletRequest createTestRequest() {
        MockHttpServletRequest originalRequest = new MockHttpServletRequest("get", "http://georchestra.org/geonetwork");
        originalRequest.setRemoteHost("someserver.com");
        originalRequest.addHeader(SEC_USERNAME, "jeichar");
        originalRequest.addHeader(SEC_ROLES, "ROLE_GN_ADMIN");
        originalRequest.addHeader(IMP_USERNAME, "imp_user");
        originalRequest.addHeader(IMP_ROLES, "ROLE_GN_IMP");
        originalRequest.addHeader("other_header", "value");
        originalRequest.addHeader(SEC_ORG, "value");
        originalRequest.addHeader(SEC_EMAIL, "value");
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
        assertTrue(hasHeader(SEC_USERNAME, proxyRequest));
        assertEquals(proxyRequest.getHeaders(SEC_USERNAME)[0].getValue(), "jeichar");

        proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, false, null);
        assertFalse(hasHeader(SEC_USERNAME, proxyRequest));

        proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, true, null);
        assertTrue(hasHeader(SEC_USERNAME, proxyRequest));
        assertEquals(proxyRequest.getHeaders(SEC_USERNAME)[0].getValue(), "jeichar");
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
        Cookie cookieReceived = response.getCookie("custom_key");
        assertNotNull(cookieReceived);
        assertEquals("custom_value", cookieReceived.getValue());
        // The other cookie should be rewritten before being sent to the client
        // with the actual path (here: /console)
        assertEquals("/console", cookieReceived.getPath());
    }

    /**
     * Verify the list of {@link CookieAffinity} config objects is loaded from
     * <code>${georchestra.datadir}/security-proxy/cookie-mappings.json</code>
     */
    @Test
    public void testResponseCookieAffinityConfigLoading() throws IOException {
        File root = this.tmpDatadir.getRoot();
        File contextDataDir = this.tmpDatadir.newFolder("security-proxy");

        System.setProperty("georchestra.datadir", root.getAbsolutePath());
        GeorchestraConfiguration georconfig = new GeorchestraConfiguration("security-proxy");
        assertTrue(georconfig.activated());

        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        // force the @Autowired assignment on GeorchestraConfig
        headerManagement.setGeorchestraConfig(georconfig);
        // force the @PostConstruct call on initConfig(), there's no
        // cookie-mappings.json file
        headerManagement.initConfig();

        List<CookieAffinity> parsedConfigs = headerManagement.getCookieAffinityConfig();
        assertNotNull(parsedConfigs);
        assertTrue(parsedConfigs.isEmpty());

        List<CookieAffinity> configs = List.of(
                new CookieAffinity().setName("XSRF-TOKEN").setFrom("/geonetwork").setTo("/datahub"),
                new CookieAffinity().setName("XSRF-TOKEN").setFrom("/geonetwork").setTo("/console"));
        new ObjectMapper().writeValue(new File(contextDataDir, "cookie-mappings.json"), configs);

        // force the @PostConstruct call on initConfig(), there is a
        // cookie-mappings.json file
        headerManagement.initConfig();
        parsedConfigs = headerManagement.getCookieAffinityConfig();
        assertEquals(configs, parsedConfigs);
        System.clearProperty("georchestra.datadir");
    }

    @Test
    public void testResponseCookieAffinityMapping() throws IOException {
        final List<CookieAffinity> configs = List.of(
                new CookieAffinity().setName("XSRF-TOKEN").setFrom("/geonetwork").setTo("/datahub"),
                new CookieAffinity().setName("custom-cookie").setFrom("/console").setTo("/atlas"));

        final HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        headerManagement.setCookieAffinity(configs);

        String gnCookie1 = "XSRF-TOKEN=abc;Path=/geonetwork";
        String gnCookie2 = "custom-cookie=def;Path=/geonetwork";

        HttpSession session = new MockHttpSession();
        Header setGnCookie1 = new BasicHeader(HeaderNames.SET_COOKIE_ID, gnCookie1);
        Header setGnCookie2 = new BasicHeader(HeaderNames.SET_COOKIE_ID, gnCookie2);

        MockHttpServletResponse response = new MockHttpServletResponse();

        headerManagement.handleResponseCookies("/geonetwork/srv/api", response,
                new Header[] { setGnCookie1, setGnCookie2 }, session);

        ComparableCookie xsrfOrig = new ComparableCookie("XSRF-TOKEN", "abc");
        xsrfOrig.setPath("/geonetwork");
        xsrfOrig.setVersion(0);

        ComparableCookie xsrfAffinity = (ComparableCookie) xsrfOrig.clone();
        xsrfAffinity.setPath("/datahub");

        ComparableCookie customOrig = new ComparableCookie("custom-cookie", "def");
        customOrig.setPath("/geonetwork");
        customOrig.setVersion(0);

        ComparableCookie customAffinity = (ComparableCookie) xsrfOrig.clone();
        customAffinity.setPath("/geonetwork");

        Set<ComparableCookie> expected = Set.of(xsrfOrig, xsrfAffinity, customOrig);
        Set<ComparableCookie> actual = Arrays.stream(response.getCookies()).map(this::toHttpCookie)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @SuppressWarnings("serial")
    private static class ComparableCookie extends Cookie {

        public ComparableCookie(String name, String value) {
            super(name, value);
        }

        public @Override boolean equals(Object obj) {
            if (!(obj instanceof ComparableCookie)) {
                return false;
            }
            ComparableCookie other = (ComparableCookie) obj;

            // One http cookie equals to another cookie (RFC 2965 sec. 3.3.3) if:
            // 1. they come from same domain (case-insensitive),
            // 2. have same name (case-insensitive),
            // 3. and have same path (case-sensitive).
            return getName().equalsIgnoreCase(other.getName())
                    && (getDomain() == null ? "" : getDomain())
                            .equalsIgnoreCase((other.getDomain() == null ? "" : other.getDomain()))
                    && Objects.equals(getPath(), other.getPath()) && Objects.equals(getValue(), other.getValue());
        }

        public @Override int hashCode() {
            return Objects.hash(getName(), getDomain(), getPath());
        }

        public @Override String toString() {
            return String.format("%s=%s;Path=%s;Domain=%s", getName(), getValue(), getPath(), getDomain());
        }
    }

    /**
     * Helper method to use {@link ComparableCookie} instead of {@link Cookie}
     * because the former implements equals() and hashCode() while the later doesn't
     *
     * @param cookie
     * @return
     */
    private ComparableCookie toHttpCookie(javax.servlet.http.Cookie cookie) {
        ComparableCookie c = new ComparableCookie(cookie.getName(), cookie.getValue());
        c.setComment(cookie.getComment());
        if (cookie.getDomain() != null)
            c.setDomain(cookie.getDomain());
        c.setHttpOnly(cookie.isHttpOnly());
        c.setMaxAge(cookie.getMaxAge());
        c.setPath(cookie.getPath());
        c.setSecure(cookie.getSecure());
        c.setVersion(cookie.getVersion());
        return c;
    }
}