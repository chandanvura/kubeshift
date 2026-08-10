variable "kubeshift_namespace" {
  description = "Namespace for KubeShift application"
  type        = string
  default     = "kubeshift"
}

variable "monitoring_namespace" {
  description = "Namespace for monitoring stack"
  type        = string
  default     = "monitoring"
}

variable "grafana_admin_password" {
  description = "Admin password for Grafana"
  type        = string
  sensitive   = true
  default     = "kubeshift-admin"
}

variable "enable_argocd" {
  description = "Whether to deploy ArgoCD"
  type        = bool
  default     = true
}

variable "kube_config_path" {
  description = "Path to kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}
