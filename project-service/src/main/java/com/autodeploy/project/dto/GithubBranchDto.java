package com.autodeploy.project.dto;

public record GithubBranchDto(
        String name,
        String commitSha
) {}
