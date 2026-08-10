from analyzer.rightsizing_engine import RightsizingEngine

def test_recommendation_generation(test_settings, sample_metrics):
    engine = RightsizingEngine(test_settings)
    recs = engine.generate_recommendations(sample_metrics)
    
    assert len(recs) > 0
    # They should be sorted by savings descending
    assert recs[0].savings_usd >= recs[-1].savings_usd

def test_minimum_threshold_enforcement(test_settings, sample_metrics):
    engine = RightsizingEngine(test_settings)
    
    # Modify a metric to have extremely low usage
    low_usage = sample_metrics[3].model_copy()
    low_usage.cpu_usage_millis = 1.0 # Very low
    low_usage.memory_usage_bytes = 1024 # 1KB
    
    recs = engine.generate_recommendations([low_usage])
    
    cpu_rec = next(r for r in recs if r.resource_type == "CPU")
    mem_rec = next(r for r in recs if r.resource_type == "Memory")
    
    assert cpu_rec.recommended_value >= 50.0
    assert mem_rec.recommended_value >= 64 * 1024 * 1024

def test_confidence_scoring(test_settings, sample_metrics):
    engine = RightsizingEngine(test_settings)
    recs = engine.generate_recommendations(sample_metrics)
    
    for rec in recs:
        assert rec.confidence_score > 0
        assert rec.confidence_score <= 1.0
