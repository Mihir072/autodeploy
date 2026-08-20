package com.autodeploy.domain.service;

import com.autodeploy.common.exception.ConflictException;
import com.autodeploy.common.exception.ResourceNotFoundException;
import com.autodeploy.domain.dto.AddDomainRequest;
import com.autodeploy.domain.dto.DomainResponse;
import com.autodeploy.domain.entity.Domain;
import com.autodeploy.domain.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DomainService {

    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public List<DomainResponse> listDomains(UUID projectId) {
        return domainRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream().map(this::toResponse).toList();
    }

    public DomainResponse addDomain(UUID userId, AddDomainRequest request) {
        String cleanDomain = request.domainName().trim().toLowerCase();

        if (domainRepository.existsByDomainName(cleanDomain)) {
            throw new ConflictException("Domain '" + cleanDomain + "' is already registered to a project");
        }

        Domain domain = new Domain();
        domain.setProjectId(request.projectId());
        domain.setUserId(userId);
        domain.setDomainName(cleanDomain);
        domain.setType("CUSTOM");
        domain.setVerificationToken("autodeploy-verify=" + UUID.randomUUID().toString().replace("-", ""));
        domain.setVerified(false);
        domain.setSslStatus("PENDING");
        domain.setStatus("ACTIVE");

        Domain saved = domainRepository.save(domain);
        log.info("Registered custom domain '{}' for project {}", cleanDomain, request.projectId());
        return toResponse(saved);
    }

    public DomainResponse verifyDomain(UUID domainId) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + domainId));

        domain.setVerified(true);
        domain.setVerifiedAt(Instant.now());
        domain.setSslStatus("ACTIVE");
        Domain saved = domainRepository.save(domain);
        log.info("Verified domain '{}' with SSL ACTIVE", domain.getDomainName());
        return toResponse(saved);
    }

    public void deleteDomain(UUID domainId) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + domainId));
        domainRepository.delete(domain);
        log.info("Deleted domain '{}'", domain.getDomainName());
    }

    public DomainResponse toResponse(Domain d) {
        String cname = "cname.autodeploy.app";
        String status = d.getVerified() ? "VERIFIED" : "PENDING_VERIFICATION";
        return new DomainResponse(
                d.getId(),
                d.getProjectId(),
                d.getDomainName(),
                d.getType(),
                status,
                d.getSslStatus(),
                d.getVerified(),
                d.getVerificationToken(),
                cname,
                d.getCreatedAt()
        );
    }
}
