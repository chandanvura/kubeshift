package com.kubeshift.core.controller;

import com.kubeshift.core.model.ClusterHealthStatus;
import com.kubeshift.core.service.ClusterHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cluster")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterHealthService clusterHealthService;

    @GetMapping("/health")
    public ClusterHealthStatus getClusterHealth() {
        return clusterHealthService.getClusterHealth();
    }
}
