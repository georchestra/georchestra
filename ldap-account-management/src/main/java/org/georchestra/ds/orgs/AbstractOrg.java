/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import org.springframework.ldap.core.DirContextAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractOrg<T extends AbstractOrg<?>> implements ReferenceAware<T> {

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
