package com.autodeploy.deployment.controller;

import com.autodeploy.common.dto.ApiResponse;
import com.autodeploy.deployment.dto.CreateDeploymentRequest;
import com.autodeploy.deployment.dto.DeploymentResponse;
import com.autodeploy.deployment.service.DeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/deployments")
@RequiredArgsConstructor
@Tag(name = "Deployments", description = "Deployment lifecycle and container orchestration")
public class DeploymentController {

    private final DeploymentService deploymentService;

    @GetMapping
    @Operation(summary = "List deployments for a project")
    public ResponseEntity<ApiResponse<List<DeploymentResponse>>> getDeployments(
            @RequestParam("projectId") UUID projectId) {
        List<DeploymentResponse> deployments = deploymentService.listDeployments(projectId);
        return ResponseEntity.ok(ApiResponse.success(deployments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get deployment by ID")
    public ResponseEntity<ApiResponse<DeploymentResponse>> getDeploymentById(@PathVariable UUID id) {
        DeploymentResponse deployment = deploymentService.getDeployment(id);
        return ResponseEntity.ok(ApiResponse.success(deployment));
    }

    @PostMapping
    @Operation(summary = "Create and trigger a new deployment")
    public ResponseEntity<ApiResponse<DeploymentResponse>> createDeployment(
            @Valid @RequestBody CreateDeploymentRequest request,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        DeploymentResponse deployment = deploymentService.createDeployment(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("Deployment initiated", deployment));
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "Rollback deployment to previous version")
    public ResponseEntity<ApiResponse<DeploymentResponse>> rollbackDeployment(@PathVariable UUID id) {
        DeploymentResponse deployment = deploymentService.rollback(id);
        return ResponseEntity.ok(ApiResponse.success("Deployment rolled back", deployment));
    }
}
