from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime

class ResourceMetrics(BaseModel):
    namespace: str
    deployment_name: str
    container_name: str
    cpu_request_millis: float
    cpu_limit_millis: float
    cpu_usage_millis: float
    memory_request_bytes: float
    memory_limit_bytes: float
    memory_usage_bytes: float
    cpu_utilization_percent: float
    memory_utilization_percent: float
    collected_at: datetime

class CostAnalysis(BaseModel):
    namespace: str
    deployment_name: str
    estimated_monthly_cost_usd: float
    optimized_monthly_cost_usd: float
    potential_savings_usd: float
    savings_percent: float
    is_overprovisioned: bool
    recommendations: List[str]

class AnomalyAlert(BaseModel):
    namespace: str
    deployment_name: str
    metric_name: str
    current_value: float
    baseline_value: float
    z_score: float
    severity: str
    detected_at: datetime
    description: str

class RightsizingRecommendation(BaseModel):
    namespace: str
    deployment_name: str
    container_name: str
    resource_type: str
    current_value: float
    recommended_value: float
    savings_usd: float
    confidence_score: float

class AnalyzerHealthStatus(BaseModel):
    status: str
    metrics_count: int
    anomalies_detected: int
    last_analysis_at: Optional[datetime] = None
