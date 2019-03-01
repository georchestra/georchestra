package org.georchestra.console.dto;

import org.georchestra.console.ds.OrgsDao;
import org.springframework.ldap.core.DirContextAdapter;

public interface ReferenceAware<T> {
    DirContextAdapter getReference();

    void setReference(DirContextAdapter reference);

    void setPending(boolean pending);

    OrgsDao.Extension<T> getExtension(OrgsDao orgDao);

    boolean isPending();

}
