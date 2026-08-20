package com.autodeploy.deployment.repository;

import com.autodeploy.deployment.entity.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {

    List<Deployment> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Deployment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Deployment> findByIdAndUserId(UUID id, UUID userId);
}
