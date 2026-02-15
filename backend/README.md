# MenuLens FastAPI Skeleton

## Run

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Set these values in `.env` before running:
- `GOOGLE_CLOUD_VISION_API_KEY`
- `GEMINI_API_KEY`
- `GOOGLE_CSE_API_KEY`
- `GOOGLE_CSE_CX`

Optional:
- `GEMINI_MODEL` (default: `gemini-1.5-flash`)

## Test endpoint

```bash
curl -X POST "http://127.0.0.1:8000/v1/scan_menu" \
  -F "image=@sample.jpg" \
  -F "target_lang=en" \
  -F "device_id=11111111-1111-1111-1111-111111111111" \
  -F "app_version=0.1.0" \
  -F "timezone=Asia/Tokyo"
```
