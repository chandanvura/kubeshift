import numpy as np
from collections import deque
from typing import List, Dict
from datetime import datetime, timezone
from .models import ResourceMetrics, AnomalyAlert
from .config import Settings


class AnomalyDetector:
    def __init__(self, config: Settings):
        self.config = config
        self.history: Dict[str, deque] = {}

    def _get_key(self, metric: ResourceMetrics) -> str:
        return f"{metric.namespace}/{metric.deployment_name}/{metric.container_name}"  # noqa: E501

    def _calculate_z_score(self, values: List[float], current: float) -> float:
        if len(values) < self.config.MIN_DATA_POINTS:
            return 0.0
        mean = np.mean(values)
        std_dev = np.std(values)
        if std_dev == 0:
            if current == mean:
                return 0.0
            std_dev = 1e-6
        return (current - mean) / std_dev

    def detect_anomalies(
        self, metrics: List[ResourceMetrics]
    ) -> List[AnomalyAlert]:
        alerts = []
        now = datetime.now(timezone.utc)

        for metric in metrics:
            key = self._get_key(metric)
            if key not in self.history:
                self.history[key] = deque(maxlen=1000)

            # Analyze CPU usage anomaly
            current_cpu = metric.cpu_usage_millis
            history_list = list(self.history[key])
            cpu_history = [m.cpu_usage_millis for m in history_list]

            z_score = self._calculate_z_score(cpu_history, current_cpu)
            abs_z = abs(z_score)

            if abs_z > self.config.ANOMALY_THRESHOLD:
                severity = "LOW"
                if abs_z > 5:
                    severity = "CRITICAL"
                elif abs_z > 4:
                    severity = "HIGH"
                elif abs_z > 3:
                    severity = "MEDIUM"

                baseline = np.mean(cpu_history) if cpu_history else current_cpu
                desc = (
                    "Cost spike detected"
                    if z_score > 0
                    else "Sudden utilization drop detected"
                )

                alerts.append(
                    AnomalyAlert(
                        namespace=metric.namespace,
                        deployment_name=metric.deployment_name,
                        metric_name="cpu_usage",
                        current_value=current_cpu,
                        baseline_value=baseline,
                        z_score=z_score,
                        severity=severity,
                        detected_at=now,
                        description=desc,
                    )
                )

            self.history[key].append(metric)

        return alerts
