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

import java.util.List;
import java.util.Objects;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Utility to create a stable hash for a {@link GeorchestraUser},
 * {@link Organization}, and {@link Role} objects, that can be used as their
 * {@code lastUpdated} property, in order to quickly compare whether a given
 * instance matches a previously seen version, where the system may save the
 * previously seen "hash" but not the user instance itself.
 */
public class GeorchestraUserHasher {

    public static String createHash(GeorchestraUser user) {
        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putUnencodedChars(nonNull(user.getId()));
        hasher.putUnencodedChars(nonNull(user.getUsername()));
        hasher.putUnencodedChars(nonNull(user.getFirstName()));
        hasher.putUnencodedChars(nonNull(user.getLastName()));
        hasher.putUnencodedChars(nonNull(user.getOrganization()));
        hasher.putUnencodedChars(nonNull(user.getEmail()));
        hasher.putUnencodedChars(nonNull(user.getNotes()));
        hasher.putUnencodedChars(nonNull(user.getPostalAddress()));
        hasher.putUnencodedChars(nonNull(user.getTelephoneNumber()));
        hasher.putUnencodedChars(nonNull(user.getTitle()));
        hashList(user.getRoles(), hasher);

        String hexHash = hasher.hash().toString();
        return hexHash;
    }

    public static String createHash(Organization organization, int logoSize) {
        Hasher hasher = Hashing.sha256().newHasher();
        hashOrg(organization, logoSize, hasher);
        String hexHash = hasher.hash().toString();
        return hexHash;
    }

    public static String createHash(Role role) {
        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putUnencodedChars(nonNull(role.getId()));
        hasher.putUnencodedChars(nonNull(role.getName()));
        hasher.putUnencodedChars(nonNull(role.getDescription()));
        hashList(role.getMembers(), hasher);
        String hexHash = hasher.hash().toString();
        return hexHash;
    }

    private static void hashOrg(Organization org, int logoSize, Hasher hasher) {
        hasher.putUnencodedChars("org");
        if (null != org) {
            hasher.putUnencodedChars(nonNull(org.getId()));
            hasher.putUnencodedChars(nonNull(org.getShortName()));
            hasher.putUnencodedChars(nonNull(org.getName()));
            hasher.putUnencodedChars(nonNull(org.getLastUpdated()));
            hasher.putUnencodedChars(nonNull(org.getCategory()));
            hasher.putUnencodedChars(nonNull(org.getMail()));
            hasher.putUnencodedChars(nonNull(org.getDescription()));
            hasher.putUnencodedChars(nonNull(org.getLinkage()));
            hasher.putUnencodedChars(nonNull(org.getNotes()));
            hasher.putUnencodedChars(logoSize > 0 ? Integer.toString(logoSize) : "");
            hashList(org.getMembers(), hasher);
        }
    }

    private static CharSequence nonNull(String s) {
        return s == null ? "" : s;
    }

    private static void hashList(List<String> list, Hasher hasher) {
        if (list != null) {
            list.stream().filter(Objects::nonNull).sorted().forEach(role -> hasher.putUnencodedChars(nonNull(role)));
        }
    }

}
