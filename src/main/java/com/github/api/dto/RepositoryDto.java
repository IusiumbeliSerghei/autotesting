package com.github.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a GitHub Repository resource returned by the API.
 * Fields not listed here are silently ignored (ignoreUnknown = true).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("private")
    private boolean isPrivate;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("clone_url")
    private String cloneUrl;

    @JsonProperty("owner")
    private OwnerDto owner;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("visibility")
    private String visibility;

    // ────────────────────────── Getters ──────────────────────────

    public long getId()              { return id; }
    public String getName()          { return name; }
    public String getFullName()      { return fullName; }
    public String getDescription()   { return description; }
    public boolean isPrivate()       { return isPrivate; }
    public String getHtmlUrl()       { return htmlUrl; }
    public String getCloneUrl()      { return cloneUrl; }
    public OwnerDto getOwner()       { return owner; }
    public String getDefaultBranch() { return defaultBranch; }
    public String getVisibility()    { return visibility; }

    // ────────────────────────── Setters ──────────────────────────

    public void setId(long id)                      { this.id = id; }
    public void setName(String name)                { this.name = name; }
    public void setFullName(String fullName)        { this.fullName = fullName; }
    public void setDescription(String description)  { this.description = description; }
    public void setPrivate(boolean isPrivate)       { this.isPrivate = isPrivate; }
    public void setHtmlUrl(String htmlUrl)          { this.htmlUrl = htmlUrl; }
    public void setCloneUrl(String cloneUrl)        { this.cloneUrl = cloneUrl; }
    public void setOwner(OwnerDto owner)            { this.owner = owner; }
    public void setDefaultBranch(String b)          { this.defaultBranch = b; }
    public void setVisibility(String visibility)    { this.visibility = visibility; }

    @Override
    public String toString() {
        return "RepositoryDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", description='" + description + '\'' +
                ", private=" + isPrivate +
                ", visibility='" + visibility + '\'' +
                '}';
    }

    // ──────────────── Nested OwnerDto ────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OwnerDto {

        @JsonProperty("login")
        private String login;

        @JsonProperty("id")
        private long id;

        @JsonProperty("html_url")
        private String htmlUrl;

        public String getLogin()   { return login; }
        public long getId()        { return id; }
        public String getHtmlUrl() { return htmlUrl; }

        public void setLogin(String login)     { this.login = login; }
        public void setId(long id)             { this.id = id; }
        public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

        @Override
        public String toString() {
            return "OwnerDto{login='" + login + "', id=" + id + '}';
        }
    }
}
