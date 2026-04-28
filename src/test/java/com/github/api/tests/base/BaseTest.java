package com.github.api.tests.base;

import com.github.api.config.Config;
import com.github.api.dto.CreateRepoRequest;
import com.github.api.dto.RepositoryDto;
import com.github.api.utils.ResponseUtils;
import com.github.api.utils.TestDataGenerator;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Base class for all GitHub API tests.
 *
 * Responsibilities:
 *  - Configure RestAssured (base URI, auth headers, content type) via @BeforeSuite
 *  - Provide shared helper methods: createRepo(), deleteRepo(), getRepo()
 *  - Track created repositories and clean them up in @AfterSuite (safety net)
 */
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    /** Shared RequestSpecification — built once, used by all tests */
    protected static RequestSpecification requestSpec;

    /** Safety-net list: repos created during the suite that must be deleted */
    private static final List<String> reposToCleanup = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════
    //  SUITE SETUP & TEARDOWN
    // ═══════════════════════════════════════════════════════════

    /**
     * Initialises RestAssured once before the entire test suite.
     * Sets the base URI and shared request headers.
     */
    @BeforeSuite(alwaysRun = true)
    public void initRestAssured() {
        log.info("════════════════════════════════════════════");
        log.info(" Initialising GitHub API Test Suite");
        log.info(" Base URL : {}", Config.getBaseUrl());
        log.info(" Username : {}", Config.getUsername());
        log.info("════════════════════════════════════════════");

        RestAssured.baseURI = Config.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + Config.getToken())
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .setContentType(ContentType.JSON)
                .build();

        log.info("RestAssured configured successfully.");
    }

    /**
     * Safety-net cleanup: deletes any repositories registered via trackRepo()
     * that were not deleted by individual test teardown methods.
     */
    @AfterSuite(alwaysRun = true)
    public void globalCleanup() {
        if (reposToCleanup.isEmpty()) {
            log.info("No repositories to clean up.");
            return;
        }
        log.warn("Suite teardown: cleaning up {} remaining repository(ies)...", reposToCleanup.size());
        for (String repoName : new ArrayList<>(reposToCleanup)) {
            try {
                deleteRepo(repoName);
                log.info("Cleaned up repo: {}", repoName);
            } catch (Exception e) {
                log.warn("Could not clean up repo '{}': {}", repoName, e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER METHODS (reusable across all test classes)
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a new GitHub repository via POST /user/repos.
     *
     * @param repoName    Name for the new repository
     * @param description Repository description
     * @param isPrivate   true → private, false → public
     * @return Deserialized RepositoryDto from the API response
     */
    protected RepositoryDto createRepo(String repoName, String description, boolean isPrivate) {
        log.info("Creating repository: name='{}', private={}", repoName, isPrivate);

        CreateRepoRequest body = new CreateRepoRequest(repoName, description, isPrivate);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/user/repos")
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 201);

        RepositoryDto repo = ResponseUtils.deserialize(response, RepositoryDto.class);
        log.info("Repository created: {}", repo);

        // Register for safety-net cleanup
        reposToCleanup.add(repoName);
        return repo;
    }

    /**
     * Deletes a GitHub repository via DELETE /repos/{owner}/{repo}.
     *
     * @param repoName Name of the repository to delete
     */
    protected void deleteRepo(String repoName) {
        String owner = Config.getUsername();
        log.info("Deleting repository: {}/{}", owner, repoName);

        Response response = given()
                .spec(requestSpec)
                .when()
                .delete("/repos/{owner}/{repo}", owner, repoName)
                .then()
                .extract().response();

        int status = response.getStatusCode();
        if (status == 204) {
            log.info("Repository '{}' deleted successfully (204 No Content).", repoName);
            reposToCleanup.remove(repoName);
        } else if (status == 404) {
            log.warn("Repository '{}' was already deleted (404).", repoName);
            reposToCleanup.remove(repoName);
        } else if (status == 403) {
            log.error("403 Forbidden when deleting repo '{}'. " +
                    "Your GitHub token is missing the 'delete_repo' scope. " +
                    "Fix: https://github.com/settings/tokens → edit token → check 'delete_repo'. " +
                    "Response: {}", repoName, response.getBody().asString());
        } else {
            log.error("Unexpected status {} when deleting repo '{}'. Body: {}",
                    status, repoName, response.getBody().asString());
        }
    }

    /**
     * Fetches a repository via GET /repos/{owner}/{repo}.
     *
     * @param repoName Name of the repository
     * @return Raw RestAssured Response
     */
    protected Response getRepo(String repoName) {
        String owner = Config.getUsername();
        log.info("Fetching repository: {}/{}", owner, repoName);

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/repos/{owner}/{repo}", owner, repoName)
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        return response;
    }

    /**
     * Registers a repository name for suite-level cleanup.
     * Call this whenever a test creates a repository.
     *
     * @param repoName Repository name to track
     */
    protected void trackRepo(String repoName) {
        if (!reposToCleanup.contains(repoName)) {
            reposToCleanup.add(repoName);
        }
    }

    /**
     * Removes a repository name from the cleanup list (already cleaned up).
     *
     * @param repoName Repository name to untrack
     */
    protected void untrackRepo(String repoName) {
        reposToCleanup.remove(repoName);
    }
}
