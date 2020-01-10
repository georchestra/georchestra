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

package org.georchestra.console.dto.orgs;

import org.georchestra.console.ds.OrgsDao;

public class OrgExt extends AbstractOrg implements Cloneable {

    public static final String JSON_ADDRESS = "address";
    public static final String JSON_ORG_TYPE = "orgType";

    private String id;
    private String orgType;
    private String address;
    private String description;
    private String url;
    private String logo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "OrgExt{" + "id='" + id + '\'' + ", orgType='" + orgType + '\'' + ", address='" + address + '\''
                + ", description='" + description + '\'' + '}';
    }

    @Override
    public OrgsDao.Extension<OrgExt> getExtension(OrgsDao orgDao) {
        return orgDao.getExtension(this);
    }

    @Override
    public OrgExt clone() throws CloneNotSupportedException {
        return (OrgExt) super.clone();
    }
}