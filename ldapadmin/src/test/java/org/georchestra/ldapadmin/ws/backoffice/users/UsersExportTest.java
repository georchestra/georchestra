package org.georchestra.ldapadmin.ws.backoffice.users;

import static org.junit.Assert.assertTrue;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.dto.AccountImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UsersExportTest {

    private UsersExport us;

    @Before
    public void setUp() throws Exception {
        AccountDao mockedDao = Mockito.mock(AccountDao.class);
        AccountImpl a = new AccountImpl();
        a.setCommonName("Pierre");
        a.setSurname("Mauduit");
        a.setEmail("abc@example.com");

        Mockito.when(mockedDao.findByUID(Mockito.anyString())).thenReturn(a);
        us = new UsersExport(mockedDao);
    }

    @Test
    public void testGetUsersAsCsv() throws Exception {
        System.out.println(us.getUsersAsCsv("[ \"pmauduit\" ]"));

    }

    @Test
    public void testGetUsersAsVcf() throws Exception {
        String s = us.getUsersAsVcard("[ \"pmauduit\" ]");
        assertTrue("expected ret containing BEGIN:VCARD, not found", s.startsWith("BEGIN:VCARD"));
    }

}
