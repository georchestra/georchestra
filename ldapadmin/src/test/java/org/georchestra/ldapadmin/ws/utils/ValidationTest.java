package org.georchestra.ldapadmin.ws.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.JUnit4;

import java.util.HashSet;
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

        Assert.assertEquals( validation.getRequiredUserFields(), requiredUserFields );
    }

    @Test
    public void testHardCodedOrgFields(){

        Validation validation = new Validation("");
        Set<String> requiredOrgFields = new HashSet<String>();

        requiredOrgFields.add("name");

        Assert.assertEquals( validation.getRequiredOrgFields(), requiredOrgFields );

    }

    @Test
    public void testConfiguredFields(){
        Validation validation = new Validation("homePostalAddress, telephoneNumber , roomNumber");

        Assert.assertTrue(validation.isUserFieldRequired("homePostalAddress"));
        Assert.assertTrue(validation.isUserFieldRequired("telephoneNumber"));
        Assert.assertTrue(validation.isUserFieldRequired("roomNumber"));

        Assert.assertFalse(validation.isUserFieldRequired("description"));



    }

}
