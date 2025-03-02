package org.georchestra.ds.orgs;

import org.georchestra.ds.users.AccountDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.util.StringUtils;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

class OrgLdapWrapper extends LdapWrapper<Org> {

    private AccountDaoImpl accountDao;
    private String basePath;

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

    public void setBasePath(String basePath) {
        this.basePath = basePath;
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
            org.setMembers(asStringStream(attrs, "member").map(LdapNameBuilder::newInstance).map(LdapNameBuilder::build)
                    .map(name -> name.getRdn(name.size() - 1).getValue().toString()).collect(Collectors.toList()));
            org.setPending(pending);
            return org;
        };
    }

    public void setAccountDao(AccountDaoImpl accountDao) {
        this.accountDao = accountDao;
    }
}