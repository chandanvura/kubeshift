package com.kubeshift.core.controller;

import com.kubeshift.core.model.CostReport;
import com.kubeshift.core.model.NamespaceCostSummary;
import com.kubeshift.core.service.CostAnalyzerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CostController.class)
class CostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CostAnalyzerService costAnalyzerService;

    @Test
    void testGetAllReports() throws Exception {
        CostReport report = new CostReport("default", "app1", 100.0, 50.0, 50.0, 50.0, List.of(), Instant.now());
        when(costAnalyzerService.getAllReports()).thenReturn(List.of(report));

        mockMvc.perform(get("/api/v1/cost/reports")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].namespace").value("default"))
                .andExpect(jsonPath("$[0].estimatedMonthlyCostUsd").value(100.0));
    }

    @Test
    void testGetReportsByNamespace() throws Exception {
        CostReport report = new CostReport("kube-system", "dns", 50.0, 40.0, 10.0, 20.0, List.of(), Instant.now());
        when(costAnalyzerService.getReportsByNamespace(anyString())).thenReturn(List.of(report));

        mockMvc.perform(get("/api/v1/cost/reports/kube-system")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].namespace").value("kube-system"));
    }

    @Test
    void testGetNamespaceSummaries() throws Exception {
        NamespaceCostSummary summary = new NamespaceCostSummary("default", 5, 500.0, 200.0, 40.0, Instant.now());
        when(costAnalyzerService.getNamespaceSummaries()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/cost/summary")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].namespace").value("default"))
                .andExpect(jsonPath("$[0].deploymentCount").value(5));
    }
}
