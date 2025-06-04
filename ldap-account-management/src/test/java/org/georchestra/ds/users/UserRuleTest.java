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

package org.georchestra.ds.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
