package com.kubeshift.core.model;

import java.time.Instant;

public record DoraMetrics(
    double deploymentFrequencyPerDay,
    double leadTimeForChangesHours,
    double changeFailureRatePercent,
    double meanTimeToRecoveryMinutes,
    String performanceLevel,
    Instant calculatedAt
) {}
