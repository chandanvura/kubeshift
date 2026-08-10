from pydantic_settings import BaseSettings
class Settings(BaseSettings):
    CORE_SERVICE_URL: str = 'http://kubeshift-core:8080'
    COLLECTION_INTERVAL: int = 60
    ANOMALY_THRESHOLD: float = 2.0
    MIN_DATA_POINTS: int = 10
    CPU_RATE_PER_CORE_HOUR: float = 0.048
    MEMORY_RATE_PER_GB_HOUR: float = 0.006

    class Config:
        env_file = ".env"

config = Settings()
settings = config
