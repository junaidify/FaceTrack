"""FaceService configuration."""
from __future__ import annotations

from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False, extra="ignore")

    app_port: int = 8002
    log_level: str = "INFO"
    insightface_model: str = "buffalo_l"
    det_size: int = 640


@lru_cache
def get_settings() -> Settings:
    return Settings()  # type: ignore[arg-type]
