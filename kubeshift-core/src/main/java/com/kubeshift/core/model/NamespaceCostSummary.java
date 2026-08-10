package com.kubeshift.core.model;

import java.time.Instant;

public record NamespaceCostSummary(
    String namespace,
    int deploymentCount,
    double totalMonthlyCostUsd,
    double totalPotentialSavingsUsd,
    double savingsPercent,
    Instant generatedAt
) {}
