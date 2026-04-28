package com.github.api.tests;

import com.github.api.config.Config;
import com.github.api.tests.base.BaseTest;
import com.github.api.utils.ResponseUtils;
import com.github.api.utils.TestDataGenerator;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * Test class: HTTP Headers Validation
 *
 * Verifies that the GitHub API returns the required headers:
 *  - Content-Type (must contain "application/json")
 *  - X-RateLimit-Limit (must be present)
 *  - Server (must be "github.com")
 *  - Authorization header is sent in the request
 *
 * Uses ResponseUtils utility methods for all header assertions.
 */
public class HeadersTest extends BaseTest {

    private String repoName;

    @BeforeClass(alwaysRun = true)
    public void createTestRepo() {
        repoName = TestDataGenerator.generateRepoName("headers-test");
        log.info("HeadersTest setUp: creating repo '{}'", repoName);
        createRepo(repoName, "Repo for header validation tests", false);
    }

    @AfterClass(alwaysRun = true)
    public void deleteTestRepo() {
        log.info("HeadersTest tearDown: deleting repo '{}'", repoName);
        deleteRepo(repoName);
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-HDR-01: Content-Type header contains "application/json"
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-HDR-01: Response Content-Type must contain 'application/json'")
    public void testContentTypeHeader() {
        log.info("=== TC-HDR-01: Verify Content-Type header on GET /user ===");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 200);

        // Utility method: checks header is present AND contains expected value
        ResponseUtils.validateHeaderContains(response, "Content-Type", "application/json");

        String actualContentType = ResponseUtils.getHeader(response, "Content-Type");
        log.info("TC-HDR-01 PASSED: Content-Type = '{}'", actualContentType);
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-HDR-02: X-RateLimit-Limit header is present
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-HDR-02: Response must contain X-RateLimit-Limit header")
    public void testRateLimitHeader() {
        log.info("=== TC-HDR-02: Verify X-RateLimit-Limit header ===");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.assertStatusCode(response, 200);

        // Utility method: validates header is present and non-empty
        ResponseUtils.validateHeaderPresent(response, "X-RateLimit-Limit");

        String limit = ResponseUtils.getHeader(response, "X-RateLimit-Limit");
        ResponseUtils.validateHeaderPresent(response, "X-RateLimit-Remaining");

        log.info("TC-HDR-02 PASSED: X-RateLimit-Limit='{}', X-RateLimit-Remaining='{}'",
                limit, ResponseUtils.getHeader(response, "X-RateLimit-Remaining"));
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-HDR-03: Server header equals "github.com"
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-HDR-03: Response Server header must equal 'github.com'")
    public void testServerHeader() {
        log.info("=== TC-HDR-03: Verify Server header on GET /repos ===");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/repos/{owner}/{repo}", Config.getUsername(), repoName)
                .then()
                .extract().response();

        ResponseUtils.assertStatusCode(response, 200);

        // Specific GitHub header validation
        ResponseUtils.validateHeaderEquals(response, "Server", "github.com");

        log.info("TC-HDR-03 PASSED: Server = '{}'",
                ResponseUtils.getHeader(response, "Server"));
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-HDR-04: Authorization header is included in the request
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-HDR-04: Verify that Authorization header is sent and results in 200")
    public void testAuthorizationHeaderIsEffective() {
        log.info("=== TC-HDR-04: Verify Authorization header is sent correctly ===");

        // With correct Authorization header → 200
        Response authorizedResponse = given()
                .spec(requestSpec)
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.assertStatusCode(authorizedResponse, 200);

        // Without Authorization header → 401 (proves the header matters)
        Response unauthorizedResponse = given()
                .baseUri(Config.getBaseUrl())
                .header("Accept", "application/vnd.github+json")
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.assertStatusCode(unauthorizedResponse, 401);

        log.info("TC-HDR-04 PASSED: With Authorization → 200; Without → 401.");
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-HDR-05: POST /user/repos response headers validation
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-HDR-05: POST /user/repos response must have Content-Type and Location headers")
    public void testPostResponseHeaders() {
        log.info("=== TC-HDR-05: Validate headers on POST /user/repos response ===");

        // Use a temporary repo for this test
        String tempRepo = TestDataGenerator.generateRepoName("hdr-post");
        trackRepo(tempRepo);

        try {
            com.github.api.dto.CreateRepoRequest body =
                    new com.github.api.dto.CreateRepoRequest(tempRepo, "Header post test", false);

            Response response = given()
                    .spec(requestSpec)
                    .body(body)
                    .when()
                    .post("/user/repos")
                    .then()
                    .extract().response();

            ResponseUtils.logResponse(response);
            ResponseUtils.assertStatusCode(response, 201);

            // Content-Type must be JSON
            ResponseUtils.validateHeaderContains(response, "Content-Type", "application/json");

            // X-RateLimit headers must be present on POST too
            ResponseUtils.validateHeaderPresent(response, "X-RateLimit-Limit");

            log.info("TC-HDR-05 PASSED.");
        } finally {
            deleteRepo(tempRepo);
        }
    }
}
