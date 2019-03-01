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

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.model.DelegationEntry;
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
    private DataSource ds;

    public List<DelegationEntry> findByOrg(String org) throws SQLException {
        final String sql = "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(orgs)";
        try (Connection c = ds.getConnection(); //
                PreparedStatement byOrgStatement = c.prepareStatement(sql)) {
            byOrgStatement.setString(1, org);
            try (ResultSet resultSet = byOrgStatement.executeQuery()) {
                return this.parseResults(resultSet);
            }
        }
    }

    public List<DelegationEntry> findByRole(String cn) throws SQLException {
        final String sql = "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(roles)";
        try (Connection c = ds.getConnection(); //
                PreparedStatement byRoleStatement = c.prepareStatement(sql)) {
            byRoleStatement.setString(1, cn);
            try (ResultSet resultSet = byRoleStatement.executeQuery()) {
                return this.parseResults(resultSet);
            }
        }
    }

    public Set<String> findUsersUnderDelegation(String delegatedAdmin) {
        HashSet<String> res = new HashSet<String>();

        DelegationEntry delegation = this.delegationDao.findOne(delegatedAdmin);
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
        DelegationEntry res = new DelegationEntry(sql.getString("uid"),
                sql.getString("orgs").split(","),
                sql.getString("roles").split(","));
        return res;
    }
}
