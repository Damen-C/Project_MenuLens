# MenuLens Scaffold (Step 1)

This repository now includes:
- `android/` Kotlin + Compose app skeleton with MVVM-ready structure and route placeholders.
- `backend/` FastAPI skeleton with `POST /v1/scan_menu` hardcoded contract response.
- `.github/workflows/android-quality.yml` CI workflow for Android lint + unit tests.
- `docs/QUALITY_CHECKLIST.md` release-quality checklist and test commands.

## Android Build Prerequisite

- Use JDK 17 for local Android/Gradle builds.
- Do not commit `org.gradle.java.home` in `android/gradle.properties` because it is machine-specific.

PowerShell (current terminal session):

```powershell
$env:JAVA_HOME="C:\Users\tsai1\.jdks\temurin-17.0.18"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```
