package com.autodeploy.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Auth Service — GitHub OAuth2 login and JWT issuance.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>GitHub OAuth2 authorization code flow (Spring Security handles the redirect)</li>
 *   <li>On first login: create user record with encrypted GitHub access token</li>
 *   <li>Issue platform access + refresh JWT tokens after OAuth callback</li>
 *   <li>Refresh token rotation endpoint</li>
 *   <li>{@code GET /auth/me} — return current authenticated user's profile</li>
 * </ul>
 *
 * <p>Runs on port 8081.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
