package my.edu.utar.freshtrackai.logic

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * ExpiryCalculator.kt
 * Member 3 — Expiry Logic
 *
 * Core business logic for:
 * - Calculating days remaining until expiry
 * - Estimating expiry date when no manufacturer date is provided
 * - Determining expiry status (FRESH / WATCH / CRITICAL / EXPIRED)
 *
 * Replaces Ian's mock functions in DashboardData.kt:
 *   - urgencyForDays(days: Int)
 *   - estimateExpiresInDays(...)
 */
object ExpiryCalculator {

    // ─────────────────────────────────────────────────────────────
    // Expiry Status Model
    // ─────────────────────────────────────────────────────────────

    enum class ExpiryStatus {
        FRESH,      // More than 7 days remaining
        WATCH,      // 4–7 days remaining
        CRITICAL,   // 1–3 days remaining
        EXPIRED     // 0 or negative days
    }

    data class ExpiryResult(
        val daysRemaining: Long,
        val status: ExpiryStatus,
        val expiryDate: LocalDate
    )

    // ─────────────────────────────────────────────────────────────
    // Thresholds (easily adjustable)
    // ─────────────────────────────────────────────────────────────
    private const val CRITICAL_THRESHOLD = 3L
    private const val WATCH_THRESHOLD    = 7L

    // ─────────────────────────────────────────────────────────────
    // Supported date formats
    //
    // BUG FIX EXPLANATION:
    // The UI placeholder shows "Oct 24, 2026" → format is "MMM d, yyyy"
    // The old ExpiryCalculator only had "d MMM yyyy" (e.g. "24 Oct 2026")
    // which is the REVERSED order. That's why "Apr 16, 2026" gave wrong results —
    // it was falling through ALL formats and returning the 14-day fallback,
    // which happened to show as ~24 days (because today + 14 ≠ Apr 16).
    // ─────────────────────────────────────────────────────────────
    private val DATE_FORMATS = listOf(
        DateTimeFormatter.ofPattern("MMM d, yyyy"),    // "Apr 16, 2026"  ← UI format (WAS MISSING)
        DateTimeFormatter.ofPattern("MMMM d, yyyy"),   // "April 16, 2026"
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),     // "2026-04-16"
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),     // "16/04/2026"
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),     // "04/16/2026"
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),     // "16-04-2026"
        DateTimeFormatter.ofPattern("d MMM yyyy"),     // "16 Apr 2026"
        DateTimeFormatter.ofPattern("d MMMM yyyy"),    // "16 April 2026"
    )

    // ─────────────────────────────────────────────────────────────
    // Core Calculations
    // ─────────────────────────────────────────────────────────────

    /**
     * Calculates how many days are left until [expiryDate].
     * Returns negative if already expired.
     */
    fun daysUntilExpiry(expiryDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
    }

    /**
     * Estimates the expiry date based on [dateAdded] and the item's [category].
     */
    fun estimateExpiryDate(
        dateAdded: LocalDate,
        category: ShelfLifeRules.FoodCategory
    ): LocalDate {
        val shelfDays = ShelfLifeRules.getShelfLifeDays(category)
        return dateAdded.plusDays(shelfDays.toLong())
    }

    /**
     * Estimates the expiry date based on [dateAdded] and the food [itemName].
     * Auto-detects category from item name via ShelfLifeRules.
     */
    fun estimateExpiryDateByName(dateAdded: LocalDate, itemName: String): LocalDate {
        val shelfDays = ShelfLifeRules.getShelfLifeByName(itemName)
        return dateAdded.plusDays(shelfDays.toLong())
    }

    /**
     * Determines ExpiryStatus from a raw [daysRemaining] value.
     * Drop-in replacement for Ian's urgencyForDays() mock.
     */
    fun getExpiryStatus(daysRemaining: Long): ExpiryStatus = when {
        daysRemaining <= 0                  -> ExpiryStatus.EXPIRED
        daysRemaining <= CRITICAL_THRESHOLD -> ExpiryStatus.CRITICAL
        daysRemaining <= WATCH_THRESHOLD    -> ExpiryStatus.WATCH
        else                                -> ExpiryStatus.FRESH
    }

    /**
     * Full calculation: given an expiry date, returns ExpiryResult.
     */
    fun calculate(expiryDate: LocalDate): ExpiryResult {
        val days   = daysUntilExpiry(expiryDate)
        val status = getExpiryStatus(days)
        return ExpiryResult(daysRemaining = days, status = status, expiryDate = expiryDate)
    }

    /**
     * Full calculation for items WITHOUT a manufacturer expiry date.
     */
    fun calculateByName(dateAdded: LocalDate, itemName: String): ExpiryResult {
        val expiryDate = estimateExpiryDateByName(dateAdded, itemName)
        return calculate(expiryDate)
    }

    // ─────────────────────────────────────────────────────────────
    // Dashboard Integration — drop-in replacements for DashboardData.kt
    // ─────────────────────────────────────────────────────────────

    /**
     * Drop-in replacement for Ian's urgencyForDays(days: Int): String
     */
    fun urgencyLabel(daysRemaining: Long): String = when (getExpiryStatus(daysRemaining)) {
        ExpiryStatus.EXPIRED  -> "Expired"
        ExpiryStatus.CRITICAL -> "Critical"
        ExpiryStatus.WATCH    -> "Watch"
        ExpiryStatus.FRESH    -> "Fresh"
    }

    /**
     * Drop-in replacement for Ian's estimateExpiresInDays(...): Int
     *
     * BUG FIX: Now correctly parses "Apr 16, 2026" (the format the UI uses).
     * The old code was missing "MMM d, yyyy" causing it to fall back to 14 days
     * instead of computing the real date difference.
     *
     * @param expiryDateString the raw string the user typed, e.g. "Apr 16, 2026"
     * @return days from TODAY until that date (negative = already expired)
     */
    fun estimateExpiresInDays(expiryDateString: String?): Long {
        if (expiryDateString.isNullOrBlank()) return 14L

        val cleaned = expiryDateString.trim()

        for (formatter in DATE_FORMATS) {
            try {
                val date = LocalDate.parse(cleaned, formatter)
                return daysUntilExpiry(date)
            } catch (e: DateTimeParseException) {
                // try next format
            }
        }

        // Last resort: ISO default
        try {
            return daysUntilExpiry(LocalDate.parse(cleaned))
        } catch (e: Exception) { /* nothing matched */ }

        return 14L // safe fallback
    }

    /**
     * Parses a date string into a LocalDate, or returns null if unparseable.
     * Useful when you need the actual LocalDate object (e.g. for WorkManager checks).
     */
    fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null
        val cleaned = dateString.trim()
        for (formatter in DATE_FORMATS) {
            try {
                return LocalDate.parse(cleaned, formatter)
            } catch (e: DateTimeParseException) { /* try next */ }
        }
        return null
    }

    // ─────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────

    /**
     * Human-readable display string for UI.
     * e.g. "Expires in 3 days", "Expired 2 days ago", "Expires today!"
     */
    fun expiryDisplayText(daysRemaining: Long): String = when {
        daysRemaining < 0   -> "Expired ${-daysRemaining} day(s) ago"
        daysRemaining == 0L -> "Expires today!"
        daysRemaining == 1L -> "Expires tomorrow!"
        else                -> "Expires in $daysRemaining days"
    }
}