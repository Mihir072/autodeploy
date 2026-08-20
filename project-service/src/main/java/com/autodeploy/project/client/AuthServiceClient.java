package com.autodeploy.project.client;

import com.autodeploy.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/auth/internal/users/{userId}/github-token")
    ApiResponse<String> getDecryptedGithubToken(@PathVariable("userId") UUID userId);
}
