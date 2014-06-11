package org.georchestra.security;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Allows certain white-listed users to impersonate other users.
 *
 * @author Jesse on 5/5/2014.
 */
public class ImpersonateUserRequestHeaderProvider extends HeaderProvider {
    private List<String> trustedUsers = new ArrayList<String>();

    @Override
    protected Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest) {
        if (originalRequest.getHeader(HeaderNames.IMP_USERNAME) != null) {

            Authentication authentication = SecurityContextHolder.getContext()
                    .getAuthentication();

            if (authentication != null && trustedUsers != null && trustedUsers.contains(authentication.getName())) {
                List<Header> headers = new ArrayList<Header>(2);

                headers.add(new BasicHeader(HeaderNames.SEC_USERNAME, originalRequest.getHeader(HeaderNames.IMP_USERNAME)));
                headers.add(new BasicHeader(HeaderNames.SEC_ROLES, originalRequest.getHeader(HeaderNames.IMP_ROLES)));

                return headers;
            }
        }
        return Collections.emptyList();

    }

    /**
     * Set the users who are allowed to impersonate other users.
     *
     * @param trustedUsers list of trusted users
     */
    public void setTrustedUsers(List<String> trustedUsers) {
        this.trustedUsers = trustedUsers;
    }
}
