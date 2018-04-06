package org.georchestra.console.dao;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Org;
import org.georchestra.console.model.DelegationEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Delegation2Dao {

    @Autowired
    private JpaTransactionManager tm;

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private OrgsDao orgsDao;

    private PreparedStatement byOrgStatement;

    @PostConstruct
    public void init() throws SQLException {
        this.byOrgStatement = this.tm.getDataSource().getConnection().prepareStatement(
                "SELECT uid, array_to_string(orgs, ',') AS orgs, array_to_string(roles, ',') AS roles FROM console.delegations WHERE ? = ANY(orgs)");
    }

    public List<DelegationEntry> findByOrg(String org) throws SQLException {

        List<DelegationEntry> res = new LinkedList<DelegationEntry>();
        this.byOrgStatement.setString(1,org);
        ResultSet rawRes = this.byOrgStatement.executeQuery();
        while(rawRes.next()) {
            DelegationEntry de = new DelegationEntry(rawRes.getString("uid"),
                    rawRes.getString("orgs").split(","),
                    rawRes.getString("roles").split(","));
            res.add(de);
        }

        return res;
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

}
