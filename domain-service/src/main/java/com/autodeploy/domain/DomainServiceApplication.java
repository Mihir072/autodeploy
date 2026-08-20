package com.autodeploy.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Domain Service — DNS and TLS certificate management.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Auto-assigns subdomain on first deploy: {@code {project-slug}.yourdomain.com}</li>
 *   <li>Creates AWS Route53 A/CNAME records pointing to the Traefik load balancer</li>
 *   <li>Custom domain flow: user submits domain → generate TXT verification token → verify → CNAME</li>
 *   <li>Instructs Traefik to provision Let's Encrypt certificates via Docker label injection</li>
 *   <li>Scheduled task polls DNS propagation for pending custom domain verifications</li>
 * </ul>
 *
 * <p>Runs on port 8085.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class DomainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainServiceApplication.class, args);
    }
}
