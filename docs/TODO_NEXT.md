# TODO - Next Iteration

## Goal A: UI polish (keep PRD behavior unchanged)
1. Build a reusable design system layer
- Create common components: `PrimaryCtaButton`, `SecondaryCtaButton`, `StatusChip`, `LockedCard`.
- Centralize spacing/shape constants to avoid per-screen drift.

2. Improve visual consistency across screens
- Harmonize card/header/button sizes and shadow depth.
- Add lightweight motion:
  - screen enter transitions,
  - button press feedback,
  - processing skeleton shimmer.

3. Upgrade result/detail clarity
- Results:
  - clearer lock icon treatment,
  - free vs gated section separation.
- Detail:
  - stronger locked placeholders,
  - more obvious reveal CTA states.

4. Add iconography and illustration assets
- Introduce small in-app icon set for scan/result/lock/pro states.
- Add one mascot/illustration per major screen (optimized PNG/WebP).

## Goal B: Real English translation + backend integration
1. Wire Android to real backend scan endpoint
- Implement Retrofit call for `POST /v1/scan_menu` multipart.
- Send required fields:
  - `image`,
  - `target_lang="en"`,
  - `device_id`,
  - `app_version`,
  - `timezone="Asia/Tokyo"`.

2. Replace hardcoded items with API response mapping
- Create DTOs matching frozen contract exactly.
- Map DTO -> domain model used by `ResultsViewModel`.
- Handle loading/error/empty states on `Processing` and `Results`.

3. Implement backend translation/explanation pipeline
- Keep endpoint: `POST /v1/scan_menu`.
- Add/complete modules:
  - OCR stub/parser,
  - title translation,
  - Gemini dish explanation (`google-genai`),
  - restaurant type detection,
  - image retrieval stub.
- Enforce `en_description` constraints:
  - 1-2 sentences,
  - cautious language,
  - tourist-friendly,
  - fallback text on failure.

4. Security/config correctness
- Read `GEMINI_API_KEY` and optional `GEMINI_MODEL` via env vars.
- Ensure image is processed-and-discarded (no storage/logging raw image).
- Add `MAX_ITEMS` support.

## Goal C: Tests to add immediately
1. Unit tests
- Reveal decrement when not Pro.
- No decrement when already unlocked.
- Paywall when 0 credits and not Pro.
- Tokyo-day reset behavior.

2. Integration/UI tests
- Scan flow: camera/gallery path -> processing -> results.
- Locked item shows no English text before reveal.
- Show-to-staff screen never exposes gated English content.

## Definition of Done for next cycle
- Real backend scan call returns and renders live data.
- PRD gating remains intact with passing tests.
- UI polish applied consistently without regressions.
