/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.ds.orgs;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.util.StringUtils;

/**
 * This class manage organization membership
 */
@SuppressWarnings("unchecked")
public class OrgsDaoImpl implements OrgsDao {

    private @Autowired AccountDaoImpl accountDao;

    private LdapTemplate ldapTemplate;
    private String[] orgTypeValues;
    private String basePath;
    private String orgSearchBaseDN;
    private String pendingOrgSearchBaseDN;
    private OrgLdapWrapper orgLdapWrapper = new OrgLdapWrapper();
    private OrgExtLdapWrapper orgExtLdapWrapper = new OrgExtLdapWrapper();

    public abstract class LdapWrapper<T extends ReferenceAware> {

        private AndFilter objectClassFilter;

        public LdapWrapper() {
            objectClassFilter = new AndFilter();
            for (int i = 0; i < getObjectClass().length; i++) {
                objectClassFilter.and(new EqualsFilter("objectClass", getObjectClass()[i]));
            }
        }

        public AndFilter getObjectClassFilter() {
            return objectClassFilter;
        }

        public Name buildOrgDN(T org) {
            return LdapNameBuilder.newInstance(org.isPending() ? pendingOrgSearchBaseDN : orgSearchBaseDN)
                    .add(getLdapKeyField(), org.getId()).build();
        }

        public ContextMapperSecuringReferenceAndMappingAttributes<T> getContextMapper(boolean pending) {
            return new ContextMapperSecuringReferenceAndMappingAttributes<>(getAttributeMapper(pending));
        }

        public void mapToContext(T org, DirContextOperations context) {
            Set<String> values = new HashSet<>();

            if (context.getStringAttributes("objectClass") != null) {
                Collections.addAll(values, context.getStringAttributes("objectClass"));
            }
            Collections.addAll(values, getObjectClass());

            context.setAttributeValues("objectClass", values.toArray());

            context.setAttributeValue(getLdapKeyField(), org.getId());
            mapPayloadToContext(org, context);
        }

        public <O extends ReferenceAware> O findById(String id) {
            String ldapKeyField = this.getLdapKeyField();
            try {
                Name dn = LdapNameBuilder.newInstance(orgSearchBaseDN).add(ldapKeyField, id).build();
                return (O) ldapTemplate.lookup(dn, this.getContextMapper(false));
            } catch (NameNotFoundException ex) {
                Name dn = LdapNameBuilder.newInstance(pendingOrgSearchBaseDN).add(ldapKeyField, id).build();
                return (O) ldapTemplate.lookup(dn, this.getContextMapper(true));
            }
        }

        protected abstract void mapPayloadToContext(T org, DirContextOperations context);

        protected abstract String getLdapKeyField();

        protected abstract String[] getObjectClass();

        public abstract AttributesMapper<T> getAttributeMapper(boolean pending);
    }

    class OrgLdapWrapper extends LdapWrapper<Org> {

        @Override
        public void mapPayloadToContext(Org org, DirContextOperations context) {
            String seeAlsoValueExt = LdapNameBuilder
                    .newInstance((org.isPending() ? pendingOrgSearchBaseDN : orgSearchBaseDN) + "," + basePath)
                    .add("o", org.getId()).build().toString();

            context.setAttributeValue("seeAlso", seeAlsoValueExt);

            // Mandatory attribute
            context.setAttributeValue("o", org.getName());

            if (org.getMembers() != null) {
                context.setAttributeValues("member", //
                        org.getMembers().stream() //
                                .map(accountDao::findByUID) //
                                .filter(Objects::nonNull) //
                                .map(account -> accountDao.buildFullUserDn(account)) //
                                .collect(Collectors.toList()).toArray(new String[] {}));
            }

            // Optional ones
            if (org.getShortName() != null)
                context.setAttributeValue("ou", org.getShortName());

            if (org.getCities() != null) {
                StringBuilder buffer = new StringBuilder();
                List<String> descriptions = new ArrayList<>();
                int maxFieldSize = 1000;

                // special case where cities is empty
                if (org.getCities().size() == 0) {
                    Object[] values = context.getObjectAttributes("description");
                    if (values != null) {
                        Arrays.asList(values).stream().forEach(v -> context.removeAttributeValue("description", v));
                    }
                } else {
                    for (String city : org.getCities()) {
                        if (buffer.length() > maxFieldSize) {
                            descriptions.add(buffer.substring(1));
                            buffer = new StringBuilder();
                        }
                        buffer.append("," + city);
                    }
                }
                if (buffer.length() > 0)
                    descriptions.add(buffer.substring(1));

                if (descriptions.size() > 0)
                    context.setAttributeValues("description", descriptions.toArray());
            }
        }

        @Override
        protected String getLdapKeyField() {
            return "cn";
        }

        @Override
        protected String[] getObjectClass() {
            return new String[] { "top", "groupOfMembers" };
        }

        @Override
        public AttributesMapper<Org> getAttributeMapper(boolean pending) {
            return attrs -> {
                Org org = new Org();
                org.setId(asStringStream(attrs, "cn").collect(joining(",")));
                org.setName(asStringStream(attrs, "o").collect(joining(",")));
                org.setShortName(asStringStream(attrs, "ou").collect(joining(",")));
                org.setCities(asStringStream(attrs, "description").flatMap(Pattern.compile(",")::splitAsStream)
                        .collect(Collectors.toList()));
                org.setMembers(asStringStream(attrs, "member").map(LdapNameBuilder::newInstance)
                        .map(LdapNameBuilder::build).map(name -> name.getRdn(name.size() - 1).getValue().toString())
                        .collect(Collectors.toList()));
                org.setOrgUniqueId(asStringStream(attrs, "orgUniqueId").collect(joining(",")));
                org.setPending(pending);
                return org;
            };
        }
    }

    class OrgExtLdapWrapper extends LdapWrapper<OrgExt> {

        @Override
        public void mapPayloadToContext(OrgExt org, DirContextOperations context) {
            if (org.getOrgType() != null)
                context.setAttributeValue("businessCategory", org.getOrgType());
            if (org.getAddress() != null)
                context.setAttributeValue("postalAddress", org.getAddress());
            if (org.getUniqueIdentifier() == null) {
                org.setUniqueIdentifier(UUID.randomUUID());
            }
            setOrDeleteField(context, "mail", org.getMail());
            setOrDeleteField(context, "georchestraObjectIdentifier", org.getUniqueIdentifier().toString());
            setOrDeleteField(context, "knowledgeInformation", org.getNote());
            setOrDeleteField(context, "description", org.getDescription());
            setOrDeleteField(context, "labeledURI", org.getUrl());
            setOrDeletePhoto(context, "jpegPhoto", org.getLogo());
            setOrDeleteField(context, "orgUniqueId", org.getOrgUniqueId());
        }

        @Override
        protected String getLdapKeyField() {
            return "o";
        }

        @Override
        protected String[] getObjectClass() {
            return new String[] { "top", "organization", "georchestraOrg", "extensibleObject" };
        }

        @Override
        public AttributesMapper<OrgExt> getAttributeMapper(boolean pending) {
            return new AttributesMapper() {
                public OrgExt mapFromAttributes(Attributes attrs) throws NamingException {
                    OrgExt orgExt = new OrgExt();
                    // georchestraObjectIdentifier
                    orgExt.setUniqueIdentifier(asUuid(attrs.get("georchestraObjectIdentifier")));
                    orgExt.setId(asString(attrs.get("o")));

                    String businessCategory = asString(attrs.get("businessCategory"));
                    orgExt.setOrgType(businessCategory);// .isEmpty() ? null : businessCategory);

                    orgExt.setAddress(asString(attrs.get("postalAddress")));
                    orgExt.setDescription(listToCommaSeparatedString(attrs, "description"));
                    orgExt.setUrl(listToCommaSeparatedString(attrs, "labeledURI"));
                    orgExt.setNote(listToCommaSeparatedString(attrs, "knowledgeInformation"));
                    orgExt.setLogo(asPhoto(attrs.get("jpegPhoto")));
                    orgExt.setPending(pending);
                    orgExt.setMail(asString(attrs.get("mail")));
                    orgExt.setOrgUniqueId(asString(attrs.get("orgUniqueId")));
                    return orgExt;
                }

                private String listToCommaSeparatedString(Attributes atts, String attName) throws NamingException {
                    return asStringStream(atts, attName).collect(joining(","));
                }
            };
        }
    }

    public LdapWrapper<Org> getOrgLdapWrapper() {
        return orgLdapWrapper;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setOrgTypeValues(String orgTypeValues) {
        this.orgTypeValues = orgTypeValues.split("\\s*,\\s*");
    }

    public String[] getOrgTypeValues() {
        return orgTypeValues;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setOrgSearchBaseDN(String orgSearchBaseDN) {
        this.orgSearchBaseDN = orgSearchBaseDN;
    }

    public String getOrgSearchBaseDN() {
        return this.orgSearchBaseDN;
    }

    public void setPendingOrgSearchBaseDN(String pendingOrgSearchBaseDN) {
        this.pendingOrgSearchBaseDN = pendingOrgSearchBaseDN;
    }

    public void setAccountDao(AccountDaoImpl accountDao) {
        this.accountDao = accountDao;
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
        List<Org> active = ldapTemplate.search(orgSearchBaseDN, filter.encode(),
                orgLdapWrapper.getAttributeMapper(false));
        List<Org> pending = ldapTemplate.search(pendingOrgSearchBaseDN, filter.encode(),
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
        List<OrgExt> active = ldapTemplate.search(orgSearchBaseDN, filter.encode(),
                orgExtLdapWrapper.getAttributeMapper(false));
        List<OrgExt> pending = ldapTemplate.search(pendingOrgSearchBaseDN, filter.encode(),
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
        return ldapTemplate.search(orgSearchBaseDN, filter.encode(), notPendingMapper)//
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
        {
            DirContextAdapter orgContext = new DirContextAdapter(orgLdapWrapper.buildOrgDN(org));
            orgLdapWrapper.mapToContext(org, orgContext);
            ldapTemplate.bind(orgContext);
        }
        {
            OrgExt ext = org.getExt();
            DirContextAdapter orgExtContext = new DirContextAdapter(orgExtLdapWrapper.buildOrgDN(ext));
            orgExtLdapWrapper.mapToContext(ext, orgExtContext);
            ldapTemplate.bind(orgExtContext);
        }
    }

    @Override
    public void update(Org org) {
        {
            Name newName = orgLdapWrapper.buildOrgDN(org);
            if (newName.compareTo(org.getReference().getDn()) != 0) {
                this.ldapTemplate.rename(org.getReference().getDn(), newName);
            }
            DirContextOperations context = this.ldapTemplate.lookupContext(newName);
            orgLdapWrapper.mapToContext(org, context);
            this.ldapTemplate.modifyAttributes(context);
        }
        {
            OrgExt ext = org.getExt();
            Name newName = orgExtLdapWrapper.buildOrgDN(ext);
            if (newName.compareTo(ext.getReference().getDn()) != 0) {
                this.ldapTemplate.rename(ext.getReference().getDn(), newName);
            }
            DirContextOperations context = this.ldapTemplate.lookupContext(newName);
            orgExtLdapWrapper.mapToContext(ext, context);
            this.ldapTemplate.modifyAttributes(context);
        }
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

    private void setOrDeleteField(DirContextOperations context, String fieldName, String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                Attribute attributeToDelete = context.getAttributes().get(fieldName);
                if (attributeToDelete != null) {
                    Collections.list(attributeToDelete.getAll()).stream()
                            .forEach(x -> context.removeAttributeValue(fieldName, x));
                }
            } else {
                context.setAttributeValue(fieldName, value);
            }
        } catch (NamingException e) {
            // no need to remove an nonexistant attribute
        }
    }

    private void setOrDeletePhoto(DirContextOperations context, String fieldName, String value) {
        try {
            if (value == null || value.length() == 0) {
                Attribute attributeToDelete = context.getAttributes().get(fieldName);
                if (attributeToDelete != null) {
                    Collections.list(attributeToDelete.getAll()).stream()
                            .forEach(x -> context.removeAttributeValue(fieldName, x));
                }
            } else {
                context.setAttributeValue(fieldName, Base64.getMimeDecoder().decode(value));
            }
        } catch (NamingException e) {
            // no need to remove an nonexistant attribute
        }
    }

    private String asString(Attribute att) throws NamingException {
        String v = att == null ? null : (String) att.get();
        return StringUtils.isEmpty(v) ? "" : v;
    }

    private UUID asUuid(Attribute att) throws NamingException {
        String asString = asString(att);
        if (StringUtils.hasLength(asString)) {
            return UUID.fromString(asString);
        }
        return null;
    }

    private String asPhoto(Attribute att) throws NamingException {
        if (att == null)
            return "";
        return Base64.getMimeEncoder().encodeToString((byte[]) att.get());
    }

    private Stream<String> asStringStream(Attributes attributes, String attributeName) throws NamingException {
        Attribute attribute = attributes.get(attributeName);
        if (attribute == null) {
            return Stream.empty();
        }
        return Collections.list(attribute.getAll()).stream().map(Object::toString);
    }

    private class ContextMapperSecuringReferenceAndMappingAttributes<T extends ReferenceAware>
            implements ContextMapper<T> {

        private AttributesMapper<T> attributesMapper;

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
