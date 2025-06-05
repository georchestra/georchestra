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

package org.georchestra.console.ws.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.util.regex.Pattern;

/**
 *
 * @author Mauricio Pazos
 *
 */
public final class PasswordUtils {

    private Pattern LOWERS_PATTERN = Pattern.compile("[a-z]");
    private Pattern UPPERS_PATTERN = Pattern.compile("[A-Z]");
    private Pattern DIGITS_PATTERN = Pattern.compile("[0-9]");
    private Pattern SPECIALS_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    @Autowired
    private Validation validation;

    @Getter
    @Setter
    private int minimumLength = 8;
    @Getter
    @Setter
    private boolean requireLowers = false;
    @Getter
    @Setter
    private boolean requireUppers = false;
    @Getter
    @Setter
    private boolean requireDigits = false;
    @Getter
    @Setter
    private boolean requireSpecials = false;

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public void validate(final String password, final String confirmPassword, Errors errors) {

        final String pwd1 = password.trim();
        final String pwd2 = confirmPassword.trim();

        if (!StringUtils.hasLength(pwd1) && validation.isUserFieldRequired("password"))
            errors.rejectValue("password", "password.error.required", "required");

        if (!StringUtils.hasLength(pwd2) && validation.isUserFieldRequired("confirmPassword"))
            errors.rejectValue("confirmPassword", "confirmPassword.error.required", "required");

        if (StringUtils.hasLength(pwd1) && StringUtils.hasLength(pwd2)) {
            if (!pwd1.equals(pwd2)) {
                errors.rejectValue("confirmPassword", "confirmPassword.error.pwdNotEquals",
                        "These passwords don't match");
            }
            if (pwd1.length() < minimumLength) {
                errors.rejectValue("password", "password.error.sizeError",
                        new String[] { Integer.toString(minimumLength) },
                        String.format("%s%s%s", "The password must have at least ", minimumLength, " characters"));
            }
            if (requireLowers && !LOWERS_PATTERN.matcher(pwd1).find()) {
                errors.rejectValue("password", "password.error.requireLowers", "The password must contain lower cases");
            }
            if (requireUppers && !UPPERS_PATTERN.matcher(pwd1).find()) {
                errors.rejectValue("password", "password.error.requireUppers", "The password must contain upper cases");
            }
            if (requireDigits && !DIGITS_PATTERN.matcher(pwd1).find()) {
                errors.rejectValue("password", "password.error.requireDigits", "The password must contain digits");
            }
            if (requireSpecials && !SPECIALS_PATTERN.matcher(pwd1).find()) {
                errors.rejectValue("password", "password.error.requireSpecials",
                        "The password must contain special characters");
            }
        }
    }
}
