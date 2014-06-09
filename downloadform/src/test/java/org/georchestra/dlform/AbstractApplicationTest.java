package org.georchestra.dlform ;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class AbstractApplicationTest {

    private AbstractApplication ctrl;

    @Before
    public void setUp() {
        DataSource ds = Mockito.mock(DataSource.class);
        // Since we cannot instantiate the abstractApplication class directly
        // we would use ExtractorApp for the current test suite
        ctrl = new ExtractorApp(ds, true);
    }

    @Test
    public void testInitializeVariables() {
        // We need to fake specific headers, which is not possible
        // using the mock provided by Spring. Using Mockito instead
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("sec-firstname")).thenReturn("Scott");
        Mockito.when(req.getHeader("sec-lastname")).thenReturn("Tiger");
        Mockito.when(req.getHeader("sec-org")).thenReturn("geOrchestra");
        Mockito.when(req.getHeader("sec-email")).thenReturn("root@localhost");
        Mockito.when(req.getHeader("sec-tel")).thenReturn("+331234567890");
        Mockito.when(req.getParameter("datause")).thenReturn("Testing purposes");
        Mockito.when(req.getParameter("comment")).thenReturn("blah blah blah ...");
        Mockito.when(req.getParameter("ok")).thenReturn("on");
        Mockito.when(req.getHeader("sec-username")).thenReturn("testuser");
        Mockito.when(req.getParameter("sessionid")).thenReturn("JSSESSIONID-1234567");

        ctrl.initializeVariables(req);
        assertFalse("Expected valid form", ctrl.isInvalid());
    }

    @Test
    public void testBadInitializeVariables() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("sec-firstname")).thenReturn(null);
        ctrl.initializeVariables(req);
        assertTrue("Expected invalid form", ctrl.isInvalid());
    }

}