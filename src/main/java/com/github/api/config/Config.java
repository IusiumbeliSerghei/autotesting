package com.github.api.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration loader.
 * Reads all settings from src/test/resources/config.properties.
 */
public class Config {

    private static final Logger log = LogManager.getLogger(Config.class);
    private static final Properties PROPERTIES = new Properties();

    static {
        String fileName = "config.properties";
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException("Cannot find '" + fileName + "' on classpath. " +
                        "Make sure src/test/resources/config.properties exists.");
            }
            PROPERTIES.load(is);
            log.info("Configuration loaded from '{}'", fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + fileName, e);
        }
    }

    /** Returns the raw property value for the given key. */
    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Required property '" + key + "' is missing or empty in config.properties");
        }
        return value.trim();
    }

    /** GitHub API base URL (e.g. https://api.github.com) */
    public static String getBaseUrl() {
        return get("base.url");
    }

    /** GitHub Personal Access Token */
    public static String getToken() {
        return get("github.token");
    }

    /** GitHub username (account login) */
    public static String getUsername() {
        return get("github.username");
    }
}
