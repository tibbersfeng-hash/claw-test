package com.openclaw.test.controller;

import com.openclaw.test.dto.RequirementCreateRequest;
import com.openclaw.test.dto.RequirementHistoryResponse;
import com.openclaw.test.dto.RequirementResponse;
import com.openclaw.test.dto.RequirementUpdateRequest;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.RequirementStatus;
import com.openclaw.test.service.RequirementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping
    public ResponseEntity<RequirementResponse> createRequirement(
            @Valid @RequestBody RequirementCreateRequest request,
            @RequestAttribute("identity") Identity identity) {
        RequirementResponse response = requirementService.createRequirement(request, identity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequirementResponse> updateRequirement(
            @PathVariable Long id,
            @Valid @RequestBody RequirementUpdateRequest request,
            @RequestAttribute("identity") Identity identity) {
        RequirementResponse response = requirementService.updateRequirement(id, request, identity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<RequirementResponse> submitRequirement(
            @PathVariable Long id,
            @RequestAttribute("identity") Identity identity) {
        RequirementResponse response = requirementService.submitRequirement(id, identity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RequirementResponse> approveRequirement(
            @PathVariable Long id,
            @RequestAttribute("identity") Identity identity) {
        RequirementResponse response = requirementService.approveRequirement(id, identity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<RequirementResponse> rejectRequirement(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestAttribute("identity") Identity identity) {
        String remark = body != null ? body.get("remark") : null;
        RequirementResponse response = requirementService.rejectRequirement(id, remark, identity);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RequirementResponse>> getRequirements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RequirementStatus status,
            @RequestParam(required = false) String keyword) {
        Page<RequirementResponse> requirements = requirementService.getRequirements(page, size, status, keyword);
        return ResponseEntity.ok(requirements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequirementResponse> getRequirementById(@PathVariable Long id) {
        RequirementResponse response = requirementService.getRequirementById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{requirementNumber}")
    public ResponseEntity<RequirementResponse> getRequirementByNumber(@PathVariable String requirementNumber) {
        RequirementResponse response = requirementService.getRequirementByNumber(requirementNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<RequirementHistoryResponse>> getRequirementHistory(@PathVariable Long id) {
        List<RequirementHistoryResponse> history = requirementService.getRequirementHistory(id);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequirement(
            @PathVariable Long id,
            @RequestAttribute("identity") Identity identity) {
        requirementService.deleteRequirement(id, identity);
        return ResponseEntity.noContent().build();
    }
}
