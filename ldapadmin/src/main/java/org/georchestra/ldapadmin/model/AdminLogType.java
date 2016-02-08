/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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


