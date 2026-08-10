package com.kubeshift.core.model;

public record RightsizingRecommendation(
    String namespace,
    String deploymentName,
    String containerName,
    String resourceType,
    String currentValue,
    String recommendedValue,
    String reason,
    RiskLevel riskLevel,
    double estimatedSavingsUsd
) {}
