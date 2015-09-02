package org.georchestra.ldapadmin.mailservice;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

public class AccountCreationInProcessEmailTest extends EmailTest {


    @Before
    public void setUp() {
        doSetUp();

        ServletContext sc = new MockServletContext();
        mail = new AccountCreationInProcessEmail(recipients, emailSubject, smtpHost, smtpPort,
                emailHtml, replyTo, from, bodyEncoding,
                subjectEncoding, languages, fileTemplate, sc, null);

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testAccountCreationInProcessEmail() {
        //fail("Not yet implemented");
    }

    @Test
    public void testSendMsgStringString() {
        //fail("Not yet implemented");
    }

}
