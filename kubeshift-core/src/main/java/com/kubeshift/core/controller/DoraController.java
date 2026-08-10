package com.kubeshift.core.controller;

import com.kubeshift.core.model.DoraMetrics;
import com.kubeshift.core.service.DoraMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dora")
@RequiredArgsConstructor
public class DoraController {

    private final DoraMetricsService doraMetricsService;

    @GetMapping("/metrics")
    public DoraMetrics getDoraMetrics() {
        return doraMetricsService.getDoraMetrics();
    }

    @PostMapping("/deployments")
    public void recordDeployment(@RequestParam(defaultValue = "true") boolean success) {
        doraMetricsService.recordDeployment(success);
    }
}
