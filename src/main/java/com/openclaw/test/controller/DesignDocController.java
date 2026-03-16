package com.openclaw.test.controller;

import com.openclaw.test.config.AuthInterceptor;
import com.openclaw.test.dto.DesignDocCreateRequest;
import com.openclaw.test.dto.DesignDocResponse;
import com.openclaw.test.dto.DesignDocUpdateRequest;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.service.DesignDocService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/design-docs")
public class DesignDocController {

    private final DesignDocService designDocService;

    public DesignDocController(DesignDocService designDocService) {
        this.designDocService = designDocService;
    }

    @PostMapping
    public ResponseEntity<DesignDocResponse> createDoc(
            @Valid @RequestBody DesignDocCreateRequest request,
            HttpServletRequest httpRequest) {
        Identity identity = (Identity) httpRequest.getAttribute(AuthInterceptor.IDENTITY_ATTRIBUTE);
        DesignDocResponse response = designDocService.createDoc(request, identity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DesignDocResponse>> getDocs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long taskId) {

        size = Math.min(size, 100);
        page = Math.max(page, 0);

        Page<DesignDocResponse> response = designDocService.getDocs(page, size, taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignDocResponse> getDocById(@PathVariable Long id) {
        DesignDocResponse response = designDocService.getDocById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignDocResponse> updateDoc(
            @PathVariable Long id,
            @Valid @RequestBody DesignDocUpdateRequest request) {
        DesignDocResponse response = designDocService.updateDoc(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoc(@PathVariable Long id) {
        designDocService.deleteDoc(id);
        return ResponseEntity.noContent().build();
    }
}
