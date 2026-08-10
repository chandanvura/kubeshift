package com.kubeshift.core.service;

import com.kubeshift.core.model.DoraMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class DoraMetricsService {

    private final AtomicInteger deploymentsCount = new AtomicInteger(10);
    private final AtomicInteger failedDeploymentsCount = new AtomicInteger(1);
    
    // Simulate some times
    private double leadTimeHours = 4.5;
    private double mttrMinutes = 35.0;

    public void recordDeployment(boolean success) {
        deploymentsCount.incrementAndGet();
        if (!success) {
            failedDeploymentsCount.incrementAndGet();
        }
    }

    public DoraMetrics getDoraMetrics() {
        int total = deploymentsCount.get();
        int failed = failedDeploymentsCount.get();
        
        double freqPerDay = total / 7.0; // Simulated over a week
        double failRate = total > 0 ? (double) failed / total * 100.0 : 0;

        String perf = classifyPerformance(freqPerDay, leadTimeHours, failRate, mttrMinutes);

        return new DoraMetrics(freqPerDay, leadTimeHours, failRate, mttrMinutes, perf, Instant.now());
    }

    private String classifyPerformance(double freq, double leadTime, double failRate, double mttr) {
        if (freq > 1.0 && leadTime < 24.0 && failRate < 15.0 && mttr < 60.0) {
            return "Elite";
        } else if (freq > 0.1 && leadTime < 168.0 && failRate < 30.0 && mttr < 1440.0) {
            return "High";
        } else if (freq > 0.03 && leadTime < 720.0 && failRate < 45.0 && mttr < 10080.0) {
            return "Medium";
        }
        return "Low";
    }
}
