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

package org.georchestra.ds.orgs;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.util.StringUtils;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

public class OrgExtLdapWrapper extends LdapWrapper<OrgExt> {

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
}