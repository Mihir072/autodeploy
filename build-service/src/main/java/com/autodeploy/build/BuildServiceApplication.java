package com.autodeploy.build;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Build Service — container image builder.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Consumes {@code build.requested} messages from RabbitMQ</li>
 *   <li>Shallow-clones the repository at the specified commit SHA</li>
 *   <li>Auto-generates a Dockerfile if none exists (Node.js / Java / Python detection)</li>
 *   <li>Triggers a Kaniko build (as a sub-process or Kubernetes Job) in an isolated context</li>
 *   <li>Pushes the resulting image to AWS ECR with tag {@code {projectId}-{commitSha}}</li>
 *   <li>Publishes real-time log lines to Redis Pub/Sub channel {@code autodeploy:build-logs:{buildId}}</li>
 *   <li>Publishes {@code build.completed} or {@code build.failed} events to RabbitMQ</li>
 *   <li>Exposes SSE endpoint for direct log streaming (fallback when notification-service is unavailable)</li>
 * </ul>
 *
 * <p>Runs on port 8083.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class BuildServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuildServiceApplication.class, args);
    }
}
