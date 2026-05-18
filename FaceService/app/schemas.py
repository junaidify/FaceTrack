"""Request/response shapes for the FaceService."""
from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, Field


class EmbedIn(BaseModel):
    image: str = Field(..., min_length=32, description="Base64 image (no data-URL prefix expected).")


class EmbedOut(BaseModel):
    embedding: List[float]
    bbox: Optional[List[float]] = None
    det_score: Optional[float] = None


class MatchIn(BaseModel):
    image: str = Field(..., min_length=32)
    candidates: List[List[float]]
    threshold: float = Field(0.45, ge=0.0, le=1.0)


class MatchOut(BaseModel):
    matched: bool
    best_index: Optional[int] = None
    similarity: Optional[float] = None
