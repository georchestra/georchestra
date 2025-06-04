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

package org.georchestra.console.ws.newaccount;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This model maintains the account form data.
 *
 * @author Mauricio Pazos
 *
 */
public class AccountFormBean implements Serializable {

    private static final long serialVersionUID = 6955470190631684934L;

    @Getter
    @Setter
    private String uid;
    @Getter
    @Setter
    private String firstName;
    @Getter
    @Setter
    private String surname;

    @Getter
    @Setter
    private String org;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String phone;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String confirmPassword;
    @Setter
    private boolean privacyPolicyAgreed;
    @Setter
    private boolean consentAgreed;

    @Getter
    @Setter
    private String recaptcha_response_field;

    // Org creation fields
    @Setter
    private boolean createOrg;
    private @Getter @Setter String orgName;
    private @Getter @Setter String orgShortName;
    private @Getter @Setter String orgAddress;
    private @Getter @Setter String orgType;
    private @Getter @Setter String orgDescription;
    private @Getter @Setter String orgUrl;
    private @Getter @Setter String orgLogo;

    private @Getter @Setter String orgMail;

    private @Getter @Setter String orgUniqueId;

    public boolean getPrivacyPolicyAgreed() {
        return privacyPolicyAgreed;
    }

    public boolean getCreateOrg() {
        return createOrg;
    }

    public boolean getConsentAgreed() {
        return consentAgreed;
    }

    @Override
    public String toString() {
        return "AccountFormBean [uid=" + uid + ", firstName=" + firstName + ", surname=" + surname + ", org=" + org
                + ", title=" + title + ", email=" + email + ", phone=" + phone + ", description=" + description
                + ", password=" + password + ", confirmPassword=" + confirmPassword + ", privacyPolicyAgreed="
                + privacyPolicyAgreed + ", consentAgreed=" + consentAgreed + ", recaptcha_response_field="
                + recaptcha_response_field + "]";
    }

}
