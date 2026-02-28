# MenuLens Request Flow (1-Page)

This diagram shows the end-to-end runtime flow from Android capture to backend processing and back to app rendering.

```mermaid
flowchart TD
    A[Android App\nJetpack Compose UI\nScanScreen] --> B[Build multipart request\nimage + target_lang + device metadata]
    B --> C[POST /v1/scan_menu\nFastAPI backend]

    subgraph Backend[FastAPI Pipeline]
      C --> D[Validate request + env\nprovider/mode config]
      D --> E[Google Cloud Vision API\nOCR text extraction]
      E --> F{OCR_PIPELINE_MODE}

      F -->|hybrid| G[Gemini normalize OCR text]
      F -->|vision_only| H[Use raw Vision OCR text]

      G --> I[Gemini parse menu items\nJP text + EN title/desc + tags + image_query]
      H --> I

      I --> J{IMAGE_SEARCH_PROVIDER}
      J -->|vertex| K[Vertex AI Search\nretrieve dish image URLs]
      J -->|cse| L[Google CSE\nretrieve image URLs]
      J -->|none| M[Skip image lookup]

      K --> N[Assemble ScanMenuResponse\nitems + diagnostics]
      L --> N
      M --> N
    end

    N --> O[HTTP JSON response\nscan_id + detected_type + items]
    O --> P[Android Repository / ViewModel]
    P --> Q[Compose navigation + render\nProcessing -> Results -> Detail]

    R[(Cloud Run on GCP)] -. hosts .-> C
```

## Notes

- Android sends the captured menu image as multipart form data to `v1/scan_menu`.
- FastAPI orchestrates OCR, optional normalization, parsing, and optional image search.
- Provider selection controls image lookup strategy (`none | cse | vertex`).
- The final JSON response is displayed in Android `Results` and `Detail` screens.
