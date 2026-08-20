package com.autodeploy.build.repository;

import com.autodeploy.build.entity.Build;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildRepository extends JpaRepository<Build, UUID> {

    List<Build> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Build> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Build> findByIdAndUserId(UUID id, UUID userId);
}
