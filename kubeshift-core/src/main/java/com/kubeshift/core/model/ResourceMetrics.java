package com.kubeshift.core.model;

import java.time.Instant;

public record ResourceMetrics(
    String namespace,
    String deploymentName,
    String containerName,
    long cpuRequestMillis,
    long cpuLimitMillis,
    long cpuUsageMillis,
    long memoryRequestBytes,
    long memoryLimitBytes,
    long memoryUsageBytes,
    double cpuUtilizationPercent,
    double memoryUtilizationPercent,
    Instant collectedAt
) {}
