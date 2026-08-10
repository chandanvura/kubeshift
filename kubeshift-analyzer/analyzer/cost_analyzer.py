from typing import List, Dict
from .models import ResourceMetrics, CostAnalysis
from .config import Settings


class CostAnalyzer:
    def __init__(self, config: Settings):
        self.config = config
        self.metrics_cache = {}

    def analyze_costs(
        self, metrics: List[ResourceMetrics]
    ) -> List[CostAnalysis]:
        analyses = []
        HOURS_PER_MONTH = 730

        for metric in metrics:
            cpu_req_cores = metric.cpu_request_millis / 1000.0
            mem_req_gb = metric.memory_request_bytes / (1024**3)

            monthly_cost = (
                cpu_req_cores * self.config.CPU_RATE_PER_CORE_HOUR
                + mem_req_gb * self.config.MEMORY_RATE_PER_GB_HOUR
            ) * HOURS_PER_MONTH

            cpu_usage_cores = metric.cpu_usage_millis / 1000.0
            mem_usage_gb = metric.memory_usage_bytes / (1024**3)

            optimized_cost = (
                (cpu_usage_cores * 1.2) * self.config.CPU_RATE_PER_CORE_HOUR
                + (mem_usage_gb * 1.2) * self.config.MEMORY_RATE_PER_GB_HOUR
            ) * HOURS_PER_MONTH

            # Avoid division by zero
            if monthly_cost > 0:
                savings_usd = max(0, monthly_cost - optimized_cost)
                savings_percent = (savings_usd / monthly_cost) * 100
            else:
                savings_usd = 0.0
                savings_percent = 0.0

            is_overprovisioned = False
            recommendations = []

            avg_util = (
                metric.cpu_utilization_percent
                + metric.memory_utilization_percent
            ) / 2

            if avg_util < 30:
                is_overprovisioned = True
                recommendations.append(
                    "Severely overprovisioned. Consider reducing limits by at least 50%."  # noqa: E501
                )
            elif avg_util < 50:
                is_overprovisioned = True
                recommendations.append(
                    "Moderately overprovisioned. Consider right-sizing to match usage + 20% buffer."  # noqa: E501
                )

            analyses.append(
                CostAnalysis(
                    namespace=metric.namespace,
                    deployment_name=metric.deployment_name,
                    estimated_monthly_cost_usd=round(monthly_cost, 2),
                    optimized_monthly_cost_usd=round(optimized_cost, 2),
                    potential_savings_usd=round(savings_usd, 2),
                    savings_percent=round(savings_percent, 2),
                    is_overprovisioned=is_overprovisioned,
                    recommendations=recommendations,
                )
            )

        return analyses

    def get_namespace_summary(
        self, analyses: List[CostAnalysis]
    ) -> Dict[str, dict]:
        summary = {}
        for analysis in analyses:
            ns = analysis.namespace
            if ns not in summary:
                summary[ns] = {"total_cost": 0.0, "total_savings": 0.0}
            summary[ns]["total_cost"] += analysis.estimated_monthly_cost_usd
            summary[ns]["total_savings"] += analysis.potential_savings_usd

        for ns in summary:
            summary[ns]["total_cost"] = round(summary[ns]["total_cost"], 2)
            summary[ns]["total_savings"] = round(
                summary[ns]["total_savings"], 2
            )

        return summary
