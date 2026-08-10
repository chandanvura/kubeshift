package com.kubeshift.core.controller;

import com.kubeshift.core.model.CostReport;
import com.kubeshift.core.model.NamespaceCostSummary;
import com.kubeshift.core.service.CostAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cost")
@RequiredArgsConstructor
public class CostController {

    private final CostAnalyzerService costAnalyzerService;

    @GetMapping("/reports")
    public List<CostReport> getAllReports() {
        return costAnalyzerService.getAllReports();
    }

    @GetMapping("/reports/{namespace}")
    public List<CostReport> getReportsByNamespace(@PathVariable String namespace) {
        return costAnalyzerService.getReportsByNamespace(namespace);
    }

    @GetMapping("/summary")
    public List<NamespaceCostSummary> getNamespaceSummaries() {
        return costAnalyzerService.getNamespaceSummaries();
    }

    @GetMapping("/total-savings")
    public double getTotalPotentialSavings() {
        return costAnalyzerService.getTotalPotentialSavings();
    }
}
