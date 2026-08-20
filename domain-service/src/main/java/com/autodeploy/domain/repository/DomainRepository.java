package com.autodeploy.domain.repository;

import com.autodeploy.domain.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {

    List<Domain> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<Domain> findByDomainName(String domainName);

    boolean existsByDomainName(String domainName);
}
