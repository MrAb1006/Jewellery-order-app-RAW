# PDF Pagination Context

## Current Implementation Analysis

### 1. Customer Order Section (`DashboardScreen.kt`)
- **Function:** `generateKacchiPawtiPdf`
- **Page Limit:** Strictly 1 page.
- **Logic:**
  - Uses `PdfDocument.PageInfo.Builder(595, 842, 1).create()` to start a single page.
  - Contains explicit `break` statements in loops (e.g., `if (y > 780f) break`) for both ordered items and exchanged old metal.
  - **Result:** If data exceeds the page height, it is simply omitted from the PDF.

### 2. Karigar Section (`KarigarDashboardScreen.kt`)
- **Function:** `generateKarigarBalancePdf`
- **Page Limit:** Partially dynamic but flawed.
- **Logic:**
  - Starts with 1 page.
  - In "Consolidated Item Specifications", it uses `return@forEach` if `y > 750`, which stops processing all remaining items.
  - In "Combined Transaction History", it attempts to call `pdfDocument.startPage`, but does not reset the `y` coordinate or update the `canvas` and `paint` references effectively for the new page.
  - **Result:** Likely leads to data loss or potential crashes/blank pages when attempting to span multiple pages.

## Requirements for Fix
- Replace fixed-page logic with a dynamic pagination system.
- Implement a mechanism to:
    - Detect when the `y` coordinate exceeds a threshold (e.g., 780f).
    - Finish the current page.
    - Start a new page.
    - Reset the `y` coordinate.
    - Carry over `canvas` and `paint` objects.
    - Re-draw table headers on subsequent pages.
- Ensure the footer/signature section is always on the final page.
