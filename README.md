# MenuLens

MenuLens is an Android app for scanning Japanese menus and showing English-friendly dish results.

## Current Status

- Android app: Kotlin + Compose + MVVM flow (`Scan -> Processing -> Results -> Detail`)
- Backend API: FastAPI `POST /v1/scan_menu`
- Live pipeline implemented:
  - OCR: Google Cloud Vision API
  - Dish extraction/translation/description: Gemini
  - Image retrieval: provider-based (`none | cse | vertex`)
  - Current recommended provider: Vertex AI Search
- CI: Android lint + unit tests + debug assemble on GitHub Actions

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

- `GEMINI_MODEL` (default `gemini-1.5-flash`)
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

## Known Issue (Current)

- Custom Search JSON API has onboarding limitations for new users/projects.
- Some retrieved image URLs can still be source-dependent and may fail on-device.
- Fallback behavior is implemented: scan continues with OCR/English text even if image retrieval fails.

## Docs

- `docs/WORKLOG_2026-02-15.md`
- `docs/WORKLOG_2026-02-16.md`
- `docs/WORKLOG_2026-02-17.md`
- `docs/QUALITY_CHECKLIST.md`
