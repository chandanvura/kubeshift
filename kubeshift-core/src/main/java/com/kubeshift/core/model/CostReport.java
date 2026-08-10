package com.kubeshift.core.model;

import java.time.Instant;
import java.util.List;

public record CostReport(
    String namespace,
    String deploymentName,
    double estimatedMonthlyCostUsd,
    double optimizedMonthlyCostUsd,
    double potentialSavingsUsd,
    double savingsPercent,
    List<RightsizingRecommendation> recommendations,
    Instant generatedAt
) {}
