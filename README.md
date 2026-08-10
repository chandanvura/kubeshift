<div align="center">

# ⚡ KubeShift

### Kubernetes Cost Intelligence & GitOps Remediation Engine

[![CI](https://github.com/chandanvura/kubeshift/actions/workflows/ci.yml/badge.svg)](https://github.com/chandanvura/kubeshift/actions/workflows/ci.yml)
[![Security Scan](https://github.com/chandanvura/kubeshift/actions/workflows/security-scan.yml/badge.svg)](https://github.com/chandanvura/kubeshift/actions/workflows/security-scan.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5.svg)](https://kubernetes.io/)

**Stop paying for cloud resources you don't use. KubeShift automatically identifies overprovisioned Kubernetes workloads and generates GitOps-compatible pull requests to right-size them.**

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [API Reference](#-api-reference) • [Contributing](#-contributing)

</div>

---

## 🔥 The Problem

Organizations waste **30-35% of their cloud spend** on overprovisioned resources. Existing tools like OpenCost and Kubecost show you the waste, but leave the fix as a manual task. KubeShift bridges this gap.

## ✨ Features

### 💰 Cost Intelligence
- **Real-time cost analysis** per namespace, deployment, and container
- **Rightsizing recommendations** with confidence scores
- **Cost anomaly detection** using statistical analysis (z-score)
- **Namespace-level cost attribution** for chargeback/showback

### 🔧 Automated GitOps Remediation
- **Auto-generates GitHub PRs** with optimized resource configurations
- Supports **Kubernetes manifests**, **Helm values**, and **Kustomize overlays**
- **Approval workflow** — never auto-merges (safety first)
- Full audit trail of all remediation actions

### 📊 DORA Metrics
- Tracks **Deployment Frequency**, **Lead Time**, **Change Failure Rate**, **MTTR**
- Performance classification (Elite → Low)
- Grafana dashboards for engineering visibility

### 🔒 DevSecOps Pipeline
- **Trivy** container vulnerability scanning
- **SonarQube/SonarCloud** code quality analysis
- **GitHub Actions** CI/CD with security gates
- **ArgoCD** GitOps continuous delivery

### 📈 Full Observability Stack
- **Prometheus** metrics from all services
- **Grafana** dashboards (cost overview + DORA metrics)
- **Loki** centralized logging
- **Alertmanager** cost spike notifications

## 🏗️ Architecture

KubeShift is built on a modern microservices architecture optimized for performance and maintainability.

- **KubeShift Core (Java 21/Spring Boot 3)**: The central orchestrator that interfaces with the Kubernetes API, stores metrics, and manages the lifecycle of recommendations.
- **KubeShift Analyzer (Python/FastAPI)**: A high-performance data science engine that crunches resource usage metrics, computes z-scores for anomaly detection, and calculates rightsizing thresholds.
- **KubeShift Remediate (Java 21/Spring Boot 3)**: The GitOps integration service responsible for translating rightsizing recommendations into PRs across various configuration formats (Helm, Kustomize, standard manifests).
- **KubeShift Dashboard (HTML/CSS/JS)**: An intuitive frontend for operators to visualize costs and review proposed remediations.

### Architecture Diagram

```text
  Kubernetes Cluster
  ┌────────────────────────────────────────────────────────┐
  │                                                        │
  │  [Prometheus] ──────> [KubeShift Core] <────── [K8s API]
  │                            │    │                      │
  │                            │    │                      │
  │  [KubeShift Analyzer] <────┘    └────> [KubeShift      │
  │    (Cost & Anomalies)                   Remediate]     │
  │                                             │          │
  └─────────────────────────────────────────────┼──────────┘
                                                │
                                                ▼
  [Grafana Dashboards]                      [GitHub PR]
   (Cost & DORA)                           (GitOps Flow)
        ▲                                       │
        │                                       ▼
  [User / Operator] <───────────────────── [ArgoCD / K8s]
```

## 🛠️ Tech Stack

| Category | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.4 |
| **Analysis Engine** | Python 3.12, FastAPI, NumPy |
| **Frontend** | HTML5, CSS3, Vanilla JS |
| **Infrastructure** | Kubernetes, Docker, Terraform |
| **CI/CD** | GitHub Actions, ArgoCD, Helm |
| **Monitoring** | Prometheus, Grafana, Loki, Alertmanager |
| **Security** | Trivy, SonarQube |

## 🚀 Quick Start

### Prerequisites
- Java 21
- Python 3.12+
- Docker Desktop
- Minikube
- `kubectl`, `helm`, `terraform`

### Local Development (Docker Compose)
```bash
git clone https://github.com/chandanvura/kubeshift.git
cd kubeshift
docker compose up -d
```

### Kubernetes Deployment
```bash
minikube start --cpus=4 --memory=8192
cd deploy/terraform
terraform init && terraform apply
helm install kubeshift deploy/helm/kubeshift -n kubeshift
```

### Access Services

| Service | Local URL (Docker) | K8s Port-Forward | Description |
| :--- | :--- | :--- | :--- |
| **Core API** | `http://localhost:8080` | `8080` | Main application API |
| **Analyzer API** | `http://localhost:8082` | `8082` | Data analysis endpoints |
| **Dashboard** | `http://localhost:3000` | `3000` | UI Dashboard |
| **Grafana** | `http://localhost:3001` | `3001` | Monitoring Dashboards |

## 📡 API Reference

### Cost Analysis Endpoints
```bash
# Get cost analysis for all monitored deployments
curl -X GET http://localhost:8082/api/v1/analyzer/cost

# Get rightsizing recommendations
curl -X GET http://localhost:8082/api/v1/analyzer/recommendations
```

### Remediation Endpoints
```bash
# Trigger a GitHub PR generation for a specific recommendation
curl -X POST http://localhost:8080/api/v1/remediate/trigger \
  -H "Content-Type: application/json" \
  -d '{"recommendation_id": "rec-12345", "target_branch": "main"}'
```

### DORA Metrics Endpoints
```bash
# Fetch calculated DORA metrics summary
curl -X GET http://localhost:8080/api/v1/metrics/dora/summary
```

### Cluster Health Endpoints
```bash
# Check system health and service connectivity
curl -X GET http://localhost:8080/health
```

## 📊 Dashboards

KubeShift includes pre-configured Grafana dashboards to give you immediate insights:
- **Cost Overview Dashboard**: View current burn rates, namespace attribution, and potential savings.
- **DORA Metrics Dashboard**: Track Deployment Frequency, Lead Time for Changes, Change Failure Rate, and Time to Restore Service over time.

## 🧪 Testing

```bash
# Java tests
mvn clean verify

# Python tests  
cd kubeshift-analyzer && pytest -v

# Full test suite
make test
```

## 🤝 Contributing

We welcome contributions from the community!

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

## 🙏 Acknowledgments

Inspired by OpenCost, Kubecost, Goldilocks, and the CNCF ecosystem.

---

<div align="center">
Made with ❤️ for the Kubernetes community
</div>
