package com.kubeshift.core.model;

import java.time.Instant;

public record ClusterHealthStatus(
    int totalNodes,
    int readyNodes,
    int totalPods,
    int runningPods,
    int totalDeployments,
    int availableDeployments,
    double clusterCpuUtilization,
    double clusterMemoryUtilization,
    String overallStatus,
    Instant checkedAt
) {}
