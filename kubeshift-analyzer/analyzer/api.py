from fastapi import APIRouter
from typing import List, Dict, Any
from .models import (
    ResourceMetrics,
    CostAnalysis,
    AnomalyAlert,
    RightsizingRecommendation,
)
from .config import settings
from .cost_analyzer import CostAnalyzer
from .anomaly_detector import AnomalyDetector
from .rightsizing_engine import RightsizingEngine
from .mock_data import generate_mock_metrics

router = APIRouter(prefix="/api/v1/analyzer", tags=["analyzer"])

cost_analyzer = CostAnalyzer(settings)
anomaly_detector = AnomalyDetector(settings)
rightsizing_engine = RightsizingEngine(settings)

# Temporary in-memory state for mock purposes
latest_metrics = generate_mock_metrics()


@router.get("/cost", response_model=List[CostAnalysis])
def get_cost_analysis():
    # In a real scenario, this would fetch from core service
    # For now we use the mock data
    return cost_analyzer.analyze_costs(latest_metrics)


@router.get("/anomalies", response_model=List[AnomalyAlert])
def get_anomalies():
    return anomaly_detector.detect_anomalies(latest_metrics)


@router.get("/recommendations", response_model=List[RightsizingRecommendation])
def get_recommendations():
    return rightsizing_engine.generate_recommendations(latest_metrics)


@router.get("/summary", response_model=Dict[str, Any])
def get_summary():
    analyses = cost_analyzer.analyze_costs(latest_metrics)
    ns_summary = cost_analyzer.get_namespace_summary(analyses)
    recs = rightsizing_engine.generate_recommendations(latest_metrics)
    anomalies = anomaly_detector.detect_anomalies(latest_metrics)

    return {
        "namespace_summary": ns_summary,
        "total_recommendations": len(recs),
        "total_anomalies": len(anomalies),
    }


@router.post("/analyze", response_model=Dict[str, str])
def trigger_analysis(metrics: List[ResourceMetrics]):
    global latest_metrics
    latest_metrics = metrics
    return {"status": "Analysis triggered successfully"}
