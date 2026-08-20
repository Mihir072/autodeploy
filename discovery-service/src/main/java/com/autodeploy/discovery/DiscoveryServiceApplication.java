package com.autodeploy.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Discovery Service — Eureka Server.
 *
 * <p>All microservices register themselves here on startup.
 * The API Gateway uses this registry for client-side load balancing (lb:// URIs).</p>
 *
 * <p>Dashboard: http://localhost:8761 (credentials: admin / admin in local dev)</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}
