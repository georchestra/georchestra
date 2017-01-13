package org.georchestra.ldapadmin.ws.backoffice.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.dto.AccountImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

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
        String s = us.getUsersAsCsv("[ \"pmauduit\" ]");

        assertFalse("The CSV contains \"null\", unexpected", s.contains("null"));
        assertTrue("The CSV should contain \"abc@example.com\"", s.contains("abc@example.com"));
    }

    @Test
    public void testGetUsersAsVcf() throws Exception {
        String s = us.getUsersAsVcard("[ \"pmauduit\" ]");
        assertTrue("expected ret containing BEGIN:VCARD, not found", s.startsWith("BEGIN:VCARD"));
    }

    private void setUpAgainstRealLdap() {
        assumeTrue(System.getProperty("ldapadmin.test.openldap.ldapurl") != null
                && System.getProperty("ldapadmin.test.openldap.basedn") != null);

        String ldapUrl = System.getProperty("ldapadmin.test.openldap.ldapurl");
        String baseDn = System.getProperty("ldapadmin.test.openldap.basedn");

        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(ldapUrl + baseDn);
        contextSource.setBase(baseDn);
        contextSource.setUrl(ldapUrl);
        contextSource.setBaseEnvironmentProperties(new HashMap<String, Object>());
        contextSource.setAnonymousReadOnly(true);
        contextSource.setCacheEnvironmentProperties(false);

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        AccountDaoImpl adao = new AccountDaoImpl(ldapTemplate, null, null);
        adao.setUserSearchBaseDN("ou=users");
        us.setAccountDao(adao);
    }

    @Test
    public void testGetUsersAsVcfAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();

        String vcf = us.getUsersAsVcard("[\"testadmin\", \"testuser\" ]");
        assertTrue("VCARD should contain both email address for testadmin and testuser",
                vcf.contains("psc+testuser@georchestra.org") && vcf.contains("psc+testadmin@georchestra.org"));
    }
    
    @Test
    public void testGetUsersAsCsvAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();

        String csv = us.getUsersAsCsv("[\"testadmin\", \"testuser\" ]");
        assertTrue("CSV should contain both email address for testadmin and testuser",
                csv.contains("psc+testuser@georchestra.org") && csv.contains("psc+testadmin@georchestra.org"));
        assertTrue("CSV should contain 3 lines", csv.split("\r\n").length == 3);
    }
    

}
