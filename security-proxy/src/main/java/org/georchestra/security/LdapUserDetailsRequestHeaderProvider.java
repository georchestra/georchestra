/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
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
    private Pattern pattern;
    private String orgsSearchBaseDN;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    public LdapUserDetailsRequestHeaderProvider(LdapUserSearch userSearch, String orgsSearchBaseDN, Map<String, String> headerMapping) {
        Assert.notNull(userSearch, "userSearch must not be null");
        Assert.notNull(headerMapping, "headerMapping must not be null");
        this._userSearch = userSearch;
        this._headerMapping = headerMapping;
        this.orgsSearchBaseDN = orgsSearchBaseDN;

        this.pattern = Pattern.compile("([^=,]+)=([^=,]+)," + orgsSearchBaseDN + ".*");
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

        // Don't use this provider for trusted request
        if(session.getAttribute("pre-auth") != null){
            return Collections.emptyList();
        }

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

                // Add user organization
                String orgCn = null;
                try {
                    // Retreive memberOf attributes
                    String[] attrs = {"memberOf"};
                    ((FilterBasedLdapUserSearch) this._userSearch).setReturningAttributes(attrs);
                    userData = _userSearch.searchForUser(username);
                    Attribute attributes = userData.getAttributes().get("memberOf");
                    if (attributes != null) {
                        NamingEnumeration<?> all = attributes.getAll();

                        while (all.hasMore()) {
                            String memberOf = all.next().toString();
                            Matcher m = this.pattern.matcher(memberOf);
                            if (m.matches()) {
                                orgCn = m.group(2);
                                headers.add(new BasicHeader("sec-org", orgCn));
                                break;
                            }
                        }
                    }
                } catch (javax.naming.NamingException e) {
                    logger.error("problem adding headers for request: organization", e);
                } finally {
                    // restore standard attribute list
                    ((FilterBasedLdapUserSearch) this._userSearch).setReturningAttributes(null);
                }

                // add sec-orgname
                if(orgCn != null) {
                    try {
                        DirContextOperations ctx = this.ldapTemplate.lookupContext("cn=" + orgCn + "," + this.orgsSearchBaseDN);
                        headers.add(new BasicHeader("sec-orgname", ctx.getStringAttribute("o")));
                    }catch (RuntimeException ex){
                        logger.warn("Cannot find associated org with cn " + orgCn);
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
