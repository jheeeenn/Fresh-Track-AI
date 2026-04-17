package com.example.foodtracker.expiry

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
 * This replaces Ian's mock functions in DashboardData.kt:
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
    private const val CRITICAL_THRESHOLD = 3L  // days
    private const val WATCH_THRESHOLD    = 7L  // days

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
     * Estimates the expiry date based on the [dateAdded] and the item's [category].
     * Uses ShelfLifeRules to determine shelf-life days.
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
     * Auto-detects category from the item name via ShelfLifeRules.
     */
    fun estimateExpiryDateByName(
        dateAdded: LocalDate,
        itemName: String
    ): LocalDate {
        val shelfDays = ShelfLifeRules.getShelfLifeByName(itemName)
        return dateAdded.plusDays(shelfDays.toLong())
    }

    /**
     * Determines the ExpiryStatus from a raw [daysRemaining] value.
     * This directly replaces Ian's urgencyForDays() mock function.
     */
    fun getExpiryStatus(daysRemaining: Long): ExpiryStatus {
        return when {
            daysRemaining <= 0              -> ExpiryStatus.EXPIRED
            daysRemaining <= CRITICAL_THRESHOLD -> ExpiryStatus.CRITICAL
            daysRemaining <= WATCH_THRESHOLD    -> ExpiryStatus.WATCH
            else                                -> ExpiryStatus.FRESH
        }
    }

    /**
     * Full calculation: given an expiry date, returns an [ExpiryResult]
     * with days remaining, status, and the date itself.
     */
    fun calculate(expiryDate: LocalDate): ExpiryResult {
        val days   = daysUntilExpiry(expiryDate)
        val status = getExpiryStatus(days)
        return ExpiryResult(
            daysRemaining = days,
            status        = status,
            expiryDate    = expiryDate
        )
    }

    /**
     * Full calculation for items WITHOUT a manufacturer expiry date.
     * Estimates the date first, then calculates the result.
     */
    fun calculateByName(
        dateAdded: LocalDate,
        itemName: String
    ): ExpiryResult {
        val expiryDate = estimateExpiryDateByName(dateAdded, itemName)
        return calculate(expiryDate)
    }

    // ─────────────────────────────────────────────────────────────
    // Dashboard Integration Helpers
    // These are drop-in replacements for Ian's DashboardData.kt functions
    // ─────────────────────────────────────────────────────────────

    /**
     * DROP-IN REPLACEMENT for Ian's urgencyForDays(days: Int): String
     * Returns a display string matching the existing UI labels.
     */
    fun urgencyLabel(daysRemaining: Long): String {
        return when (getExpiryStatus(daysRemaining)) {
            ExpiryStatus.EXPIRED  -> "Expired"
            ExpiryStatus.CRITICAL -> "Critical"
            ExpiryStatus.WATCH    -> "Watch"
            ExpiryStatus.FRESH    -> "Fresh"
        }
    }

    /**
     * DROP-IN REPLACEMENT for Ian's estimateExpiresInDays(...): Int
     * Takes the raw date string from the database and returns days remaining.
     * Handles multiple common date formats gracefully.
     */
    fun estimateExpiresInDays(expiryDateString: String?): Long {
        if (expiryDateString.isNullOrBlank()) return 14L // fallback default

        val formats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy"),
        )
        for (formatter in formats) {
            try {
                val date = LocalDate.parse(expiryDateString.trim(), formatter)
                return daysUntilExpiry(date)
            } catch (e: DateTimeParseException) {
                // try next format
            }
        }
        return 14L // fallback if no format matched
    }

    // ─────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns a human-readable summary string for display.
     * e.g. "Expires in 3 days", "Expired 2 days ago", "Expires today"
     */
    fun expiryDisplayText(daysRemaining: Long): String {
        return when {
            daysRemaining < 0  -> "Expired ${-daysRemaining} day(s) ago"
            daysRemaining == 0L -> "Expires today!"
            daysRemaining == 1L -> "Expires tomorrow!"
            else               -> "Expires in $daysRemaining days"
        }
    }
}
