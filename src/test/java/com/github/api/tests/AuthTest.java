package com.github.api.tests;

import com.github.api.config.Config;
import com.github.api.dto.UserDto;
import com.github.api.tests.base.BaseTest;
import com.github.api.utils.ResponseUtils;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * Test class: Authorization
 *
 * Verifies that:
 *  - GET /user returns HTTP 200 with the authenticated user's data
 *  - The 'login' field matches the configured GitHub username
 *  - The response body can be deserialized into UserDto
 */
public class AuthTest extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    //  TC-AUTH-01: GET /user → 200 OK
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-AUTH-01: Verify authenticated user GET /user returns 200 OK")
    public void testGetAuthenticatedUser_returns200() {
        log.info("=== TC-AUTH-01: GET /user → expect 200 OK ===");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 200);

        log.info("TC-AUTH-01 PASSED: Status code 200 confirmed.");
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-AUTH-02: Response body contains correct user login
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-AUTH-02: Verify response body contains authenticated user data")
    public void testGetAuthenticatedUser_responseBodyContainsLogin() {
        log.info("=== TC-AUTH-02: GET /user → verify user login in response body ===");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/user")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 200);

        // Deserialize to UserDto (generic method)
        UserDto user = ResponseUtils.deserialize(response, UserDto.class);

        log.info("Authenticated user: {}", user);

        // Validate key fields
        String expectedUsername = Config.getUsername();
        assertNotNull(user.getLogin(), "User 'login' field must not be null");
        assertEquals(user.getLogin(), expectedUsername,
                "Login should match configured username. Expected: " + expectedUsername);

        assertTrue(user.getId() > 0, "User 'id' must be a positive number");
        assertNotNull(user.getHtmlUrl(), "User 'html_url' must not be null");
        assertTrue(user.getHtmlUrl().contains(expectedUsername),
                "html_url should contain the username");

        log.info("TC-AUTH-02 PASSED: User login='{}', id={}", user.getLogin(), user.getId());
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-AUTH-03: User type is "User" (not "Organization")
    // ─────────────────────────────────────────────────────────────

    @Test(description = "TC-AUTH-03: Verify authenticated user type is 'User'")
    public void testGetAuthenticatedUser_typeIsUser() {
        log.info("=== TC-AUTH-03: GET /user → verify 'type' = 'User' ===");

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/user")
                .then()
                .extract().response();

        UserDto user = ResponseUtils.deserialize(response, UserDto.class);

        assertEquals(user.getType(), "User",
                "GitHub account type should be 'User'");

        log.info("TC-AUTH-03 PASSED: Account type = '{}'", user.getType());
    }
}
