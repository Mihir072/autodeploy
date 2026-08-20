package com.autodeploy.project.repository;

import com.autodeploy.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Project> findByIdAndUserId(UUID id, UUID userId);

    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByNameAndUserId(String name, UUID userId);
}
