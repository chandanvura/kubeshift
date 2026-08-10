package com.kubeshift.core.service;

import com.kubeshift.core.model.CostReport;
import com.kubeshift.core.model.NamespaceCostSummary;
import com.kubeshift.core.model.ResourceMetrics;
import com.kubeshift.core.model.RightsizingRecommendation;
import com.kubeshift.core.model.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostAnalyzerService {

    private final MetricsCollectorService metricsCollectorService;

    @Value("${kubeshift.cost.cpu-rate-per-core-hour:0.048}")
    private double cpuRate;

    @Value("${kubeshift.cost.memory-rate-per-gb-hour:0.006}")
    private double memRate;

    @Value("${kubeshift.cost.headroom-buffer-percent:20}")
    private double bufferPercent;

    private static final double HOURS_PER_MONTH = 730.0;

    public List<CostReport> getAllReports() {
        return analyzeMetrics(metricsCollectorService.getAllMetrics());
    }

    public List<CostReport> getReportsByNamespace(String namespace) {
        return analyzeMetrics(metricsCollectorService.getMetricsByNamespace(namespace));
    }

    public List<NamespaceCostSummary> getNamespaceSummaries() {
        List<CostReport> reports = getAllReports();
        Map<String, List<CostReport>> byNamespace = reports.stream()
                .collect(Collectors.groupingBy(CostReport::namespace));
        
        return byNamespace.entrySet().stream().map(entry -> {
            String ns = entry.getKey();
            List<CostReport> nsReports = entry.getValue();
            double totalCost = nsReports.stream().mapToDouble(CostReport::estimatedMonthlyCostUsd).sum();
            double totalSavings = nsReports.stream().mapToDouble(CostReport::potentialSavingsUsd).sum();
            double savingsPct = totalCost > 0 ? (totalSavings / totalCost) * 100 : 0;
            return new NamespaceCostSummary(ns, nsReports.size(), totalCost, totalSavings, savingsPct, Instant.now());
        }).collect(Collectors.toList());
    }

    public double getTotalPotentialSavings() {
        return getAllReports().stream().mapToDouble(CostReport::potentialSavingsUsd).sum();
    }

    private List<CostReport> analyzeMetrics(List<ResourceMetrics> metricsList) {
        Map<String, Map<String, List<ResourceMetrics>>> grouped = metricsList.stream()
                .collect(Collectors.groupingBy(ResourceMetrics::namespace,
                        Collectors.groupingBy(ResourceMetrics::deploymentName)));

        List<CostReport> reports = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<ResourceMetrics>>> nsEntry : grouped.entrySet()) {
            String ns = nsEntry.getKey();
            for (Map.Entry<String, List<ResourceMetrics>> depEntry : nsEntry.getValue().entrySet()) {
                String dep = depEntry.getKey();
                List<ResourceMetrics> containers = depEntry.getValue();

                double estCost = 0;
                double optCost = 0;
                List<RightsizingRecommendation> recs = new ArrayList<>();

                for (ResourceMetrics m : containers) {
                    double currentCpuCores = m.cpuRequestMillis() / 1000.0;
                    double currentMemGb = m.memoryRequestBytes() / (1024.0 * 1024.0 * 1024.0);

                    double containerEstCost = (currentCpuCores * cpuRate + currentMemGb * memRate) * HOURS_PER_MONTH;
                    estCost += containerEstCost;

                    double bufferMult = 1.0 + (bufferPercent / 100.0);
                    double optCpuCores = Math.max(m.cpuUsageMillis() / 1000.0 * bufferMult, 0.001);
                    double optMemGb = Math.max(m.memoryUsageBytes() / (1024.0 * 1024.0 * 1024.0) * bufferMult, 0.001);

                    double containerOptCost = (optCpuCores * cpuRate + optMemGb * memRate) * HOURS_PER_MONTH;

                    // If request is much higher than usage (over 50% waste)
                    if (currentCpuCores > 0 && (optCpuCores / currentCpuCores) < 0.5) {
                        double savings = (currentCpuCores - optCpuCores) * cpuRate * HOURS_PER_MONTH;
                        recs.add(new RightsizingRecommendation(
                                ns, dep, m.containerName(), "CPU",
                                m.cpuRequestMillis() + "m",
                                (long)(optCpuCores * 1000) + "m",
                                "CPU utilization is low",
                                RiskLevel.LOW, savings
                        ));
                    }
                    if (currentMemGb > 0 && (optMemGb / currentMemGb) < 0.5) {
                        double savings = (currentMemGb - optMemGb) * memRate * HOURS_PER_MONTH;
                        recs.add(new RightsizingRecommendation(
                                ns, dep, m.containerName(), "Memory",
                                (m.memoryRequestBytes() / (1024 * 1024)) + "Mi",
                                (long)(optMemGb * 1024) + "Mi",
                                "Memory utilization is low",
                                RiskLevel.LOW, savings
                        ));
                    }
                    optCost += containerOptCost;
                }

                double savings = Math.max(0, estCost - optCost);
                double savingsPct = estCost > 0 ? (savings / estCost) * 100 : 0;
                reports.add(new CostReport(ns, dep, estCost, optCost, savings, savingsPct, recs, Instant.now()));
            }
        }
        return reports;
    }
}
