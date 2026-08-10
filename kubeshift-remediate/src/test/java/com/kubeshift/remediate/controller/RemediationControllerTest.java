package com.kubeshift.remediate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubeshift.remediate.model.RemediationHistory;
import com.kubeshift.remediate.model.RemediationRequest;
import com.kubeshift.remediate.model.RemediationResult;
import com.kubeshift.remediate.service.ApprovalWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RemediationController.class)
class RemediationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalWorkflowService workflowService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSubmitForApproval() throws Exception {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo", "main", "path", Map.of()
        );

        when(workflowService.submitForApproval(any())).thenReturn("12345");

        mockMvc.perform(post("/api/v1/remediation/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12345"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testApprove() throws Exception {
        RemediationResult result = new RemediationResult(
            "url", "pr-1", "CREATED", "branch", "msg", List.of(), Instant.now()
        );

        when(workflowService.approve("123")).thenReturn(result);

        mockMvc.perform(post("/api/v1/remediation/123/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.prNumber").value("pr-1"));
    }

    @Test
    void testListPending() throws Exception {
        RemediationHistory history = new RemediationHistory(
            "123", "default", "my-app", null, "PENDING", 0, Instant.now(), null
        );

        when(workflowService.listPending()).thenReturn(List.of(history));

        mockMvc.perform(get("/api/v1/remediation/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
}
