package com.kubeshift.remediate.model;

public enum PatchType {
    KUBERNETES_MANIFEST, 
    HELM_VALUES, 
    KUSTOMIZE_OVERLAY
}
