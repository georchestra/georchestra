package org.georchestra.console.ws.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import org.json.JSONObject;

public class ValidationTest {

    private OrgsDao mockOrgsDao;
    private Org mockOrg;

    @Test
    public void testHardCodedUserFields() {

        Validation validation = new Validation("");
        Set<String> requiredUserFields = new HashSet<String>();

        requiredUserFields.add("email");
        requiredUserFields.add("newEmail");
        requiredUserFields.add("uid");
        requiredUserFields.add("password");
        requiredUserFields.add("confirmPassword");

        Assert.assertEquals(validation.getRequiredUserFields(), requiredUserFields);
    }

    @Test
    public void testHardCodedOrgFields() {

        Validation validation = new Validation("");
        Set<String> expected = new HashSet<String>();

        expected.add("name");
        expected.add("shortName");
        expected.add("type");

        Assert.assertEquals(validation.getRequiredOrgFields(), expected);

    }

    @Test
    public void testConfiguredUserFields() {
        Validation validation = new Validation("homePostalAddress, telephoneNumber , roomNumber");

        Assert.assertTrue(validation.isUserFieldRequired("homePostalAddress"));
        Assert.assertTrue(validation.isUserFieldRequired("telephoneNumber"));
        Assert.assertTrue(validation.isUserFieldRequired("roomNumber"));

        Assert.assertFalse(validation.isUserFieldRequired("description"));
    }

    @Test
    public void testConfiguredOrgFields() {
        Validation validation = new Validation("orgType, orgShortName, org, orgOtherField");

        Assert.assertTrue(validation.isOrgFieldRequired("type"));
        Assert.assertTrue(validation.isOrgFieldRequired("shortName"));
        Assert.assertTrue(validation.isOrgFieldRequired("otherField"));

        Assert.assertFalse(validation.isOrgFieldRequired(""));
        Assert.assertFalse(validation.isOrgFieldRequired("org"));
    }

    @Test
    public void testFormat() {
        Validation validation = new Validation("  ,,  ,,#,ça devrait être utilisé, 100€,,-");

        Assert.assertTrue(validation.isUserFieldRequired("ça devrait être utilisé"));
        Assert.assertTrue(validation.isUserFieldRequired("100€"));
        Assert.assertTrue(validation.isUserFieldRequired("#"));
        int defaultFieldCount = (new Validation("")).getRequiredUserFields().size();
        Assert.assertTrue(validation.getRequiredUserFields().size() == defaultFieldCount + 4);

    }

    @Test
    public void testValidateMethod() {
        Validation v = new Validation("required_field, orgRequired_org_field");

        // non required user field
        Assert.assertTrue(v.validateUserField("name", "josé"));
        Assert.assertTrue(v.validateUserField("name", ""));
        Assert.assertTrue(v.validateUserField("name", (String) null));

        // required user field (default)
        Assert.assertTrue(v.validateUserField("uid", "josé"));
        Assert.assertFalse(v.validateUserField("uid", ""));
        Assert.assertFalse(v.validateUserField("uid", (String) null));

        // required user field (configured)
        Assert.assertTrue(v.validateUserField("required_field", "josé"));
        Assert.assertFalse(v.validateUserField("required_field", ""));
        Assert.assertFalse(v.validateUserField("required_field", (String) null));

        // non required org field
        Assert.assertTrue(v.validateOrgField("type", "Association"));
        Assert.assertFalse(v.validateOrgField("type", (String) null));
        Assert.assertFalse(v.validateOrgField("type", ""));

        // required org field (default)
        Assert.assertTrue(v.validateOrgField("name", "josé"));
        Assert.assertFalse(v.validateOrgField("name", ""));
        Assert.assertFalse(v.validateOrgField("name", (String) null));

        // required org field (configured)
        Assert.assertTrue(v.validateOrgField("required_org_field", "josé"));
        Assert.assertFalse(v.validateOrgField("required_org_field", ""));
        Assert.assertFalse(v.validateOrgField("required_org_field", (String) null));

    }

    @Test
    public void validateBadUrl() {
        Validation v = new Validation("");

        Errors errors = new MapBindingResult(new HashMap<>(), "errors");

        Assert.assertTrue(v.validateUrlFieldWithSpecificMsg("orgUrl", "", errors));

        Assert.assertFalse(v.validateUrlFieldWithSpecificMsg("orgUrl", "radada", errors));

        Assert.assertTrue(v.validateUrlFieldWithSpecificMsg("orgUrl", "http://www.hereisthefish.org", errors));
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
        Assert.assertTrue(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // create org but orgUniqueId already exists
        mockChanges.put("orgUniqueId", fakeOrgUniqueId);
        Assert.assertFalse(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // create org with orgUniqueId that not exists
        mockChanges.put("orgUniqueId", "11113513");
        Assert.assertTrue(v.validateOrgUnicity(mockOrgsDao, mockChanges));

        // update org - no UUID and orgUniqueId already exists
        mockChanges.put("orgUniqueId", fakeOrgUniqueId);
        Assert.assertFalse(v.validateOrgUnicityByUniqueId(mockOrgsDao, mockChanges));

        // update org - unknown UUID + orgUniqueId not already exists
        mockChanges.put("uuid", UUID.randomUUID().toString());
        Assert.assertFalse(v.validateOrgUnicityByUniqueId(mockOrgsDao, mockChanges));

        // update org - verified UUID + orgUniqueId not already exists
        mockChanges.put("uuid", fakeUUID.toString());
        Assert.assertTrue(v.validateOrgUnicityByUniqueId(mockOrgsDao, mockChanges));

    }
}
