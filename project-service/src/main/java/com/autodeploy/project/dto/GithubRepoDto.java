package com.autodeploy.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepoDto(
        Long id,
        String name,
        @JsonProperty("fullName")
        String fullName,
        String owner,
        @JsonProperty("ownerAvatarUrl")
        String ownerAvatarUrl,
        @JsonProperty("private")
        Boolean isPrivate,
        @JsonProperty("defaultBranch")
        String defaultBranch,
        @JsonProperty("updatedAt")
        String updatedAt
) {}
