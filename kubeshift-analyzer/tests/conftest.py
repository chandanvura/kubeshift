import pytest
from fastapi.testclient import TestClient
from analyzer.main import app
from analyzer.config import Settings
from analyzer.mock_data import generate_mock_metrics

@pytest.fixture
def client():
    return TestClient(app)

@pytest.fixture
def test_settings():
    return Settings(
        CPU_RATE_PER_CORE_HOUR=0.048,
        MEMORY_RATE_PER_GB_HOUR=0.006,
        MIN_DATA_POINTS=3,
        ANOMALY_THRESHOLD=2.0
    )

@pytest.fixture
def sample_metrics():
    return generate_mock_metrics()
