package com.kubeshift.remediate.service;

import com.kubeshift.remediate.exception.RemediationException;
import com.kubeshift.remediate.model.RemediationHistory;
import com.kubeshift.remediate.model.RemediationRequest;
import com.kubeshift.remediate.model.RemediationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ApprovalWorkflowServiceTest {

    @Mock
    private PatchGeneratorService patchGeneratorService;

    @Mock
    private GitHubPRService gitHubPRService;

    @InjectMocks
    private ApprovalWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitCreatesPendingEntry() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "deploy.yaml", Map.of()
        );

        String id = workflowService.submitForApproval(request);
        
        assertNotNull(id);
        List<RemediationHistory> pending = workflowService.listPending();
        assertEquals(1, pending.size());
        assertEquals("PENDING", pending.get(0).status());
    }

    @Test
    void testApproveTriggersPRCreation() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "deploy.yaml", Map.of()
        );

        String id = workflowService.submitForApproval(request);

        when(patchGeneratorService.generatePatch(any(), any())).thenReturn("patch-content");
        when(gitHubPRService.createRemediationPR(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(new RemediationResult("url", "pr-1", "CREATED", "branch", "msg", List.of(), Instant.now()));

        RemediationResult result = workflowService.approve(id);

        assertNotNull(result);
        assertEquals("CREATED", result.status());
        
        RemediationHistory details = workflowService.getDetails(id);
        assertEquals("APPROVED", details.status());
        
        verify(gitHubPRService, times(1)).createRemediationPR(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testRejectUpdatesStatus() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "deploy.yaml", Map.of()
        );

        String id = workflowService.submitForApproval(request);

        workflowService.reject(id, "Not needed");

        RemediationHistory details = workflowService.getDetails(id);
        assertTrue(details.status().startsWith("REJECTED"));
        
        assertEquals(0, workflowService.listPending().size());
    }

    @Test
    void testListPendingReturnsOnlyPendingItems() {
        RemediationRequest request1 = new RemediationRequest("default", "app1", "c1", "r1", "m", "p1", Map.of());
        RemediationRequest request2 = new RemediationRequest("default", "app2", "c2", "r2", "m", "p2", Map.of());

        String id1 = workflowService.submitForApproval(request1);
        String id2 = workflowService.submitForApproval(request2);

        workflowService.reject(id1, "Reject 1");

        List<RemediationHistory> pending = workflowService.listPending();
        assertEquals(1, pending.size());
        assertEquals(id2, pending.get(0).id());
    }
}
