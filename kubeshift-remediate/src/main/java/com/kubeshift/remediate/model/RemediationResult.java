package com.kubeshift.remediate.model;

import java.time.Instant;
import java.util.List;

public record RemediationResult(
    String prUrl,
    String prNumber,
    String status,
    String branchName,
    String commitMessage,
    List<String> filesModified,
    Instant createdAt
) {}
