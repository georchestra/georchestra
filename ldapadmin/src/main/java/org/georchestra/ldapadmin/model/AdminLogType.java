package org.georchestra.ldapadmin.model;

public enum AdminLogType {

    ACCOUNT_MODERATION("Account Moderation"),
    SYSTEM_GROUP_CHANGE("Modification of system group"),
    OTHER_GROUP_CHANGE("Modification of other group"),
    LDAP_ATTRIBUTE_CHANGE("Modification of other LDAP attributes"),
    EMAIL_SENT("Email sent");

    private String name;

    private AdminLogType(String name){
        this.name = name;
    }

    public String toString(){ return name; }
}


