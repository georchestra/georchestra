/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.config.security;

import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ADDRESS;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_EMAIL;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_FIRSTNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_LASTNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_NOTES;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORGNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_ADDRESS;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_CATEGORY;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_DESCRIPTION;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_LINKAGE;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ROLES;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_TEL;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_TITLE;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_USERNAME;

import java.util.HashMap;
import java.util.Map;

import org.georchestra.commons.security.SecurityHeaders;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.http.HttpHeaders;

import lombok.Data;

@Data
public class GeorchestraAuthenticationTestSupport implements TestRule {

    private String secProxy = "true";
    private String secUsername = "testUser";
    private String secFirstname = "Gábriel";
    private String secLastname = "Roldán";
    private String secOrg = "test'org";
    private String secOrgname = "ジョルケストラコミュニティ";
    private String secRoles = "ROLE_USER";
    private String secEmail = "test@email.com";
    private String secTel = "123456";
    private String secAddress = "Test Postal Address";
    private String secTitle = "Test Title";
    private String secNotes = "Test User notes";
    private String secOrgLinkage = "http://test.com";
    private String secOrgAddress = "Test organization address";
    private String secOrgCategory = "Testcategory";
    private String secOrgDescription = "Test org description";

    @Override
    public Statement apply(Statement base, Description description) {
        return base;
    }

    public HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> rawHeaders = buildHeaders();
        rawHeaders.forEach(headers::add);
        return headers;
    }

    public Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(SecurityHeaders.SEC_PROXY, encode(secProxy));
        headers.put(SEC_USERNAME, encode(secUsername));
        headers.put(SEC_FIRSTNAME, encode(secFirstname));
        headers.put(SEC_LASTNAME, encode(secLastname));
        headers.put(SEC_ORG, encode(secOrg));
        headers.put(SEC_ORGNAME, encode(secOrgname));
        headers.put(SEC_ROLES, encode(secRoles));
        headers.put(SEC_EMAIL, encode(secEmail));
        headers.put(SEC_TEL, encode(secTel));
        headers.put(SEC_ADDRESS, encode(secAddress));
        headers.put(SEC_TITLE, encode(secTitle));
        headers.put(SEC_NOTES, encode(secNotes));
        headers.put(SEC_ORG_LINKAGE, encode(secOrgLinkage));
        headers.put(SEC_ORG_ADDRESS, encode(secOrgAddress));
        headers.put(SEC_ORG_CATEGORY, encode(secOrgCategory));
        headers.put(SEC_ORG_DESCRIPTION, encode(secOrgDescription));
        return headers;
    }

    private String encode(String value) {
        return SecurityHeaders.encodeBase64((String) value);
    }

}
