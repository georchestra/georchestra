/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ds.roles;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * A role and its users.
 *
 * @author Mauricio Pazos
 *
 */
class RoleImpl implements Role {

    private @Getter @Setter UUID uniqueIdentifier;
    private String name;
    private List<String> userList = new LinkedList<String>();
    private List<String> orgList = new LinkedList<String>();
    private String description;
    private boolean isFavorite;

    /*
     * (non-Javadoc)
     *
     * @see org.georchestra.console.dto.Role#getCommonName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.georchestra.console.dto.Role#setCommonName(java.lang.String)
     */
    @Override
    public void setName(String cn) {
        this.name = cn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.georchestra.console.dto.Role#getMemberUid()
     */
    @Override
    public List<String> getUserList() {
        return this.userList;
    }

    @Override
    public List<String> getOrgList() {
        return this.orgList;
    }

    /*
     * @see org.georchestra.console.dto.Role#setMemberUid(java.util.List)
     */
    @Override
    public void setUserList(List<String> userUidList) {
        this.userList = userUidList;
        // The full DN is stored LDAP-side, we only need
        // the user identifier (uid).
        Iterator<String> uids = userUidList.iterator();
        while (uids.hasNext()) {
            String cur = uids.next();
            cur = cur.replaceAll("uid=([^,]+).*$", "$1");
        }
    }

    /*
     * @see org.georchestra.console.dto.Role#addMemberUid(java.lang.String)
     */
    @Override
    public void addUser(String userUid) {
        // Extracting the uid
        this.userList.add(userUid.replaceAll("uid=([^,]+).*$", "$1"));
    }

    @Override
    public void addOrg(String orgUid) {
        // Extracting the uid
        this.orgList.add(orgUid.replaceAll("cn=([^,]+).*$", "$1"));
    }

    @Override
    public void addMembers(String[] members) {
        Map<String, List<String>> membersByOu = Arrays.stream(Optional.ofNullable(members).orElse(new String[0])) //
                .map(Members::new) //
                .collect(
                        Collectors.groupingBy(Members::getOu, Collectors.mapping(Members::getId, Collectors.toList())));
        orgList.addAll(membersByOu.getOrDefault("orgs", List.of()));
        userList.addAll(membersByOu.getOrDefault("users", List.of()));
    }

    @Getter
    private static class Members {
        private String id;
        private String ou = "unknown";

        public Members(String dn) {
            try {
                LdapName ldapName = new LdapName(dn);
                List<String> organizationalUnits = ldapName.getRdns().stream() //
                        .filter(rdn -> "ou".equals(rdn.getType())) //
                        .map(Rdn::getValue).map(Objects::toString).collect(Collectors.toList());
                if (organizationalUnits.contains("orgs") && !organizationalUnits.contains("users")) {
                    ou = "orgs";
                    id = dn.replaceAll("cn=([^,]+).*$", "$1");
                } else if (!organizationalUnits.contains("orgs") && organizationalUnits.contains("users")) {
                    ou = "users";
                    id = dn.replaceAll("uid=([^,]+).*$", "$1");
                }
            } catch (InvalidNameException e) {
            }
        }
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    @Override
    public boolean isFavorite() {
        return this.isFavorite;
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public void setPending(boolean pending) {

    }

    @Override
    public String toString() {
        return "RoleImpl [name=" + name + ", userList=" + userList + ", description=" + description + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RoleImpl)) {
            return false;
        }
        RoleImpl other = (RoleImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Role o) {

        return this.name.compareTo(o.getName());
    }

}
