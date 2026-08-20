package com.autodeploy.project.controller;

import com.autodeploy.common.dto.ApiResponse;
import com.autodeploy.project.dto.CreateProjectRequest;
import com.autodeploy.project.dto.GithubBranchDto;
import com.autodeploy.project.dto.GithubRepoDto;
import com.autodeploy.project.dto.ProjectResponse;
import com.autodeploy.project.dto.UpdateProjectRequest;
import com.autodeploy.project.service.GitHubService;
import com.autodeploy.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management and GitHub repository integration")
public class ProjectController {

    private final ProjectService projectService;
    private final GitHubService gitHubService;

    @GetMapping
    @Operation(summary = "List user projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        List<ProjectResponse> projects = projectService.listUserProjects(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        ProjectResponse project = projectService.getProjectById(id, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        ProjectResponse created = projectService.createProject(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @RequestBody UpdateProjectRequest request,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        ProjectResponse updated = projectService.updateProject(id, UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        projectService.deleteProject(id, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    @GetMapping({"/github/repos", "/github-repos"})
    @Operation(summary = "List user GitHub repositories")
    public ResponseEntity<ApiResponse<List<GithubRepoDto>>> getGitHubRepos(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        List<GithubRepoDto> repos = gitHubService.listUserRepositories(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(repos));
    }

    @GetMapping("/github/branches")
    @Operation(summary = "List branches for a GitHub repository")
    public ResponseEntity<ApiResponse<List<GithubBranchDto>>> getGitHubBranches(
            @RequestParam("repo") String repoFullName,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        List<GithubBranchDto> branches = gitHubService.listBranches(UUID.fromString(userId), repoFullName);
        return ResponseEntity.ok(ApiResponse.success(branches));
    }
}
