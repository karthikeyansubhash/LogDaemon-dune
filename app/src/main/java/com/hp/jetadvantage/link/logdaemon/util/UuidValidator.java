package com.hp.jetadvantage.link.logdaemon.util;

/**
 * Utility class for UUID format validation.
 * Validates that UUID strings follow the standard 8-4-4-4-12 hex format
 * before they are used as filesystem path components.
 */
public class UuidValidator {
    private static final String UUID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private UuidValidator() {
        // Utility class
    }

    /**
     * Validate that a string is a well-formed UUID (8-4-4-4-12 hex format).
     * Returns false for null, empty, or malformed strings.
     *
     * @param uuid The string to validate
     * @return true if the string matches UUID format
     */
    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        return uuid.matches(UUID_REGEX);
    }
}
