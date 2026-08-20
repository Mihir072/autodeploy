package com.autodeploy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — Spring Cloud Gateway (reactive).
 *
 * <p>Single entry point for all frontend requests. Responsibilities:</p>
 * <ul>
 *   <li>JWT authentication filter (validates token, forwards user identity as headers)</li>
 *   <li>Dynamic routing to all microservices via Eureka service discovery</li>
 *   <li>Per-user rate limiting backed by Redis</li>
 *   <li>CORS configuration</li>
 * </ul>
 *
 * <p>Runs on port 8080. All API paths are prefixed with {@code /api/<service>}.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
