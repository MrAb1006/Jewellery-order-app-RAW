package com.example.ui

// ── Data Models ──────────────────────────────────────────────────────────────

enum class Metal { GOLD, SILVER }

data class CalcFields(
    val grossWt: String = "",
    val purity: String = "",       // in %, e.g. 91.6 for 22K gold
    val fineWt: String = "",       // always auto-derived, never user-entered
    val ratePerGram: String = "",
    val totalPrice: String = ""
)

data class UserSet(
    val grossWt: Boolean = false,
    val purity: Boolean = false,
    val ratePerGram: Boolean = false,
    val totalPrice: Boolean = false
    // fineWt intentionally excluded — it is always derived
)

data class CalcState(
    val metal: Metal = Metal.GOLD,
    val fields: CalcFields = CalcFields(),
    val userSet: UserSet = UserSet(),
    val lastEdited: String? = null
)

// ── Helpers ───────────────────────────────────────────────────────────────────

fun fmt(v: Double, decimals: Int = 4): String {
    if (v.isNaN() || v.isInfinite()) return ""
    return "%.${decimals}f".format(v).trimEnd('0').trimEnd('.')
}

fun Double.ok() = !isNaN() && !isInfinite() && this > 0

// ── Bidirectional Calculation Engine ─────────────────────────────────────────
//
//  Core formulae:
//    fineWt      = grossWt * purity / 100
//    totalPrice  = fineWt  * ratePerGram
//
//  Back-calculations:
//    purity      = (totalPrice / ratePerGram / grossWt) * 100
//    grossWt     = (totalPrice / ratePerGram) / (purity / 100)
//    ratePerGram = totalPrice / fineWt
//
//  KEY DESIGN: `userSet` tracks which fields the user TYPED vs auto-filled.
//  This prevents stale auto-values from polluting back-calculations.

fun recalc(
    name: String,
    value: String,
    fields: CalcFields,
    userSet: UserSet
): Pair<CalcFields, UserSet> {

    // Apply the changed field
    var s = when (name) {
        "grossWt"     -> fields.copy(grossWt     = value)
        "purity"      -> fields.copy(purity       = value)
        "ratePerGram" -> fields.copy(ratePerGram  = value)
        "totalPrice"  -> fields.copy(totalPrice   = value)
        else          -> fields
    }
    var us = when (name) {
        "grossWt"     -> userSet.copy(grossWt     = value.isNotEmpty())
        "purity"      -> userSet.copy(purity       = value.isNotEmpty())
        "ratePerGram" -> userSet.copy(ratePerGram  = value.isNotEmpty())
        "totalPrice"  -> userSet.copy(totalPrice   = value.isNotEmpty())
        else          -> userSet
    }

    val gw = s.grossWt.toDoubleOrNull()     ?: Double.NaN
    val pu = s.purity.toDoubleOrNull()      ?: Double.NaN
    val rg = s.ratePerGram.toDoubleOrNull() ?: Double.NaN
    val tp = s.totalPrice.toDoubleOrNull()  ?: Double.NaN

    // Always reset fineWt — recompute below
    s = s.copy(fineWt = "")

    when (name) {

        "grossWt" -> {
            if (us.purity && gw.ok() && pu.ok()) {
                val fw = gw * pu / 100
                s = s.copy(fineWt = fmt(fw))
                when {
                    us.ratePerGram && rg.ok() -> { s = s.copy(totalPrice   = fmt(fw * rg, 2)); us = us.copy(totalPrice   = false) }
                    us.totalPrice  && tp.ok() -> { s = s.copy(ratePerGram  = fmt(tp / fw, 2)); us = us.copy(ratePerGram  = false) }
                }
            } else if (us.totalPrice && us.ratePerGram && tp.ok() && rg.ok()) {
                val fw = tp / rg
                s = s.copy(fineWt = fmt(fw))
                if (gw.ok()) { s = s.copy(purity = fmt(fw / gw * 100)); us = us.copy(purity = false) }
            }
        }

        "purity" -> {
            if (us.grossWt && gw.ok() && pu.ok()) {
                val fw = gw * pu / 100
                s = s.copy(fineWt = fmt(fw))
                when {
                    us.ratePerGram && rg.ok() -> { s = s.copy(totalPrice  = fmt(fw * rg, 2)); us = us.copy(totalPrice  = false) }
                    us.totalPrice  && tp.ok() -> { s = s.copy(ratePerGram = fmt(tp / fw, 2)); us = us.copy(ratePerGram = false) }
                }
            }
        }

        "ratePerGram" -> {
            if (us.totalPrice && tp.ok() && rg.ok()) {
                val fw = tp / rg
                s = s.copy(fineWt = fmt(fw))
                when {
                    us.grossWt && gw.ok() -> { s = s.copy(purity  = fmt(fw / gw * 100)); us = us.copy(purity  = false) }
                    us.purity  && pu.ok() -> { s = s.copy(grossWt = fmt(fw * 100 / pu)); us = us.copy(grossWt = false) }
                }
            } else if (us.grossWt && us.purity && gw.ok() && pu.ok() && rg.ok()) {
                val fw = gw * pu / 100
                s = s.copy(fineWt = fmt(fw), totalPrice = fmt(fw * rg, 2))
                us = us.copy(totalPrice = false)
            }
        }

        "totalPrice" -> {
            if (us.ratePerGram && rg.ok() && tp.ok()) {
                val fw = tp / rg
                s = s.copy(fineWt = fmt(fw))
                when {
                    us.grossWt && gw.ok() -> { s = s.copy(purity  = fmt(fw / gw * 100)); us = us.copy(purity  = false) }
                    us.purity  && pu.ok() -> { s = s.copy(grossWt = fmt(fw * 100 / pu)); us = us.copy(grossWt = false) }
                }
            } else if (us.grossWt && us.purity && gw.ok() && pu.ok() && tp.ok()) {
                val fw = gw * pu / 100
                s = s.copy(fineWt = fmt(fw))
                if (fw > 0) { s = s.copy(ratePerGram = fmt(tp / fw, 2)); us = us.copy(ratePerGram = false) }
            }
        }
    }

    return Pair(s, us)
}
