/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.autoconf;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

import lombok.NonNull;

public class GeorchestraNameNormalizer {

    private static Pattern BEGINNING_BY_INT_OR_MINUS_OR_DOT_PATTERN = Pattern.compile("^[0-9-.]+");
    private static Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[^\\w-_.]");

    /**
     * @return lower-cased {@link #normalizeName normalized} {@code orgName}
     */
    public @NonNull String resolveDatabaseSchemaName(@NonNull String orgName) {
        return normalizeName(orgName).toLowerCase();
    }

    /**
     * @return lower-cased {@link #normalizeName normalized} {@code orgName}
     */
    public String resolveWorkspaceName(String orgName) {
        return normalizeName(orgName).toLowerCase();
    }

    /**
     * @return {@link #normalizeName normalized} {@code proposedName}
     */
    public String resolveLayerName(@NonNull String proposedName) {
        return normalizeName(proposedName);
    }

    /**
     * Returns a normalized representation of the argument string
     * <ul>
     * <li>spaces removed
     * <li>accentuated chars replaced by their unaccentuated equivalent letter
     * <li>special chars removed
     * <li>if the short name starts with a digit, the digit is removed (repeated
     * until the workspace name starts with a letter)
     * </ul>
     */
    public String normalizeName(@NonNull String name) {
        // Canonical decomposition.
        String normalized = Normalizer.normalize(name, Form.NFD);
        // remove unicode accents and diacritics
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = FORBIDDEN_CHARS_PATTERN.matcher(normalized).replaceAll("_");
        normalized = BEGINNING_BY_INT_OR_MINUS_OR_DOT_PATTERN.matcher(normalized).replaceAll("");
        if (normalized.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Name was normalized until empty. Orginal name was: %s", name));
        }
        return normalized;
    }
}
