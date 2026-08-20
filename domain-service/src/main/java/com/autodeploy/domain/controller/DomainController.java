package com.autodeploy.domain.controller;

import com.autodeploy.common.dto.ApiResponse;
import com.autodeploy.domain.dto.AddDomainRequest;
import com.autodeploy.domain.dto.DomainResponse;
import com.autodeploy.domain.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
@Tag(name = "Domains", description = "Custom domain management and DNS verification")
public class DomainController {

    private final DomainService domainService;

    @GetMapping
    @Operation(summary = "List custom domains for a project")
    public ResponseEntity<ApiResponse<List<DomainResponse>>> getDomains(
            @RequestParam("projectId") UUID projectId) {
        List<DomainResponse> domains = domainService.listDomains(projectId);
        return ResponseEntity.ok(ApiResponse.success(domains));
    }

    @PostMapping
    @Operation(summary = "Add a custom domain to a project")
    public ResponseEntity<ApiResponse<DomainResponse>> addDomain(
            @Valid @RequestBody AddDomainRequest request,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        DomainResponse domain = domainService.addDomain(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Domain added", domain));
    }

    @GetMapping("/{id}/verify")
    @Operation(summary = "Verify domain DNS configuration")
    public ResponseEntity<ApiResponse<DomainResponse>> verifyDomain(@PathVariable UUID id) {
        DomainResponse domain = domainService.verifyDomain(id);
        return ResponseEntity.ok(ApiResponse.success("Domain verified successfully", domain));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete custom domain")
    public ResponseEntity<ApiResponse<Void>> deleteDomain(@PathVariable UUID id) {
        domainService.deleteDomain(id);
        return ResponseEntity.ok(ApiResponse.success("Domain deleted successfully"));
    }
}
