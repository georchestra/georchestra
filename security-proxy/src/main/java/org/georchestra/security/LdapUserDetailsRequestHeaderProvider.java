package org.georchestra.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.util.Assert;

/**
 * Reads information from a user node in LDAP and adds the information as
 * headers to the request.
 *
 * @author jeichar
 */
public class LdapUserDetailsRequestHeaderProvider extends HeaderProvider {

    protected static final Log logger = LogFactory.getLog(LdapUserDetailsRequestHeaderProvider.class.getPackage().getName());

    private LdapUserSearch      _userSearch;
    private Map<String, String> _headerMapping;

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    public LdapUserDetailsRequestHeaderProvider(LdapUserSearch userSearch, Map<String, String> headerMapping) {
        Assert.notNull(userSearch, "userSearch must not be null");
        Assert.notNull(headerMapping, "headerMapping must not be null");
        this._userSearch = userSearch;
        this._headerMapping = headerMapping;
    }

    public void init() throws IOException {
        if ((georchestraConfiguration != null) && (georchestraConfiguration.activated())) {
            Properties pHmap = georchestraConfiguration.loadCustomPropertiesFile("headers-mapping");
            _headerMapping.clear();
            for (String key: pHmap.stringPropertyNames()) {
                _headerMapping.put(key, pHmap.getProperty(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken){
            return Collections.emptyList();
        }
        String username = authentication.getName();
        DirContextOperations userData;

        Collection<Header> headers = Collections.emptyList();

		synchronized (session) {

			if (session.getAttribute("security-proxy-cached-attrs") != null) {
				try {
					headers = (Collection<Header>) session.getAttribute("security-proxy-cached-attrs");
					String expectedUsername = (String) session.getAttribute("security-proxy-cached-username");

					if (username.equals(expectedUsername)) {
						return headers;
					}
				} catch (Exception e) {
					logger.info("Unable to lookup cached user's attributes for user :" + username, e);
				}
			} else {
				try {
					userData = _userSearch.searchForUser(username);
				} catch (Exception e) {
					logger.info("Unable to lookup user:" + username, e);
					return Collections.emptyList();
				}
				headers = new ArrayList<Header>();
				for (Map.Entry<String, String> entry : _headerMapping
						.entrySet()) {
					try {
						Attribute attributes = userData.getAttributes().get(
								entry.getValue());
						if (attributes != null) {
							NamingEnumeration<?> all = attributes.getAll();
							StringBuilder value = new StringBuilder();
							while (all.hasMore()) {
								if (value.length() > 0) {
									value.append(',');
								}
								value.append(all.next());
							}
							headers.add(new BasicHeader(entry.getKey(), value
									.toString()));
						}
					} catch (javax.naming.NamingException e) {
						logger.error("problem adding headers for request:"
								+ entry.getKey(), e);
					}
				}
				logger.info("Storing attributes into session for user :" + username);
				session.setAttribute("security-proxy-cached-username", username);
				session.setAttribute("security-proxy-cached-attrs", headers);
			}
		}

        return headers;
    }
}
