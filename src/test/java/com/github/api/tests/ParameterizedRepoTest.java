package com.github.api.tests;

import com.github.api.config.Config;
import com.github.api.dto.CreateRepoRequest;
import com.github.api.dto.RepositoryDto;
import com.github.api.tests.base.BaseTest;
import com.github.api.utils.ResponseUtils;
import com.github.api.utils.TestDataGenerator;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * Test class: Parameterized Repository Tests using DataProvider
 *
 * Demonstrates the DataProvider pattern in TestNG.
 * Each data set creates, validates, and deletes its own repository.
 *
 * TC-PARAM-01: Create repository with multiple parameter sets
 * TC-PARAM-02: Create private and public repositories (visibility DataProvider)
 */
public class ParameterizedRepoTest extends BaseTest {

    // ═══════════════════════════════════════════════════════════
    //  DATA PROVIDERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Provides test data for repository creation:
     * { namePrefix, description, isPrivate, expectedVisibility }
     */
    @DataProvider(name = "repoCreationData")
    public Object[][] provideRepoCreationData() {
        return new Object[][] {
            // namePrefix,         description,                    isPrivate, expectedVisibility
            { "param-public-1",  "First public parameterized repo",  false,  "public"  },
            { "param-public-2",  "Second public parameterized repo",  false,  "public"  },
            { "param-private-1", "First private parameterized repo",  true,   "private" },
        };
    }

    /**
     * Provides test data for visibility-specific validation:
     * { isPrivate, expectedVisibility, description }
     */
    @DataProvider(name = "visibilityData")
    public Object[][] provideVisibilityData() {
        return new Object[][] {
            { false, "public",  "Public visibility test repo"  },
            { true,  "private", "Private visibility test repo" },
        };
    }

    // ═══════════════════════════════════════════════════════════
    //  TC-PARAM-01: Parameterized create + verify + delete
    // ═══════════════════════════════════════════════════════════

    /**
     * TC-PARAM-01: Parameterized test — creates a repo, verifies key fields, then deletes it.
     *
     * @param namePrefix          Prefix for the generated repo name
     * @param description         Repository description
     * @param isPrivate           Whether the repo should be private
     * @param expectedVisibility  Expected 'visibility' field value in the response
     */
    @Test(dataProvider = "repoCreationData",
          description = "TC-PARAM-01: Parameterized POST /user/repos — create, verify, delete")
    public void testCreateRepository_parameterized(
            String namePrefix, String description, boolean isPrivate, String expectedVisibility) {

        String repoName = TestDataGenerator.generateRepoName(namePrefix);
        log.info("=== TC-PARAM-01 [{} | private={}] → repo='{}' ===",
                namePrefix, isPrivate, repoName);

        trackRepo(repoName);
        try {
            // ── POST: create repository ─────────────────────────
            CreateRepoRequest body = new CreateRepoRequest(repoName, description, isPrivate);

            Response createResponse = given()
                    .spec(requestSpec)
                    .body(body)
                    .when()
                    .post("/user/repos")
                    .then()
                    .extract().response();

            ResponseUtils.logResponse(createResponse);
            ResponseUtils.assertStatusCode(createResponse, 201);

            // ── Deserialize and validate ────────────────────────
            RepositoryDto repo = ResponseUtils.deserialize(createResponse, RepositoryDto.class);

            assertNotNull(repo.getName(), "name must not be null");
            assertEquals(repo.getName(), repoName, "name must match the requested name");
            assertEquals(repo.getDescription(), description, "description must match");
            assertEquals(repo.isPrivate(), isPrivate, "private flag must match");
            assertEquals(repo.getVisibility(), expectedVisibility,
                    "visibility must be '" + expectedVisibility + "'");
            assertEquals(repo.getOwner().getLogin(), Config.getUsername(),
                    "owner login must match authenticated user");

            log.info("TC-PARAM-01 PASSED: repo='{}', visibility='{}'",
                    repo.getName(), repo.getVisibility());

        } finally {
            // ── DELETE: always clean up ─────────────────────────
            deleteRepo(repoName);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  TC-PARAM-02: Verify visibility field with DataProvider
    // ═══════════════════════════════════════════════════════════

    /**
     * TC-PARAM-02: Verifies that the 'visibility' field in the GET response
     * matches the value used during creation.
     *
     * @param isPrivate           Whether the repo should be private
     * @param expectedVisibility  Expected visibility string
     * @param description         Description for the test repo
     */
    @Test(dataProvider = "visibilityData",
          description = "TC-PARAM-02: Verify repository visibility via GET after creation")
    public void testRepositoryVisibility_parameterized(
            boolean isPrivate, String expectedVisibility, String description) {

        String repoName = TestDataGenerator.generateRepoName("visibility");
        log.info("=== TC-PARAM-02 [isPrivate={}, expected='{}'] → repo='{}' ===",
                isPrivate, expectedVisibility, repoName);

        trackRepo(repoName);
        try {
            // Create
            RepositoryDto created = createRepo(repoName, description, isPrivate);
            assertEquals(created.getVisibility(), expectedVisibility,
                    "POST response visibility mismatch");

            // Verify with GET
            Response getResponse = getRepo(repoName);
            ResponseUtils.assertStatusCode(getResponse, 200);

            RepositoryDto fetched = ResponseUtils.deserialize(getResponse, RepositoryDto.class);
            assertEquals(fetched.getVisibility(), expectedVisibility,
                    "GET response visibility must match expected: " + expectedVisibility);
            assertEquals(fetched.isPrivate(), isPrivate,
                    "GET response 'private' field must match: " + isPrivate);

            log.info("TC-PARAM-02 PASSED: visibility='{}' confirmed via GET.", fetched.getVisibility());

        } finally {
            deleteRepo(repoName);
        }
    }
}
