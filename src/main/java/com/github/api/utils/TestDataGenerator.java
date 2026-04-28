package com.github.api.utils;

import java.time.Instant;
import java.util.UUID;

/**
 * Utility class for generating unique test data.
 *
 * Ensures that each test run uses unique repository names
 * to avoid conflicts on the GitHub account.
 */
public class TestDataGenerator {

    private static final String DEFAULT_PREFIX = "auto-test-repo";

    private TestDataGenerator() { /* utility class – no instantiation */ }

    /**
     * Generates a unique repository name using a timestamp.
     * Example: "auto-test-repo-1714300000000"
     *
     * @return Unique repository name safe for GitHub API
     */
    public static String generateRepoName() {
        return DEFAULT_PREFIX + "-" + Instant.now().toEpochMilli();
    }

    /**
     * Generates a unique repository name with a custom prefix.
     * Example: "my-prefix-1714300000000"
     *
     * @param prefix Custom prefix for the repository name
     * @return Unique repository name
     */
    public static String generateRepoName(String prefix) {
        return prefix + "-" + Instant.now().toEpochMilli();
    }

    /**
     * Generates a unique repository name using UUID (guaranteed uniqueness).
     * Example: "auto-test-repo-3f2504e0"
     *
     * @return Unique repository name
     */
    public static String generateRepoNameUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return DEFAULT_PREFIX + "-" + uuid;
    }

    /**
     * Generates a repository description with a timestamp for traceability.
     *
     * @return Repository description string
     */
    public static String generateDescription() {
        return "Automated test repository created at " + Instant.now();
    }

    /**
     * Generates an updated description (used in PATCH tests).
     *
     * @return Updated description string
     */
    public static String generateUpdatedDescription() {
        return "UPDATED by automation at " + Instant.now();
    }
}
