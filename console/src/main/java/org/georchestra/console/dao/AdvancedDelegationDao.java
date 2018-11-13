package org.georchestra.console.dao;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Org;
import org.georchestra.console.model.DelegationEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AdvancedDelegationDao {

    public static final GrantedAuthority ROLE_SUPERUSER = new SimpleGrantedAuthority("ROLE_SUPERUSER");

    @Autowired
    private JpaTransactionManager tm;

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private OrgsDao orgsDao;

    private PreparedStatement byOrgStatement;
    private PreparedStatement byRoleStatement;

    @PostConstruct
    public void init() throws SQLException {
        this.byOrgStatement = this.tm.getDataSource().getConnection().prepareStatement(
                "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(orgs)");
        this.byRoleStatement = this.tm.getDataSource().getConnection().prepareStatement(
                "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(roles)");
    }

    public List<DelegationEntry> findByOrg(String org) throws SQLException {
        List<DelegationEntry> res = new LinkedList<DelegationEntry>();
        recreateStamentIfConnectionClosed();
        this.byOrgStatement.setString(1, org);
        return this.parseResults(this.byOrgStatement.executeQuery());
    }

    public List<DelegationEntry> findByRole(String cn) throws SQLException {
        List<DelegationEntry> res = new LinkedList<DelegationEntry>();
        recreateStamentIfConnectionClosed();
        this.byRoleStatement.setString(1, cn);
        return this.parseResults(this.byRoleStatement.executeQuery());
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

    private void recreateStamentIfConnectionClosed() throws SQLException {
        if(!this.byOrgStatement.getConnection().isValid(0)) {
            this.init();
        }
    }
}
