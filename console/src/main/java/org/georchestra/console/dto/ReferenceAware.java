package org.georchestra.console.dto;

import org.springframework.ldap.core.DirContextAdapter;

public interface ReferenceAware {
    DirContextAdapter getReference();
   void setReference(DirContextAdapter reference);
}
