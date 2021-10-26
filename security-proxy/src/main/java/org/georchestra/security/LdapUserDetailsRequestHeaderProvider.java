/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.security.LdapHeaderMappings.HeaderMapping;
import org.georchestra.security.LdapHeaderMappings.HeaderMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

import lombok.NonNull;

/**
 * Reads information from a user node in LDAP and adds the information as
 * headers to the request.
 * <p>
 * Adds the remaining standard request headers {@code sec-org} and
 * {@code sec-orgname} (while {@link SecurityRequestHeaderProvider} adds
 * {@code sec-username} and {@code sec-roles}), and then any extra request
 * header that can be extracted from the user's LDAP info and is configured in
 * the datadirectory's {@code security-proxy/headers-mapping.properties} file.
 * <p>
 * The final set of request headers sent to each specific proxified application
 * is the aggregation of default headers and service specific headers.
 * <p>
 * Header mappings that apply to a specific target service take precedence over
 * default headers.
 * <p>
 * Service name matches the ones assigned in {@code targets-mapping.properties}.
 * For example, for service {@code analytics},
 * {@code targets-mappings.properties} contains
 * {@code analytics=http://analytics:8080/analytics/}, and
 * {@code headers-mappings.properties} may contain
 * {@code analytics.sec-firstname=givenName}.
 * <p>
 * For more information {@link LdapHeaderMappings}
 * 
 * @author jeichar
 * @see SecurityRequestHeaderProvider
 */
public class LdapUserDetailsRequestHeaderProvider extends HeaderProvider {

    private static final String MEMBER_OF_PATTERN = "([^=,]+)=([^=,]+),%s.*";

    private final Supplier<FilterBasedLdapUserSearch> userSearchFactory;
    private final Pattern orgSearchMemberOfPattern;
    private final String orgSearchBaseDN;
    private final LdapHeaderMappings mappingsSupport = new LdapHeaderMappings();

    @Autowired
    @VisibleForTesting
    LdapTemplate ldapTemplate;

    private @Autowired Environment env;

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    /**
     * Cache of collected headers by user/service. Entries should expire in a long
     * enough time to avoid concurrent requests flooding the LDAP server with
     * requests, but short enough to allow propagating changes down to proxied
     * services asap.
     */
    private final Cache<String, Map<String, String>> cache;

    /**
     * Cache time-to-live in milliseconds, can be given through an external
     * configuration property {@code security-proxy.ldap.cache.ttl}
     */
    private static final long DEFAULT_CACHE_TTL = 2000;

    /**
     * @param contextSource    provider for the base LDAP path
     * @param ldapOrgsRdn      search base for user organizations (e.g.
     *                         {@code ou=orgs}
     * @param ldapUsersRdn     search base for users (e.g. {@code ou=users})
     * @param userSearchFilter single-user search filter (e.g. {@code uid={0}})
     */
    public LdapUserDetailsRequestHeaderProvider(BaseLdapPathContextSource contextSource, String ldapOrgsRdn,
            String ldapUsersRdn, String userSearchFilter) {

        requireNonNull(contextSource, "contextSource must not be null");
        requireNonNull(ldapOrgsRdn, "ldapOrgsRdn must not be null");
        requireNonNull(ldapUsersRdn, "ldapUsersRdn must not be null");
        requireNonNull(userSearchFilter, "userSearchFilter must not be null");

        this.orgSearchBaseDN = ldapOrgsRdn;
        this.userSearchFactory = () -> new FilterBasedLdapUserSearch(ldapUsersRdn, userSearchFilter, contextSource);
        this.orgSearchMemberOfPattern = Pattern.compile(format(MEMBER_OF_PATTERN, ldapOrgsRdn));
        this.cache = createCache();
    }

    @VisibleForTesting
    LdapUserDetailsRequestHeaderProvider(Supplier<FilterBasedLdapUserSearch> userSearchFactory, String ldapOrgsRdn) {
        this.orgSearchBaseDN = ldapOrgsRdn;
        this.userSearchFactory = userSearchFactory;
        this.orgSearchMemberOfPattern = Pattern.compile(format(MEMBER_OF_PATTERN, ldapOrgsRdn));
        this.cache = createCache();
    }

    private Cache<String, Map<String, String>> createCache() {
        long ttl = DEFAULT_CACHE_TTL;
        if (this.env != null) {
            ttl = this.env.getProperty("security-proxy.ldap.cache.ttl", Long.class, DEFAULT_CACHE_TTL);
        }
        logger.info(String.format("Setting up LDAP headers cache ttl %,d ms", ttl));
        return CacheBuilder.newBuilder().expireAfterWrite(ttl, TimeUnit.MILLISECONDS).build();
    }

    @PostConstruct
    public void init() throws IOException {
        LdapHeaderMappings.EMBEDDED_MAPPINGS
                .forEach((h, m) -> logger.info(format("Will contribute standard header %s", h)));

        final boolean loadExternalConfig = (georchestraConfiguration != null) && (georchestraConfiguration.activated());
        if (loadExternalConfig) {
            Properties mappingsFromFile = georchestraConfiguration.loadCustomPropertiesFile("headers-mapping");
            Map<String, String> allMappings = Maps.fromProperties(mappingsFromFile);
            logger.info("Loading header mappings from "
                    + new File(georchestraConfiguration.getContextDataDir(), "headers-mapping.properties"));
            loadConfig(allMappings);
        }
    }

    @VisibleForTesting
    void loadConfig(Map<String, String> allMappings) {
        this.mappingsSupport.loadFrom(allMappings);
    }

    @Override
    public Map<String, String> getCustomRequestHeaders(final HttpServletRequest originalRequest,
            final String targetServiceName) {

        // Don't use this provider for trusted request
        if (isPreAuthorized(originalRequest) || isAnnonymous()) {
            return Collections.emptyMap();
        }

        final String key = serviceCacheKey(targetServiceName);
        try {
            return cache.get(key, () -> collectHeaders(targetServiceName));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /**
     * Actually performs the building of the request headers list
     */
    private Map<String, String> collectHeaders(@Nullable String serviceName) {
        final String username = getCurrentUserName();

        final HeaderMappings mappings = serviceName == null ? mappingsSupport.getDefaultMappings()
                : mappingsSupport.getMappings(serviceName);
        logger.debug("Collecting headers, service = " + serviceName + ", mappings:" + mappings.all());

        Map<String, String> headers = new HashMap<>();
        try {
            final DirContextOperations userContext = loadUser(username, mappings.getUserHeaders());
            final Optional<String> orgCn = findOrgCn(userContext);
            final Optional<String> manager = findManagerUid(userContext);

            logger.debug(format("Loading %s headers for user %s, org %s, manager %s", //
                    serviceName == null ? "default" : serviceName, //
                    username, //
                    orgCn.orElse(null), //
                    manager.orElse(null)));

            listcontext("* LDAP user context", userContext);
            addHeaders(userContext, mappings.getUserHeaders(), headers);

            orgCn.ifPresent(org -> addOrgHeaders(org, mappings, headers));
            manager.ifPresent(uid -> addManagerHeaders(uid, mappings.getUserManagerHeaders(), headers));

            // Collections.sort(headers, (h1, h2) -> h1.getName().compareTo(h2.getName()));
        } catch (Exception e) {
            logger.error("Unable to collect headers for user:" + username, e);
            return Collections.emptyMap();
        }
        return headers;
    }

    private void addManagerHeaders(String uid, List<HeaderMapping> mappings, Map<String, String> target) {
        if (!mappings.isEmpty()) {
            DirContextOperations managerContext = userSearchFactory.get().searchForUser(uid);
            listcontext("* LDAP user's manager context", managerContext);
            addHeaders(managerContext, mappings, target);
        }
    }

    private void addOrgHeaders(String orgCn, HeaderMappings mappings, Map<String, String> target) {
        final String groupDn = format("cn=%s,%s", orgCn, this.orgSearchBaseDN);
        DirContextOperations orgContext = this.ldapTemplate.lookupContext(groupDn);
        listcontext("* LDAP org context", orgContext);

        addHeaders(orgContext, mappings.getOrgHeaders(), target);

        final Optional<String> seeAlsoOrgName = findSeeAlsoOrg(orgContext);
        seeAlsoOrgName.ifPresent(cn -> addOrgExtHeaders(cn, mappings.getOrgExtensionHeaders(), target));
    }

    private void addOrgExtHeaders(String cn, List<HeaderMapping> mappings, Map<String, String> target) {
        if (mappings.isEmpty()) {
            return;
        }
        final String seeAlsoDn = format("o=%s,ou=orgs", cn);
        DirContextOperations orgExtCtx = this.ldapTemplate.lookupContext(seeAlsoDn);
        listcontext("* LDAP orgExt context", orgExtCtx);
        addHeaders(orgExtCtx, mappings, target);
    }

    private Optional<String> findSeeAlsoOrg(DirContextOperations orgContext) {
        String seeAlso = orgContext.getStringAttribute("seeAlso");
        if (null != seeAlso) {
            Matcher matcher = Pattern.compile("^o=(.*),ou=orgs,.*").matcher(seeAlso);
            if (matcher.matches()) {
                String seeAlsoOrg = matcher.group(1);
                return Optional.of(seeAlsoOrg);
            }
        }
        return Optional.empty();
    }

    private void addHeaders(DirContextOperations context, List<HeaderMapping> mappings, Map<String, String> target) {
        mappings.stream()//
                .map(mapping -> buildHeader(context, mapping))//
                .filter(Predicates.notNull())//
                .forEach(h -> target.put(h.getName(), h.getValue()));

    }

    private Header buildHeader(DirContextOperations context, HeaderMapping mapping) {
        String value = buildValue(context, mapping);
        return value == null ? null : new BasicHeader(mapping.getHeaderName(), value);
    }

    private Optional<String> findManagerUid(DirContextOperations userContext) {
        // e.g. uid=testeditor,ou=users,dc=georchestra,dc=org
        final Pattern pattern = Pattern.compile("^uid=(.*),(ou=users),.*");
        String managerDn = userContext.getStringAttribute("manager");
        return Optional.ofNullable(managerDn).map(pattern::matcher).filter(Matcher::matches)//
                .map(matcher -> matcher.group(1));
    }

    private DirContextOperations loadUser(String username, List<HeaderMapping> userHeaders) {
        FilterBasedLdapUserSearch userSearch = userSearchFactory.get();
        Set<String> attrs = userHeaders.stream()//
                .map(HeaderMapping::getLdapAttribute)//
                .collect(Collectors.toSet());
        attrs.add("memberOf");// to gather the organization cn
        attrs.add("manager");

        logger.debug("Requesting user attributes: " + attrs);
        userSearch.setReturningAttributes(attrs.toArray(new String[attrs.size()]));

        return userSearch.searchForUser(username);
    }

    private Optional<String> findOrgCn(DirContextOperations userContext) {
        Optional<String[]> memberOf = Optional.ofNullable(userContext.getStringAttributes("memberOf"));
        memberOf.map(Arrays::stream);
        final Optional<String> orgCn = memberOf.map(Arrays::stream).orElse(Stream.empty())//
                .map(this.orgSearchMemberOfPattern::matcher)//
                .filter(Matcher::matches).map(matcher -> matcher.group(2)).findFirst();
        return orgCn;
    }

    private String buildValue(DirContextOperations context, HeaderMapping mapping) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Loading header mapping %s=%s", mapping.getHeaderName(), mapping.getLdapAttribute()));
        }
        String[] values = context.getStringAttributes(mapping.getLdapAttribute());
        if (values == null || values.length == 0) {
            logger.debug(format("Found no values for header mapping %s=%s", mapping.getHeaderName(),
                    mapping.getLdapAttribute()));
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(format("Found values for header %s: %s", mapping.getHeaderName(),
                    Arrays.stream(values).collect(Collectors.joining(","))));
        }
        String value = Arrays.stream(values)//
                .filter(Predicates.notNull())//
                .map(mapping::encode)//
                .collect(Collectors.joining(","));
        return value;
    }

    private void listcontext(String title, DirContextOperations ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug(title);
            Collections.list(ctx.getAttributes().getAll()).forEach(att -> {
                try {
                    logger.debug(format("%s='%s'", att.getID(), att.get()));
                } catch (Exception e) {
                    logger.error("Error getting attribute " + att.getID(), e);
                }
            });
        }
    }

    private @NonNull String getCurrentUserName() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Request is not authenticated");
        }
        return authentication.getName();
    }

    @VisibleForTesting
    Map<String, String> getCachedHeaders(String service) {
        final String cacheKey = serviceCacheKey(service);
        Map<String, String> headers = cache.getIfPresent(cacheKey);
        return headers == null ? Collections.emptyMap() : headers;
    }

    @VisibleForTesting
    void setCachedHeaders(@NonNull Map<String, String> headers, String service) {
        final String cacheKey = serviceCacheKey(service);
        logger.debug("Storing attributes into session for " + cacheKey);
        cache.put(cacheKey, new HashMap<>(headers));
    }

    private String serviceCacheKey(String service) {
        String userName = getCurrentUserName();
        return String.format("%s@%s", userName, service == null ? "global" : service);
    }

    private boolean isAnnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken;
    }
}
