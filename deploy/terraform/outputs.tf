output "kubeshift_namespace" {
  value = kubernetes_namespace.kubeshift.metadata[0].name
}

output "monitoring_namespace" {
  value = kubernetes_namespace.monitoring.metadata[0].name
}

output "grafana_url" {
  value = "http://localhost:3000 (after port-forwarding)"
}

output "prometheus_url" {
  value = "http://localhost:9090 (after port-forwarding)"
}

output "argocd_url" {
  value = var.enable_argocd ? "http://localhost:8080 (after port-forwarding)" : ""
}
