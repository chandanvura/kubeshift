from typing import List
from .models import ResourceMetrics, RightsizingRecommendation
from .config import Settings


class RightsizingEngine:
    def __init__(self, config: Settings):
        self.config = config

    def generate_recommendations(
        self, metrics: List[ResourceMetrics]
    ) -> List[RightsizingRecommendation]:
        recommendations = []
        HOURS_PER_MONTH = 730

        for metric in metrics:
            # CPU Recommendation
            if metric.cpu_utilization_percent < 50:
                recommended_cpu = max(50.0, metric.cpu_usage_millis * 1.2)
                if recommended_cpu < metric.cpu_request_millis:
                    savings = (
                        (
                            (metric.cpu_request_millis - recommended_cpu)
                            / 1000.0
                        )
                        * self.config.CPU_RATE_PER_CORE_HOUR
                        * HOURS_PER_MONTH
                    )
                    recommendations.append(
                        RightsizingRecommendation(
                            namespace=metric.namespace,
                            deployment_name=metric.deployment_name,
                            container_name=metric.container_name,
                            resource_type="CPU",
                            current_value=metric.cpu_request_millis,
                            recommended_value=recommended_cpu,
                            savings_usd=round(savings, 2),
                            confidence_score=0.85,
                        )
                    )

            # Memory Recommendation
            if metric.memory_utilization_percent < 50:
                recommended_mem = max(
                    64 * 1024 * 1024, metric.memory_usage_bytes * 1.3
                )
                if recommended_mem < metric.memory_request_bytes:
                    savings = (
                        (
                            (metric.memory_request_bytes - recommended_mem)
                            / (1024**3)
                        )
                        * self.config.MEMORY_RATE_PER_GB_HOUR
                        * HOURS_PER_MONTH
                    )
                    recommendations.append(
                        RightsizingRecommendation(
                            namespace=metric.namespace,
                            deployment_name=metric.deployment_name,
                            container_name=metric.container_name,
                            resource_type="Memory",
                            current_value=metric.memory_request_bytes,
                            recommended_value=recommended_mem,
                            savings_usd=round(savings, 2),
                            confidence_score=0.85,
                        )
                    )

        # Sort by savings descending
        return sorted(
            recommendations, key=lambda x: x.savings_usd, reverse=True
        )
