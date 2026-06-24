# Master Technical Specification: Aurelia Jewel Vault (Lumina Atelier)

This document is a 1:1 functional and visual blueprint for the **Aurelia Jewel Vault** (internally Suhas Jewellers). It is designed as a "Developer-to-AI" specification to allow an AI agent to recreate the application with zero additional input.

---

## 1. Project DNA

### Tech Stack
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (Material 3)
- **Minimum SDK**: 24 (Android 7.0)
- **Target/Compile SDK**: 36 (Android 15+)
- **Architecture**: MVVM (Model-View-ViewModel) with a Repository Pattern.
- **Database**: Room Persistence Library (Offline-first).
- **Gradle Plugins**: Android Application (9.1.1), Kotlin Compose, KSP, Secrets, Roborazzi.

### Critical Dependencies
- `androidx.room:room-runtime:2.7.0`: Local SQLite management.
- `androidx.lifecycle:lifecycle-runtime-compose:2.8.7`: Reactive UI state collection.
- `androidx.compose.material:material-icons-extended`: Luxury iconography (Diamond, Gavel, Payments).
- `org.json:json`: Manual serialization for complex nested list storage in Room.

---

## 2. Complete Data Blueprint

### Data Models (`com.example.data`)

#### 1. `Order` Entity (Primary Table)
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `Int` | Primary Key, auto-generated. |
| `customerName` | `String` | Customer display name. |
| `customerPhone` | `String` | Contact number for dialer integration. |
| `jewelleryType` | `String` | Comma-separated summary of ordered items. |
| `metalType` | `String` | Primary metal (Gold/Silver). |
| `purity` | `String` | Metal purity (e.g., "91.6", "22K"). |
| `approxWeight` | `Double` | Total aggregate weight in grams. |
| `agreedRate` | `Double` | Market rate per gram. |
| `makingCharges` | `Double` | Percentage (Gold) or Flat (Silver). |
| `otherCharges` | `Double` | Fixed accessory/stone charges. |
| `advancePaid` | `Double` | Cash deposit given in advance. |
| `totalAmount` | `Double` | Final billable amount after exchange credit. |
| `orderDate` | `Long` | Timestamp of registration. |
| `expectedDeliveryDate` | `Long` | Timestamp of target handover. |
| `status` | `String` | `Pending`, `In Progress`, `Completed`, `Delivered`. |
| `itemsJson` | `String` | Serialized `List<OrderItem>`. |
| `oldItemsJson` | `String` | Serialized `List<OldOrderItem>`. |

#### 2. `DeletedOrder` (Recycle Bin)
Identical schema to `Order` with an additional `deletedAt: Long` field for 60-day retention logic.

---

## 3. UI/UX Technical Anatomy: "Sophisticated Dark"

### Color Palette (Luxury Obsidian & Gold)
- **Background**: `0xFF0F0F0F` (Deep Charcoal Noir)
- **Surface/Card**: `0xFF161616` / `0xFF222222`
- **Primary (Gold)**: `0xFFD4AF37` (Metallic Gold)
- **Secondary (Gold)**: `0xFFF9E498` (Soft Champagne)
- **Tertiary (Muted Gold)**: `0xFFAA8A31`
- **Typography**: `SophisticatedWhiteMuted (0xFFD1D5DB)`

### Layout Hierarchy
1. **Interactive Scaffold**:
   - **FAB**: Golden `+` button for "Add Custom Order".
   - **Footer**: Signature seal: `"SUHAS JEWELLERS · v. 0.2.4"` with a Diamond icon.
2. **DashboardScreen**:
   - **MetricsPanel**: Fluid adaptive grid showing:
     - `Active`: Count of non-delivered orders.
     - `Advance`: Sum of `advancePaid`.
     - `Receivable`: Sum of `totalAmount - advancePaid`.
     - `Due Soon`: Warning icon if delivery is within 3 days.
   - **Control Bar**: Integrated Search (leading icon) and Sort Dropdown (trailing).
   - **Status Filter**: Horizontal scrollable tabs (All, Pending, etc.).
3. **AddEditOrderDialog**:
   - Multi-item management: Append/Remove `OrderItem` or `OldOrderItem` cards.
   - **Live Balancing Sheet**: A real-time calculated card showing Gold vs Silver fine weight reconciliation before saving.

---

## 4. Logic & Workflow Mapping

### Calculation Formulas
1. **New Item Value**: `(Weight * Rate * Purity%) + (Making Charge) + (Other Charges)`.
   - *Note*: Gold making is `Weight * Rate * %`. Silver making is a flat value.
2. **Old Item Credit**: `(OldWeight * OldPurity%) * Rate`.
3. **Net Payable**: `Sum(New Items) - Sum(Old Exchange Credits)`.

### Event Flows
- **Delete Order**: Moves `Order` to `DeletedOrder` table. UI triggers a Toast.
- **Backup/Restore**: `DashboardScreen` uses `ActivityResultContracts` to export/import JSON blobs of the entire order list.
- **Kacchi Pawti PDF**:
  - Generates a `595x842pt` canvas.
  - Draws customer metadata, itemized table, exchange credits, and signature lines.
  - Shares via `FileProvider` to WhatsApp/Gmail.

---

## 5. Edge-Case Handling
- **Retention**: App clean-up on launch deletes `DeletedOrder` entries older than 60 days.
- **Validation**: "Create Order" button disabled unless `customerName` is filled and `approxWeight` is `> 0`.
- **Offline Reliability**: All operations are local-first; Room Flows ensure UI stays synced with the database state automatically.

---

## Instruction for AI Builder:
*"Reconstruct an offline-first premium Jewelry Order app using the provided 'Sophisticated Dark' luxury theme and Room DB schema. Implement the 'Kacchi Pawti' PDF generation logic and the multi-item balancing sheet for Gold/Silver reconciliation exactly as mapped in this specification."*
