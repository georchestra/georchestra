package org.georchestra.console.ws.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

public class ValidationTest {

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
}
