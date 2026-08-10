package com.kubeshift.core.service;

import com.kubeshift.core.model.ClusterHealthStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterHealthService {

    private final KubernetesClient kubernetesClient;

    public ClusterHealthStatus getClusterHealth() {
        int totalNodes = 0;
        int readyNodes = 0;
        int totalPods = 0;
        int runningPods = 0;
        int totalDeployments = 0;
        int availableDeployments = 0;
        double cpuUtil = 0.0;
        double memUtil = 0.0;

        try {
            var nodes = kubernetesClient.nodes().list().getItems();
            totalNodes = nodes.size();
            readyNodes = (int) nodes.stream().filter(n -> n.getStatus().getConditions().stream()
                    .anyMatch(c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus()))).count();

            var pods = kubernetesClient.pods().inAnyNamespace().list().getItems();
            totalPods = pods.size();
            runningPods = (int) pods.stream().filter(p -> "Running".equalsIgnoreCase(p.getStatus().getPhase())).count();

            var deployments = kubernetesClient.apps().deployments().inAnyNamespace().list().getItems();
            totalDeployments = deployments.size();
            availableDeployments = (int) deployments.stream().filter(d -> {
                Integer available = d.getStatus().getAvailableReplicas();
                Integer expected = d.getSpec().getReplicas();
                if (expected == null) expected = 1;
                return available != null && available >= expected;
            }).count();

            cpuUtil = 45.5; // simulated
            memUtil = 60.2; // simulated

        } catch (Exception e) {
            log.error("Failed to retrieve cluster health", e);
        }

        String status = "HEALTHY";
        if (readyNodes < totalNodes || availableDeployments < totalDeployments) {
            status = "DEGRADED";
        }
        if (readyNodes == 0 || availableDeployments < (totalDeployments / 2)) {
            status = "CRITICAL";
        }
        if (totalNodes == 0) {
            status = "UNKNOWN";
        }

        return new ClusterHealthStatus(
                totalNodes, readyNodes, totalPods, runningPods,
                totalDeployments, availableDeployments, cpuUtil, memUtil, status, Instant.now()
        );
    }
}
