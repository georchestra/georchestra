/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.console.ws.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import org.json.JSONObject;

public class ValidationTest {

    private OrgsDao mockOrgsDao;

    @Test
    public void testHardCodedUserFields() {

        Validation validation = new Validation("");
        Set<String> requiredUserFields = new HashSet<String>();

        requiredUserFields.add("email");
        requiredUserFields.add("newEmail");
        requiredUserFields.add("uid");
        requiredUserFields.add("password");
        requiredUserFields.add("confirmPassword");

        Assertions.assertEquals(validation.getRequiredUserFields(), requiredUserFields);
    }

    @Test
    public void testHardCodedOrgFields() {

        Validation validation = new Validation("");
        Set<String> expected = new HashSet<String>();

        expected.add("name");
        expected.add("shortName");
        expected.add("type");

        Assertions.assertEquals(validation.getRequiredOrgFields(), expected);

    }

    @Test
    public void testConfiguredUserFields() {
        Validation validation = new Validation("homePostalAddress, telephoneNumber , roomNumber");

        Assertions.assertTrue(validation.isUserFieldRequired("homePostalAddress"));
        Assertions.assertTrue(validation.isUserFieldRequired("telephoneNumber"));
        Assertions.assertTrue(validation.isUserFieldRequired("roomNumber"));

        Assertions.assertFalse(validation.isUserFieldRequired("description"));
    }

    @Test
    public void testConfiguredOrgFields() {
        Validation validation = new Validation("orgType, orgShortName, org, orgOtherField");

        Assertions.assertTrue(validation.isOrgFieldRequired("type"));
        Assertions.assertTrue(validation.isOrgFieldRequired("shortName"));
        Assertions.assertTrue(validation.isOrgFieldRequired("otherField"));

        Assertions.assertFalse(validation.isOrgFieldRequired(""));
        Assertions.assertFalse(validation.isOrgFieldRequired("org"));
    }

    @Test
    public void testFormat() {
        Validation validation = new Validation("  ,,  ,,#,ça devrait être utilisé, 100€,,-");

        Assertions.assertTrue(validation.isUserFieldRequired("ça devrait être utilisé"));
        Assertions.assertTrue(validation.isUserFieldRequired("100€"));
        Assertions.assertTrue(validation.isUserFieldRequired("#"));
        int defaultFieldCount = (new Validation("")).getRequiredUserFields().size();
        Assertions.assertTrue(validation.getRequiredUserFields().size() == defaultFieldCount + 4);

    }

    @Test
    public void testValidateMethod() {
        Validation v = new Validation("required_field, orgRequired_org_field");

        // non required user field
        Assertions.assertTrue(v.validateUserField("name", "josé"));
        Assertions.assertTrue(v.validateUserField("name", ""));
        Assertions.assertTrue(v.validateUserField("name", (String) null));

        // required user field (default)
        Assertions.assertTrue(v.validateUserField("uid", "josé"));
        Assertions.assertFalse(v.validateUserField("uid", ""));
        Assertions.assertFalse(v.validateUserField("uid", (String) null));

        // required user field (configured)
        Assertions.assertTrue(v.validateUserField("required_field", "josé"));
        Assertions.assertFalse(v.validateUserField("required_field", ""));
        Assertions.assertFalse(v.validateUserField("required_field", (String) null));

        // non required org field
        Assertions.assertTrue(v.validateOrgField("type", "Association"));
        Assertions.assertFalse(v.validateOrgField("type", (String) null));
        Assertions.assertFalse(v.validateOrgField("type", ""));

        // required org field (default)
        Assertions.assertTrue(v.validateOrgField("name", "josé"));
        Assertions.assertFalse(v.validateOrgField("name", ""));
        Assertions.assertFalse(v.validateOrgField("name", (String) null));

        // required org field (configured)
        Assertions.assertTrue(v.validateOrgField("required_org_field", "josé"));
        Assertions.assertFalse(v.validateOrgField("required_org_field", ""));
        Assertions.assertFalse(v.validateOrgField("required_org_field", (String) null));

    }

    @Test
    public void validateBadUrl() {
        Validation v = new Validation("");

        Errors errors = new MapBindingResult(new HashMap<>(), "errors");

        Assertions.assertTrue(v.validateUrlFieldWithSpecificMsg("orgUrl", "", errors));

        Assertions.assertFalse(v.validateUrlFieldWithSpecificMsg("orgUrl", "radada", errors));

        Assertions.assertTrue(v.validateUrlFieldWithSpecificMsg("orgUrl", "http://www.hereisthefish.org", errors));
    }

    @Test
    public void validateBadOrgUnicityUpdate() {
        Validation v = new Validation("");

        UUID fakeUUID = UUID.randomUUID();
        String fakeOrgUniqueId = "5413513131";

        mockOrgsDao = mock(OrgsDao.class);

        Org mockOrg = new Org();
        mockOrg.setId("georTest");
        mockOrg.setName("geOrchestra testing LLC");
        mockOrg.setOrgType("Non profit");
        mockOrg.setAddress("fake address");
        mockOrg.setUrl("https://georchestra.org");
        mockOrg.setDescription("A test desc");
        mockOrg.setOrgUniqueId(fakeOrgUniqueId);
        mockOrg.setUniqueIdentifier(fakeUUID);

        when(mockOrgsDao.findByOrgUniqueId(fakeOrgUniqueId)).thenReturn(mockOrg);

        /** Fake JSON from request */
        JSONObject mockChanges = new JSONObject();
        mockChanges.put("name", "foo");
        mockChanges.put("orgType", "Non profit");

        // create org without orguniqueId + without uuid
        Assertions.assertTrue(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // create org but orgUniqueId already exists
        mockChanges.put("orgUniqueId", fakeOrgUniqueId);
        Assertions.assertFalse(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // create org with orgUniqueId that not exists
        mockChanges.put("orgUniqueId", "11113513");
        Assertions.assertTrue(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // update org - no UUID and orgUniqueId already exists
        mockChanges.put("orgUniqueId", fakeOrgUniqueId);
        Assertions.assertFalse(v.validateOrgUnicityByUniqueId(mockOrgsDao, mockChanges));

        // update org - unknown UUID + orgUniqueId not already exists
        mockChanges.put("uuid", UUID.randomUUID().toString());
        Assertions.assertFalse(v.validateOrgUnicityByUniqueId(mockOrgsDao, mockChanges));

        // update org - verified UUID + orgUniqueId not already exists
        mockChanges.put("uuid", fakeUUID.toString());
        Assertions.assertTrue(v.validateOrgUnicityByUniqueId(mockOrgsDao, mockChanges));

    }

    @Test
    public void validateOrgUnicityByShortName() {
        Validation v = new Validation("");

        UUID fakeUUID = UUID.randomUUID();
        String fakeShortName = "testorg";

        mockOrgsDao = mock(OrgsDao.class);

        Org mockOrg = new Org();
        mockOrg.setId("testorg");
        mockOrg.setName("Test Organization");
        mockOrg.setShortName(fakeShortName);
        mockOrg.setOrgType("Non profit");
        mockOrg.setAddress("fake address");
        mockOrg.setUrl("https://georchestra.org");
        mockOrg.setDescription("A test desc");
        mockOrg.setUniqueIdentifier(fakeUUID);

        when(mockOrgsDao.findByShortName(fakeShortName)).thenReturn(mockOrg);

        /** Fake JSON from request */
        JSONObject mockChanges = new JSONObject();
        mockChanges.put("name", "foo");
        mockChanges.put("orgType", "Non profit");

        // create org without shortName
        Assertions.assertTrue(v.validateOrgUnicityByShortName(mockOrgsDao, mockChanges));

        // create org but shortName already exists
        mockChanges.put("shortName", fakeShortName);
        Assertions.assertFalse(v.validateOrgUnicityByShortName(mockOrgsDao, mockChanges));

        // create org with shortName that does not exist
        mockChanges.put("shortName", "neworg");
        Assertions.assertTrue(v.validateOrgUnicityByShortName(mockOrgsDao, mockChanges));

        // update org - no UUID and shortName already exists
        mockChanges.put("shortName", fakeShortName);
        Assertions.assertFalse(v.validateOrgUnicityByShortName(mockOrgsDao, mockChanges));

        // update org - unknown UUID + shortName already exists
        mockChanges.put("uuid", UUID.randomUUID().toString());
        Assertions.assertFalse(v.validateOrgUnicityByShortName(mockOrgsDao, mockChanges));

        // update org - verified UUID + shortName already exists (same org)
        mockChanges.put("uuid", fakeUUID.toString());
        Assertions.assertTrue(v.validateOrgUnicityByShortName(mockOrgsDao, mockChanges));
    }

    @Test
    public void validateOrgUnicityIncludesShortName() {
        Validation v = new Validation("");

        UUID fakeUUID = UUID.randomUUID();
        String fakeShortName = "existingorg";

        mockOrgsDao = mock(OrgsDao.class);

        Org mockOrg = new Org();
        mockOrg.setId("existingorg");
        mockOrg.setName("Existing Organization");
        mockOrg.setShortName(fakeShortName);
        mockOrg.setUniqueIdentifier(fakeUUID);

        when(mockOrgsDao.findByShortName(fakeShortName)).thenReturn(mockOrg);

        JSONObject mockChanges = new JSONObject();
        mockChanges.put("name", "New Org");
        mockChanges.put("shortName", fakeShortName);

        // validateOrgUnicity should fail when shortName already exists
        Assertions.assertFalse(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // validateOrgUnicity should pass when shortName doesn't exist
        mockChanges.put("shortName", "brandneworg");
        Assertions.assertTrue(v.validateOrgUnicity(mockOrgsDao, mockChanges));
    }
}
