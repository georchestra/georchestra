package org.georchestra.ds;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;

@Accessors(chain = true)
public class LdapDaoProperties {

    @Getter
    @Setter
    String basePath;

    @Getter
    @Setter
    String roleSearchBaseDN;

    @Getter
    @Setter
    String orgSearchBaseDN;

    @Getter
    @Setter
    String pendingOrgSearchBaseDN;

    @Getter
    @Setter
    String[] orgTypeValues;

    @Getter
    LdapName userSearchBaseDN;

    @Getter
    LdapName pendingUserSearchBaseDN;

    public LdapDaoProperties setOrgTypeValues(String orgTypeValues) {
        this.orgTypeValues = orgTypeValues.split("\\s*,\\s*");
        return this;
    }

    public LdapDaoProperties setUserSearchBaseDN(String userSearchBaseDN) {
        this.userSearchBaseDN = LdapNameBuilder.newInstance(userSearchBaseDN).build();
        return this;
    }

    public LdapDaoProperties setPendingUserSearchBaseDN(String pendingUserSearchBaseDN) {
        this.pendingUserSearchBaseDN = LdapNameBuilder.newInstance(pendingUserSearchBaseDN).build();
        return this;
    }
}
