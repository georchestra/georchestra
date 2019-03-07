package org.georchestra.console.dto;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.AbstractOrg;;
import org.springframework.ldap.core.DirContextAdapter;

public interface ReferenceAware<T  extends AbstractOrg> {
    DirContextAdapter getReference();

    void setReference(DirContextAdapter reference);

    void setPending(boolean pending);

    OrgsDao.Extension<T> getExtension(OrgsDao orgDao);


}
