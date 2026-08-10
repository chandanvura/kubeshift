package com.kubeshift.remediate.controller;

import com.kubeshift.remediate.model.RemediationHistory;
import com.kubeshift.remediate.model.RemediationRequest;
import com.kubeshift.remediate.model.RemediationResult;
import com.kubeshift.remediate.service.ApprovalWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/remediation")
@RequiredArgsConstructor
public class RemediationController {

    private final ApprovalWorkflowService workflowService;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitForApproval(@Valid @RequestBody RemediationRequest request) {
        String id = workflowService.submitForApproval(request);
        return ResponseEntity.ok(Map.of("id", id, "status", "PENDING"));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RemediationResult> approve(@PathVariable String id) {
        RemediationResult result = workflowService.approve(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> reject(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "No reason provided");
        workflowService.reject(id, reason);
        return ResponseEntity.ok(Map.of("id", id, "status", "REJECTED"));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RemediationHistory>> listPending() {
        return ResponseEntity.ok(workflowService.listPending());
    }

    @GetMapping("/history")
    public ResponseEntity<List<RemediationHistory>> getHistory() {
        return ResponseEntity.ok(workflowService.getHistory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RemediationHistory> getDetails(@PathVariable String id) {
        RemediationHistory details = workflowService.getDetails(id);
        if (details == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(details);
    }
}
