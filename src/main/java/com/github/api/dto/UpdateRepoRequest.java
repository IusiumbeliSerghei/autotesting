package com.github.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the request body for PATCH /repos/{owner}/{repo}.
 * Only non-null fields are serialized, allowing partial updates.
 *
 * Note: GitHub REST API uses PATCH (not PUT) for partial repository updates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateRepoRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("private")
    private Boolean isPrivate;

    @JsonProperty("has_issues")
    private Boolean hasIssues;

    @JsonProperty("has_wiki")
    private Boolean hasWiki;

    // ──────────── Constructors ────────────

    public UpdateRepoRequest() {}

    /** Convenience constructor for updating only the description. */
    public UpdateRepoRequest(String description) {
        this.description = description;
    }

    // ──────────── Getters ────────────

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Boolean isPrivate()     { return isPrivate; }
    public Boolean getHasIssues()  { return hasIssues; }
    public Boolean getHasWiki()    { return hasWiki; }

    // ──────────── Setters ────────────

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrivate(Boolean isPrivate)      { this.isPrivate = isPrivate; }
    public void setHasIssues(Boolean hasIssues)    { this.hasIssues = hasIssues; }
    public void setHasWiki(Boolean hasWiki)        { this.hasWiki = hasWiki; }

    @Override
    public String toString() {
        return "UpdateRepoRequest{name='" + name + "', description='" + description +
               "', private=" + isPrivate + '}';
    }
}
