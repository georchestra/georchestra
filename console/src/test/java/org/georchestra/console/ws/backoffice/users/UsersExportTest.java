package org.georchestra.console.ws.backoffice.users;

import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.dto.AccountImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class UsersExportTest {

    private UsersExport us;

    @Before
    public void setUp() throws Exception {
        AccountDao mockedDao = mock(AccountDao.class);
        AccountImpl a = new AccountImpl();
        a.setCommonName("Pierre");
        a.setSurname("Mauduit");
        a.setEmail("abc@example.com");

        Authentication auth = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = Collections.singleton(AdvancedDelegationDao.ROLE_SUPERUSER);
        doReturn(authorities).when(auth).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(auth);

        Mockito.when(mockedDao.findByUID(Mockito.anyString())).thenReturn(a);
        UserInfoExporter exporter = new UserInfoExporterImpl(mockedDao);
        us = new UsersExport(exporter);
    }

    @Test
    public void testGetUsersAsCsv() throws Exception {
        String s = us.getUsersAsCsv("[\"pmauduit\"]");
        assertFalse("The CSV contains \"null\", unexpected", s.contains("null"));
        assertTrue("The CSV should contain \"abc@example.com\"", s.contains("abc@example.com"));
        assertTrue("The CSV should have the headers", s.startsWith("pom"));
    }

    @Test
    public void testGetUsersAsVcf() throws Exception {
        String s = us.getUsersAsVcard("[\"pmauduit\"]");

        assertTrue("expected ret containing BEGIN:VCARD, not found",
                s.startsWith("BEGIN:VCARD"));
        assertTrue("Expect vcard version to be 3", s.contains("VERSION:3.0"));
    }

    // TODO: these tests are never being run, refactor them as proper integration
    // tests
    private void setUpAgainstRealLdap() {
        assumeTrue(System.getProperty("console.test.openldap.ldapurl") != null
                && System.getProperty("console.test.openldap.basedn") != null);

        String ldapUrl = System.getProperty("console.test.openldap.ldapurl");
        String baseDn = System.getProperty("console.test.openldap.basedn");

        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(ldapUrl + baseDn);
        contextSource.setBase(baseDn);
        contextSource.setUrl(ldapUrl);
        contextSource.setBaseEnvironmentProperties(new HashMap<String, Object>());
        contextSource.setAnonymousReadOnly(true);
        contextSource.setCacheEnvironmentProperties(false);

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        AccountDaoImpl adao = new AccountDaoImpl(ldapTemplate);
        adao.setUserSearchBaseDN("ou=users");

//        us.setAccountDao(adao);
    }

    @Test
    public void testGetUsersAsVcfAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();
        String vcf = us.getUsersAsVcard("[\"testadmin\", \"testuser\"]");
        assertTrue("VCARD should contain both email address for testadmin and testuser",
                vcf.contains("psc+testuser@georchestra.org") && vcf.contains("psc+testadmin@georchestra.org"));
    }

    @Test
    public void testGetUsersAsCsvAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();
        String csv = us.getUsersAsVcard("[\"testadmin\", \"testuser\"]");

        assertTrue("CSV should contain both email address for testadmin and testuser",
                csv.contains("psc+testuser@georchestra.org") && csv.contains("psc+testadmin@georchestra.org"));
        assertEquals("CSV should contain 3 lines", 3, csv.split("\r\n").length);
    }

}
