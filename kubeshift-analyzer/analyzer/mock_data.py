from datetime import datetime, timezone
from typing import List
from .models import ResourceMetrics

def generate_mock_metrics() -> List[ResourceMetrics]:
    now = datetime.now(timezone.utc)
    return [
        ResourceMetrics(
            namespace="production",
            deployment_name="api-gateway",
            container_name="gateway",
            cpu_request_millis=500,
            cpu_limit_millis=1000,
            cpu_usage_millis=180,
            memory_request_bytes=512 * 1024 * 1024,
            memory_limit_bytes=1024 * 1024 * 1024,
            memory_usage_bytes=200 * 1024 * 1024,
            cpu_utilization_percent=36.0,
            memory_utilization_percent=39.0,
            collected_at=now
        ),
        ResourceMetrics(
            namespace="production",
            deployment_name="payment-service",
            container_name="payment",
            cpu_request_millis=1000,
            cpu_limit_millis=2000,
            cpu_usage_millis=250,
            memory_request_bytes=1024 * 1024 * 1024,
            memory_limit_bytes=2048 * 1024 * 1024,
            memory_usage_bytes=300 * 1024 * 1024,
            cpu_utilization_percent=25.0,
            memory_utilization_percent=29.2,
            collected_at=now
        ),
        ResourceMetrics(
            namespace="production",
            deployment_name="user-service",
            container_name="user",
            cpu_request_millis=250,
            cpu_limit_millis=500,
            cpu_usage_millis=200,
            memory_request_bytes=256 * 1024 * 1024,
            memory_limit_bytes=512 * 1024 * 1024,
            memory_usage_bytes=220 * 1024 * 1024,
            cpu_utilization_percent=80.0,
            memory_utilization_percent=85.9,
            collected_at=now
        ),
        ResourceMetrics(
            namespace="staging",
            deployment_name="api-gateway",
            container_name="gateway",
            cpu_request_millis=500,
            cpu_limit_millis=1000,
            cpu_usage_millis=50,
            memory_request_bytes=512 * 1024 * 1024,
            memory_limit_bytes=1024 * 1024 * 1024,
            memory_usage_bytes=80 * 1024 * 1024,
            cpu_utilization_percent=10.0,
            memory_utilization_percent=15.6,
            collected_at=now
        ),
        ResourceMetrics(
            namespace="staging",
            deployment_name="notification-service",
            container_name="notification",
            cpu_request_millis=200,
            cpu_limit_millis=400,
            cpu_usage_millis=30,
            memory_request_bytes=256 * 1024 * 1024,
            memory_limit_bytes=512 * 1024 * 1024,
            memory_usage_bytes=40 * 1024 * 1024,
            cpu_utilization_percent=15.0,
            memory_utilization_percent=15.6,
            collected_at=now
        )
    ]
