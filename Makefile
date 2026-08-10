.PHONY: help build test docker-build docker-up docker-down clean

help:
	@echo "KubeShift - Available Commands"
	@echo "build          - Build all Java modules"
	@echo "test           - Run all tests"
	@echo "docker-build   - Build Docker images"
	@echo "docker-up      - Start all services"
	@echo "docker-down    - Stop all services"
	@echo "clean          - Clean build artifacts"
	@echo "minikube-setup - Setup Minikube cluster"
	@echo "tf-init        - Initialize Terraform"
	@echo "tf-apply       - Apply Terraform"

build:
	cd kubeshift-core && mvn clean package -DskipTests
	cd kubeshift-remediate && mvn clean package -DskipTests

test:
	cd kubeshift-core && mvn clean verify
	cd kubeshift-remediate && mvn clean verify
	cd kubeshift-analyzer && python -m pytest tests -v

docker-build:
	docker build -t kubeshift-core:latest ./kubeshift-core
	docker build -t kubeshift-remediate:latest ./kubeshift-remediate
	docker build -t kubeshift-analyzer:latest ./kubeshift-analyzer
	docker build -t kubeshift-dashboard:latest ./kubeshift-dashboard

docker-up:
	docker compose up -d

docker-down:
	docker compose down

clean:
	cd kubeshift-core && mvn clean
	cd kubeshift-remediate && mvn clean
	find kubeshift-analyzer -name '__pycache__' -exec rm -rf {} +

minikube-setup:
	minikube start --cpus=4 --memory=8192 --driver=docker
	minikube addons enable metrics-server
	minikube addons enable ingress

tf-init:
	cd deploy/terraform && terraform init

tf-apply:
	cd deploy/terraform && terraform apply -auto-approve
