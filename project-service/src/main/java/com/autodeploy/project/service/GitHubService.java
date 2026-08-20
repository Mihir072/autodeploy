package com.autodeploy.project.service;

import com.autodeploy.project.client.AuthServiceClient;
import com.autodeploy.project.dto.GithubBranchDto;
import com.autodeploy.project.dto.GithubRepoDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<GithubRepoDto> listUserRepositories(UUID userId) {
        String token = fetchToken(userId);
        if (token == null || token.isBlank()) {
            log.warn("No GitHub access token available for user {}", userId);
            return List.of();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            headers.set("User-Agent", "AutoDeploy-Platform");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user/repos?sort=updated&per_page=100&affiliation=owner,collaborator",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                List<GithubRepoDto> repos = new ArrayList<>();
                if (root.isArray()) {
                    for (JsonNode node : root) {
                        Long id = node.path("id").asLong();
                        String name = node.path("name").asText();
                        String fullName = node.path("full_name").asText();
                        String owner = node.path("owner").path("login").asText();
                        String ownerAvatarUrl = node.path("owner").path("avatar_url").asText();
                        boolean isPrivate = node.path("private").asBoolean(false);
                        String defaultBranch = node.path("default_branch").asText("main");
                        String updatedAt = node.path("updated_at").asText();

                        repos.add(new GithubRepoDto(id, name, fullName, owner, ownerAvatarUrl, isPrivate, defaultBranch, updatedAt));
                    }
                }
                return repos;
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub repositories for user {}: {}", userId, e.getMessage());
        }

        return List.of();
    }

    public List<GithubBranchDto> listBranches(UUID userId, String repoFullName) {
        String token = fetchToken(userId);
        if (token == null || token.isBlank() || repoFullName == null) {
            return List.of(new GithubBranchDto("main", null));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("User-Agent", "AutoDeploy-Platform");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/repos/" + repoFullName + "/branches?per_page=100",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                List<GithubBranchDto> branches = new ArrayList<>();
                if (root.isArray()) {
                    for (JsonNode node : root) {
                        String name = node.path("name").asText();
                        String commitSha = node.path("commit").path("sha").asText();
                        branches.add(new GithubBranchDto(name, commitSha));
                    }
                }
                return branches;
            }
        } catch (Exception e) {
            log.error("Failed to fetch branches for repo {}: {}", repoFullName, e.getMessage());
        }

        return List.of(new GithubBranchDto("main", null));
    }

    private String fetchToken(UUID userId) {
        try {
            var res = authServiceClient.getDecryptedGithubToken(userId);
            if (res != null && res.getData() != null) {
                return res.getData();
            }
        } catch (Exception e) {
            log.warn("Could not fetch GitHub token via Feign: {}", e.getMessage());
        }
        return null;
    }
}
