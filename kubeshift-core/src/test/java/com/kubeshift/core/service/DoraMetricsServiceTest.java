package com.kubeshift.core.service;

import com.kubeshift.core.model.DoraMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoraMetricsServiceTest {

    private DoraMetricsService doraMetricsService;

    @BeforeEach
    void setUp() {
        doraMetricsService = new DoraMetricsService();
    }

    @Test
    void testDeploymentFrequencyCalculation() {
        for (int i = 0; i < 7; i++) {
            doraMetricsService.recordDeployment(true);
        }
        DoraMetrics metrics = doraMetricsService.getDoraMetrics();
        // Base is 10, added 7 -> 17. 17 / 7 = 2.42
        assertTrue(metrics.deploymentFrequencyPerDay() > 2.0);
    }

    @Test
    void testPerformanceLevelClassification() {
        DoraMetrics metrics = doraMetricsService.getDoraMetrics();
        assertNotNull(metrics.performanceLevel());
        // By default base values are 10 deploy, 1 fail, lead 4.5, mttr 35 -> Elite
        assertEquals("Elite", metrics.performanceLevel());
    }

    @Test
    void testFailedDeploymentsIncreasesFailureRate() {
        for (int i = 0; i < 5; i++) {
            doraMetricsService.recordDeployment(false);
        }
        DoraMetrics metrics = doraMetricsService.getDoraMetrics();
        assertTrue(metrics.changeFailureRatePercent() > 0);
    }
}
