package com.autodeploy.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Project Service — repository management and build orchestration.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Fetch user's GitHub repositories using stored access token (via auth-service)</li>
 *   <li>CRUD for "projects" (a project = one configured deployment target)</li>
 *   <li>Store and encrypt per-project environment variables</li>
 *   <li>Register GitHub push webhooks on project creation</li>
 *   <li>Publish {@code build.requested} events to RabbitMQ when a push or manual trigger occurs</li>
 * </ul>
 *
 * <p>Runs on port 8082.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class ProjectServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}
