package com.kubeshift.core.service;

import com.kubeshift.core.model.CostReport;
import com.kubeshift.core.model.NamespaceCostSummary;
import com.kubeshift.core.model.ResourceMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostAnalyzerServiceTest {

    @Mock
    private MetricsCollectorService metricsCollectorService;

    @InjectMocks
    private CostAnalyzerService costAnalyzerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(costAnalyzerService, "cpuRate", 0.048);
        ReflectionTestUtils.setField(costAnalyzerService, "memRate", 0.006);
        ReflectionTestUtils.setField(costAnalyzerService, "bufferPercent", 20.0);
    }

    @Test
    void testCostCalculation() {
        ResourceMetrics m1 = new ResourceMetrics(
                "default", "app1", "c1",
                2000, 2000, 500, // 2 cores req, 0.5 cores usage
                4L * 1024 * 1024 * 1024, 4L * 1024 * 1024 * 1024, 1L * 1024 * 1024 * 1024, // 4GB req, 1GB usage
                25.0, 25.0, Instant.now()
        );
        when(metricsCollectorService.getAllMetrics()).thenReturn(List.of(m1));

        List<CostReport> reports = costAnalyzerService.getAllReports();

        assertEquals(1, reports.size());
        CostReport report = reports.get(0);
        assertEquals("default", report.namespace());
        assertEquals("app1", report.deploymentName());
        
        // Est cost: (2 * 0.048 + 4 * 0.006) * 730 = (0.096 + 0.024) * 730 = 0.12 * 730 = 87.6
        assertEquals(87.6, report.estimatedMonthlyCostUsd(), 0.1);
    }

    @Test
    void testRightsizingRecommendationGeneration() {
        ResourceMetrics m1 = new ResourceMetrics(
                "default", "app1", "c1",
                2000, 2000, 100, // 2 cores req, 0.1 cores usage -> Huge waste
                4L * 1024 * 1024 * 1024, 4L * 1024 * 1024 * 1024, 500L * 1024 * 1024, // 4GB req, 0.5GB usage -> Huge waste
                5.0, 12.5, Instant.now()
        );
        when(metricsCollectorService.getAllMetrics()).thenReturn(List.of(m1));

        List<CostReport> reports = costAnalyzerService.getAllReports();
        CostReport report = reports.get(0);

        assertFalse(report.recommendations().isEmpty());
        assertEquals(2, report.recommendations().size()); // Both CPU and Memory
    }

    @Test
    void testSavingsPercentageCalculation() {
        ResourceMetrics m1 = new ResourceMetrics(
                "default", "app1", "c1",
                1000, 1000, 100,
                1L * 1024 * 1024 * 1024, 1L * 1024 * 1024 * 1024, 100L * 1024 * 1024,
                10.0, 10.0, Instant.now()
        );
        when(metricsCollectorService.getAllMetrics()).thenReturn(List.of(m1));

        List<CostReport> reports = costAnalyzerService.getAllReports();
        CostReport report = reports.get(0);

        assertTrue(report.savingsPercent() > 0);
        assertTrue(report.savingsPercent() <= 100);
        assertTrue(report.potentialSavingsUsd() > 0);
    }

    @Test
    void testNamespaceSummaries() {
        ResourceMetrics m1 = new ResourceMetrics("ns1", "app1", "c1", 1000, 1000, 500, 1024L*1024*1024, 1024L*1024*1024, 500L*1024*1024, 50, 50, Instant.now());
        ResourceMetrics m2 = new ResourceMetrics("ns2", "app2", "c2", 1000, 1000, 500, 1024L*1024*1024, 1024L*1024*1024, 500L*1024*1024, 50, 50, Instant.now());
        when(metricsCollectorService.getAllMetrics()).thenReturn(List.of(m1, m2));

        List<NamespaceCostSummary> summaries = costAnalyzerService.getNamespaceSummaries();
        assertEquals(2, summaries.size());
    }

    @Test
    void testTotalPotentialSavings() {
        ResourceMetrics m1 = new ResourceMetrics("ns1", "app1", "c1", 2000, 2000, 100, 2048L*1024*1024, 2048L*1024*1024, 200L*1024*1024, 5, 10, Instant.now());
        when(metricsCollectorService.getAllMetrics()).thenReturn(List.of(m1));

        double savings = costAnalyzerService.getTotalPotentialSavings();
        assertTrue(savings > 0);
    }
}
