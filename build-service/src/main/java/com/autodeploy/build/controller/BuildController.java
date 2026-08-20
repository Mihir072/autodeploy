package com.autodeploy.build.controller;

import com.autodeploy.build.dto.BuildResponse;
import com.autodeploy.build.dto.TriggerBuildRequest;
import com.autodeploy.build.service.BuildService;
import com.autodeploy.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/builds")
@RequiredArgsConstructor
@Tag(name = "Builds", description = "Build pipeline orchestration and real-time log streaming")
public class BuildController {

    private final BuildService buildService;

    @GetMapping
    @Operation(summary = "List builds for a project")
    public ResponseEntity<ApiResponse<List<BuildResponse>>> getBuilds(
            @RequestParam("projectId") UUID projectId) {
        List<BuildResponse> builds = buildService.listBuilds(projectId);
        return ResponseEntity.ok(ApiResponse.success(builds));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get build by ID")
    public ResponseEntity<ApiResponse<BuildResponse>> getBuildById(@PathVariable UUID id) {
        BuildResponse build = buildService.getBuild(id);
        return ResponseEntity.ok(ApiResponse.success(build));
    }

    @PostMapping("/trigger")
    @Operation(summary = "Trigger a new build")
    public ResponseEntity<ApiResponse<BuildResponse>> triggerBuild(
            @Valid @RequestBody TriggerBuildRequest request,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        BuildResponse build = buildService.triggerBuild(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("Build triggered", build));
    }

    @GetMapping(value = {"/{id}/logs", "/{id}/logs/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream live build logs via Server-Sent Events (SSE)")
    public SseEmitter streamBuildLogs(@PathVariable UUID id) {
        return buildService.createLogEmitter(id);
    }
}
