package com.autodeploy.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "domains",
    indexes = {
        @Index(name = "idx_domains_project_id", columnList = "project_id"),
        @Index(name = "idx_domains_user_id", columnList = "user_id"),
        @Index(name = "idx_domains_domain_name", columnList = "domain_name"),
        @Index(name = "idx_domains_verified", columnList = "verified"),
        @Index(name = "idx_domains_type", columnList = "type")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Domain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "domain_name", nullable = false, unique = true, length = 500)
    private String domainName;

    @Column(name = "type", nullable = false, length = 20)
    private String type = "CUSTOM";

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "ssl_status", nullable = false, length = 50)
    private String sslStatus = "PENDING";

    @Column(name = "route53_record_id", length = 255)
    private String route53RecordId;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (type == null) {
            type = "CUSTOM";
        }
        if (verified == null) {
            verified = false;
        }
        if (sslStatus == null) {
            sslStatus = "PENDING";
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
