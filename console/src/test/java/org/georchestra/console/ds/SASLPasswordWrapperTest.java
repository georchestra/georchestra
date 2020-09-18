package org.georchestra.console.ds;

import org.georchestra.console.dto.UserSchema;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextOperations;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
