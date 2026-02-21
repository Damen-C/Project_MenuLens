# MenuLens Premium UI Theme Spec (Codex‑Ready)

**File purpose:** Single source of truth for a coherent, premium UI across all MenuLens screens.  
**Target stack:** Android • Jetpack Compose • Material 3  
**Goal:** Neutral, modern, “premium” look (less pastel-heavy) with consistent tokens + reusable components.

---

## 0) Principles

1. **Neutral surfaces first** (no tinted full-screen backgrounds).
2. **One brand accent** (MenuLens Green) used sparingly.
3. **Consistent hierarchy** (typography + spacing).
4. **Reusable components** over per-screen custom styling.
5. **Accessible by default** (contrast, touch targets, readable line lengths).

---

## 1) Design Tokens

### 1.1 Color Palette (Light)

**Brand**
- `BrandPrimary` (MenuLens Green): `#1DB954`
- `BrandPrimaryContainer`: `#D7FBE6`
- `OnBrandPrimary`: `#FFFFFF`

**Neutrals**
- `Bg`: `#F7F8FA` (app background)
- `Surface`: `#FFFFFF` (cards/sheets)
- `SurfaceVariant`: `#F1F3F6` (chips/sub-panels)
- `OnSurface`: `#0F172A` (primary text)
- `OnSurfaceMuted`: `#475569` (secondary text)
- `Outline`: `#E2E8F0` (borders/dividers)

**Semantic**
- `Success`: `#16A34A`
- `Warning` (only for “Show to staff” primary CTA): `#F59E0B`
- `Error`: `#DC2626`

**Overlays**
- `Scrim`: `#000000` @ 60% alpha for dialogs/image popup backdrop

**Rules**
- Background must be `Bg` on all screens (no green/pastel full-screen backgrounds).
- Prefer **subtle border OR subtle elevation** for cards—pick one and keep consistent.
- `Warning` is only for the special utility CTA (“Show to staff”).

---

### 1.2 Typography

Use Material 3 typography as baseline; apply these semantic roles:

- **Screen title:** `TitleLarge` (22–24sp), weight **700**
- **Card title / list item title:** `TitleMedium` (18–20sp), weight **700**
- **Body:** `BodyMedium` (14–16sp), weight **400**
- **Secondary:** `BodySmall` (12–13sp), weight **400**, color `OnSurfaceMuted`
- **Japanese line:** same size as Secondary, weight **500** for legibility

**Rules**
- Avoid ALL CAPS.
- Keep paragraph line length comfortable (use padding; don’t stretch edge-to-edge).

---

### 1.3 Spacing (8dp system)

- `xs = 4dp`
- `sm = 8dp`
- `md = 16dp`
- `lg = 24dp`
- `xl = 32dp`

**Rules**
- Screen padding: **16dp**
- Card internal padding: **16dp**
- Section spacing: **24dp**
- List item vertical padding: **12–16dp**

---

### 1.4 Shape & Elevation

- Card radius: **20dp**
- Button radius: **14dp**
- Dialog / sheet radius: **24dp**
- Image radius inside card: **16dp**

Elevation (if used):
- Card: **1–2dp**
- Floating CTA: **3dp**
- Dialog: **6dp**

---

### 1.5 Illustration & Decoration Rules

- Keep your cute illustrations, but: **one hero illustration max per screen**.
- **No floating decorative circles** in the background.
- Avoid mixing too many sticker elements on one screen.

---

## 2) Global Layout Rules

### 2.1 App Bar Rule
Every screen uses a consistent top bar:
- Title left aligned
- Optional subtitle under title
- No “big rounded header card” blocks

### 2.2 Card Rule
Cards are always:
- Background `Surface`
- Radius 20dp
- Internal padding 16dp
- Border `Outline` (1dp) **or** subtle elevation (choose one consistently)

### 2.3 Button Hierarchy Rule
- **Primary:** Filled with `BrandPrimary` (except “Show to staff” uses `Warning`)
- **Secondary:** Outlined with `Outline`
- **Tertiary:** Text button (minimal)
- Touch target min: **48dp height**

### 2.4 Results Density Rule
Results should feel “content-rich”:
- Consistent row height
- Include thumbnail placeholder even if no image
- Strong title + muted Japanese subtitle

---

## 3) Compose Component Library (Must Implement)

Create these composables and use them across ALL screens.

1. `MenuLensScaffold(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}, content: @Composable () -> Unit)`
2. `MenuLensCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)`
3. `PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier)`
4. `SecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier)`
5. `MenuItemRow(titleEn: String, titleJa: String, thumbnailUrl: String? = null, onClick: () -> Unit, modifier: Modifier = Modifier)`
6. `LoadingStateCard(stepText: String, modifier: Modifier = Modifier, progress: Float? = null)`
7. `EmptyState(title: String, body: String, modifier: Modifier = Modifier, illustrationRes: Int? = null)`
8. `ImageDialog(imageUrlOrRes: String, onDismiss: () -> Unit)`  
   - Must use scrim overlay (60% black)
   - Rounded corners 24dp

**Implementation notes**
- Put theme + tokens in a `designsystem/` package.
- Provide preview composables for key components.

---

## 4) Screen Mapping (How Each Screen Should Look)

### 4.1 Scan Screen
- App bar: **MenuLens** + subtitle “Understand Japanese menus”
- Content:
  - One hero illustration
  - Primary CTA: “Scan menu” (filled `BrandPrimary`)
  - Secondary CTA: “Upload photo” (outlined)
  - Optional muted helper text

### 4.2 Processing Screen
- App bar: “Reading menu”
- Center:
  - `LoadingStateCard` with rotating step text:
    - “Detecting text…”
    - “Translating items…”
    - “Generating descriptions…”
  - Optional hero illustration below
- Avoid empty vertical space; keep centered.

### 4.3 Results Screen
- App bar: “Scan results” + subtitle “Tap an item to see details”
- Body: list of `MenuItemRow`
  - Left: circular thumbnail placeholder
  - Right: English title bold + Japanese muted
- If zero items: `EmptyState`

### 4.4 Detail Screen
- App bar: “Item detail”
- Content:
  - Card with title, description, Japanese line, image (rounded 16dp)
- Primary CTA: **“Show to staff”** (filled `Warning`)
- Navigation uses app bar back; do not add a big “Back” button.

### 4.5 Image Popup / Dialog
- Use `ImageDialog`
- Scrim 60%
- Close icon or text action, not a big green bar.

### 4.6 Show to Staff Screen
- App bar: “Show to staff”
- Two stacked cards:
  - Japanese: larger, bolder
  - English: normal body
- Add hint: “Please show this to a staff member.”

---

## 5) Acceptance Criteria (Definition of Done)

- All screens background is `Bg` (`#F7F8FA`).
- No rounded header cards; all titles use app bar.
- Cards share identical radius/padding and border/elevation style.
- Button hierarchy is consistent:
  - Primary filled (green),
  - Secondary outlined,
  - Only “Show to staff” uses amber.
- Results list uses `MenuItemRow` with thumbnail placeholder.
- Dialog uses scrim and premium rounded styling.

---

## 6) Codex Prompts (Copy/Paste)

### 6.1 One-time setup prompt (theme + components)
```text
Implement MENU_LENS_UI_THEME.md in this Android Jetpack Compose project (Material3):
1) Create designsystem package with tokens and MenuLensTheme (lightColorScheme).
2) Implement the shared components: MenuLensScaffold, MenuLensCard, PrimaryButton, SecondaryButton, MenuItemRow, LoadingStateCard, EmptyState, ImageDialog.
3) Replace ad-hoc colors/spacing with tokens.
Output: list of created/modified files + brief notes.
```

### 6.2 Refactor a single screen prompt (repeat per screen)
```text
Load MENU_LENS_UI_THEME.md as the source of truth.
Refactor <SCREEN_NAME>.kt to use MenuLensTheme + shared components only.
Do not introduce new styling constants in the screen unless necessary; put them into the designsystem tokens instead.
Ensure acceptance criteria are met for this screen.
Output: code changes + brief before/after notes.
```

### 6.3 Full app refactor prompt (all screens)
```text
Load MENU_LENS_UI_THEME.md as the source of truth.
Refactor these screens to match the spec exactly:
- ScanScreen
- ProcessingScreen
- ResultsScreen
- DetailScreen
- ShowToStaffScreen
- Image popup/dialog
Use the shared components everywhere.
Output:
- File list
- Summary of changes per screen
- Any TODOs if a refactor requires additional assets.
```

---

## 7) Recommended File Placement

- Put this spec at project root: `MENU_LENS_UI_THEME.md`
- Optional: keep additional design docs under `/docs/`

---

## 8) Future Extension (Optional)

- Dark theme tokens
- Localization typography rules (JA line height, font fallback)
- Motion spec (durations, easing)
- Component states (loading/disabled/error)

