from uuid import uuid4

from fastapi import FastAPI, File, Form, UploadFile
from pydantic import BaseModel


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
    price_text: str
    confidence: float
    preview: Preview


class ScanMenuResponse(BaseModel):
    scan_id: str
    detected_type: DetectedType
    items: list[ScanItem]


app = FastAPI(title="MenuLens API", version="0.1.0")


@app.post("/v1/scan_menu", response_model=ScanMenuResponse)
async def scan_menu(
    image: UploadFile = File(...),
    target_lang: str = Form(...),
    device_id: str = Form(...),
    app_version: str = Form(...),
    timezone: str = Form(...),
) -> ScanMenuResponse:
    _ = (image, target_lang, device_id, app_version, timezone)

    return ScanMenuResponse(
        scan_id=str(uuid4()),
        detected_type=DetectedType(type="ramen", confidence=0.72),
        items=[
            ScanItem(
                item_id=str(uuid4()),
                jp_text="醤油ラーメン",
                price_text="900円",
                confidence=0.91,
                preview=Preview(
                    en_title="Shoyu ramen",
                    en_description="Soy sauce-based ramen, often served with pork and green onion.",
                    tags=["pork_possible"],
                    images=[
                        ImagePreview(
                            url="https://example.com/images/shoyu-ramen.jpg",
                            score=0.83,
                        )
                    ],
                ),
            )
        ],
    )
