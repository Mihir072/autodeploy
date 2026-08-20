package com.autodeploy.project.service;

import com.autodeploy.common.exception.ResourceNotFoundException;
import com.autodeploy.common.util.AesEncryptionUtil;
import com.autodeploy.common.util.SlugUtil;
import com.autodeploy.project.dto.CreateProjectRequest;
import com.autodeploy.project.dto.EnvironmentVariableDto;
import com.autodeploy.project.dto.ProjectResponse;
import com.autodeploy.project.dto.UpdateProjectRequest;
import com.autodeploy.project.entity.Project;
import com.autodeploy.project.repository.ProjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ProjectResponse> listUserProjects(UUID userId) {
        List<Project> projects = projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return projects.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id, UUID userId) {
        Project project = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return toResponse(project);
    }

    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        Project project = new Project();
        project.setUserId(userId);
        project.setName(request.name().trim());
        project.setRepoFullName(request.repositoryName().trim());
        project.setRepoUrl(request.repositoryUrl().trim());
        project.setBranch(request.branch() != null && !request.branch().isBlank() ? request.branch().trim() : "main");
        project.setBuildCommand(request.buildCommand());
        project.setDockerfilePath(request.dockerfilePath());
        project.setStatus("INACTIVE");

        // Generate unique slug
        String slug = SlugUtil.generateUniqueSlug(request.name());
        while (projectRepository.existsBySlug(slug)) {
            slug = SlugUtil.generateUniqueSlug(request.name());
        }
        project.setSlug(slug);

        // Encrypt environment variables if provided
        if (request.environmentVariables() != null && !request.environmentVariables().isEmpty()) {
            project.setEnvVarsEncrypted(encryptEnvVars(request.environmentVariables()));
        }

        Project saved = projectRepository.save(project);
        log.info("Created project '{}' (id={}, slug={}) for user {}", saved.getName(), saved.getId(), saved.getSlug(), userId);
        return toResponse(saved);
    }

    public ProjectResponse updateProject(UUID id, UUID userId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            project.setName(request.name().trim());
        }
        if (request.branch() != null && !request.branch().isBlank()) {
            project.setBranch(request.branch().trim());
        }
        if (request.buildCommand() != null) {
            project.setBuildCommand(request.buildCommand());
        }
        if (request.dockerfilePath() != null) {
            project.setDockerfilePath(request.dockerfilePath());
        }
        if (request.environmentVariables() != null) {
            project.setEnvVarsEncrypted(encryptEnvVars(request.environmentVariables()));
        }

        Project updated = projectRepository.save(project);
        log.info("Updated project id={} for user {}", id, userId);
        return toResponse(updated);
    }

    public void deleteProject(UUID id, UUID userId) {
        Project project = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
        log.info("Deleted project id={} for user {}", id, userId);
    }

    private String encryptEnvVars(List<EnvironmentVariableDto> vars) {
        try {
            String json = objectMapper.writeValueAsString(vars);
            return aesEncryptionUtil.encrypt(json);
        } catch (Exception e) {
            log.error("Failed to encrypt environment variables: {}", e.getMessage());
            return null;
        }
    }

    private List<EnvironmentVariableDto> decryptEnvVars(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            return new ArrayList<>();
        }
        try {
            String json = aesEncryptionUtil.decrypt(encrypted);
            return objectMapper.readValue(json, new TypeReference<List<EnvironmentVariableDto>>() {});
        } catch (Exception e) {
            log.error("Failed to decrypt environment variables: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public ProjectResponse toResponse(Project project) {
        List<EnvironmentVariableDto> envVars = decryptEnvVars(project.getEnvVarsEncrypted());
        String subdomain = project.getSlug() + ".autodeploy.app";

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getRepoUrl(),
                project.getRepoFullName(),
                project.getBranch(),
                project.getBuildCommand(),
                null,
                project.getDockerfilePath(),
                project.getStatus(),
                envVars,
                null,
                subdomain,
                null,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
