/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.console.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.georchestra.console.model.DelegationEntry;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AdvancedDelegationDao {

    public static final GrantedAuthority ROLE_SUPERUSER = new SimpleGrantedAuthority("ROLE_SUPERUSER");

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private OrgsDao orgsDao;

    @Autowired
    private DataSource dataSource;

    public List<DelegationEntry> findByOrg(String org) throws SQLException {
        final String sql = "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(orgs)";
        try (Connection c = dataSource.getConnection(); //
                PreparedStatement byOrgStatement = c.prepareStatement(sql)) {
            byOrgStatement.setString(1, org);
            try (ResultSet resultSet = byOrgStatement.executeQuery()) {
                return this.parseResults(resultSet);
            }
        }
    }

    public List<DelegationEntry> findByRole(String cn) throws SQLException {
        final String sql = "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(roles)";
        try (Connection c = dataSource.getConnection(); //
                PreparedStatement byRoleStatement = c.prepareStatement(sql)) {
            byRoleStatement.setString(1, cn);
            try (ResultSet resultSet = byRoleStatement.executeQuery()) {
                return this.parseResults(resultSet);
            }
        }
    }

    public Set<String> findUsersUnderDelegation(String delegatedAdmin) {
        HashSet<String> res = new HashSet<String>();

        DelegationEntry delegation = this.delegationDao.findFirstByUid(delegatedAdmin);
        if (delegation == null) {
            return res;
        }
        String[] orgs = delegation.getOrgs();
        for (String o : orgs) {
            Org orga = orgsDao.findByCommonName(o);
            res.addAll(orga.getMembers());
        }
        return res;
    }

    private List<DelegationEntry> parseResults(ResultSet sql) throws SQLException {
        List<DelegationEntry> res = new LinkedList<DelegationEntry>();
        while (sql.next())
            res.add(this.hydrateFromSQLResult(sql));
        return res;
    }

    private DelegationEntry hydrateFromSQLResult(ResultSet sql) throws SQLException {
        DelegationEntry res = new DelegationEntry(sql.getString("uid"), sql.getString("orgs").split(","),
                sql.getString("roles").split(","));
        return res;
    }
}
