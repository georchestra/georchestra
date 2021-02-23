package org.georchestra.datafeeder.autoconf;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

import lombok.NonNull;

public class GeorchestraNameNormalizer {
    private static Pattern BEGINNING_BY_INT_OR_MINUS_OR_DOT_PATTERN = Pattern.compile("^[0-9-.]+");
    private static Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[^\\w-_.]");

    public @NonNull String resolveDatabaseSchemaName(@NonNull String orgName) {
        return normalizeName(orgName);
    }

    public String resolveWorkspaceName(String orgName) {
        return normalizeName(orgName).toLowerCase();
    }

    public String resolveLayerName(@NonNull String proposedName) {
        return normalizeName(proposedName);
    }

    /**
     * Returns a normalized representation of the argument string
     * <ul>
     * <li>spaces removed
     * <li>accentuated chars replaced by their unaccentuated equivalent letter
     * <li>special chars removed
     * <li>lowercased
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
        return normalized;
    }
}
