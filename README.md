# MenuLens

MenuLens is an Android app for scanning Japanese menus and showing English-friendly dish results.

## Auth And Gating Status

Current implementation includes:

- Android Firebase anonymous sign-in wiring
- Backend Firebase token verification scaffold
- Monthly scan quota enforcement (`free: 10/month`, `pro: 250/month`)
- Request idempotency field (`request_id`) in `POST /v1/scan_menu`
- Safe developer bypass allowlist via `DEV_BYPASS_QUOTA_UIDS`
- Fixed results-screen scrolling and bottom inset handling for long menus

Quick backend checks:

- Open `/docs` for `POST /v1/scan_menu`
- Confirm `request_id` exists in the request form

Current limitations:

- Pro billing is not connected yet
- Quota storage still uses SQLite and should be moved to shared production storage before real multi-instance rollout

## Current Status

- Android app: Kotlin + Compose + MVVM flow (`Scan -> Processing -> Results -> Detail`)
- Backend API: FastAPI `POST /v1/scan_menu`
- Live pipeline implemented:
  - OCR: Google Cloud Vision API
  - Dish extraction/translation/description: Gemini
  - Image retrieval: provider-based (`none | cse | vertex`)
  - Current recommended provider: Vertex AI Search
- CI: Android lint + unit tests + debug assemble on GitHub Actions

## Next Step: LLMOps

The next major step for MenuLens is turning the current AI pipeline into a stronger LLMOps case study.

LLMOps progress now in place:

- Real menu-image eval dataset under `backend/evals/dataset/examples.json` and `backend/evals/fixtures/`
- Repeatable offline eval runner: `python -m evals.run_evals`
- JSON eval reports under `backend/evals/results/`
- Excel-compatible eval run ledger: `backend/evals/results/eval_runs.csv`
- Versioned prompts under `backend/app/prompts/`
- Env-selectable prompt versions via `MENU_PARSE_PROMPT_VERSION` and `OCR_NORMALIZE_PROMPT_VERSION`
- Improved Japanese item-name scorer with fuzzy matching and match diagnostics
- Live pipeline diagnostics now include model, prompt versions, and stage latencies

Current baseline from `backend/evals/results/eval_report_20260411T013853Z.json`:

- Dataset size: 8 real menu fixtures
- Model: `gemini-2.5-flash`
- Prompt versions: `menu_parse_v1`, `ocr_normalize_v1`
- Parse success rate: `1.0`
- Item recall: `0.8708`
- Item precision proxy: `0.7704`
- Hallucinated item rate: `0.2296`
- Mean latency: `20646.6 ms`
- P95 latency: `51410.5 ms`
- Main remaining weak cases: over-extraction on `menu-1`, generic item extraction on `menu-4`, extra item extraction on `menu-5`, handwritten kakiage extraction on `menu-6`

Run evals locally:

```powershell
cd backend
python -m evals.run_evals
```

Immediate follow-up:

- Save the current eval summary as a committed baseline file
- Add a baseline comparison script for prompt/model regression checks
- Create `menu_parse_v2` focused on exact Japanese item-name extraction
- Compare `menu_parse_v2` against the baseline before adopting it
- Later: add CI regression gating, structured trace persistence, and a low-confidence review queue

The concrete implementation plan lives in [`LLOps Planning.md`](LLOps%20Planning.md).

## Demo Video

[![Watch MenuLens demo](docs/menulens-0225-ezgif.gif)](https://example.com/menulens-demo)

## Repo Structure

- `android/` Android application
- `backend/` FastAPI service
- `docs/` work logs, quality checklist, next agenda

## Prerequisites

### Android

- JDK 17 required for Gradle builds
- Android SDK + device/emulator

PowerShell (current terminal session):

```powershell
$env:JAVA_HOME="C:\Users\tsai1\.jdks\temurin-17.0.18"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

Do not commit `org.gradle.java.home` into `android/gradle.properties` (machine-specific).

### Backend

- Python 3.12 recommended (Python 3.14 caused dependency build issues)

## Backend Setup

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

Set required keys in `backend/.env`:

- `GOOGLE_CLOUD_VISION_API_KEY`
- `GEMINI_API_KEY`

Optional:

- `GEMINI_MODEL` (default `gemini-2.5-flash`)
- `OCR_PIPELINE_MODE=hybrid|vision_only` (default `hybrid`)
- `MAX_MENU_ITEMS` (default `10`, valid `1..20`)
- `ENABLE_IMAGE_SEARCH=true|false` (default `true`)
- `IMAGE_SEARCH_PROVIDER=none|cse|vertex`

If `IMAGE_SEARCH_PROVIDER=cse`:
- `GOOGLE_CSE_API_KEY`
- `GOOGLE_CSE_CX`

If `IMAGE_SEARCH_PROVIDER=vertex`:
- `GCP_PROJECT_ID`
- `VERTEX_SEARCH_LOCATION` (usually `global`)
- `VERTEX_SEARCH_APP_ID`

Vertex auth (required when using `vertex`):
- Service-account JSON: set `GOOGLE_APPLICATION_CREDENTIALS` to key file path, or
- Local ADC login: `gcloud auth application-default login`

Run backend:

```powershell
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

API docs:

- `http://127.0.0.1:8000/docs`

## Cloud Deployment (GCP Cloud Run)

This project is deployed on Cloud Run in Tokyo region (`asia-northeast1`).

### 1) Enable required GCP APIs

- Cloud Run Admin API
- Cloud Build API
- Artifact Registry API

### 2) Container files

Required files in `backend/`:
- `Dockerfile`
- `.dockerignore`

### 3) Build and push image

From repo root:

```bash
gcloud config set project menulens-487512
gcloud builds submit backend --tag asia-northeast1-docker.pkg.dev/menulens-487512/menulens-backend/api:latest
```

### 4) Deploy to Cloud Run

```bash
gcloud run deploy menulens-api \
  --image asia-northeast1-docker.pkg.dev/menulens-487512/menulens-backend/api:latest \
  --platform managed \
  --region asia-northeast1 \
  --allow-unauthenticated \
  --min-instances 0 \
  --set-env-vars GOOGLE_CLOUD_VISION_API_KEY=YOUR_VISION_KEY,GEMINI_API_KEY=YOUR_GEMINI_KEY,GEMINI_MODEL=gemini-2.5-flash,OCR_PIPELINE_MODE=hybrid,MAX_MENU_ITEMS=10,ENABLE_IMAGE_SEARCH=true,IMAGE_SEARCH_PROVIDER=vertex,GCP_PROJECT_ID=menulens-487512,VERTEX_SEARCH_LOCATION=global,VERTEX_SEARCH_APP_ID=YOUR_VERTEX_APP_ID
```

Get deployed URL:

```bash
gcloud run services describe menulens-api --region asia-northeast1 --format='value(status.url)'
```

### 5) Vertex IAM for image retrieval

Grant Cloud Run runtime service account:
- `roles/discoveryengine.viewer`
- `roles/discoveryengine.user` (if needed)

### 6) Validate deployment

- Swagger UI: `https://<cloud-run-url>/docs`
- Functional endpoint: `POST /v1/scan_menu`
- Note: `GET /` currently returns `Not Found` (expected, no root route defined)

### 7) Update Android backend URL

Set `API_BASE_URL` in `android/app/build.gradle.kts` to:
- `https://<cloud-run-url>/`

## Android Setup

Install app:

```powershell
cd android
.\gradlew installDebug
```

Backend base URL is configured in:

- `android/app/build.gradle.kts` (`API_BASE_URL`)

Use:

- Emulator: `http://10.0.2.2:8000/`
- Physical phone: `http://<your-pc-lan-ip>:8000/`
- Cloud Run deployed API: `https://menulens-api-gpw37tchwq-an.a.run.app/`

## Operations and Cost Management (GCP)

- Cloud Run with `min-instances=0` scales to zero when idle.
- Main variable costs are API usage (Vision/Gemini/Vertex), not just Cloud Run compute.
- Recommended controls:
  - GCP Billing budget + alert thresholds
  - Cloud Run max instances cap
  - Cloud Run logs and metrics monitoring
  - Secret Manager migration for API keys
