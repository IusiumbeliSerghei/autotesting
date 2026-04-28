package com.github.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the authenticated GitHub user returned by GET /user.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("login")
    private String login;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("public_repos")
    private int publicRepos;

    @JsonProperty("followers")
    private int followers;

    @JsonProperty("following")
    private int following;

    @JsonProperty("type")
    private String type;

    // ──────────── Getters ────────────

    public long getId()          { return id; }
    public String getLogin()     { return login; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getHtmlUrl()   { return htmlUrl; }
    public int getPublicRepos()  { return publicRepos; }
    public int getFollowers()    { return followers; }
    public int getFollowing()    { return following; }
    public String getType()      { return type; }

    // ──────────── Setters ────────────

    public void setId(long id)               { this.id = id; }
    public void setLogin(String login)       { this.login = login; }
    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setHtmlUrl(String htmlUrl)   { this.htmlUrl = htmlUrl; }
    public void setPublicRepos(int n)        { this.publicRepos = n; }
    public void setFollowers(int n)          { this.followers = n; }
    public void setFollowing(int n)          { this.following = n; }
    public void setType(String type)         { this.type = type; }

    @Override
    public String toString() {
        return "UserDto{id=" + id + ", login='" + login + "', name='" + name +
               "', publicRepos=" + publicRepos + '}';
    }
}
