package org.geowebcache.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.FilterChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 4/24/2014.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:georchestra-acegi-config.xml" })
public class PreAuthFilterTest {

    @Autowired
    private PreAuthFilter preAuthFilter;

    @BeforeClass
    public static void before() {

    }
    @Test
    public void testDoFilter() throws Exception {
        SecurityContextHolder.clearContext();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = Mockito.mock(FilterChain.class);
        preAuthFilter.doFilter(request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        final String username = "username";
        request.addHeader("sec-username", username);
        final String roleAdmin = "ROLE_ADMINISTRATOR";
        final String roleOther = "ROLE_OTHER";

        request.addHeader("sec-roles", roleAdmin + ";" + roleOther);

        chain = Mockito.mock(FilterChain.class);
        preAuthFilter.doFilter(request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(auth instanceof PreAuthToken);
        PreAuthToken preAuthToken = (PreAuthToken) auth;

        assertEquals(username, preAuthToken.getPrincipal());
        assertEquals(2, preAuthToken.getAuthorities().length);
        assertEquals(roleAdmin, preAuthToken.getAuthorities()[0].getAuthority());
        assertEquals(roleOther, preAuthToken.getAuthorities()[1].getAuthority());

    }




}
