"""FaceService FastAPI app."""
from __future__ import annotations

import logging
import sys
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status
from fastapi.responses import JSONResponse

from app.config import get_settings
from app.engine import FaceNotFound, get_engine
from app.schemas import EmbedIn, EmbedOut, MatchIn, MatchOut


def _configure_logging() -> None:
    settings = get_settings()
    level = getattr(logging, settings.log_level.upper(), logging.INFO)
    h = logging.StreamHandler(sys.stdout)
    h.setFormatter(
        logging.Formatter("%(asctime)s | %(levelname)-7s | %(name)s | %(message)s", "%Y-%m-%dT%H:%M:%S")
    )
    root = logging.getLogger()
    root.handlers.clear()
    root.addHandler(h)
    root.setLevel(level)


log = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    _configure_logging()
    log.info("Warming up FaceService engine...")
    get_engine()  # eager-load the model
    log.info("FaceService ready.")
    yield


app = FastAPI(title="FaceTrack FaceService", version="1.0.0", lifespan=lifespan)


@app.get("/healthz", tags=["meta"])
async def healthz():
    return {"status": "ok"}


@app.post("/embed", response_model=EmbedOut)
async def embed(body: EmbedIn) -> EmbedOut:
    try:
        emb, bbox, score = get_engine().embed(body.image)
    except FaceNotFound:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="No face detected in the image.",
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return EmbedOut(embedding=emb, bbox=bbox, det_score=score)


@app.post("/match", response_model=MatchOut)
async def match(body: MatchIn) -> MatchOut:
    eng = get_engine()
    try:
        probe, _, _ = eng.embed(body.image)
    except FaceNotFound:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="No face detected in the image.",
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

    matched, best_index, similarity = eng.match(probe, body.candidates, body.threshold)
    return MatchOut(matched=matched, best_index=best_index, similarity=similarity)


@app.exception_handler(Exception)
async def _unhandled(_, exc: Exception):
    log.exception("FaceService unhandled error: %s", exc)
    return JSONResponse(
        status_code=500,
        content={"error": "InternalServerError", "message": "Something went wrong."},
    )
