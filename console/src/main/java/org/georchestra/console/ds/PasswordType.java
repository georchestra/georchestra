package org.georchestra.console.ds;

/**
 * See https://www.openldap.org/doc/admin24/security.html section "Password
 * Storage"
 */
public enum PasswordType {
    SHA, SASL, CRYPT, MD5, SMD5, SSHA, UNKNOWN
}
