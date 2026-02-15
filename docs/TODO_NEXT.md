# TODO - Next Iteration

## Goal A: Unblock image retrieval in production path
1. Resolve Google Custom Search 403
- Verify billing + API enablement + key restriction propagation for `Custom Search API`.
- Validate `GOOGLE_CSE_API_KEY` + `GOOGLE_CSE_CX` with direct API test.
- Remove temporary CSE failure dependency on manual troubleshooting.

2. Keep scans functional while CSE is unstable
- Keep `ENABLE_IMAGE_SEARCH` flag behavior documented and tested.
- Add explicit "images unavailable" fallback UI in Detail when image list is empty.

3. Add stronger backend observability
- Log per-stage timing and status (Vision, Gemini, CSE) without logging raw images.
- Add structured error codes in API response for easier mobile debugging.

## Goal B: Improve end-to-end reliability on real phones
1. Networking hardening
- Replace hardcoded `API_BASE_URL` with build flavor/env-driven config.
- Add clear "cannot reach backend" diagnosis text for LAN/firewall mismatch cases.
- Add retry/backoff for transient upstream failures.

2. Image upload performance
- Resize/compress captured images before upload to reduce timeout risk.
- Add max upload size guard and user-facing error when image is too large.

3. Processing UX
- Show phase hints: "Reading text", "Translating", "Finding images".
- Add cancel action during long scans.

## Goal C: Feature completion for current product intent
1. Render real images in detail screen
- Replace placeholders with Coil image cards from backend URLs.
- Add tap-to-expand and graceful fallback when URL fails.

2. Data/contract tightening
- Add response model tests for malformed Gemini output.
- Add input/output validation for minimum required fields per item.

3. Localization and copy pass
- Keep English-first experience.
- Ensure Japanese fallback text renders correctly (UTF-8 sanity checks).

## Goal D: Test and release hygiene
1. Automated tests
- Add backend unit tests for parsing/fallback behavior.
- Add Android integration tests for:
  - scan success flow,
  - timeout/retry flow,
  - empty-image fallback rendering.

2. CI updates
- Add backend lint/test job to GitHub Actions.
- Add API smoke test step (mocked external services).

## Definition of Done for next cycle
- Phone scan returns stable English text results without timeout regressions.
- Image section either renders real photos or a clean fallback state.
- CSE failure no longer blocks user flow and is diagnosable from logs.
- Backend + Android test coverage added for critical scan paths.
