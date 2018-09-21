package org.georchestra.console.dto;

import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.directory.DirContext;

public interface ReferenceAware {
    DirContextAdapter getReference();
   void setReference(DirContextAdapter reference);
}
