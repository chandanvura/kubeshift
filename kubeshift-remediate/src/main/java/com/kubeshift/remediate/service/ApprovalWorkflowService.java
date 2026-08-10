package com.kubeshift.remediate.service;

import com.kubeshift.remediate.exception.RemediationException;
import com.kubeshift.remediate.model.PatchType;
import com.kubeshift.remediate.model.RemediationHistory;
import com.kubeshift.remediate.model.RemediationRequest;
import com.kubeshift.remediate.model.RemediationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApprovalWorkflowService {

    private final PatchGeneratorService patchGeneratorService;
    private final GitHubPRService gitHubPRService;

    // In-memory store for pending and history
    private final Map<String, RemediationHistory> historyMap = new ConcurrentHashMap<>();
    private final Map<String, RemediationRequest> requestMap = new ConcurrentHashMap<>();

    public String submitForApproval(RemediationRequest request) {
        String id = UUID.randomUUID().toString();
        
        RemediationHistory history = new RemediationHistory(
            id,
            request.namespace(),
            request.deploymentName(),
            null,
            "PENDING",
            0.0, // calculate savings logic would go here
            Instant.now(),
            null
        );

        historyMap.put(id, history);
        requestMap.put(id, request);
        
        log.info("Submitted remediation {} for approval", id);
        return id;
    }

    public RemediationResult approve(String remediationId) {
        RemediationRequest request = requestMap.get(remediationId);
        RemediationHistory history = historyMap.get(remediationId);

        if (request == null || history == null) {
            throw new RemediationException("Remediation ID not found: " + remediationId);
        }

        if (!"PENDING".equals(history.status())) {
            throw new RemediationException("Remediation is not in PENDING state: " + history.status());
        }

        // Generate patch
        String patchContent = patchGeneratorService.generatePatch(request, PatchType.KUBERNETES_MANIFEST);
        
        String branchName = "kubeshift-optimize-" + request.deploymentName() + "-" + Instant.now().toEpochMilli();
        String commitMessage = "[KubeShift] Optimize resources for " + request.deploymentName();

        RemediationResult result = gitHubPRService.createRemediationPR(
            request.targetRepository(),
            branchName,
            request.manifestPath(),
            patchContent,
            commitMessage,
            request.deploymentName(),
            request.namespace()
        );

        // Update history
        RemediationHistory updatedHistory = new RemediationHistory(
            history.id(),
            history.namespace(),
            history.deploymentName(),
            result.prUrl(),
            "APPROVED",
            history.estimatedSavingsUsd(),
            history.createdAt(),
            Instant.now()
        );
        historyMap.put(remediationId, updatedHistory);
        requestMap.remove(remediationId);

        log.info("Approved remediation {}", remediationId);
        return result;
    }

    public void reject(String remediationId, String reason) {
        RemediationHistory history = historyMap.get(remediationId);
        if (history == null) {
            throw new RemediationException("Remediation ID not found: " + remediationId);
        }

        if (!"PENDING".equals(history.status())) {
            throw new RemediationException("Remediation is not in PENDING state: " + history.status());
        }

        RemediationHistory updatedHistory = new RemediationHistory(
            history.id(),
            history.namespace(),
            history.deploymentName(),
            null,
            "REJECTED: " + reason,
            history.estimatedSavingsUsd(),
            history.createdAt(),
            Instant.now()
        );
        historyMap.put(remediationId, updatedHistory);
        requestMap.remove(remediationId);
        
        log.info("Rejected remediation {}: {}", remediationId, reason);
    }

    public List<RemediationHistory> listPending() {
        return historyMap.values().stream()
                .filter(h -> "PENDING".equals(h.status()))
                .collect(Collectors.toList());
    }

    public List<RemediationHistory> getHistory() {
        return new ArrayList<>(historyMap.values());
    }
    
    public RemediationHistory getDetails(String id) {
        return historyMap.get(id);
    }
}
