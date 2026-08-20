package com.autodeploy.deployment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ec2_instances")
@Getter
@Setter
@NoArgsConstructor
public class Ec2Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "instance_id", nullable = false, unique = true, length = 50)
    private String instanceId;

    @Column(name = "host", nullable = false, length = 255)
    private String host;

    @Column(name = "ssh_port", nullable = false)
    private Integer sshPort = 22;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVE";

    @Column(name = "active_deployments", nullable = false)
    private Integer activeDeployments = 0;

    @Column(name = "max_deployments", nullable = false)
    private Integer maxDeployments = 10;

    @Column(name = "region", nullable = false, length = 50)
    private String region = "us-east-1";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
