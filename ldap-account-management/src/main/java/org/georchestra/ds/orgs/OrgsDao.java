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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.georchestra.ds.users.Account;

/**
 * This class manage organization membership
 */
public interface OrgsDao {

    /**
     * Search all organizations defined in ldap. this.orgSearchBaseDN hold search
     * path in ldap.
     *
     * @return list of organizations
     */
    public List<Org> findAll();

    /**
     * Search for validated organizations defined in ldap.
     *
     * @return list of validated organizations
     */
    public List<Org> findValidated();

    /**
     * Search organization with 'commonName' as distinguish name
     * 
     * @param commonName distinguish name of organization for example : 'psc' to
     *                   retrieve 'cn=psc,ou=orgs,dc=georchestra,dc=org'
     * @return Org instance with specified DN
     */
    public Org findByCommonName(String commonName);

    public Org findByUser(Account user);

    /**
     * Search by {@link Org#getUniqueIdentifier()}
     *
     * @return the matching organization or {@code null}
     */
    public Org findById(UUID uuid);

    public void insert(Org org);

    public void update(Org org);

    public void delete(Org org);

    public void linkUser(Account user);

    public void unlinkUser(Account user);

    public String reGenerateId(String orgName, String allowedId) throws IOException;

    public String generateId(String org_name) throws IOException;

    public String[] getOrgTypeValues();
}
