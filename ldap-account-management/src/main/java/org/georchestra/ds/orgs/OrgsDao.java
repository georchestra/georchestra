/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

    List<Org> findAll();

    List<Org> findValidated();

    Org findByCommonName(String commonName);

    Org findByUser(Account user);

    Org findByOrgUniqueId(String orgUniqueId);

    Org findById(UUID uuid);

    void insert(Org org);

    void update(Org org);

    void delete(Org org);

    void linkUser(Account user);

    void unlinkUser(Account user);

    String reGenerateId(String orgName, String allowedId) throws IOException;

    String generateId(String org_name) throws IOException;

    String[] getOrgTypeValues();

}
