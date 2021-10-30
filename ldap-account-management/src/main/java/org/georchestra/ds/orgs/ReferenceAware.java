package org.georchestra.ds.orgs;

import org.springframework.ldap.core.DirContextAdapter;

public interface ReferenceAware<T extends AbstractOrg<?>> {
    DirContextAdapter getReference();

    void setReference(DirContextAdapter reference);

    void setPending(boolean pending);

    OrgsDao.Extension<T> getExtension(OrgsDao orgDao);

}