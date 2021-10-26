package org.georchestra.ds.orgs;

import org.springframework.ldap.core.DirContextAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
