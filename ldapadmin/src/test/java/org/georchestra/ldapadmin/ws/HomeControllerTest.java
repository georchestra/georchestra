package org.georchestra.ldapadmin.ws;

import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.ldapadmin.Configuration;
import org.georchestra.ldapadmin.bs.ExpiredTokenCleanTask;
import org.georchestra.ldapadmin.bs.ExpiredTokenManagement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author pmauduit
 *
 */
public class HomeControllerTest {

    private HomeController ctrl;
    private ExpiredTokenCleanTask tokenTask = Mockito.mock(ExpiredTokenCleanTask.class);
    private ExpiredTokenManagement expiredTokenMgmt = new ExpiredTokenManagement(tokenTask);
    private Configuration configuration = new Configuration();


    @Before
    public void setUp() {
        expiredTokenMgmt.setDelayInDays(1);
        ctrl = new HomeController(expiredTokenMgmt, configuration);
    }

    @Test
    public void testHomeControllerAnonymousRedirectToCas() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ctrl.root(request, response);

        assertTrue("expected 302, got " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_FOUND);
        assertTrue("bad redirectUrl, got " + response.getRedirectedUrl(),
                response.getRedirectedUrl().contains("/account/userdetails?login"));
    }

    @Test
    public void testHomeControllerAnonymous() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("sec-roles", "ROLE_ANONYMOUS");
        ctrl.root(request, response);

        assertTrue("expected 302, got " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_FOUND);
        assertTrue("bad redirectUrl, got " + response.getRedirectedUrl(),
                response.getRedirectedUrl().contains("/account/userdetails?login"));
    }

    @Test
    public void testHomeControllerAuthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("sec-roles", "ROLE_ADMINISTRATOR");
        ctrl.root(request, response);

        assertTrue(response.getRedirectedUrl().endsWith("/account/userdetails"));

    }

    @Test
    public void testHomeControllerLdapAdmin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("sec-roles", "ROLE_MOD_LDAPADMIN");
        ctrl.root(request, response);

        assertTrue(response.getRedirectedUrl().endsWith("/privateui/"));

    }

    @Test
    public void testPrivateUiForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String ret = ctrl.privateui(request);

        assertTrue(ret.equals("forbidden"));
    }

    @Test
    public void testPrivateUi() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("sec-roles", "ROLE_MOD_LDAPADMIN");

        String ret = ctrl.privateui(request);

        assertTrue(ret.equals("privateUi"));
    }

}