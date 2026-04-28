package com.github.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the request body for POST /user/repos.
 * Only non-null fields will be included in the serialized JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRepoRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("private")
    private Boolean isPrivate;

    @JsonProperty("auto_init")
    private Boolean autoInit;

    // ──────────── Builder-style constructor ────────────

    public CreateRepoRequest() {}

    public CreateRepoRequest(String name, String description, boolean isPrivate) {
        this.name        = name;
        this.description = description;
        this.isPrivate   = isPrivate;
        this.autoInit    = false;
    }

    // ──────────── Getters ────────────

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Boolean isPrivate()     { return isPrivate; }
    public Boolean getAutoInit()   { return autoInit; }

    // ──────────── Setters ────────────

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrivate(Boolean isPrivate)      { this.isPrivate = isPrivate; }
    public void setAutoInit(Boolean autoInit)      { this.autoInit = autoInit; }

    @Override
    public String toString() {
        return "CreateRepoRequest{name='" + name + "', description='" + description +
               "', private=" + isPrivate + '}';
    }
}
