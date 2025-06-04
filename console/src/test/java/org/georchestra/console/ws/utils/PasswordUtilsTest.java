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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

public class PasswordUtilsTest {

    PasswordUtils passwordUtils = new PasswordUtils();

    @Before
    public void setUp() {
        passwordUtils.setValidation(new Validation(""));
        passwordUtils.setMinimumLength(16);
        passwordUtils.setRequireLowers(true);
        passwordUtils.setRequireUppers(true);
        passwordUtils.setRequireDigits(true);
        passwordUtils.setRequireSpecials(true);
    }

    @Test
    public void invalidPasswords() {
        assertPasswordErrorCount("justLessThan16!", 1);
        assertPasswordErrorCount("lowercaseslowercases", 3);
        assertPasswordErrorCount("lowercaseslowercases0", 2);
        assertPasswordErrorCount("lowercaseslowercases!", 2);
        assertPasswordErrorCount("lowercaseslowercases0!", 1);
        assertPasswordErrorCount("UPPERCASESUPPERCASES", 3);
        assertPasswordErrorCount("UPPERCASESUPPERCASES!", 2);
        assertPasswordErrorCount("UPPERCASESUPPERCASES0", 2);
        assertPasswordErrorCount("UPPERCASESUPPERCASES0!", 1);
        assertPasswordErrorCount("MiXedCaSesMiXedCaSes", 2);
        assertPasswordErrorCount("MiXedCaSesMiXedCaSes!", 1);
        assertPasswordErrorCount("MiXedCaSesMiXedCaSes0", 1);
        assertPasswordErrorCount("12345678901234567", 3);
        assertPasswordErrorCount("12345678901234567!", 2);
        assertPasswordErrorCount(",?;.:/!\\$%*µ(){}+-_#&", 3);
        assertPasswordErrorCount("OnlyLettersAnd123456", 1);
    }

    @Test
    public void differentPasswords() {
        Errors errors = new MapBindingResult(new HashMap<>(), "errors");
        passwordUtils.validate("differentPasswords123!", "differentPasswords124!", errors);
        Assert.assertEquals(1, errors.getErrorCount());
    }

    @Test
    public void validPassword() {
        PasswordUtils passwordUtils = new PasswordUtils();
        passwordUtils.setValidation(new Validation("password, confirmPassword"));

        Errors errors = new MapBindingResult(new HashMap<>(), "errors");
        passwordUtils.validate("$valid-Password_Of(30)Symbols!", "$valid-Password_Of(30)Symbols!", errors);
        Assert.assertEquals(false, errors.hasErrors());
    }

    private void assertPasswordErrorCount(String password, int expectedErrorCount) {
        Errors errors = new MapBindingResult(new HashMap<>(), "errors");
        passwordUtils.validate(password, password, errors);
        Assert.assertEquals(expectedErrorCount, errors.getErrorCount());
    }
}
