package org.georchestra.ds.users;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserRuleTest {

    private UserRule testUserRule;

    @BeforeEach
    public void setUp() {
        testUserRule = new UserRule();
    }

    @Test
    public void testIsProtected() {
        String[] protectedUsrs = new String[] { "protected" };

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
