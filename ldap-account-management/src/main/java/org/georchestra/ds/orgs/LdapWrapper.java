package org.georchestra.ds.orgs;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.util.StringUtils;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class LdapWrapper<T extends ReferenceAware> {

    private AndFilter objectClassFilter;
    protected String orgSearchBaseDN;
    protected String pendingOrgSearchBaseDN;
    private LdapTemplate ldapTemplate;

    public LdapWrapper() {
        objectClassFilter = new AndFilter();
        for (int i = 0; i < getObjectClass().length; i++) {
            objectClassFilter.and(new EqualsFilter("objectClass", getObjectClass()[i]));
        }
    }

    public void setOrgSearchBaseDN(String orgSearchBaseDN) {
        this.orgSearchBaseDN = orgSearchBaseDN;
    }

    public void setPendingOrgSearchBaseDN(String pendingOrgSearchBaseDN) {
        this.pendingOrgSearchBaseDN = pendingOrgSearchBaseDN;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
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

    public void update(T ref) {
        Name newName = buildOrgDN(ref);
        if (newName.compareTo(ref.getReference().getDn()) != 0) {
            this.ldapTemplate.rename(ref.getReference().getDn(), newName);
        }
        DirContextOperations context = this.ldapTemplate.lookupContext(newName);
        mapToContext(ref, context);
        this.ldapTemplate.modifyAttributes(context);
    }

    public void insert(T ref) {
        DirContextAdapter context = new DirContextAdapter(buildOrgDN(ref));
        mapToContext(ref, context);
        ldapTemplate.bind(context);
    }

    protected String asString(Attribute att) throws NamingException {
        String v = att == null ? null : (String) att.get();
        return StringUtils.isEmpty(v) ? "" : v;
    }

    protected UUID asUuid(Attribute att) throws NamingException {
        String asString = asString(att);
        if (StringUtils.hasLength(asString)) {
            return UUID.fromString(asString);
        }
        return null;
    }

    protected String asPhoto(Attribute att) throws NamingException {
        if (att == null)
            return "";
        return Base64.getMimeEncoder().encodeToString((byte[]) att.get());
    }

    protected Stream<String> asStringStream(Attributes attributes, String attributeName) throws NamingException {
        Attribute attribute = attributes.get(attributeName);
        if (attribute == null) {
            return Stream.empty();
        }
        return Collections.list(attribute.getAll()).stream().map(Object::toString);
    }

    protected abstract void mapPayloadToContext(T org, DirContextOperations context);

    protected abstract String getLdapKeyField();

    protected abstract String[] getObjectClass();

    public abstract AttributesMapper<T> getAttributeMapper(boolean pending);
}
