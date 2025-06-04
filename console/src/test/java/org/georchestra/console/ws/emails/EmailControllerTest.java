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

package org.georchestra.console.ws.emails;

import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LASTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmailControllerTest {

    private HttpServletRequest request;
    private EmailController ctrl;
    private AccountDao accountDao;

    @Before
    public void setUpConfiguration() throws MessagingException {

        // Mock headers
        this.request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(SEC_ROLES))).thenReturn("ROLE_TEST");
        when(request.getHeader(eq(SEC_FIRSTNAME))).thenReturn("Test");
        when(request.getHeader(eq(SEC_LASTNAME))).thenReturn("Admin");
        when(request.getHeader(eq(SEC_EMAIL))).thenReturn("test-admin@georchestra.org");

        // Instanciate controller
        this.ctrl = new EmailController();
        this.ctrl.setEmailProxyMaxRecipient("10");
        this.ctrl.setEmailProxyMaxBodySize("10000");
        this.ctrl.setEmailProxyMaxSubjectSize("200");
        this.ctrl.setEmailProxyRecipientWhitelist(
                "psc@georchestra.org, postmaster@georchestra.org, listmaster@georchestra.org");

        // Account DAO
        this.accountDao = mock(AccountDao.class);
        this.ctrl.setAccountDao(accountDao);
    }

    /**
     * This test checks that JSON is valid
     */

    @Test(expected = JSONException.class)
    public void testJSONParse()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {

        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"secret@rm.fr, missing closing square bracket, " + "\"subject\": \"test email\", "
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        this.ctrl.emailProxy(jsonPayload, this.request);
    }

    /**
     * This test checks that JSON contains an array under 'to', 'cc' or 'bbc' key
     */
    @Test(expected = JSONException.class)
    public void testJSONParse2()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {

        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": \"valid@address.com\", " + "\"subject\": \"test email\", "
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        this.ctrl.emailProxy(jsonPayload, this.request);
    }

    /**
     * This test checks that EMail addresses are valid. Tests populateRecipient()
     * method
     */
    @Test(expected = AddressException.class)
    public void testParseAddress()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {

        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"invalid email address\"], " + "\"subject\": \"test email\", "
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        this.ctrl.emailProxy(jsonPayload, this.request);
    }

    /**
     * Checks that subject is present
     */
    @Test
    public void testSubjectPresent()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {
        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"valid@address.com\"], " + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (JSONException ex) {
            Assert.assertEquals("No subject specified, 'subject' field is required", ex.getMessage());
        }
    }

    /**
     * Checks that subject is not empty
     */
    @Test
    public void testSubjectNotEmpty()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {
        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"valid@address.com\"], " + "\"subject\": \"\","
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (JSONException ex) {
            Assert.assertEquals("No subject specified, 'subject' field is required", ex.getMessage());
        }
    }

    /**
     * Checks that subject length do not exceed configuration :
     * emailProxyMaxSubjectSize
     */
    @Test
    public void testSubjectTooLong()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {
        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"valid@address.com\"], "
                + "\"subject\": \"This is a very very very very very very very very very very very very very very very "
                + "very very very very very very very very very very very very very very very long subject that should "
                + "exceed configured max subject size\", "
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";
        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Subject is too long, it should not exceed 200 bytes", ex.getMessage());
        }
    }

    /**
     * Checks that body is present
     */
    @Test
    public void testBodyPresent()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {
        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"valid@address.com\"], " + "\"subject\": \"Hello\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (JSONException ex) {
            Assert.assertEquals("No body specified, 'body' field is required", ex.getMessage());
        }
    }

    /**
     * Checks that body length do not exceed configuration : emailProxyMaxBodySize
     */
    @Test
    public void testBodyTooLong()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {
        // Build a body that exceed 10000 bytes
        int bodySize = 0;
        StringBuilder body = new StringBuilder();
        while (bodySize < 10000) {
            body.append("Hi, this a test EMail, please do not reply. ");
            bodySize = body.length();
        }

        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\"], " + "\"cc\": [\"him@rm.fr\"], "
                + "\"bcc\": [\"valid@address.com\"], " + "\"subject\": \"Hello\", " + "\"body\": \"" + body + "\" }";
        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Body is too long, it should not exceed 10000 bytes", ex.getMessage());
        }
    }

    /**
     * Checks that reicpient is not missing either as 'to', 'cc' or 'bcc'
     */
    @Test
    public void testNoRecipient()
            throws UnsupportedEncodingException, MessagingException, DataServiceException, JSONException {
        String jsonPayload = "{ \"subject\": \"Hello\","
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (JSONException ex) {
            Assert.assertEquals("One of 'to', 'cc' or 'bcc' must be present in request", ex.getMessage());
        }

        jsonPayload = "{ \"to\": [], " + "\"subject\": \"Hello\","
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (JSONException ex) {
            Assert.assertEquals("One of 'to', 'cc' or 'bcc' must be present in request", ex.getMessage());
        }

        jsonPayload = "{ \"to\": [\"\"], " + "\"subject\": \"Hello\","
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (AddressException ex) {
            Assert.assertEquals("Missing final '@domain'", ex.getMessage());
        }

        jsonPayload = "{ \"to\": [], " + "\"cc\": [], " + "\"bcc\": [], " + "\"subject\": \"Hello\","
                + "\"body\": \"Hi, this a test EMail, please do not reply.\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (JSONException ex) {
            Assert.assertEquals("One of 'to', 'cc' or 'bcc' must be present in request", ex.getMessage());
        }
    }

    /**
     * Checks that recipients count does not exceed configuration
     */
    @Test
    public void testRecipientSize()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {

        String jsonPayload = "{ \"to\": [\"you@rm.fr\", \"another-guy@rm.fr\", \"another-guy@rm.fr\", \"another-guy@rm.fr\"], "
                + "\"cc\": [\"him@rm.fr\", \"another-guy@rm.fr\", \"another-guy@rm.fr\"], "
                + "\"bcc\": [\"valid@address.com\", \"another-guy@rm.fr\", \"another-guy@rm.fr\", \"another-guy@rm.fr\"], "
                + "\"subject\": \"Hello\", " + "\"body\": \"Hello, maybe there is too many recipient ?\" }";
        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Too many recipient in request, max recipient : 10", ex.getMessage());
        }
    }

    /**
     * Checks recipients whitelist
     */
    @Test
    public void testRecipientAgainstWhitelist()
            throws MessagingException, DataServiceException, JSONException, UnsupportedEncodingException {

        String jsonPayload = "{ \"to\": [\"psc@georchestra.org\"], " + "\"cc\": [\"postmaster@georchestra.org\"], "
                + "\"bcc\": [\"listmaster@georchestra.org\"], " + "\"subject\": \"Hello\", "
                + "\"body\": \"Hello, maybe there is too many recipient ?\" }";
        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
        } catch (NullPointerException ex) {
            // NullPointerException is throw because configuration is missing
            // EmailFactoryImpl
        }

        jsonPayload = "{ \"to\": [\"psc-noexist@georchestra.org\"], " + "\"cc\": [\"postmaster@georchestra.org\"], "
                + "\"bcc\": [\"listmaster@georchestra.org\"], " + "\"subject\": \"Hello\", "
                + "\"body\": \"Hello, maybe there is too many recipient ?\" }";

        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Recipient not allowed : psc-noexist@georchestra.org", ex.getMessage());
        }
    }

    /**
     * Check recipient against LDAP directory
     */
    @Test
    public void testRecipientAgainstLdap()
            throws DataServiceException, UnsupportedEncodingException, MessagingException, JSONException {
        Account account = mock(Account.class);
        when(this.accountDao.findByEmail(eq("adresse1@georchestra.org"))).thenReturn(account);
        when(this.accountDao.findByEmail(eq("adresse2@georchestra.org"))).thenReturn(account);
        when(this.accountDao.findByEmail(eq("adresse3@georchestra.org"))).thenReturn(account);

        String jsonPayload = "{ \"to\": [\"adresse1@georchestra.org\"], " + "\"cc\": [\"adresse2@georchestra.org\"], "
                + "\"bcc\": [\"adresse3@georchestra.org\"], " + "\"subject\": \"Hello\", "
                + "\"body\": \"Hello, maybe there is too many recipient ?\" }";
        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
        } catch (NullPointerException ex) {
            // NullPointerException is throw because configuration is missing
            // EmailFactoryImpl
        }

        jsonPayload = "{ \"to\": [\"adresse-tata@georchestra.org\"], " + "\"cc\": [\"adresse-toto@georchestra.org\"], "
                + "\"bcc\": [\"adresse-nonexist@georchestra.org\"], " + "\"subject\": \"Hello\", "
                + "\"body\": \"Hello, maybe there is too many recipient ?\" }";
        try {
            this.ctrl.emailProxy(jsonPayload, this.request);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Recipient not allowed : adresse-tata@georchestra.org", ex.getMessage());
        }
    }

}
