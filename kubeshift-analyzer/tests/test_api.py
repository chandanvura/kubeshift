def test_health_endpoint(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "UP"}

def test_cost_analysis_endpoint(client):
    response = client.get("/api/v1/analyzer/cost")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0
    assert "estimated_monthly_cost_usd" in data[0]

def test_recommendations_endpoint(client):
    response = client.get("/api/v1/analyzer/recommendations")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    if len(data) > 0:
        assert "recommended_value" in data[0]

def test_summary_endpoint(client):
    response = client.get("/api/v1/analyzer/summary")
    assert response.status_code == 200
    data = response.json()
    assert "namespace_summary" in data
    assert "total_recommendations" in data
    assert "total_anomalies" in data
