package com.kubeshift.core.controller;

import com.kubeshift.core.model.ResourceMetrics;
import com.kubeshift.core.service.MetricsCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Validated
public class MetricsController {

    private final MetricsCollectorService metricsCollectorService;

    @GetMapping
    public List<ResourceMetrics> getAllMetrics() {
        return metricsCollectorService.getAllMetrics();
    }

    @GetMapping("/{namespace}")
    public List<ResourceMetrics> getMetricsByNamespace(@PathVariable @Pattern(regexp = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$") String namespace) {
        return metricsCollectorService.getMetricsByNamespace(namespace);
    }
}
