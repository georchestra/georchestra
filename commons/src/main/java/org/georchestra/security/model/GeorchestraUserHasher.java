/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.security.model;

import java.util.Objects;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Utility to create a stable hash for a {@link GeorchestraUser} that can be
 * used as its {@link GeorchestraUser#getLastUpdated lastUpdated} property, in
 * order to quickly compare whether a given instance matches a previously seen
 * version, where the system may save the previously seen "hash" but not the
 * user instance itself.
 */
public class GeorchestraUserHasher {

    public static String createLastUpdatedUserHash(GeorchestraUser user) {
        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putUnencodedChars(nonNull(user.getId()));
        hasher.putUnencodedChars(nonNull(user.getUsername()));
        hasher.putUnencodedChars(nonNull(user.getFirstName()));
        hasher.putUnencodedChars(nonNull(user.getLastName()));
        hasher.putUnencodedChars(nonNull(user.getEmail()));
        hasher.putUnencodedChars(nonNull(user.getNotes()));
        hasher.putUnencodedChars(nonNull(user.getPostalAddress()));
        hasher.putUnencodedChars(nonNull(user.getTelephoneNumber()));
        hasher.putUnencodedChars(nonNull(user.getTitle()));

        if (null != user.getRoles()) {
            user.getRoles().stream().filter(Objects::nonNull).sorted()
                    .forEach(role -> hasher.putUnencodedChars(nonNull(role)));
        }

        Organization organization = user.getOrganization();
        hashOrg(organization, hasher);
        String hexHash = hasher.hash().toString();
        return hexHash;
    }

    public static String createLastUpdatedOrgHash(Organization organization) {
        Hasher hasher = Hashing.sha256().newHasher();
        hashOrg(organization, hasher);
        String hexHash = hasher.hash().toString();
        return hexHash;
    }

    private static void hashOrg(Organization org, Hasher hasher) {
        if (null != org) {
            hasher.putUnencodedChars(nonNull(org.getId()));
            hasher.putUnencodedChars(nonNull(org.getShortName()));
            hasher.putUnencodedChars(nonNull(org.getName()));
            hasher.putUnencodedChars(nonNull(org.getLastUpdated()));
            hasher.putUnencodedChars(nonNull(org.getCategory()));
            hasher.putUnencodedChars(nonNull(org.getDescription()));
            hasher.putUnencodedChars(nonNull(org.getLinkage()));
            hasher.putUnencodedChars(nonNull(org.getNotes()));
        }
    }

    private static CharSequence nonNull(String s) {
        return s == null ? "" : s;
    }
}
