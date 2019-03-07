package org.georchestra.console.dto.orgs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.georchestra.console.dto.ReferenceAware;
import org.springframework.ldap.core.DirContextAdapter;

public abstract class AbstractOrg implements ReferenceAware {

    @JsonIgnore
    protected boolean isPending;
    @JsonIgnore
    private DirContextAdapter reference;

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public DirContextAdapter getReference() {
        return reference;
    }

    public void setReference(DirContextAdapter reference) {
        this.reference = reference;
    }

    public abstract String getId();
}
