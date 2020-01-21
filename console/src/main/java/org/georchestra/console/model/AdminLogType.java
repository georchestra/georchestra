/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.model;

public enum AdminLogType {
    /**
     * Initial events logs types Aborted since 10/2019
     * 
     * Databases table with logs before 10/2019 search the code witch correspond to
     * this old type. Keep this types to retrieve old logs and avoid server error.
     */
    ACCOUNT_MODERATION("Account moderation"), SYSTEM_ROLE_CHANGE("System role"), OTHER_ROLE_CHANGE("Other role"),
    LDAP_ATTRIBUTE_CHANGE("User attributes"),

    /**
     * New events logs types insert since 10/2019
     * 
     * Added to get more informations about logs and used to identify the real
     * modification for each action
     */
    CUSTOM_ROLE_ADDED("A custom role was added"), CUSTOM_ROLE_REMOVED("A custom role was removed"),

    EMAIL_SENT("Email sent"), EMAIL_RECOVERY_SENT("Email to recover password sent"),

    ORG_ATTRIBUTE_CHANGED("Attribute was changed for an org"),

    ORG_CREATED("An org was created"), ORG_DELETED("An org was deleted"),

    PENDING_ORG_ACCEPTED("Pending org was accepted"), PENDING_ORG_CREATED("Pending org was created"),
    PENDING_ORG_REFUSED("Pending org was refused"),

    PENDING_USER_ACCEPTED("Pending user was accepted"), PENDING_USER_CREATED("Pending user was created"),
    PENDING_USER_REFUSED("Pending user was refused"),

    ROLE_CREATED("A role was created"), ROLE_DELETED("A role was deleted"),

    SYSTEM_ROLE_REMOVED("System role was removed"), SYSTEM_ROLE_ADDED("System role was added"),

    USER_ATTRIBUTE_CHANGED("Attribute was changed for a user"),

    USER_CREATED("A user was created"), USER_DELETED("A user was deleted"),

    USER_PASSWORD_CHANGED("Password was changed for a user");

    private String name;

    private AdminLogType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
