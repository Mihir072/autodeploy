package com.autodeploy.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility for generating URL-safe slugs from arbitrary strings.
 * Used to generate subdomains like {@code my-cool-project-3a8f1c2b.yourdomain.com}.
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s_]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern LEADING_TRAILING_DASH = Pattern.compile("^-|-$");

    private SlugUtil() {}

    /**
     * Converts an arbitrary string to a URL-safe slug.
     * Example: "My Cool App!" → "my-cool-app"
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "project";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String lowercased = normalized.toLowerCase(Locale.ROOT);
        String noSpaces = WHITESPACE.matcher(lowercased).replaceAll("-");
        String noSpecial = NON_LATIN.matcher(noSpaces).replaceAll("");
        String noDuplicateDashes = MULTI_DASH.matcher(noSpecial).replaceAll("-");
        return LEADING_TRAILING_DASH.matcher(noDuplicateDashes).replaceAll("");
    }

    /**
     * Generates a unique project slug: {@code {slug}-{8-char-uuid-fragment}}.
     * Example: "My App" → "my-app-3a8f1c2b"
     */
    public static String generateUniqueSlug(String name) {
        String base = toSlug(name);
        if (base.isBlank()) {
            base = "project";
        }
        String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return base + "-" + shortId;
    }

    /**
     * Returns true if the string is a valid subdomain label
     * (RFC 1123: alphanumeric + hyphens, no leading/trailing hyphen, max 63 chars).
     */
    public static boolean isValidSubdomain(String label) {
        if (label == null || label.isBlank() || label.length() > 63) {
            return false;
        }
        return label.matches("[a-z0-9]([a-z0-9-]*[a-z0-9])?");
    }
}
