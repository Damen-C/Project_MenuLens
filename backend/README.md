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

Optional:
- `GEMINI_MODEL` (default: `gemini-1.5-flash`)
- `ENABLE_IMAGE_SEARCH` (default: `true`)
- `IMAGE_SEARCH_PROVIDER` (`none`, `cse`, or `vertex`; default: `cse`)
- `GOOGLE_CSE_API_KEY` and `GOOGLE_CSE_CX` (required only when `IMAGE_SEARCH_PROVIDER=cse`)
- `GCP_PROJECT_ID`, `VERTEX_SEARCH_LOCATION`, `VERTEX_SEARCH_APP_ID` (required only when `IMAGE_SEARCH_PROVIDER=vertex`)

Notes:
- `Custom Search JSON API` may be unavailable for new projects/accounts.
- `IMAGE_SEARCH_PROVIDER=none` disables image retrieval while keeping OCR/translation flow functional.
- Vertex provider uses Google ADC credentials (service-account JSON via `GOOGLE_APPLICATION_CREDENTIALS` or `gcloud auth application-default login`).

### Recommended vertex config

```env
ENABLE_IMAGE_SEARCH=true
IMAGE_SEARCH_PROVIDER=vertex
GCP_PROJECT_ID=your_gcp_project_id
VERTEX_SEARCH_LOCATION=global
VERTEX_SEARCH_APP_ID=your_vertex_search_app_id
```

### Vertex auth setup (Windows PowerShell)

Service account JSON:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="D:\Project_MenuLens\backend\keys\menulens-sa.json"
```

or local ADC login:

```powershell
gcloud auth application-default login
```

## Test endpoint

```bash
curl -X POST "http://127.0.0.1:8000/v1/scan_menu" \
  -F "image=@sample.jpg" \
  -F "target_lang=en" \
  -F "device_id=11111111-1111-1111-1111-111111111111" \
  -F "app_version=0.1.0" \
  -F "timezone=Asia/Tokyo"
```
