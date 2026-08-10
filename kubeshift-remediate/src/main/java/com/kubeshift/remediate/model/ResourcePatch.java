package com.kubeshift.remediate.model;

public record ResourcePatch(
    String resourceType,
    String currentValue,
    String recommendedValue
) {}
