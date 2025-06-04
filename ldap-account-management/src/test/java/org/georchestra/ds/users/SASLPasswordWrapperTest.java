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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextOperations;

public class SASLPasswordWrapperTest {

    @Test
    public void testSASLPasswordWrapper() {
        DirContextOperations context = Mockito.mock(DirContextOperations.class);
        Mockito.when(context.getObjectAttribute(Mockito.eq(UserSchema.USER_PASSWORD_KEY))).thenReturn(
                "{SASL}dudu".getBytes(), "{sasl}popo".getBytes(), "#3242}}{{{{@@@@".getBytes(),
                "{ssha}12345677889999666==".getBytes(), "{SSHA}2343243244".getBytes(), "{sha}232343zedze==".getBytes());

        AccountDaoImpl.SASLPasswordWrapper spw = new AccountDaoImpl.SASLPasswordWrapper(context);
        assertTrue(spw.getPasswordType().equals(PasswordType.SASL));
        assertTrue(spw.getUserName().equals("dudu"));

        spw = new AccountDaoImpl.SASLPasswordWrapper(context);
        assertTrue(spw.getPasswordType().equals(PasswordType.SASL));
        assertTrue(spw.getUserName().equals("popo"));

        spw = new AccountDaoImpl.SASLPasswordWrapper(context);
        assertTrue(spw.getPasswordType().equals(PasswordType.UNKNOWN));
        assertNull(spw.getUserName());

        spw = new AccountDaoImpl.SASLPasswordWrapper(context);
        assertTrue(spw.getPasswordType().equals(PasswordType.SSHA));
        assertNull(spw.getUserName());

        spw = new AccountDaoImpl.SASLPasswordWrapper(context);
        assertTrue(spw.getPasswordType().equals(PasswordType.SSHA));
        assertNull(spw.getUserName());

        spw = new AccountDaoImpl.SASLPasswordWrapper(context);
        assertTrue(spw.getPasswordType().equals(PasswordType.SHA));
        assertNull(spw.getUserName());
    }
}
