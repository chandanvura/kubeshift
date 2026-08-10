from analyzer.anomaly_detector import AnomalyDetector
from datetime import datetime, timezone
from analyzer.models import ResourceMetrics

def test_z_score_calculation(test_settings):
    detector = AnomalyDetector(test_settings)
    values = [10, 10, 10, 10]
    # mean is 10, std is 0
    assert detector._calculate_z_score(values, 10) == 0.0

def test_anomaly_detection_with_spike(test_settings, sample_metrics):
    detector = AnomalyDetector(test_settings)
    
    # Feed initial normal data
    for _ in range(5):
        detector.detect_anomalies(sample_metrics)
        
    # Introduce a spike
    spike_metrics = []
    for m in sample_metrics:
        spike_m = m.model_copy()
        spike_m.cpu_usage_millis = m.cpu_usage_millis * 10  # 10x spike
        spike_metrics.append(spike_m)
        
    alerts = detector.detect_anomalies(spike_metrics)
    
    assert len(alerts) > 0
    for alert in alerts:
        assert alert.z_score > 0
        assert "Cost spike detected" in alert.description

def test_severity_classification(test_settings, sample_metrics):
    detector = AnomalyDetector(test_settings)
    
    # Send some steady state
    steady_metrics = [sample_metrics[0].model_copy() for _ in range(10)]
    for m in steady_metrics:
        detector.detect_anomalies([m])
        
    # Trigger a massive spike
    massive_spike = sample_metrics[0].model_copy()
    massive_spike.cpu_usage_millis *= 50
    alerts = detector.detect_anomalies([massive_spike])
    
    assert len(alerts) == 1
    assert alerts[0].severity in ["HIGH", "CRITICAL"]
