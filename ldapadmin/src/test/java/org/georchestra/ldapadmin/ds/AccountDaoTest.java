package org.georchestra.ldapadmin.ds;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import org.georchestra.ldapadmin.dto.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;


public class AccountDaoTest {

    private AccountDao us;
    private LdapContextSource contextSource;
    
    @Before
    public void setUp() throws Exception {
        assumeTrue(System.getProperty("ldapadmin.test.openldap.ldapurl") != null
                && System.getProperty("ldapadmin.test.openldap.basedn") != null
                && System.getProperty("ldapadmin.test.openldap.binddn") != null
                && System.getProperty("ldapadmin.test.openldap.password") != null);

        String ldapUrl = System.getProperty("ldapadmin.test.openldap.ldapurl");
        String baseDn = System.getProperty("ldapadmin.test.openldap.basedn");
        String ldapAdminDn = System.getProperty("ldapadmin.test.openldap.binddn");
        String ldapAdminDnPw = System.getProperty("ldapadmin.test.openldap.password");

        contextSource = new LdapContextSource();
        contextSource.setBase(baseDn);
        contextSource.setUrl(ldapUrl);
        contextSource.setBaseEnvironmentProperties(new HashMap<String, Object>());
        contextSource.setUserDn(ldapAdminDn);
        contextSource.setPassword(ldapAdminDnPw);
        contextSource.setAnonymousReadOnly(true);
        contextSource.setCacheEnvironmentProperties(false);
        AuthenticationSource authsrc =  Mockito.mock(AuthenticationSource.class);
        Mockito.when(authsrc.getPrincipal()).thenReturn(ldapAdminDn);
        Mockito.when(authsrc.getCredentials()).thenReturn(ldapAdminDnPw);
        contextSource.setAuthenticationSource(authsrc);

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        us = new AccountDaoImpl(ldapTemplate, null);
        ((AccountDaoImpl) us).setUserSearchBaseDN("ou=users");
    }

    @Test
    public void testBlankFields_issues_1086_1096() throws Exception {
        Account testadminAc  = us.findByUID("testadmin");
        String org = testadminAc.getOrg();
        
        testadminAc.setOrg(null);
        
        us.update(testadminAc);
        
        Attributes attrs = contextSource.getReadWriteContext().getAttributes(new LdapName("uid=testadmin,ou=users"));
            
        boolean hasStillUserPassword = attrs.get("userPassword") != null;
        boolean noOrgAnymore = attrs.get("o") == null;
        
        // restoring 'o' attribute before assertions, to keep original state
        testadminAc.setOrg(org);
        us.update(testadminAc);

        assertTrue("No userPassword found for testadmin, expected one", hasStillUserPassword);
        assertTrue("Found a 'o' attribute, expeceted none", noOrgAnymore);
        
    }
 
}
