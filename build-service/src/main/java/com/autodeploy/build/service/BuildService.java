package com.autodeploy.build.service;

import com.autodeploy.build.dto.BuildResponse;
import com.autodeploy.build.dto.TriggerBuildRequest;
import com.autodeploy.build.entity.Build;
import com.autodeploy.build.repository.BuildRepository;
import com.autodeploy.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

    private final BuildRepository buildRepository;
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<BuildResponse> listBuilds(UUID projectId) {
        return buildRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BuildResponse getBuild(UUID id) {
        Build build = buildRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Build not found with id: " + id));
        return toResponse(build);
    }

    @Transactional
    public BuildResponse triggerBuild(UUID userId, TriggerBuildRequest request) {
        String branch = request.branch() != null && !request.branch().isBlank() ? request.branch() : "main";
        String sha = request.commitSha() != null && !request.commitSha().isBlank()
                ? request.commitSha()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 40);
        String msg = request.commitMessage() != null ? request.commitMessage() : "Manual trigger build";

        Build build = new Build();
        build.setProjectId(request.projectId());
        build.setUserId(userId);
        build.setBranch(branch);
        build.setCommitSha(sha);
        build.setCommitMessage(msg);
        build.setStatus("RUNNING");
        build.setTriggeredBy("MANUAL");
        build.setStartedAt(Instant.now());
        build.setImageTag(build.getProjectId().toString().substring(0, 8) + "-" + sha.substring(0, 7));
        build.setImageUri("autodeploy.ecr.aws/" + build.getImageTag());

        Build saved = buildRepository.save(build);
        log.info("Triggered build id={} for project {}", saved.getId(), saved.getProjectId());

        // Run asynchronous build simulation and log streaming
        executeAsyncBuildPipeline(saved.getId());

        return toResponse(saved);
    }

    public SseEmitter createLogEmitter(UUID buildId) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 minutes timeout
        emitters.computeIfAbsent(buildId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(buildId, emitter));
        emitter.onTimeout(() -> removeEmitter(buildId, emitter));
        emitter.onError((e) -> removeEmitter(buildId, emitter));

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event().name("message").data("[INIT] Connected to live build stream for build " + buildId));
        } catch (Exception ignored) {}

        return emitter;
    }

    private void removeEmitter(UUID buildId, SseEmitter emitter) {
        var list = emitters.get(buildId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(buildId);
            }
        }
    }

    public void broadcastLog(UUID buildId, String logLine) {
        var list = emitters.get(buildId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("message").data(logLine));
                } catch (Exception e) {
                    emitter.complete();
                }
            }
        }
    }

    @Async
    public void executeAsyncBuildPipeline(UUID buildId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                broadcastLog(buildId, "[BUILD] Initializing rootless Kaniko executor in isolated container sandbox...");
                Thread.sleep(1500);
                broadcastLog(buildId, "[BUILD] Fetching Git source tree from repository at target commit SHA...");
                Thread.sleep(1500);
                broadcastLog(buildId, "[BUILD] Analyzing repository dependencies and Dockerfile configuration...");
                Thread.sleep(2000);
                broadcastLog(buildId, "[BUILD] Executing build command: npm ci && npm run build");
                Thread.sleep(2500);
                broadcastLog(buildId, "[BUILD] Compiling TypeScript AST and optimizing production bundles...");
                Thread.sleep(2000);
                broadcastLog(buildId, "[BUILD] Generated static assets and server output artifacts");
                Thread.sleep(1500);
                broadcastLog(buildId, "[BUILD] Pushing container image layer cache to AWS ECR...");
                Thread.sleep(2000);
                broadcastLog(buildId, "[BUILD] Container image digest pushed: sha256:" + UUID.randomUUID().toString().replace("-", ""));
                Thread.sleep(1000);
                broadcastLog(buildId, "[SUCCESS] Build completed successfully in 14.2s");

                // Update build status in DB
                buildRepository.findById(buildId).ifPresent(b -> {
                    b.setStatus("SUCCESS");
                    b.setFinishedAt(Instant.now());
                    buildRepository.save(b);
                });
            } catch (Exception e) {
                broadcastLog(buildId, "[ERROR] Build failed: " + e.getMessage());
                buildRepository.findById(buildId).ifPresent(b -> {
                    b.setStatus("FAILED");
                    b.setErrorMessage(e.getMessage());
                    b.setFinishedAt(Instant.now());
                    buildRepository.save(b);
                });
            }
        });
    }

    public BuildResponse toResponse(Build build) {
        return new BuildResponse(
                build.getId(),
                build.getProjectId(),
                build.getStatus(),
                build.getCommitSha(),
                build.getCommitMessage(),
                build.getBranch(),
                build.getImageUri(),
                build.getImageTag(),
                build.getErrorMessage(),
                build.getTriggeredBy(),
                build.getStartedAt(),
                build.getFinishedAt(),
                build.getCreatedAt()
        );
    }
}
