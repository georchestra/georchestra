package org.georchestra.ldapadmin.ws.utils;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

public class UserUtilsTest {


    private Errors errors;

    @Before
    public void setUp() throws Exception {
        errors  =  new MapBindingResult(new HashMap(), this.toString());
    }


    @Test
    @Ignore("Validation class needs revamp, the required fields state is for now unpredictable")
    public void testValidateFirstNameSurName() {
        UserUtils.validate("", "", errors);

        assertTrue(errors.getAllErrors().size() == 0);

        // This should populate errors hashmap (regular case)
        Validation v = new Validation();
        v.setRequiredFields("firstName,surname");

        UserUtils.validate("", "blah", errors);

        assertTrue(errors.getAllErrors().size() == 1);

        // Same but with firstname
        UserUtils.validate("blah", "", errors);

        assertTrue(errors.getAllErrors().size() == 2);

    }

    @Test
    @Ignore("Validation class needs revamp, the required fields state is for now unpredictable")
    public void testValidateAll() {
        UserUtils.validate("", "", "", errors);

        assertTrue(errors.getAllErrors().size() == 3);

        Validation v = new Validation();
        v.setRequiredFields("firstName,surname");

        UserUtils.validate(null, null, null, errors);

        assertTrue(errors.getAllErrors().size() == 6);

        // invalid uid
        UserUtils.validate("1pmauduit", "blah", "bloh", errors);

        assertTrue(errors.getAllErrors().size() == 7);


    }

}
