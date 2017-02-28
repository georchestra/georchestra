package org.georchestra.security;

import org.apache.http.Header;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProxyTrustAnotherProxyTest {

//    @Test
//    public void testUntrustedProxy() throws Exception {
//        final MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setRemoteAddr("sdi.georchestra.org");
//        request.addHeader(HeaderNames.IMP_USERNAME, "imp-user");
//        request.addHeader(HeaderNames.IMP_ROLES, "ROLE_IMP");
//
//        ProxyTrustAnotherProxy filter = new ProxyTrustAnotherProxy();
//        filter.init();
//
//        filter.doFilter(request, );
//
//        final ImpersonateUserRequestHeaderProvider provider = new ImpersonateUserRequestHeaderProvider();
//        List<String> trustedUsers = new ArrayList<String>();
//        trustedUsers.add("jeichar");
//        provider.setTrustedUsers(trustedUsers);
//        assertEquals(0, provider.getCustomRequestHeaders(null, request).size());
//
//        Authentication auth = new UsernamePasswordAuthenticationToken("randomUser", "random");
//        SecurityContextHolder.getContext().setAuthentication(auth);
//        assertEquals(0, provider.getCustomRequestHeaders(null, request).size());
//    }



}