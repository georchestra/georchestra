/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.ds.security.OrganizationsApiImpl;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

import lombok.NonNull;

/**
 * Adds a header with a full object payload as a Base64 encoded JSON
 * representation, using a provided config property to determine whether the
 * header is globally enabled or enabled for specific services.
 * <p>
 * For example, the {@code sec-XXX} headers may be enabled through the
 * {@code send-json-XXX} config property.
 * <p>
 * Concrete implementations must provide the config property name and the header
 * name as {@link #JSONRequestHeaderProvider(String, String) constructor
 * arguments}, and implement the {@link #getPayloadObject(String)} method to
 * return the object that's to be added as Base64 encoded JSON representation
 * for the header.
 */
abstract class JSONRequestHeaderProvider extends HeaderProvider {

    private final String configProperty;
    private final String headerName;

    /** Key used for lookup of whether this header provider is globally enabled **/
    private static final String GLOBAL_KEY = "global";

    private PropertyResolver env;

    private @Autowired GeorchestraConfiguration georchestraConfiguration;

    /**
     * Encoder to create the JSON String value for a {@link GeorchestraUser}
     * obtained from {@link OrganizationsApiImpl}
     */
    private ObjectMapper encoder;

    /**
     * Cache of Base64-JSON encoded {@link GeorchestraUser user} by username.
     * <p>
     * Entries should expire in a long enough time to avoid concurrent requests
     * flooding the LDAP server with requests, but short enough to allow propagating
     * changes down to proxied services asap.
     */
    private Cache<String, String> cache;

    /**
     * Individual service names for which to add the {@link #headerName} request
     * header. Must be configured in {@code headers-mapping.properties} as
     * {@code <service-name>.<}{@link #configProperty}{@code>=true} , for example:
     * 
     * <pre>
     * <code>
     * geonetwork.send-json-sec-user=true
     * datafeeder.send-json-sec-user=true 
     * </code>
     * </pre>
     * 
     * If {@link #configProperty}{@code  = "send-json-sec-user"}.
     */
    private Map<String, Boolean> enabledServices = Collections.singletonMap(GLOBAL_KEY, Boolean.FALSE);

    /**
     * Cache time-to-live in milliseconds, can be given through an external
     * configuration property {@code security-proxy.ldap.cache.ttl}
     */
    private static final long DEFAULT_CACHE_TTL = 2000;

    public JSONRequestHeaderProvider(String configPropertyName, String headerName) {
        this.configProperty = configPropertyName;
        this.headerName = headerName;
        this.encoder = new ObjectMapper();
        this.encoder.configure(SerializationFeature.INDENT_OUTPUT, Boolean.FALSE);
        this.encoder.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, Boolean.FALSE);
        this.encoder.setSerializationInclusion(Include.NON_NULL);
    }

    @PostConstruct
    public void init() throws IOException {
        final boolean loadExternalConfig = (georchestraConfiguration != null) && (georchestraConfiguration.activated());
        if (loadExternalConfig) {
            Properties configProps = georchestraConfiguration.loadCustomPropertiesFile("headers-mapping");
            init(configProps);
        }
    }

    public @Autowired void setPropertyResolver(PropertyResolver env) {
        this.env = env;
    }

    private Cache<String, String> createCache() {
        long ttl = DEFAULT_CACHE_TTL;
        if (this.env != null) {
            String sttl = this.env.getProperty("security-proxy.ldap.cache.ttl");
            ttl = sttl == null ? DEFAULT_CACHE_TTL : Long.valueOf(sttl);
            if (ttl < 0) {
                ttl = 0;// disabled
            }
        }
        logger.info(String.format("Setting up LDAP headers cache ttl %,d ms", ttl));
        return CacheBuilder.newBuilder().expireAfterWrite(ttl, TimeUnit.MILLISECONDS).build();
    }

    final @VisibleForTesting void init(Properties configProps) {
        Map<String, String> rawConfig = Maps.fromProperties(configProps);
        this.enabledServices = loadConfig(rawConfig);
        this.cache = createCache();
    }

    private Map<String, Boolean> loadConfig(final Map<String, String> rawConfig) {
        Map<String, Boolean> config = new HashMap<>();
        rawConfig.forEach((k, v) -> {
            String service = configToServiceName(k);
            if (service != null) {
                Boolean enabled = Boolean.valueOf(v);
                config.put(service, enabled);
                logger.info(headerName + " with full JSON user payload enabled for service " + service);
            }
        });
        config.computeIfAbsent(GLOBAL_KEY, k -> false);
        return config;
    }

    private String configToServiceName(String configPropName) {
        String serviceName = null;
        if (configPropName.contains(configProperty)) {
            boolean isGlobalConfig = configProperty.equals(configPropName);
            if (isGlobalConfig) {
                serviceName = GLOBAL_KEY;
            } else {
                int index = configPropName.indexOf('.');
                serviceName = configPropName.substring(0, index);
            }
        }
        return serviceName;
    }

    @Override
    public Map<String, String> getCustomRequestHeaders(final HttpServletRequest request, final String service) {

        // REVISIT: what shall we do with pre-authorized requests? Guess we need to send
        // the user that's being impersonated?
        if (isPreAuthorized(request) || isAnnonymous() || !isEnabledForService(service)) {
            return Collections.emptyMap();
        }

        final String userName = getCurrentUserName();
        final String base64UserJson = resolveValue(userName);
        return Collections.singletonMap(headerName, base64UserJson);
    }

    private String resolveValue(String userName) {
        String base64UserJson;
        try {
            base64UserJson = cache.get(userName, () -> buildHeaderValue(userName));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
        return base64UserJson;
    }

    private boolean isEnabledForService(String service) {
        boolean global = this.enabledServices.get(GLOBAL_KEY).booleanValue();
        return service == null ? global : this.enabledServices.getOrDefault(service, global);
    }

    private String buildHeaderValue(@NonNull String userName) {
        Object user = getPayloadObject(userName);
        String json = encodeJson(user);
        return encodeBase64(json);
    }

    protected abstract Object getPayloadObject(String userName);

    private String encodeJson(Object payloadObject) {
        try {
            return this.encoder.writer().writeValueAsString(payloadObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeBase64(String json) {
        return SecurityHeaders.encodeBase64(json);
    }

    private @NonNull String getCurrentUserName() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Request is not authenticated");
        }
        return authentication.getName();
    }

    private boolean isAnnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken;
    }
}
