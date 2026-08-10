from analyzer.cost_analyzer import CostAnalyzer


def test_cost_calculation(test_settings, sample_metrics):
    analyzer = CostAnalyzer(test_settings)
    analyses = analyzer.analyze_costs(sample_metrics)

    assert len(analyses) == len(sample_metrics)
    # Check if first element is analyzed properly
    assert analyses[0].estimated_monthly_cost_usd > 0
    assert analyses[0].optimized_monthly_cost_usd > 0


def test_overprovisioned_detection(test_settings, sample_metrics):
    analyzer = CostAnalyzer(test_settings)
    analyses = analyzer.analyze_costs(sample_metrics)

    # The 4th item in mock data is staging/api-gateway (10% cpu, 15% mem) -> should be overprovisioned  # noqa: E501
    staging_gateway = next(
        (
            a
            for a in analyses
            if a.deployment_name == "api-gateway" and a.namespace == "staging"
        ),
        None,
    )
    assert staging_gateway is not None
    assert staging_gateway.is_overprovisioned is True
    assert "Severely overprovisioned" in staging_gateway.recommendations[0]


def test_well_provisioned_detection(test_settings, sample_metrics):
    analyzer = CostAnalyzer(test_settings)
    analyses = analyzer.analyze_costs(sample_metrics)

    # user-service has 80% cpu, 85% mem utilization -> should not be overprovisioned  # noqa: E501
    user_service = next(
        (a for a in analyses if a.deployment_name == "user-service"), None
    )
    assert user_service is not None
    assert user_service.is_overprovisioned is False


def test_namespace_summary_grouping(test_settings, sample_metrics):
    analyzer = CostAnalyzer(test_settings)
    analyses = analyzer.analyze_costs(sample_metrics)
    summary = analyzer.get_namespace_summary(analyses)

    assert "production" in summary
    assert "staging" in summary
    assert summary["production"]["total_cost"] > 0
    assert summary["staging"]["total_cost"] > 0
