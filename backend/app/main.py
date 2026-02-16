import base64
import json
import logging
import os
from typing import Any
from urllib.parse import urlparse
from uuid import uuid4

import httpx
import google.auth
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from google.auth.transport.requests import Request
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
_VERTEX_SCOPE = "https://www.googleapis.com/auth/cloud-platform"
logger = logging.getLogger("menulens")
_IMAGE_EXTENSIONS = (".jpg", ".jpeg", ".png", ".webp", ".gif", ".avif", ".bmp")


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


def _vertex_access_token() -> str:
    credentials, _ = google.auth.default(scopes=[_VERTEX_SCOPE])
    credentials.refresh(Request())
    token = credentials.token
    if not token:
        raise HTTPException(status_code=500, detail="Failed to acquire Vertex access token")
    return token


def _is_http_url(value: str) -> bool:
    lowered = value.lower()
    return lowered.startswith("http://") or lowered.startswith("https://")


def _collect_url_candidates(payload: Any, key_path: str = "") -> list[tuple[str, str]]:
    candidates: list[tuple[str, str]] = []
    stack: list[tuple[str, Any]] = [(key_path, payload)]
    visited = 0

    while stack and visited < 2000:
        path, current = stack.pop()
        visited += 1

        if isinstance(current, dict):
            for key, value in current.items():
                child_path = f"{path}.{key}" if path else str(key)
                stack.append((child_path, value))
            continue

        if isinstance(current, list):
            for idx, value in enumerate(current):
                child_path = f"{path}[{idx}]"
                stack.append((child_path, value))
            continue

        if isinstance(current, str) and _is_http_url(current):
            candidates.append((path.lower(), current))

    return candidates


def _is_likely_image_url(key_hint: str, url: str) -> bool:
    parsed = urlparse(url)
    path = parsed.path.lower()
    hint = key_hint.lower()

    if path.endswith(_IMAGE_EXTENSIONS):
        return True

    image_hint_keys = ("image", "thumbnail", "thumb", "poster", "photo", "icon")
    if any(token in hint for token in image_hint_keys):
        return True

    return False


def _pick_image_url_from_vertex_result(result: dict[str, Any]) -> str | None:
    candidates = _collect_url_candidates(result)
    if not candidates:
        return None

    ranked = sorted(
        candidates,
        key=lambda item: (
            0 if _is_likely_image_url(item[0], item[1]) else 1,
            0 if "image" in item[0] else 1,
            0 if "thumbnail" in item[0] else 1,
            len(item[0]),
        ),
    )

    for key_hint, url in ranked:
        if _is_likely_image_url(key_hint, url):
            return url
    return None


async def _vertex_search(
    query: str,
    project_id: str,
    location: str,
    app_id: str,
    access_token: str,
) -> list[ImagePreview]:
    serving_config = (
        f"projects/{project_id}/locations/{location}/collections/default_collection/"
        f"engines/{app_id}/servingConfigs/default_search"
    )
    url = f"https://discoveryengine.googleapis.com/v1alpha/{serving_config}:search"
    payload = {
        "query": query,
        "pageSize": 10,
        "queryExpansionSpec": {"condition": "AUTO"},
        "spellCorrectionSpec": {"mode": "AUTO"},
        "languageCode": "en-US",
    }
    headers = {"Authorization": f"Bearer {access_token}"}

    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.post(url, json=payload, headers=headers)
        response.raise_for_status()
        body = response.json()

    previews: list[ImagePreview] = []
    first_result = None
    for idx, result in enumerate(body.get("results", [])):
        if first_result is None:
            first_result = result
        image_url = _pick_image_url_from_vertex_result(result) if isinstance(result, dict) else None
        if not image_url:
            continue
        previews.append(ImagePreview(url=image_url, score=max(0.5, 0.9 - idx * 0.1)))
        if len(previews) >= 2:
            break

    if not previews:
        logger.warning(
            "Vertex search returned no usable image URL. query=%s results=%s sample_keys=%s",
            query,
            len(body.get("results", [])),
            list(first_result.keys()) if isinstance(first_result, dict) else None,
        )

    return previews


async def _vertex_search_safe(
    query: str,
    project_id: str,
    location: str,
    app_id: str,
    access_token: str,
) -> list[ImagePreview]:
    try:
        return await _vertex_search(
            query=query,
            project_id=project_id,
            location=location,
            app_id=app_id,
            access_token=access_token,
        )
    except httpx.HTTPStatusError as exc:
        response_text = exc.response.text if exc.response is not None else ""
        logger.error(
            "Vertex HTTP status error. query=%s status=%s body=%s",
            query,
            exc.response.status_code if exc.response is not None else "unknown",
            response_text[:1000],
        )
        return []
    except httpx.HTTPError:
        logger.exception("Vertex HTTP transport error. query=%s", query)
        return []


def _resolve_image_search_provider() -> str:
    provider = os.getenv("IMAGE_SEARCH_PROVIDER", "cse").strip().lower()
    if not provider:
        return "cse"
    if provider not in {"none", "cse", "vertex"}:
        raise HTTPException(
            status_code=500,
            detail=(
                "Invalid IMAGE_SEARCH_PROVIDER. "
                "Use one of: none, cse, vertex."
            ),
        )
    return provider


async def _image_search_by_provider(
    query: str,
    provider: str,
    cse_api_key: str,
    cse_cx: str,
    vertex_project_id: str,
    vertex_location: str,
    vertex_app_id: str,
    vertex_access_token: str,
) -> list[ImagePreview]:
    if provider == "none":
        return []
    if provider == "cse":
        return await _image_search_safe(query=query, api_key=cse_api_key, cx=cse_cx)
    if provider == "vertex":
        return await _vertex_search_safe(
            query=query,
            project_id=vertex_project_id,
            location=vertex_location,
            app_id=vertex_app_id,
            access_token=vertex_access_token,
        )
    raise ValueError(f"Unsupported image search provider: {provider}")


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
    provider = _resolve_image_search_provider()
    if not enable_image_search:
        provider = "none"

    cse_key = _require_env("GOOGLE_CSE_API_KEY") if provider == "cse" else ""
    cse_cx = _require_env("GOOGLE_CSE_CX") if provider == "cse" else ""
    vertex_project_id = _require_env("GCP_PROJECT_ID") if provider == "vertex" else ""
    vertex_location = _require_env("VERTEX_SEARCH_LOCATION") if provider == "vertex" else ""
    vertex_app_id = _require_env("VERTEX_SEARCH_APP_ID") if provider == "vertex" else ""
    vertex_access_token = _vertex_access_token() if provider == "vertex" else ""

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
            images = await _image_search_by_provider(
                query=query,
                provider=provider,
                cse_api_key=cse_key,
                cse_cx=cse_cx,
                vertex_project_id=vertex_project_id,
                vertex_location=vertex_location,
                vertex_app_id=vertex_app_id,
                vertex_access_token=vertex_access_token,
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
