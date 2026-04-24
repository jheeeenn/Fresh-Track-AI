package my.edu.utar.freshtrackai.logic

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/**
 * ExpiryCalculator.kt
 * Member 3 — Expiry Logic
 *
 * Core business logic for:
 * - Calculating days remaining until expiry
 * - Estimating expiry date when no manufacturer date is provided
 * - Determining expiry status (FRESH / WATCH / CRITICAL / EXPIRED)
 *
 * This centralizes the app's expiry calculations so dashboard and form flows
 * use the same date parsing and status rules.
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

    // ENHANCEMENT: Added UI-ready fields with default values so it doesn't break existing code
    data class ExpiryResult(
        val daysRemaining: Int, // CHANGED TO INT to match UI models
        val status: ExpiryStatus,
        val expiryDate: LocalDate,
        val colorHex: String = "#4CAF50", // Professional UI tip: Logic suggests urgency color
        val displayLabel: String = ""
    )

    // ─────────────────────────────────────────────────────────────
    // Thresholds (easily adjustable)
    // ─────────────────────────────────────────────────────────────
    private const val CRITICAL_THRESHOLD = 3
    private const val WATCH_THRESHOLD    = 7

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
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),    // "Apr 16, 2026"
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),   // "April 16, 2026"
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),     // "2026-04-16"
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),     // "16/04/2026"
        DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH),     // "04/16/2026"
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),     // "16-04-2026"
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),     // "16 Apr 2026"
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),    // "16 April 2026"
    )

    // ─────────────────────────────────────────────────────────────
    // Core Calculations
    // ─────────────────────────────────────────────────────────────

    /**
     * Calculates how many days are left until [expiryDate].
     * Returns negative if already expired.
     */
    fun daysUntilExpiry(expiryDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate).toInt() // CHANGED TO INT
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
    // UPDATED: Added 'suspend' and changed to getShelfLifeByNameAI
    suspend fun estimateExpiryDateByName(dateAdded: LocalDate, itemName: String): LocalDate {
        val shelfDays = ShelfLifeRules.getShelfLifeByNameAI(itemName)
        return dateAdded.plusDays(shelfDays.toLong())
    }

    /**
     * Determines ExpiryStatus from a raw [daysRemaining] value.
     */
    fun getExpiryStatus(daysRemaining: Int): ExpiryStatus = when {
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

        // ENHANCEMENT: Injecting the professional UI colors and labels here
        return ExpiryResult(
            daysRemaining = days,
            status = status,
            expiryDate = expiryDate,
            colorHex = when(status) {
                ExpiryStatus.EXPIRED -> "#B00020" // Material Error Red
                ExpiryStatus.CRITICAL -> "#FF9800" // Warning Orange
                ExpiryStatus.WATCH -> "#FBC02D"    // Watch Yellow
                ExpiryStatus.FRESH -> "#4CAF50"    // Success Green
            },
            displayLabel = expiryDisplayText(days)
        )
    }

    /**
     * Full calculation for items WITHOUT a manufacturer expiry date.
     */
    // UPDATED: Added 'suspend' because it relies on the AI function
    suspend fun calculateByName(dateAdded: LocalDate, itemName: String): ExpiryResult {
        val expiryDate = estimateExpiryDateByName(dateAdded, itemName)
        return calculate(expiryDate)
    }

    // ─────────────────────────────────────────────────────────────
    // Dashboard integration helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns a simple dashboard-friendly urgency label.
     */
    fun urgencyLabel(daysRemaining: Int): String = when (getExpiryStatus(daysRemaining)) {
        ExpiryStatus.EXPIRED  -> "Expired"
        ExpiryStatus.CRITICAL -> "Critical"
        ExpiryStatus.WATCH    -> "Watch"
        ExpiryStatus.FRESH    -> "Fresh"
    }

    /**
     * BUG FIX: Now correctly parses "Apr 16, 2026" (the format the UI uses).
     * The old code was missing "MMM d, yyyy" causing it to fall back to 14 days
     * instead of computing the real date difference.
     *
     * @param expiryDateString the raw string the user typed, e.g. "Apr 16, 2026"
     * @return days from TODAY until that date (negative = already expired)
     */
    fun estimateExpiresInDays(expiryDateString: String?): Int {
        if (expiryDateString.isNullOrBlank()) return 14

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

        return 14 // safe fallback
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
    fun expiryDisplayText(daysRemaining: Int): String = when {
        daysRemaining < 0   -> "Expired ${abs(daysRemaining)} day(s) ago"
        daysRemaining == 0  -> "Expires today!"
        daysRemaining == 1  -> "Expires tomorrow!"
        else                -> "Expires in $daysRemaining days"
    }
}
