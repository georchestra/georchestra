package org.georchestra.gateway.security.ldap;

import lombok.Data;

/**
 * Config properties, usually loaded from georchestra datadir's
 * {@literal default.properties}.
 * <p>
 * e.g.:
 * 
 * <pre>
 *{@code 
 * ldapHost=localhost
 * ldapPort=389
 * ldapScheme=ldap
 * ldapBaseDn=dc=georchestra,dc=org
 * ldapUsersRdn=ou=users
 * ldapRolesRdn=ou=roles
 * }
 * </pre>
 */
@Data
public class LdapConfigProperties {

    private String url;

    /**
     * Base DN of the LDAP directory Base Distinguished Name of the LDAP directory.
     * Also named root or suffix, see
     * http://www.zytrax.com/books/ldap/apd/index.html#base
     */

    private String baseDn = "dc=georchestra,dc=org";

    /**
     * Users RDN Relative distinguished name of the "users" LDAP organization unit.
     * E.g. if the complete name (or DN) is ou=users,dc=georchestra,dc=org, the RDN
     * is ou=users.
     */
    private String usersRdn = "ou=users";

    private String userSearchFilter = "(uid={0})";

    /**
     * Roles RDN Relative distinguished name of the "roles" LDAP organization unit.
     * E.g. if the complete name (or DN) is ou=roles,dc=georchestra,dc=org, the RDN
     * is ou=roles.
     */
    private String rolesRdn = "ou=roles";

    private String rolesSearchFilter = "(member={0})";
}
