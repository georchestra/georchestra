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

package org.georchestra.ds.orgs;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.Name;

import org.georchestra.ds.LdapDaoProperties;
import org.georchestra.ds.users.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.util.StringUtils;

/**
 * This class manage organization membership
 */
@SuppressWarnings("unchecked")
public class OrgsDaoImpl implements OrgsDao {

    private LdapDaoProperties props;
    private LdapTemplate ldapTemplate;
    private OrgLdapWrapper orgLdapWrapper;
    private OrgExtLdapWrapper orgExtLdapWrapper;

    @Autowired
    public void setLdapDaoProperties(LdapDaoProperties ldapDaoProperties) {
        this.props = ldapDaoProperties;
    }

    @Autowired
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Autowired
    public void setOrgLdapWrapper(OrgLdapWrapper orgLdapWrapper) {
        this.orgLdapWrapper = orgLdapWrapper;
    }

    @Autowired
    public void setOrgExtLdapWrapper(OrgExtLdapWrapper orgExtLdapWrapper) {
        this.orgExtLdapWrapper = orgExtLdapWrapper;
    }

    public String[] getOrgTypeValues() {
        return props.getOrgTypeValues();
    }

    /**
     * Search all organizations defined in ldap. this.orgSearchBaseDN hold search
     * path in ldap.
     *
     * @return list of organizations
     */
    @Override
    public List<Org> findAll() {
        return findAllWithExt().collect(Collectors.toList());
    }

    private Stream<Org> findAllWithExt() {
        Filter filter = orgLdapWrapper.getObjectClassFilter();
        List<Org> active = ldapTemplate.search(props.getOrgSearchBaseDN(), filter.encode(),
                orgLdapWrapper.getAttributeMapper(false));
        List<Org> pending = ldapTemplate.search(props.getPendingOrgSearchBaseDN(), filter.encode(),
                orgLdapWrapper.getAttributeMapper(true));

        Stream<Org> orgs = Stream.concat(active.stream(), pending.stream());
        // Use lower-case id matching, as per
        // https://github.com/georchestra/georchestra/issues/3626
        final Map<String, OrgExt> exts = this.findAllExt().collect(toMap(ext -> ext.getId().toLowerCase(), identity()));

        Stream<Org> all = orgs.map(o -> o.setOrgExt(exts.get(o.getId().toLowerCase())));
        return all;
    }

    /**
     * Search all organizations defined in ldap. this.orgSearchBaseDN hold search
     * path in ldap.
     *
     * @return list of organizations (ldap organization object)
     */
    private Stream<OrgExt> findAllExt() {
        Filter filter = orgExtLdapWrapper.getObjectClassFilter();
        List<OrgExt> active = ldapTemplate.search(props.getOrgSearchBaseDN(), filter.encode(),
                orgExtLdapWrapper.getAttributeMapper(false));
        List<OrgExt> pending = ldapTemplate.search(props.getPendingOrgSearchBaseDN(), filter.encode(),
                orgExtLdapWrapper.getAttributeMapper(true));
        return Stream.concat(active.stream(), pending.stream());
    }

    /**
     * Search for validated organizations defined in ldap.
     *
     * @return list of validated organizations
     */
    @Override
    public List<Org> findValidated() {
        EqualsFilter classFilter = new EqualsFilter("objectClass", "groupOfMembers");
        AndFilter filter = new AndFilter();
        filter.and(classFilter);
        AttributesMapper<Org> notPendingMapper = orgLdapWrapper.getAttributeMapper(false);
        return ldapTemplate.search(props.getOrgSearchBaseDN(), filter.encode(), notPendingMapper)//
                .stream()//
                .map(this::addExt)//
                .collect(Collectors.toList());
    }

    /**
     * Search organization with 'commonName' as distinguish name
     *
     * @param commonName distinguish name of organization for example : 'psc' to
     *                   retrieve 'cn=psc,ou=orgs,dc=georchestra,dc=org'
     * @return Org instance with specified DN
     */
    @Override
    public Org findByCommonName(String commonName) {
        if (StringUtils.isEmpty(commonName)) {
            return null;
        }
        return addExt(orgLdapWrapper.findById(commonName));
    }

    @Override
    public Org findByUser(Account user) {
        return findByCommonName(user.getOrg());
    }

    /**
     * Search by {@link Org#getUniqueIdentifier()}
     *
     * @return the matching organization or {@code null}
     */
    @Override
    public Org findById(UUID uuid) {
        return findAllWithExt().filter(o -> uuid.equals(o.getUniqueIdentifier())).findFirst().orElse(null);
    }

    @Override
    public Org findByOrgUniqueId(String orgUniqueId) {
        return findAllWithExt().filter(o -> orgUniqueId.equals(o.getOrgUniqueId())).findFirst().orElse(null);

    }

    private Org addExt(Org org) {
        if (org == null) {
            return null;
        }
        try {
            OrgExt ext = findExtById(org.getId());
            org.setOrgExt(ext);
        } catch (NameNotFoundException extNotFound) {
            org.setOrgExt(null);
        }
        return org;
    }

    /**
     * Search for organization extension with specified identifier
     *
     * @param cn distinguish name of organization for example : 'psc' to retrieve
     *           'o=psc,ou=orgs,dc=georchestra,dc=org'
     * @return OrgExt instance corresponding to extended attributes
     */
    OrgExt findExtById(String cn) {
        return orgExtLdapWrapper.findById(cn);
    }

    @Override
    public void insert(Org org) {
        orgLdapWrapper.insert(org);
        orgExtLdapWrapper.insert(org.getExt());
    }

    @Override
    public void update(Org org) {
        orgLdapWrapper.update(org);
        orgExtLdapWrapper.update(org.getExt());
    }

    @Override
    public void delete(Org org) {
        Name orgDN = orgLdapWrapper.buildOrgDN(org);
        Name orgExtDN = orgExtLdapWrapper.buildOrgDN(org.getExt());
        this.ldapTemplate.unbind(orgDN);
        this.ldapTemplate.unbind(orgExtDN);
    }

    @Override
    public void linkUser(Account user) {
        if (StringUtils.isEmpty(user.getOrg())) {
            return;
        }
        Org org = findByCommonName(user.getOrg());
        org.getMembers().remove(user.getUid());
        org.getMembers().add(user.getUid());
        this.update(org);
    }

    @Override
    public void unlinkUser(Account user) {
        if (StringUtils.isEmpty(user.getOrg())) {
            return;
        }
        Org org = findByCommonName(user.getOrg());
        boolean removed = org.getMembers().remove(user.getUid());
        if (removed) {
            this.update(org);
        }
    }

    @Override
    public String reGenerateId(String orgName, String allowedId) throws IOException {
        // Check name
        if (orgName == null || orgName.length() == 0)
            throw new IOException("Invalid org name");

        String id = orgName.replaceAll("\\W", "_");

        int i = 0;
        String suffix = "";

        while (i < 250) {
            String cnToConsider = id + suffix;
            try {
                if (cnToConsider.equalsIgnoreCase(allowedId)) {
                    return allowedId;
                }
                this.findByCommonName(cnToConsider);
                i++;
                suffix = "" + i;
            } catch (NameNotFoundException ex) {
                return cnToConsider;
            }
        }
        throw new IOException("Trouble when generating id/cn for org.");
    }

    @Override
    public String generateId(String org_name) throws IOException {
        return reGenerateId(org_name, "");
    }

    public String buildFullOrgDn(Org org) {
        return String.format("%s,%s", orgLdapWrapper.buildOrgDN(org), props.getBasePath());
    }
}
