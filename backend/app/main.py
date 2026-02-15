import base64
import json
import os
from typing import Any
from uuid import uuid4

import httpx
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from pydantic import BaseModel
from dotenv import load_dotenv


load_dotenv()


class DetectedType(BaseModel):
    type: str
    confidence: float


class ImagePreview(BaseModel):
    url: str
    score: float


class Preview(BaseModel):
    en_title: str
    en_description: str
    tags: list[str]
    images: list[ImagePreview]


class ScanItem(BaseModel):
    item_id: str
    jp_text: str
    price_text: str | None = None
    confidence: float
    preview: Preview


class ScanMenuResponse(BaseModel):
    scan_id: str
    detected_type: DetectedType
    items: list[ScanItem]


class LlmItem(BaseModel):
    jp_text: str
    price_text: str | None = None
    en_title: str
    en_description: str
    tags: list[str] = []
    image_query: str | None = None
    confidence: float = 0.7


class LlmOutput(BaseModel):
    detected_type: str = "dish"
    items: list[LlmItem]


app = FastAPI(title="MenuLens API", version="0.2.0")


def _require_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        raise HTTPException(status_code=500, detail=f"Missing required env var: {name}")
    return value


async def _vision_ocr(image_bytes: bytes, api_key: str) -> str:
    encoded = base64.b64encode(image_bytes).decode("utf-8")
    payload = {
        "requests": [
            {
                "image": {"content": encoded},
                "features": [{"type": "TEXT_DETECTION"}],
            }
        ]
    }
    url = f"https://vision.googleapis.com/v1/images:annotate?key={api_key}"
    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(url, json=payload)
        response.raise_for_status()
        body = response.json()

    ocr_text = (
        body.get("responses", [{}])[0]
        .get("fullTextAnnotation", {})
        .get("text", "")
        .strip()
    )
    if not ocr_text:
        annotations = body.get("responses", [{}])[0].get("textAnnotations", [])
        if annotations:
            ocr_text = annotations[0].get("description", "").strip()
    return ocr_text


def _extract_json_payload(text: str) -> dict[str, Any]:
    cleaned = text.strip()
    if cleaned.startswith("```"):
        cleaned = cleaned.strip("`")
        if cleaned.startswith("json"):
            cleaned = cleaned[4:].strip()
    start = cleaned.find("{")
    end = cleaned.rfind("}")
    if start == -1 or end == -1 or end <= start:
        raise ValueError("Gemini response did not contain a JSON object")
    return json.loads(cleaned[start : end + 1])


async def _gemini_parse_menu(ocr_text: str, target_lang: str, api_key: str) -> LlmOutput:
    model = os.getenv("GEMINI_MODEL", "gemini-1.5-flash")
    prompt = f"""
You are extracting Japanese restaurant menu items.
Given OCR text, produce strict JSON only.

Requirements:
- Target language: {target_lang}
- Prefer true dish names over noisy OCR fragments.
- Return 1 to 6 best dish items.
- Keep en_description short (max 25 words).
- Keep tags short lowercase tokens.
- image_query should be a good web image search query in English.

JSON schema:
{{
  "detected_type": "string",
  "items": [
    {{
      "jp_text": "string",
      "price_text": "string or empty",
      "en_title": "string",
      "en_description": "string",
      "tags": ["string"],
      "image_query": "string",
      "confidence": 0.0
    }}
  ]
}}

OCR text:
{ocr_text}
""".strip()

    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "temperature": 0.2,
            "responseMimeType": "application/json",
        },
    }
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
    async with httpx.AsyncClient(timeout=40.0) as client:
        response = await client.post(url, json=payload)
        response.raise_for_status()
        body = response.json()

    try:
        text = body["candidates"][0]["content"]["parts"][0]["text"]
    except (KeyError, IndexError, TypeError) as exc:
        raise ValueError("Gemini response format unexpected") from exc

    parsed = _extract_json_payload(text)
    return LlmOutput.model_validate(parsed)


async def _image_search(query: str, api_key: str, cx: str) -> list[ImagePreview]:
    params = {
        "key": api_key,
        "cx": cx,
        "searchType": "image",
        "num": 2,
        "safe": "active",
        "q": query,
    }
    url = "https://www.googleapis.com/customsearch/v1"
    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.get(url, params=params)
        response.raise_for_status()
        body = response.json()

    items = body.get("items", [])[:2]
    previews: list[ImagePreview] = []
    for idx, item in enumerate(items):
        image_url = item.get("link")
        if not image_url:
            continue
        previews.append(ImagePreview(url=image_url, score=max(0.5, 0.9 - idx * 0.1)))
    return previews


async def _image_search_safe(query: str, api_key: str, cx: str) -> list[ImagePreview]:
    try:
        return await _image_search(query=query, api_key=api_key, cx=cx)
    except httpx.HTTPError:
        # Temporary workaround for early development:
        # if CSE is unavailable/forbidden, keep scan result without images.
        return []


def _fallback_response() -> ScanMenuResponse:
    return ScanMenuResponse(
        scan_id=str(uuid4()),
        detected_type=DetectedType(type="dish", confidence=0.55),
        items=[
            ScanItem(
                item_id=str(uuid4()),
                jp_text="料理名を読み取れませんでした",
                price_text=None,
                confidence=0.45,
                preview=Preview(
                    en_title="Could not read menu item",
                    en_description="Please retake the photo with sharper focus and better lighting.",
                    tags=["retry"],
                    images=[],
                ),
            )
        ],
    )


@app.post("/v1/scan_menu", response_model=ScanMenuResponse)
async def scan_menu(
    image: UploadFile = File(...),
    target_lang: str = Form(...),
    device_id: str = Form(...),
    app_version: str = Form(...),
    timezone: str = Form(...),
) -> ScanMenuResponse:
    _ = (device_id, app_version, timezone)
    image_bytes = await image.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Uploaded image is empty")

    vision_key = _require_env("GOOGLE_CLOUD_VISION_API_KEY")
    gemini_key = _require_env("GEMINI_API_KEY")
    enable_image_search = os.getenv("ENABLE_IMAGE_SEARCH", "true").strip().lower() == "true"
    cse_key = _require_env("GOOGLE_CSE_API_KEY") if enable_image_search else ""
    cse_cx = _require_env("GOOGLE_CSE_CX") if enable_image_search else ""

    try:
        ocr_text = await _vision_ocr(image_bytes=image_bytes, api_key=vision_key)
        if not ocr_text:
            return _fallback_response()

        llm = await _gemini_parse_menu(ocr_text=ocr_text, target_lang=target_lang, api_key=gemini_key)
        if not llm.items:
            return _fallback_response()

        items: list[ScanItem] = []
        for raw_item in llm.items[:6]:
            query = raw_item.image_query or raw_item.en_title
            images = (
                await _image_search_safe(query=query, api_key=cse_key, cx=cse_cx)
                if enable_image_search
                else []
            )
            items.append(
                ScanItem(
                    item_id=str(uuid4()),
                    jp_text=raw_item.jp_text,
                    price_text=raw_item.price_text,
                    confidence=max(0.0, min(1.0, raw_item.confidence)),
                    preview=Preview(
                        en_title=raw_item.en_title,
                        en_description=raw_item.en_description,
                        tags=raw_item.tags[:5],
                        images=images,
                    ),
                )
            )

        return ScanMenuResponse(
            scan_id=str(uuid4()),
            detected_type=DetectedType(type=llm.detected_type, confidence=0.8),
            items=items,
        )
    except HTTPException:
        raise
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail=f"Upstream API error: {exc}") from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Scan pipeline failed: {exc}") from exc
