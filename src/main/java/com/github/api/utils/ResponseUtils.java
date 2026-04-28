package com.github.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.testng.Assert.*;

/**
 * Utility class for HTTP response inspection and validation.
 *
 * Provides reusable methods for:
 *  - Header presence/value checks
 *  - Generic JSON deserialization (Response → DTO)
 *  - Status code assertion with descriptive messages
 */
public class ResponseUtils {

    private static final Logger log = LogManager.getLogger(ResponseUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ResponseUtils() { /* utility class – no instantiation */ }

    // ═══════════════════════════════════════════════════════════
    //  STATUS CODE VALIDATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Asserts that the response status code equals the expected value.
     * Logs the actual vs expected code.
     */
    public static void assertStatusCode(Response response, int expected) {
        int actual = response.getStatusCode();
        log.info("Validating status code: expected={}, actual={}", expected, actual);
        assertEquals(actual, expected,
                "Status code mismatch. Response body: " + response.getBody().asString());
    }

    // ═══════════════════════════════════════════════════════════
    //  HEADER UTILITIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Retrieves the value of a specific response header.
     *
     * @param response   RestAssured Response object
     * @param headerName Name of the header (case-insensitive)
     * @return The header value, or null if not present
     */
    public static String getHeader(Response response, String headerName) {
        String value = response.getHeader(headerName);
        log.debug("Header '{}' = '{}'", headerName, value);
        return value;
    }

    /**
     * Asserts that a header is present (non-null and non-empty).
     *
     * @param response   RestAssured Response object
     * @param headerName Name of the expected header
     */
    public static void validateHeaderPresent(Response response, String headerName) {
        String value = response.getHeader(headerName);
        log.info("Checking header presence: '{}'", headerName);
        assertNotNull(value,
                "Expected header '" + headerName + "' to be present, but it was absent.");
        assertFalse(value.isBlank(),
                "Expected header '" + headerName + "' to be non-empty, but it was blank.");
        log.info("Header '{}' is present with value: '{}'", headerName, value);
    }

    /**
     * Asserts that a header contains the expected value (case-insensitive substring match).
     *
     * @param response      RestAssured Response
     * @param headerName    Name of the header
     * @param expectedValue Expected substring or exact value
     */
    public static void validateHeaderContains(Response response, String headerName, String expectedValue) {
        String actualValue = response.getHeader(headerName);
        log.info("Validating header '{}': expected to contain '{}', actual='{}'",
                headerName, expectedValue, actualValue);
        assertNotNull(actualValue,
                "Header '" + headerName + "' is absent.");
        assertTrue(actualValue.toLowerCase().contains(expectedValue.toLowerCase()),
                "Header '" + headerName + "' value '" + actualValue +
                "' does not contain expected '" + expectedValue + "'");
    }

    /**
     * Asserts the exact value of a response header.
     *
     * @param response      RestAssured Response
     * @param headerName    Name of the header
     * @param expectedValue Exact expected value
     */
    public static void validateHeaderEquals(Response response, String headerName, String expectedValue) {
        String actualValue = response.getHeader(headerName);
        log.info("Validating header '{}': expected='{}', actual='{}'",
                headerName, expectedValue, actualValue);
        assertNotNull(actualValue,
                "Header '" + headerName + "' is absent.");
        assertEquals(actualValue, expectedValue,
                "Header '" + headerName + "' value mismatch.");
    }

    // ═══════════════════════════════════════════════════════════
    //  JSON DESERIALIZATION (Generic)
    // ═══════════════════════════════════════════════════════════

    /**
     * Generic deserialization of a JSON response body into a DTO.
     *
     * <pre>
     *   RepositoryDto repo = ResponseUtils.deserialize(response, RepositoryDto.class);
     * </pre>
     *
     * @param response  RestAssured Response containing a JSON body
     * @param dtoClass  Target DTO class
     * @param <T>       DTO type parameter
     * @return          Deserialized DTO instance
     */
    public static <T> T deserialize(Response response, Class<T> dtoClass) {
        String json = response.getBody().asString();
        log.debug("Deserializing response body to {}: {}", dtoClass.getSimpleName(), json);
        try {
            T result = MAPPER.readValue(json, dtoClass);
            log.info("Successfully deserialized response to {}", dtoClass.getSimpleName());
            return result;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to deserialize response body to " + dtoClass.getSimpleName() +
                    ". Body was: " + json, e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  LOGGING HELPER
    // ═══════════════════════════════════════════════════════════

    /**
     * Logs full response details: status line, headers, and body.
     *
     * @param response RestAssured Response to log
     */
    public static void logResponse(Response response) {
        log.info("──────── HTTP RESPONSE ────────");
        log.info("Status : {} {}", response.getStatusCode(), response.getStatusLine());
        log.info("Headers: {}", response.getHeaders());
        log.info("Body   : {}", response.getBody().asString());
        log.info("───────────────────────────────");
    }
}
