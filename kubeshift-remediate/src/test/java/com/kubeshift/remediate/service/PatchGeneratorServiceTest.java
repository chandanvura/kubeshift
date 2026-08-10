package com.kubeshift.remediate.service;

import com.kubeshift.remediate.model.PatchType;
import com.kubeshift.remediate.model.RemediationRequest;
import com.kubeshift.remediate.model.ResourcePatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PatchGeneratorServiceTest {

    private PatchGeneratorService patchGeneratorService;

    @BeforeEach
    void setUp() {
        patchGeneratorService = new PatchGeneratorService();
        ReflectionTestUtils.setField(patchGeneratorService, "minCpuRequest", "50m");
        ReflectionTestUtils.setField(patchGeneratorService, "minMemoryRequest", "64Mi");
    }

    @Test
    void testK8sManifestPatchGeneration() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "deploy.yaml",
            Map.of(
                "requests.cpu", new ResourcePatch("requests.cpu", "200m", "100m"),
                "requests.memory", new ResourcePatch("requests.memory", "512Mi", "256Mi")
            )
        );

        String patch = patchGeneratorService.generatePatch(request, PatchType.KUBERNETES_MANIFEST);
        
        assertNotNull(patch);
        assertTrue(patch.contains("kind: Deployment"));
        assertTrue(patch.contains("name: my-app"));
        assertTrue(patch.contains("name: my-container"));
        assertTrue(patch.contains("cpu: 100m"));
        assertTrue(patch.contains("memory: 256Mi"));
    }

    @Test
    void testHelmValuesPatchGeneration() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "values.yaml",
            Map.of(
                "requests.cpu", new ResourcePatch("requests.cpu", "200m", "100m")
            )
        );

        String patch = patchGeneratorService.generatePatch(request, PatchType.HELM_VALUES);
        
        assertNotNull(patch);
        assertFalse(patch.contains("kind: Deployment"));
        assertTrue(patch.contains("requests:"));
        assertTrue(patch.contains("cpu: 100m"));
    }

    @Test
    void testKustomizeOverlayPatchGeneration() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "patch.yaml",
            Map.of(
                "limits.memory", new ResourcePatch("limits.memory", "1Gi", "512Mi")
            )
        );

        String patch = patchGeneratorService.generatePatch(request, PatchType.KUSTOMIZE_OVERLAY);
        
        assertNotNull(patch);
        assertTrue(patch.contains("kind: Deployment"));
        assertTrue(patch.contains("limits:"));
        assertTrue(patch.contains("memory: 512Mi"));
    }

    @Test
    void testMinimumResourceSafetyCheck() {
        RemediationRequest request = new RemediationRequest(
            "default", "my-app", "my-container", "repo/my-app", "main", "deploy.yaml",
            Map.of(
                "requests.cpu", new ResourcePatch("requests.cpu", "100m", "10m"), // below 50m
                "requests.memory", new ResourcePatch("requests.memory", "128Mi", "32Mi") // below 64Mi
            )
        );

        String patch = patchGeneratorService.generatePatch(request, PatchType.KUBERNETES_MANIFEST);
        
        assertNotNull(patch);
        assertTrue(patch.contains("cpu: 50m")); // Enforced min cpu
        assertTrue(patch.contains("memory: 64Mi")); // Enforced min memory
    }
}
