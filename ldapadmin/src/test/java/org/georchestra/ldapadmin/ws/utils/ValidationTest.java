package org.georchestra.ldapadmin.ws.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidationTest {

    @Test
    public void testHardCodedUserFields(){

        Validation validation = new Validation("");
        Set<String> requiredUserFields = new HashSet<String>();

        requiredUserFields.add("email");
        requiredUserFields.add("uid");
        requiredUserFields.add("password");
        requiredUserFields.add("confirmPassword");

        Assert.assertEquals(validation.getRequiredUserFields(), requiredUserFields);
    }

    @Test
    public void testHardCodedOrgFields(){

        Validation validation = new Validation("");
        Set<String> requiredOrgFields = new HashSet<String>();

        requiredOrgFields.add("name");

        Assert.assertEquals(validation.getRequiredOrgFields(), requiredOrgFields);

    }

    @Test
    public void testConfiguredUserFields(){
        Validation validation = new Validation("homePostalAddress, telephoneNumber , roomNumber");

        Assert.assertTrue(validation.isUserFieldRequired("homePostalAddress"));
        Assert.assertTrue(validation.isUserFieldRequired("telephoneNumber"));
        Assert.assertTrue(validation.isUserFieldRequired("roomNumber"));

        Assert.assertFalse(validation.isUserFieldRequired("description"));
    }

    @Test
    public void testConfiguredOrgFields(){
        Validation validation = new Validation("orgType, orgShortName, org, orgOtherField");

        Assert.assertTrue(validation.isOrgFieldRequired("type"));
        Assert.assertTrue(validation.isOrgFieldRequired("shortName"));
        Assert.assertTrue(validation.isOrgFieldRequired("otherField"));

        Assert.assertFalse(validation.isOrgFieldRequired(""));
        Assert.assertFalse(validation.isOrgFieldRequired("org"));
    }

    @Test
    public void testFormat(){
        Validation validation = new Validation("  ,,  ,,#,ça devrait être utilisé, 100€,,-");

        Assert.assertTrue(validation.isUserFieldRequired("ça devrait être utilisé"));
        Assert.assertTrue(validation.isUserFieldRequired("100€"));
        Assert.assertTrue(validation.isUserFieldRequired("#"));
        int defaultFieldCount = (new Validation("")).getRequiredUserFields().size();
        Assert.assertTrue(validation.getRequiredUserFields().size() == defaultFieldCount + 4);

    }

    @Test
    public void testValidateMethod(){
        Validation v = new Validation("required_field, orgRequired_org_field");

        // non required user field
        Assert.assertTrue(v.validateUserField("name", "josé"));
        Assert.assertTrue(v.validateUserField("name", ""));
        Assert.assertTrue(v.validateUserField("name", null));

        // required user field (default)
        Assert.assertTrue(v.validateUserField("uid", "josé"));
        Assert.assertFalse(v.validateUserField("uid", ""));
        Assert.assertFalse(v.validateUserField("uid", null));

        // required user field (configured)
        Assert.assertTrue(v.validateUserField("required_field", "josé"));
        Assert.assertFalse(v.validateUserField("required_field", ""));
        Assert.assertFalse(v.validateUserField("required_field", null));

        // non required org field
        Assert.assertTrue(v.validateOrgField("type", "josé"));
        Assert.assertTrue(v.validateOrgField("type", ""));
        Assert.assertTrue(v.validateOrgField("type", null));

        // required org field (default)
        Assert.assertTrue(v.validateOrgField("name", "josé"));
        Assert.assertFalse(v.validateOrgField("name", ""));
        Assert.assertFalse(v.validateOrgField("name", null));

        // required org field (configured)
        Assert.assertTrue(v.validateOrgField("required_org_field", "josé"));
        Assert.assertFalse(v.validateOrgField("required_org_field", ""));
        Assert.assertFalse(v.validateOrgField("required_org_field", null));

    }

}
