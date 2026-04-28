package com.github.api.tests;

import com.github.api.config.Config;
import com.github.api.dto.CreateRepoRequest;
import com.github.api.tests.base.BaseTest;
import com.github.api.utils.ResponseUtils;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * Test class: Negative Scenarios
 *
 * Verifies that the API returns the correct error codes
 * for invalid or unauthorized requests:
 *
 *  TC-NEG-01: GET non-existent repository       → 404 Not Found
 *  TC-NEG-02: GET /user without Authorization   → 401 Unauthorized
 *  TC-NEG-03: POST /user/repos with empty name  → 422 Unprocessable Entity
 *  TC-NEG-04: DELETE non-existent repository    → 404 Not Found
 *  TC-NEG-05: GET /user with invalid token      → 401 Unauthorized
 */
public class NegativeTest extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    //  TC-NEG-01: GET non-existent repository → 404
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-NEG-01: GET non-existent repository → 404 Not Found")
    public void testGetNonExistentRepository_returns404() {
        String fakeRepoName = "this-repo-does-not-exist-xyz-99999";
        log.info("=== TC-NEG-01: GET /repos/{}/{} → expect 404 ===",
                Config.getUsername(), fakeRepoName);

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/repos/{owner}/{repo}", Config.getUsername(), fakeRepoName)
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 404);

        // Verify error message in body
        String body = response.getBody().asString();
        assertTrue(body.contains("Not Found"),
                "Response body should contain 'Not Found' for a missing resource");

        log.info("TC-NEG-01 PASSED: Non-existent repo correctly returns 404.");
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-NEG-02: GET /user without Authorization header → 401
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-NEG-02: GET /user without Authorization header → 401 Unauthorized")
    public void testGetUser_withoutAuth_returns401() {
        log.info("=== TC-NEG-02: GET /user without Authorization → expect 401 ===");

        Response response = given()
                .baseUri(Config.getBaseUrl())
                .header("Accept", "application/vnd.github+json")
                // Intentionally NO Authorization header
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 401);

        log.info("TC-NEG-02 PASSED: Missing auth returns 401 Unauthorized.");
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-NEG-03: POST /user/repos with empty name → 422
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-NEG-03: POST /user/repos with empty name → 422 Unprocessable Entity")
    public void testCreateRepo_withEmptyName_returns422() {
        log.info("=== TC-NEG-03: POST /user/repos with empty name → expect 422 ===");

        // Repository name is required by GitHub API
        CreateRepoRequest invalidBody = new CreateRepoRequest();
        invalidBody.setName("");  // empty name → invalid

        Response response = given()
                .spec(requestSpec)
                .body(invalidBody)
                .when()
                .post("/user/repos")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);

        // GitHub returns 422 for validation errors
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 422 || statusCode == 400,
                "Expected 422 or 400 for invalid repo name, but got: " + statusCode);

        log.info("TC-NEG-03 PASSED: Invalid repo name returns {} (validation error).", statusCode);
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-NEG-04: DELETE non-existent repository → 404
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-NEG-04: DELETE non-existent repository → 404 Not Found")
    public void testDeleteNonExistentRepository_returns404() {
        String fakeRepoName = "non-existent-repo-to-delete-abc123";
        log.info("=== TC-NEG-04: DELETE /repos/{}/{} → expect 404 ===",
                Config.getUsername(), fakeRepoName);

        Response response = given()
                .spec(requestSpec)
                .when()
                .delete("/repos/{owner}/{repo}", Config.getUsername(), fakeRepoName)
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 404);

        log.info("TC-NEG-04 PASSED: DELETE non-existent repo returns 404.");
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-NEG-05: GET /user with invalid/wrong token → 401
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-NEG-05: GET /user with invalid token → 401 Unauthorized")
    public void testGetUser_withInvalidToken_returns401() {
        log.info("=== TC-NEG-05: GET /user with invalid token → expect 401 ===");

        Response response = given()
                .baseUri(Config.getBaseUrl())
                .header("Authorization", "Bearer invalid_token_abc_xyz_000")
                .header("Accept", "application/vnd.github+json")
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 401);

        log.info("TC-NEG-05 PASSED: Invalid token correctly returns 401 Unauthorized.");
    }
}
