/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.commons.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;

/**
 * A collection of header names commonly used by the security-proxy gateway
 * application
 *
 * @author Jesse on 5/5/2014.
 */
public class SecurityHeaders {
    // well-known header names
    public static final String SEC_PROXY = "sec-proxy";

    /**
     * Used to send the full {@link GeorchestraUser user details} as a Base64
     * encoded JSON string
     */
    public static final String SEC_USER = "sec-user";
    /**
     * Used to send the full {@link Organization} as a Base64 encoded JSON string
     */
    public static final String SEC_ORGANIZATION = "sec-organization";

    public static final String SEC_USERID = "sec-userid";
    public static final String SEC_LASTUPDATED = "sec-lastupdated";
    public static final String SEC_USERNAME = "sec-username";
    public static final String SEC_ROLES = "sec-roles";
    public static final String SEC_FIRSTNAME = "sec-firstname";
    public static final String SEC_LASTNAME = "sec-lastname";
    public static final String SEC_EMAIL = "sec-email";
    public static final String SEC_TEL = "sec-tel";
    public static final String SEC_ADDRESS = "sec-address";
    public static final String SEC_TITLE = "sec-title";
    public static final String SEC_NOTES = "sec-notes";
    public static final String SEC_ORG = "sec-org";
    public static final String SEC_ORGID = "sec-orgid";
    public static final String SEC_ORGNAME = "sec-orgname";
    public static final String SEC_ORG_LASTUPDATED = "sec-org-lastupdated";
    public static final String IMP_ROLES = "imp-roles";
    public static final String IMP_USERNAME = "imp-username";
    public static final String SEC_LDAP_REMAINING_DAYS = "sec-ldap-remaining-days";
    public static final String SEC_EXTERNAL_AUTHENTICATION = "sec-external-authentication";

    /**
     * @return the decoded header value, if it contains multiple values separated by
     *         a comma, is the returned value
     */
    public static String decode(final String headerValue) {
        if (null == headerValue) {
            return null;
        }
        final boolean isMultipleValues = headerValue.indexOf(',') > -1;
        if (isMultipleValues) {
            String[] singleValues = headerValue.split(",", -1);// -1 avoids omitting empty strings
            String decoded = Stream.of(singleValues).map(SecurityHeaders::decodeSingle).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            return decoded;
        }
        return decodeSingle(headerValue);
    }

    /**
     * @return all individual header values decoded; individual values are separated
     *         by a comma in the argument string
     */
    public static List<String> decodeAsList(String headerValue) {
        if (null == headerValue) {
            return null;
        }
        String[] singleValues = headerValue.split(",", -1);// -1 avoids omitting empty strings
        List<String> values = Stream.of(singleValues).map(SecurityHeaders::decode).collect(Collectors.toList());
        return values;
    }

    private static String decodeSingle(String value) {
        if (null == value) {
            return null;
        }
        // very simple implementation, we only support base64 so far
        if (value.startsWith("{base64}")) {
            value = value.substring("{base64}".length());
            byte[] bytes = Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return value;
    }

    /**
     * @return <code>{base64}</code> prefixed representations of all header values,
     *         separated by <b>non encoded</b> commas. e.g.
     *         <code>{base64}<encoded-value-1>,{base64}<ecoded-value-2>,...{base64}<encoded-value-n>
     */
    public static String encodeBase64(String... headerValues) {
        if (headerValues == null) {
            return null;
        }
        if (headerValues.length == 0) {
            return "";
        }
        String encoded;
        if (headerValues.length == 1) {
            encoded = encodeSingle(headerValues[0]);
        } else {
            encoded = Arrays.stream(headerValues).map(SecurityHeaders::encodeSingle).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
        }
        return encoded;
    }

    private static String encodeSingle(String value) {
        if (value == null) {
            return null;
        }
        if (value.isEmpty()) {
            return "";
        }
        String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return encoded.isEmpty() ? "" : "{base64}" + encoded;
    }
}