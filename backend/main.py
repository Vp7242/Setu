"""Setu API. Run:  uvicorn main:app --host 0.0.0.0 --port 8000"""
import logging

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from pipeline import run_pipeline

app = FastAPI(title="Setu API")
logger = logging.getLogger("setu")


class TranslateRequest(BaseModel):
    text: str = Field(min_length=1, max_length=5000)
    target_lang: str = Field(pattern="^(mr|en)$")


@app.get("/health")
def health():
    return {"ok": True}


@app.post("/translate")
def translate(req: TranslateRequest):
    try:
        return run_pipeline(req.text, req.target_lang)
    except Exception as e:  # surface a clean error to the app
        logger.exception("Error processing translation request: %s", e)
        raise HTTPException(status_code=502, detail=str(e))
