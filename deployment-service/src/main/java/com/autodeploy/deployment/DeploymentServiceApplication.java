package com.autodeploy.deployment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Deployment Service — EC2 container lifecycle management.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Consumes {@code build.completed} events from RabbitMQ</li>
 *   <li>Selects a target EC2 instance (round-robin / least-loaded)</li>
 *   <li>SSH into the EC2 instance using SSHJ and runs Docker commands</li>
 *   <li>Generates Traefik routing labels for automatic subdomain wiring</li>
 *   <li>Health-checks the new container before marking it live</li>
 *   <li>Rolls back to the previous image tag on health-check failure</li>
 *   <li>Publishes {@code deployment.completed} or {@code deployment.failed} to RabbitMQ</li>
 *   <li>Supports manual rollback via {@code POST /deployments/{id}/rollback}</li>
 * </ul>
 *
 * <p>Runs on port 8084.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class DeploymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeploymentServiceApplication.class, args);
    }
}
