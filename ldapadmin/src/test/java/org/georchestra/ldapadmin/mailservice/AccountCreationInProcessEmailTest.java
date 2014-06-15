package org.georchestra.ldapadmin.mailservice;

import org.georchestra.lib.mailservice.Email;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AccountCreationInProcessEmailTest extends EmailTest {



    @Before
    public void setUp() throws Exception {
        Email m = new AccountCreationInProcessEmail(null, null, null, 0, null, null, null, null, null, null, null, null);
        mail = m;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testToAbsoltuPath() {
        //fail("Not yet implemented");
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
