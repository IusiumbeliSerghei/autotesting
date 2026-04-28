package com.github.api.tests;

import com.github.api.config.Config;
import com.github.api.dto.RepositoryDto;
import com.github.api.dto.UpdateRepoRequest;
import com.github.api.tests.base.BaseTest;
import com.github.api.utils.ResponseUtils;
import com.github.api.utils.TestDataGenerator;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * Test class: Full Repository Lifecycle
 *
 * Executes the complete resource lifecycle in order:
 *   POST  → create repository         (TC-REPO-01)
 *   GET   → retrieve repository       (TC-REPO-02)
 *   PATCH → update repository         (TC-REPO-03)
 *   DELETE → delete repository        (TC-REPO-04)
 *   GET   → verify 404 after delete   (TC-REPO-05)
 */
public class RepositoryLifecycleTest extends BaseTest {

    private String repoName;
    private RepositoryDto createdRepo;
    private boolean repoDeleted = false;

    // ═══════════════════════════════════════════════════════════
    //  SETUP: Create a repository before all tests in this class
    // ═══════════════════════════════════════════════════════════

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        repoName = TestDataGenerator.generateRepoName("lifecycle");
        log.info("══ RepositoryLifecycleTest – setUp: creating '{}' ══", repoName);
        createdRepo = createRepo(repoName, TestDataGenerator.generateDescription(), false);
        assertNotNull(createdRepo, "Repository DTO should not be null after creation");
    }

    // ═══════════════════════════════════════════════════════════
    //  TEARDOWN: Ensure the repo is deleted even if tests fail
    // ═══════════════════════════════════════════════════════════

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (!repoDeleted && repoName != null) {
            log.warn("AfterClass: repo '{}' still exists — deleting it now.", repoName);
            deleteRepo(repoName);
        } else {
            log.info("AfterClass: repo '{}' was already deleted by TC-REPO-04.", repoName);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-REPO-01: Verify POST response fields
    // ─────────────────────────────────────────────────────────────

    @Test(priority = 1,
          description = "TC-REPO-01: POST /user/repos → verify 201 and response fields")
    public void testCreateRepository_verifyResponseFields() {
        log.info("=== TC-REPO-01: Verify created repository fields ===");

        // createdRepo was populated in @BeforeClass
        assertNotNull(createdRepo.getName(),     "Repository 'name' must not be null");
        assertNotNull(createdRepo.getFullName(), "Repository 'full_name' must not be null");
        assertNotNull(createdRepo.getHtmlUrl(),  "Repository 'html_url' must not be null");
        assertNotNull(createdRepo.getOwner(),    "Repository 'owner' must not be null");

        assertEquals(createdRepo.getName(), repoName,
                "Created repo name should match the requested name");

        String expectedOwner = Config.getUsername();
        assertEquals(createdRepo.getOwner().getLogin(), expectedOwner,
                "Owner login should match the authenticated user");

        assertTrue(createdRepo.getId() > 0,
                "Repository 'id' must be a positive number");

        assertFalse(createdRepo.isPrivate(),
                "Repository should be public (isPrivate = false)");

        log.info("TC-REPO-01 PASSED: repo='{}', id={}, owner='{}'",
                createdRepo.getName(), createdRepo.getId(),
                createdRepo.getOwner().getLogin());
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-REPO-02: GET existing repository → 200 OK
    // ─────────────────────────────────────────────────────────────

    @Test(priority = 2,
          description = "TC-REPO-02: GET /repos/{owner}/{repo} → verify 200 OK",
          dependsOnMethods = "testCreateRepository_verifyResponseFields")
    public void testGetRepository_returns200() {
        log.info("=== TC-REPO-02: GET /repos/{}/{} → expect 200 ===",
                Config.getUsername(), repoName);

        Response response = getRepo(repoName);

        ResponseUtils.assertStatusCode(response, 200);

        RepositoryDto fetched = ResponseUtils.deserialize(response, RepositoryDto.class);
        assertEquals(fetched.getName(), repoName,
                "Fetched repo name should match");
        assertEquals(fetched.getId(), createdRepo.getId(),
                "Fetched repo id should match the created repo id");

        log.info("TC-REPO-02 PASSED: GET returned repo '{}' with id={}",
                fetched.getName(), fetched.getId());
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-REPO-03: PATCH repository → update description
    // ─────────────────────────────────────────────────────────────

    @Test(priority = 3,
          description = "TC-REPO-03: PATCH /repos/{owner}/{repo} → update description, verify 200",
          dependsOnMethods = "testGetRepository_returns200")
    public void testUpdateRepository_descriptionChanged() {
        String updatedDescription = TestDataGenerator.generateUpdatedDescription();
        log.info("=== TC-REPO-03: PATCH repo '{}' — new description: '{}' ===",
                repoName, updatedDescription);

        UpdateRepoRequest body = new UpdateRepoRequest(updatedDescription);

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .patch("/repos/{owner}/{repo}", Config.getUsername(), repoName)
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 200);

        RepositoryDto updated = ResponseUtils.deserialize(response, RepositoryDto.class);

        // Verify the description was actually updated
        assertEquals(updated.getDescription(), updatedDescription,
                "Description in PATCH response must match the new description sent");

        // Verify other fields are intact (data integrity)
        assertEquals(updated.getName(), repoName,
                "Repo name must not change after PATCH");
        assertEquals(updated.getId(), createdRepo.getId(),
                "Repo id must not change after PATCH");

        log.info("TC-REPO-03 PASSED: description updated to '{}'", updated.getDescription());
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-REPO-04: DELETE repository → 204 No Content
    // ─────────────────────────────────────────────────────────────

    @Test(priority = 4,
          description = "TC-REPO-04: DELETE /repos/{owner}/{repo} → verify 204 No Content",
          dependsOnMethods = "testUpdateRepository_descriptionChanged")
    public void testDeleteRepository_returns204() {
        log.info("=== TC-REPO-04: DELETE /repos/{}/{} → expect 204 ===",
                Config.getUsername(), repoName);

        Response response = given()
                .spec(requestSpec)
                .when()
                .delete("/repos/{owner}/{repo}", Config.getUsername(), repoName)
                .then()
                .extract().response();

        ResponseUtils.logResponse(response);
        ResponseUtils.assertStatusCode(response, 204);

        repoDeleted = true;
        untrackRepo(repoName);

        log.info("TC-REPO-04 PASSED: Repository '{}' deleted (204 No Content).", repoName);
    }

    // ─────────────────────────────────────────────────────────────
    //  TC-REPO-05: GET deleted repository → 404 Not Found
    // ─────────────────────────────────────────────────────────────

    @Test(priority = 5,
          description = "TC-REPO-05: GET deleted repository → verify 404 Not Found",
          dependsOnMethods = "testDeleteRepository_returns204")
    public void testGetDeletedRepository_returns404() {
        log.info("=== TC-REPO-05: GET deleted repo '{}' → expect 404 ===", repoName);

        Response response = getRepo(repoName);

        ResponseUtils.assertStatusCode(response, 404);

        log.info("TC-REPO-05 PASSED: Deleted repo returns 404 as expected.");
    }
}
