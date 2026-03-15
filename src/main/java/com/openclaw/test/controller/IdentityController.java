package com.openclaw.test.controller;

import com.openclaw.test.dto.IdentityCreateRequest;
import com.openclaw.test.dto.IdentityResponse;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identities")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping
    public ResponseEntity<IdentityResponse> createIdentity(@Valid @RequestBody IdentityCreateRequest request) {
        IdentityResponse response = identityService.createIdentity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<IdentityResponse>> getIdentities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) IdentityType type) {

        size = Math.min(size, 100);
        page = Math.max(page, 0);

        Page<IdentityResponse> response = identityService.getIdentities(page, size, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IdentityResponse> getIdentityById(@PathVariable Long id) {
        IdentityResponse response = identityService.getIdentityById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIdentity(@PathVariable Long id) {
        identityService.deleteIdentity(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/regenerate-key")
    public ResponseEntity<IdentityResponse> regenerateApiKey(@PathVariable Long id) {
        IdentityResponse response = identityService.regenerateApiKey(id);
        return ResponseEntity.ok(response);
    }
}
