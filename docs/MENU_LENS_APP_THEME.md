# MenuLens App Theme Guide (Current UI Baseline)

This document captures the current visual language implemented in the app today.
Use this as the reference when creating new screens so UI stays consistent.

---

## 1) Overall Visual Direction

- Friendly, soft, travel-assistant style.
- Light mint-to-cream feeling background.
- Rounded cards with soft borders and subtle elevation.
- Content-first hierarchy with large, readable titles.
- Decorative sakura clusters are subtle and never compete with primary content.

---

## 2) Screen Structure Pattern

Most screens follow this order:

1. Plain text header at top (no heavy header card).
2. Main content card(s) in the center area.
3. Optional utility CTA near lower content.
4. Optional decorative elements in empty zones only.

Rules:

- Keep the header simple: text, no large boxed banner unless needed.
- Keep primary user action within thumb-reachable center/lower-middle area.
- Avoid large dead spaces; use subtle decoration only when needed.

---

## 3) Header Style

Preferred header format:

- Line 1: App/screen title (large, bold).
- Line 2: Context subtitle.
- Optional line 3: helper sentence.

Example:

- `MenuLens`
- `Understand Japanese menus`
- `Take a photo or upload a menu image to get English dish explanations.`

Rules:

- No rounded header block by default.
- Keep top placement consistent with other screens.
- Do not over-style headers with extra borders or heavy backgrounds.

---

## 4) Card System

Card shape:

- Large rounded corners (`~24dp`).
- Soft border (`~1-2dp`) with tinted accent.
- Light elevation (`~5-6dp`).

Card content spacing:

- Internal padding around `18-24dp`.
- Vertical content spacing around `8-14dp`.

Typography inside cards:

- English primary title is strongest.
- Japanese line is prominent but smaller than English title.
- Body/help text uses softer contrast.

---

## 5) Button Hierarchy

- Primary action: green filled button.
- Secondary action: blue filled button (existing app style).
- Utility CTA (`Show to staff`): amber/yellow filled, full width, larger and bold.

Rules:

- Minimum comfortable height around `56-60dp`.
- Full-width for critical actions.
- Keep button text bold for important CTAs.

---

## 6) Decorative Sakura Layer

Use sakura clusters as low-contrast background accents.

Rules:

- Place in empty areas only.
- Keep alpha subtle (`~0.09 - 0.16`).
- Vary sizes (small/medium) for natural feel.
- Avoid overlap with key text, buttons, or major circular background blobs.
- Can remain visible even when lists are long, but always behind content.

Implementation pattern:

- Add a full-screen `Box`.
- Render `SakuraCluster(...)` before main content.
- Anchor clusters with `align(...)`, `padding(...)`, `offset(...)`.

---

## 7) Content Density Guidance

If a screen feels empty:

- Increase meaningful info first (step text, hints, status).
- Group related UI into one unified card.
- Use subtle decoration second.
- Avoid adding random large ornaments near key actions.

---

## 8) Screen-Specific Notes from Today

### Scan

- Simple top text header (no header card).
- Keep helper wording exactly:
  - `Take a photo or upload a menu image to get English dish explanations.`
- Keep hero CTA card and carousel.
- Add subtle lower-area sakura clusters to reduce empty feel.

### Processing

- 3-zone layout: top context, middle process card, bottom notice.
- Rotating step messages:
  - `Detecting text...`
  - `Translating items...`
  - `Generating explanations...`
- Keep text transitions smooth and centered (no horizontal drift).

### Results

- Plain text heading instead of header card.
- English dish name larger than Japanese.
- Sakura background can remain visible with sparse or dense lists.

### Detail

- Plain top title (`Item Detail`) without boxed header.
- English dish name high contrast and larger.
- Japanese line bigger/thicker than body, but below English title.
- `Show to staff` button is full-width, larger, and bold.
- Add separation above `Show to staff`.

### Show to Staff

- Center both language cards and illustration consistently.
- Keep Japanese card first, English card second.
- Subtle sakura decoration in background.

---

## 9) Asset Guidance for Future Decorations

Recommended formats:

- `webp` (preferred for drawables).
- `png` with transparency if needed.

Asset style:

- Transparent background.
- Soft edges, low visual noise.
- Pastel tones that blend with mint/cream surfaces.

---

## 10) Implementation Checklist for New Screens

- Use plain header text style first.
- Use rounded cards with consistent padding/border/elevation.
- Keep primary action clear and full-width where appropriate.
- Ensure English/Japanese hierarchy is intentional.
- Add sakura only if layout feels visually empty.
- Validate on device for spacing near system bars.

