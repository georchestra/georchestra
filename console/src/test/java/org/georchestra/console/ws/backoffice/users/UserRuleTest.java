package org.georchestra.console.ws.backoffice.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class UserRuleTest {

    private UserRule testUserRule;

    @Before
    public void setUp() {
        testUserRule = new UserRule();
    }

    @Test
    public void testIsProtected() {
        String[] protectedUsrs = new String[]{"protected"};

        assertFalse(testUserRule.isProtected("protected"));
        assertTrue(testUserRule.getListOfprotectedUsers().size() == 0);

        testUserRule.setListOfprotectedUsers(protectedUsrs);

        assertTrue(testUserRule.isProtected("protected"));
        // Well, this test is more of about testing that the JVM isn't flawed ...
        assertTrue(testUserRule.getListOfprotectedUsers().contains("protected"));
        // TODO Why is there 2 getters which do basically the same thing ?
        assertTrue(testUserRule.getListUidProtected().contains("protected"));


    }
}
