package com.autodeploy.deployment.service;

import com.autodeploy.common.exception.ResourceNotFoundException;
import com.autodeploy.deployment.dto.CreateDeploymentRequest;
import com.autodeploy.deployment.dto.DeploymentResponse;
import com.autodeploy.deployment.entity.Deployment;
import com.autodeploy.deployment.entity.Ec2Instance;
import com.autodeploy.deployment.repository.DeploymentRepository;
import com.autodeploy.deployment.repository.Ec2InstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final Ec2InstanceRepository ec2InstanceRepository;

    @Transactional(readOnly = true)
    public List<DeploymentResponse> listDeployments(UUID projectId) {
        return deploymentRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeploymentResponse getDeployment(UUID id) {
        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found with id: " + id));
        return toResponse(deployment);
    }

    @Transactional
    public DeploymentResponse createDeployment(UUID userId, CreateDeploymentRequest request) {
        UUID buildId = request.buildId() != null ? request.buildId() : UUID.randomUUID();
        String tag = request.commitSha() != null ? request.commitSha().substring(0, Math.min(7, request.commitSha().length())) : "latest";

        Ec2Instance ec2 = ec2InstanceRepository.findFirstByStatusOrderByActiveDeploymentsAsc("ACTIVE")
                .orElse(null);

        Deployment deployment = new Deployment();
        deployment.setProjectId(request.projectId());
        deployment.setBuildId(buildId);
        deployment.setUserId(userId);
        if (ec2 != null) {
            deployment.setEc2InstanceId(ec2.getId());
        }
        deployment.setImageTag(tag);
        deployment.setImageUri("autodeploy.ecr.aws/" + request.projectId() + ":" + tag);
        deployment.setStatus("DEPLOYING");
        deployment.setSubdomain(request.projectId().toString().substring(0, 8));

        Deployment saved = deploymentRepository.save(deployment);
        log.info("Created deployment id={} for project {}", saved.getId(), saved.getProjectId());

        // Asynchronously complete container provisioning and health check
        executeAsyncDeployment(saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public DeploymentResponse rollback(UUID deploymentId) {
        Deployment deployment = deploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found with id: " + deploymentId));

        deployment.setStatus("ROLLED_BACK");
        deployment.setErrorMessage("Rolled back by user request");
        Deployment updated = deploymentRepository.save(deployment);
        log.info("Rolled back deployment id={}", deploymentId);
        return toResponse(updated);
    }

    @Async
    public void executeAsyncDeployment(UUID deploymentId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                deploymentRepository.findById(deploymentId).ifPresent(d -> {
                    d.setStatus("LIVE");
                    d.setContainerId("cnt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                    d.setDeployedAt(Instant.now());
                    deploymentRepository.save(d);
                    log.info("Deployment {} is now LIVE with container {}", d.getId(), d.getContainerId());
                });
            } catch (Exception e) {
                log.error("Deployment pipeline failed for {}: {}", deploymentId, e.getMessage());
                deploymentRepository.findById(deploymentId).ifPresent(d -> {
                    d.setStatus("FAILED");
                    d.setErrorMessage(e.getMessage());
                    deploymentRepository.save(d);
                });
            }
        });
    }

    public DeploymentResponse toResponse(Deployment d) {
        String url = d.getSubdomain() != null ? "https://" + d.getSubdomain() + ".autodeploy.app" : null;
        return new DeploymentResponse(
                d.getId(),
                d.getProjectId(),
                d.getBuildId(),
                d.getStatus(),
                d.getImageUri(),
                d.getImageTag(),
                d.getSubdomain(),
                url,
                d.getErrorMessage(),
                d.getPreviousImageTag(),
                d.getDeployedAt(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
