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

package org.georchestra.console.ds;


import org.georchestra.console.dto.Org;
import org.georchestra.console.dto.OrgExt;
import org.georchestra.console.dto.ReferenceAware;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class manage organization membership
 */
public class OrgsDao {

    private LdapTemplate ldapTemplate;
    private Name orgSearchBaseDN;
    private Name userSearchBaseDN;
    private String basePath;
    private String[] orgTypeValues;


    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setOrgSearchBaseDN(String orgSearchBaseDN) {
        this.orgSearchBaseDN = LdapNameBuilder.newInstance(orgSearchBaseDN).build();
    }

    public void setUserSearchBaseDN(String userSearchBaseDN) {
        this.userSearchBaseDN = LdapNameBuilder.newInstance(userSearchBaseDN).build();
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }

    /**
     * Search all organizations defined in ldap. this.orgSearchBaseDN hold search path in ldap.
     *
     * @return list of organizations
     */
    public List<Org> findAll(){
        EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");
        return ldapTemplate.search(this.orgSearchBaseDN, filter.encode(), new OrgsDao.OrgAttributesMapper());
    }

    /**
     * Search for validated organizations defined in ldap.
     *
     * @return list of validated organizations
     */
    public List<Org> findValidated(){
        EqualsFilter classFilter = new EqualsFilter("objectClass", "groupOfMembers");
        EqualsFilter validatedFilter = new EqualsFilter("businessCategory", Org.STATUS_REGISTERED);
        AndFilter filter = new AndFilter();
        filter.and(classFilter);
        filter.and(validatedFilter);
        return ldapTemplate.search(this.orgSearchBaseDN, filter.encode(), new OrgsDao.OrgAttributesMapper());
    }

    /**
     * Search all organizations defined in ldap. this.orgSearchBaseDN hold search path in ldap.
     *
     * @return list of organizations (ldap organization object)
     */
    public List<OrgExt> findAllExt(){
        EqualsFilter filter = new EqualsFilter("objectClass", "organization");
        return ldapTemplate.search(this.orgSearchBaseDN, filter.encode(), new OrgsDao.OrgExtAttributesMapper());
    }


    /**
     * Search organization with 'commonName' as distinguish name
     * @param commonName distinguish name of organization for example : 'psc' to retrieve
     *                   'cn=psc,ou=orgs,dc=georchestra,dc=org'
     * @return Org instance with specified DN
     */
    public Org findByCommonName(String commonName) {
        Name dn = LdapNameBuilder.newInstance(this.orgSearchBaseDN).add("cn", commonName).build();
        return this.ldapTemplate.lookup(dn, new ContextMapperSecuringReferenceAndMappingAttributes<Org>(new OrgAttributesMapper()));
    }

    /**
     * Search for organization extension with specified identifier
     * @param cn distinguish name of organization for example : 'psc' to retrieve
     *           'o=psc,ou=orgs,dc=georchestra,dc=org'
     * @return OrgExt instance corresponding to extended attributes
     */
    public OrgExt findExtById(String cn) {
        Name dn = LdapNameBuilder.newInstance(this.orgSearchBaseDN).add("o", cn).build();
        return this.ldapTemplate.lookup(dn, new ContextMapperSecuringReferenceAndMappingAttributes<OrgExt>(new OrgExtAttributesMapper()));
    }

    /**
     * Given user identifier, retrieve organization of this user.
     * @param user identifier of user (not a full DN), example : 'testadmin'
     * @return Org instance corresponding to organization of specified user or null if no organization is linked to
     * this user
     * @throws DataServiceException if more than one organization is linked to specified user
     */
    public Org findForUser(String user) throws DataServiceException {

        Name userDn = LdapNameBuilder
                .newInstance(this.basePath)
                .add(this.userSearchBaseDN)
                .add("uid", user)
                .build();

        AndFilter filter  = new AndFilter();
        filter.and(new EqualsFilter("member", userDn.toString()));
        filter.and(new EqualsFilter("objectClass", "groupOfMembers"));
        List<Org> res = ldapTemplate.search(this.orgSearchBaseDN, filter.encode(), new OrgsDao.OrgAttributesMapper());
        if(res.size() > 1)
            throw new DataServiceException("Multiple org for user : " + user);
        if(res.size() == 1)
            return res.get(0);
        else
            return null;

    }

    public void insert(Org org){
        this.ldapTemplate.bind(buildOrgDN(org.getId()), null, buildAttributes(org));
    }

    public void insert(OrgExt org){
        this.ldapTemplate.bind(buildOrgExtDN(org.getId()), null, buildAttributes(org));
    }

    public void update(Org org){
        Name newName = buildOrgDN(org.getId());
        this.ldapTemplate.rename(org.getReference().getDn(), newName);
        this.ldapTemplate.rebind(newName, null, buildAttributes(org));
    }

    public void update(OrgExt orgExt){
        Name newName = buildOrgExtDN(orgExt.getId());
        this.ldapTemplate.rename(orgExt.getReference().getDn(), newName);
        this.ldapTemplate.rebind(newName, null, buildAttributes(orgExt));
    }

    public void delete(Org org){
        this.ldapTemplate.unbind(buildOrgDN(org.getId()));
    }

    public void delete(OrgExt org){
        this.ldapTemplate.unbind(buildOrgExtDN(org.getId()));
    }

    public void addUser(String organization, String user){
        DirContextOperations context = ldapTemplate.lookupContext(buildOrgDN(organization).toString());
        context.addAttributeValue("member", buildUserDN(user).toString(), false);
        this.ldapTemplate.modifyAttributes(context);
    }

    public void removeUser(String organization, String user){
        DirContextOperations ctx = ldapTemplate.lookupContext(buildOrgDN(organization).toString());
        ctx.removeAttributeValue("member", buildUserDN(user).toString());
        this.ldapTemplate.modifyAttributes(ctx);
    }

    private Name buildUserDN(String id){
        return LdapNameBuilder.newInstance(this.userSearchBaseDN + "," + this.basePath).add("uid", id).build();
    }

    private Name buildOrgDN(String id){
        return LdapNameBuilder.newInstance(this.orgSearchBaseDN).add("cn", id).build();
    }

    private Name buildOrgExtDN(String id){
        return LdapNameBuilder.newInstance(this.orgSearchBaseDN).add("o", id).build();
    }


    private Attributes buildAttributes(Org org) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("top");
        ocattr.add("groupOfMembers");

        attrs.put(ocattr);
        attrs.put("cn", org.getId());
        attrs.put("seeAlso", LdapNameBuilder.newInstance(this.orgSearchBaseDN + "," + this.basePath)
                .add("o", org.getId()).build().toString());

        // Mandatory attribute
        attrs.put("o", org.getName());
        
        // Add members if present
        // Add members
        if(org.getMembers() != null && org.getMembers().size() > 0) {
            BasicAttribute members = new BasicAttribute("member");
            for (String member : org.getMembers())
                members.add(buildUserDN(member).toString());
            attrs.put(members);
        }

        // Optional ones
        if(org.getShortName() != null)
            attrs.put("ou", org.getShortName());

        if(org.getCities() != null) {
            BasicAttribute description = new BasicAttribute("description");
            StringBuilder buffer = new StringBuilder();
            // description field max size : 1024
            int maxFieldSize = 1000;

            for (String city : org.getCities()) {
                if (buffer.length() > maxFieldSize) {
                    description.add(buffer.substring(1));
                    buffer = new StringBuilder();
                }
                buffer.append("," + city);
            }
            if (buffer.length() > 0)
                description.add(buffer.substring(1));

            if(description.size() > 0)
                attrs.put(description);
        }

        if(org.getStatus() != null)
            attrs.put("businessCategory", org.getStatus());

        return attrs;
    }

    private Attributes buildAttributes(OrgExt org) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("top");
        ocattr.add("organization");

        attrs.put(ocattr);
        attrs.put("o", org.getId());
        if(org.getOrgType() != null)
            attrs.put("businessCategory", org.getOrgType());
        if(org.getAddress() != null)
            attrs.put("postalAddress", org.getAddress());
        if(org.getNumericId() != null)
            attrs.put("destinationIndicator", org.getNumericId().toString());

        return attrs;
    }

    public String[] getOrgTypeValues() {
        return orgTypeValues;
    }

    public void setOrgTypeValues(String orgTypeValues) {
        this.orgTypeValues = orgTypeValues.split("\\s*,\\s*");
    }

    public String generateId(String org_name) throws IOException {

        // Check name
        if(org_name == null || org_name.length() == 0)
            throw new IOException("Invalid org name");

        String id = org_name.replaceAll("\\W", "_");

        try {
            this.findByCommonName(id);
        } catch (NameNotFoundException ex){
            return id;
        }

        int i = 0;
        while(true){
            i++;
            try{
                this.findByCommonName(id + i);
                break;
            } catch (NameNotFoundException ex){
                continue;
            }
        }
        return id + i;
    }

    public Integer generateNumericId() {

        EqualsFilter filter = new EqualsFilter("objectClass", "organization");
        List<OrgExt> orgs = ldapTemplate.search(this.orgSearchBaseDN, filter.encode(), new OrgsDao.OrgExtAttributesMapper());
        Integer maxId = 0;

        for(OrgExt org : orgs){
            Integer orgId = org.getNumericId();
            if(orgId == null)
                continue;
            if(orgId > maxId)
            maxId = orgId;
        }

        return maxId + 1;

    }

    private class OrgAttributesMapper implements AttributesMapper<Org> {

        public Org mapFromAttributes(Attributes attrs) throws NamingException {
            Org org = new Org();
            org.setId(asString(attrs.get("cn")));
            org.setName(asString(attrs.get("o")));
            org.setShortName(asString(attrs.get("ou")));
            if(attrs.get("description") != null)
                org.setCities(Arrays.asList(asString(attrs.get("description")).split(",")));
            else
                org.setCities(new LinkedList<String>());
            org.setStatus(asString(attrs.get("businessCategory")));
            List<String> rawMembers = asListString(attrs.get("member"));
            List<String> filteredMembers = new LinkedList<String>();
            for (String member : rawMembers) {
                LdapNameBuilder dn = LdapNameBuilder.newInstance(member);
                LdapName name = dn.build();
                filteredMembers.add(name.getRdn(name.size() - 1 ).getValue().toString());
            }
            org.setMembers(filteredMembers);
            return org;
        }

        public String asString(Attribute att) throws NamingException {
            if(att == null)
                return null;
            else {
                StringBuilder buffer = new StringBuilder();
                for(int i = 0; i < att.size(); i++)
                    buffer.append("," + att.get(i));
                return buffer.length() > 0 ? buffer.substring(1) : "";
            }
        }

        public List<String> asListString(Attribute att) throws NamingException {
            List<String> res = new LinkedList<String>();

            if(att == null)
                return res;


            for(int i=0; i< att.size();i++)
                res.add((String) att.get(i));

            return res;
        }
    }

    private class OrgExtAttributesMapper implements AttributesMapper<OrgExt> {

        public OrgExt mapFromAttributes(Attributes attrs) throws NamingException {
            OrgExt org = new OrgExt();
            org.setId(asString(attrs.get("o")));
            org.setOrgType(asString(attrs.get("businessCategory")));
            org.setAddress(asString(attrs.get("postalAddress")));
            org.setNumericId(asInteger(attrs.get("destinationIndicator")));
            return org;
        }

        public Integer asInteger(Attribute att) throws NamingException {
            if(att == null)
                return null;
            else
                return Integer.parseInt(asString(att));
        }

        public String asString(Attribute att) throws NamingException {
            if(att == null)
                return null;
            else
                return (String) att.get();
        }
    }

    private class ContextMapperSecuringReferenceAndMappingAttributes<T extends ReferenceAware>  implements ContextMapper<T> {

        AttributesMapper<T> attributesMapper;

        public ContextMapperSecuringReferenceAndMappingAttributes(AttributesMapper<T> attributesMapper) {
            this.attributesMapper = attributesMapper;
        }

        @Override
        public T mapFromContext(Object o) throws NamingException {
            DirContextAdapter dirContext = (DirContextAdapter) o;
            T dto = attributesMapper.mapFromAttributes(dirContext.getAttributes());
            dto.setReference(dirContext);
            return dto;
        }
    }
}
