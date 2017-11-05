/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.RegexRequestMatcher;
import org.springframework.security.web.util.RequestMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * This bean is meant to redefine the URL mappings (which are by default made
 * into the applicationContext-security.xml file). The code of the current class
 * does basically the same as DefaultFilterInvocationSecurityMetadataSource.
 *
 * Spring security docs warns users who want to achieve this to think twice or
 * more before doing so, but we do need to have it separated from the webapp.
 *
 * @author pmauduit
 */

public class SecurityProxyMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static final Log LOGGER = LogFactory.getLog(SecurityProxyMetadataSource.class.getPackage().getName());

    private Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();

    public void setRequestMap(Map map) {
        requestMap.clear();
        for (String entry: (Set<String>) map.keySet()) {
            this.requestMap.put(new RegexRequestMatcher(entry, null),
                    SecurityConfig.createListFromCommaDelimitedString(map.get(entry).toString()));
        }
    }

    public void remap() {
        String configPath = System.getProperty("georchestra.datadir");
        if (configPath == null) {
            LOGGER.info("No datadir specified, skipping security remapping");
            return;
        }
        // Spring-security related beans are instanciated before the general-purposes beans
        // At this state, we cannot rely on the autowiring capacities of Spring.

        File contextDatadir = new File(configPath, "security-proxy");
        if ((! contextDatadir.exists()) || (! contextDatadir.isDirectory())) {
            LOGGER.info("Datadir set for security-proxy, but not found is not a directory");
            throw new RuntimeException("Datadir set for security-proxy, but not found is not a directory: " +
            contextDatadir.getAbsolutePath());
        }
        File securityMappings = new File(contextDatadir, "security-mappings.xml");

        if (!securityMappings.exists()) {
            throw new RuntimeException("Unable to find " + securityMappings.getPath()
                    + ", please check your security-proxy datadir.");
        }

        try {
            requestMap.clear();
            loadSecurityRules(securityMappings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSecurityRules(File f) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(f);

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList l = (NodeList) xPath.compile("//http/intercept-url").evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < l.getLength(); ++i) {
            RequestMatcher rm = new RegexRequestMatcher(l.item(i).getAttributes().getNamedItem("pattern")
                    .getTextContent(), null);
            List<ConfigAttribute> lca = SecurityConfig.createListFromCommaDelimitedString(l.item(i).getAttributes()
                    .getNamedItem("access").getTextContent());
            requestMap.put(rm, lca);
        }

    }

    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>();

        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {

            allAttributes.addAll(entry.getValue());
        }

        return allAttributes;
    }

    public Collection<ConfigAttribute> getAttributes(Object object) {
        final HttpServletRequest request = ((FilterInvocation) object).getRequest();
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
