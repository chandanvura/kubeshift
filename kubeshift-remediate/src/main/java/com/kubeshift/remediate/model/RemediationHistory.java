package com.kubeshift.remediate.model;

import java.time.Instant;

public record RemediationHistory(
    String id,
    String namespace,
    String deploymentName,
    String prUrl,
    String status,
    double estimatedSavingsUsd,
    Instant createdAt,
    Instant completedAt
) {}
