package com.kubeshift.remediate.model;

import java.util.Map;

public record RemediationRequest(
    String namespace,
    String deploymentName,
    String containerName,
    String targetRepository,
    String targetBranch,
    String manifestPath,
    Map<String, ResourcePatch> patches
) {}
