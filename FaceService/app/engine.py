"""InsightFace embedding engine wrapper.

Loads the ArcFace model once at startup; reused across requests.
Returns L2-normalized 512-d embeddings.
"""
from __future__ import annotations

import base64
import logging
import threading
from typing import List, Optional, Tuple

import cv2
import numpy as np
from insightface.app import FaceAnalysis

from app.config import get_settings

log = logging.getLogger(__name__)


class FaceEngine:
    """Thread-safe wrapper around an InsightFace FaceAnalysis instance."""

    def __init__(self) -> None:
        settings = get_settings()
        self._lock = threading.Lock()
        log.info("Loading InsightFace model '%s'...", settings.insightface_model)
        self._fa = FaceAnalysis(
            name=settings.insightface_model,
            providers=["CPUExecutionProvider"],
        )
        self._fa.prepare(ctx_id=-1, det_size=(settings.det_size, settings.det_size))
        log.info("Model loaded.")

    @staticmethod
    def _decode_image(image_b64: str) -> np.ndarray:
        # Tolerate data-URL prefix and whitespace just in case the caller forgets.
        if image_b64.startswith("data:"):
            image_b64 = image_b64.split(",", 1)[-1]
        raw = base64.b64decode(image_b64.strip(), validate=False)
        arr = np.frombuffer(raw, dtype=np.uint8)
        img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Could not decode image. Expected base64-encoded JPEG/PNG.")
        return img

    def _largest_face(self, img: np.ndarray):
        with self._lock:
            faces = self._fa.get(img)
        if not faces:
            return None
        # Pick the face with the largest bounding box area.
        def area(f):
            x1, y1, x2, y2 = f.bbox
            return max(0.0, (x2 - x1)) * max(0.0, (y2 - y1))

        return max(faces, key=area)

    def embed(self, image_b64: str) -> Tuple[List[float], List[float], float]:
        img = self._decode_image(image_b64)
        face = self._largest_face(img)
        if face is None:
            raise FaceNotFound()
        emb = face.normed_embedding.astype(np.float32)
        bbox = face.bbox.astype(float).tolist()
        score = float(getattr(face, "det_score", 0.0))
        return emb.tolist(), bbox, score

    @staticmethod
    def match(
        probe: List[float],
        candidates: List[List[float]],
        threshold: float,
    ) -> Tuple[bool, Optional[int], Optional[float]]:
        if not candidates:
            return False, None, None
        p = np.array(probe, dtype=np.float32)
        c = np.array(candidates, dtype=np.float32)
        # Cosine similarity on L2-normalized vectors == dot product.
        # Re-normalize defensively in case caller passed raw embeddings.
        p_norm = p / max(np.linalg.norm(p), 1e-9)
        c_norms = np.linalg.norm(c, axis=1, keepdims=True)
        c_norms[c_norms == 0] = 1e-9
        c_normalized = c / c_norms
        sims = c_normalized @ p_norm  # shape (N,)
        best_idx = int(np.argmax(sims))
        best_sim = float(sims[best_idx])
        # Cosine distance = 1 - similarity. Smaller is better.
        matched = (1.0 - best_sim) <= threshold
        return matched, (best_idx if matched else None), best_sim


class FaceNotFound(Exception):
    """Raised when no face is detected in the supplied image."""


_engine: FaceEngine | None = None


def get_engine() -> FaceEngine:
    global _engine
    if _engine is None:
        _engine = FaceEngine()
    return _engine
