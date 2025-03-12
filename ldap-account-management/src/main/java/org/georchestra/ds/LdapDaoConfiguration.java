package org.georchestra.ds;

import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.roles.RoleDaoImpl;
import org.georchestra.ds.users.AccountDaoImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

@Configuration
public class LdapDaoConfiguration {

    @Bean
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public RoleDaoImpl roleDao() {
        return new RoleDaoImpl();

    }

    @Bean
    public OrgsDaoImpl orgsDao() {
        return new OrgsDaoImpl();
    }

    @Bean
    public AccountDaoImpl accountDao(LdapTemplate ldapTemplate) {
        return new AccountDaoImpl(ldapTemplate);
    }

}
