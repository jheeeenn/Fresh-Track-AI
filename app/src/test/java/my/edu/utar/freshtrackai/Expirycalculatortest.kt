package my.edu.utar.freshtrackai
import my.edu.utar.freshtrackai.logic.ExpiryCalculator

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
// --- I ONLY ADDED THESE 2 LINES ---
import my.edu.utar.freshtrackai.logic.ShelfLifeRules
// ----------------------------------

/**
 * ExpiryCalculatorTest.kt
 * Member 3 — Unit Tests
 *
 * Run in Android Studio: right-click this file → "Run ExpiryCalculatorTest"
 * No device or emulator needed. Results appear in the Run panel.
 *
 * Covers ALL Member 3 features:
 * ✅ Shelf-life rule mapping
 * ✅ Expiry date calculation
 * ✅ Near-expiry / expired status logic
 * ✅ Date format parsing (including the BUG FIX for "Apr 16, 2026")
 * ✅ Category-based expiry rules table
 * ✅ Display text / urgency label
 */
class ExpiryCalculatorTest {

    // ═══════════════════════════════════════════════════════════════
    // 1. EXPIRY STATUS LOGIC
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `status - more than 7 days is FRESH`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.FRESH, ExpiryCalculator.getExpiryStatus(10))
    }

    @Test
    fun `status - exactly 7 days is WATCH`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.WATCH, ExpiryCalculator.getExpiryStatus(7))
    }

    @Test
    fun `status - 4 days is WATCH`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.WATCH, ExpiryCalculator.getExpiryStatus(4))
    }

    @Test
    fun `status - 3 days is CRITICAL`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.CRITICAL, ExpiryCalculator.getExpiryStatus(3))
    }

    @Test
    fun `status - 1 day is CRITICAL`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.CRITICAL, ExpiryCalculator.getExpiryStatus(1))
    }

    @Test
    fun `status - 0 days is EXPIRED`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.EXPIRED, ExpiryCalculator.getExpiryStatus(0))
    }

    @Test
    fun `status - negative days is EXPIRED`() {
        assertEquals(ExpiryCalculator.ExpiryStatus.EXPIRED, ExpiryCalculator.getExpiryStatus(-5))
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. SHELF-LIFE RULE MAPPING (category-based)
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `shelf life - milk category is 7 days`() {
        assertEquals(7, ShelfLifeRules.getShelfLifeDays(ShelfLifeRules.FoodCategory.DAIRY))
    }

    @Test
    fun `shelf life - eggs category is 21 days`() {
        assertEquals(21, ShelfLifeRules.getShelfLifeDays(ShelfLifeRules.FoodCategory.EGGS))
    }

    @Test
    fun `shelf life - meat is 3 days`() {
        assertEquals(3, ShelfLifeRules.getShelfLifeDays(ShelfLifeRules.FoodCategory.MEAT_POULTRY))
    }

    @Test
    fun `shelf life - seafood is 2 days`() {
        assertEquals(2, ShelfLifeRules.getShelfLifeDays(ShelfLifeRules.FoodCategory.SEAFOOD))
    }

    @Test
    fun `shelf life - rice (grains) is 365 days`() {
        assertEquals(365, ShelfLifeRules.getShelfLifeDays(ShelfLifeRules.FoodCategory.GRAINS_PASTA))
    }

    @Test
    fun `shelf life - frozen is 90 days`() {
        assertEquals(90, ShelfLifeRules.getShelfLifeDays(ShelfLifeRules.FoodCategory.FROZEN))
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. CATEGORY AUTO-DETECT FROM FOOD NAME
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `detect - milk name maps to DAIRY`() {
        assertEquals(ShelfLifeRules.FoodCategory.DAIRY, ShelfLifeRules.detectCategory("Whole Milk"))
    }

    @Test
    fun `detect - chicken name maps to MEAT_POULTRY`() {
        assertEquals(ShelfLifeRules.FoodCategory.MEAT_POULTRY, ShelfLifeRules.detectCategory("Chicken Breast"))
    }

    @Test
    fun `detect - salmon maps to SEAFOOD`() {
        assertEquals(ShelfLifeRules.FoodCategory.SEAFOOD, ShelfLifeRules.detectCategory("Fresh Salmon"))
    }

    @Test
    fun `detect - unknown item falls back to OTHER`() {
        assertEquals(ShelfLifeRules.FoodCategory.OTHER, ShelfLifeRules.detectCategory("mystery item xyz"))
    }

    @Test
    fun `detect - detection is case-insensitive`() {
        assertEquals(ShelfLifeRules.FoodCategory.EGGS, ShelfLifeRules.detectCategory("EGGS"))
        assertEquals(ShelfLifeRules.FoodCategory.EGGS, ShelfLifeRules.detectCategory("eggs"))
        assertEquals(ShelfLifeRules.FoodCategory.EGGS, ShelfLifeRules.detectCategory("Eggs"))
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. EXPIRY DATE CALCULATION
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `calculation - milk added today expires in 7 days`() {
        val today  = LocalDate.now()
        val result = ExpiryCalculator.estimateExpiryDateByName(today, "milk")
        assertEquals(today.plusDays(7), result)
    }

    @Test
    fun `calculation - eggs added today expires in 21 days`() {
        val today  = LocalDate.now()
        val result = ExpiryCalculator.estimateExpiryDateByName(today, "eggs")
        assertEquals(today.plusDays(21), result)
    }

    @Test
    fun `calculation - chicken added today expires in 3 days`() {
        val today  = LocalDate.now()
        val result = ExpiryCalculator.estimateExpiryDateByName(today, "chicken")
        assertEquals(today.plusDays(3), result)
    }

    @Test
    fun `calculation - days until expiry 5 days from now is 5`() {
        val future = LocalDate.now().plusDays(5)
        assertEquals(5L, ExpiryCalculator.daysUntilExpiry(future))
    }

    @Test
    fun `calculation - days until expiry 3 days ago is negative`() {
        val past = LocalDate.now().minusDays(3)
        assertTrue(ExpiryCalculator.daysUntilExpiry(past) < 0)
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. DATE FORMAT PARSING — BUG FIX TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `parse - UI format 'Apr 16, 2026' parses correctly (THE BUG FIX)`() {
        // This is the format the AddMissingItemScreen uses (placeholder: "Oct 24, 2026")
        // The old code was missing "MMM d, yyyy" and returned 14 (fallback) instead.
        val futureDate = LocalDate.now().plusDays(30)
        val formatted  = futureDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
        val result     = ExpiryCalculator.estimateExpiresInDays(formatted)
        assertEquals(30L, result)
    }

    @Test
    fun `parse - full month format 'April 16, 2026' parses correctly`() {
        val futureDate = LocalDate.now().plusDays(10)
        val formatted  = futureDate.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        val result     = ExpiryCalculator.estimateExpiresInDays(formatted)
        assertEquals(10L, result)
    }

    @Test
    fun `parse - ISO format 'yyyy-MM-dd' parses correctly`() {
        val futureDate = LocalDate.now().plusDays(10)
        val result     = ExpiryCalculator.estimateExpiresInDays(futureDate.toString())
        assertEquals(10L, result)
    }

    @Test
    fun `parse - null returns 14-day fallback`() {
        assertEquals(14L, ExpiryCalculator.estimateExpiresInDays(null))
    }

    @Test
    fun `parse - empty string returns 14-day fallback`() {
        assertEquals(14L, ExpiryCalculator.estimateExpiresInDays(""))
    }

    @Test
    fun `parse - garbage string returns 14-day fallback`() {
        assertEquals(14L, ExpiryCalculator.estimateExpiresInDays("not a date"))
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. FULL END-TO-END SCENARIOS
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `scenario - milk bought 5 days ago is CRITICAL`() {
        // milk shelf = 7 days, bought 5 days ago → 2 days left → CRITICAL
        val purchase = LocalDate.now().minusDays(5)
        val result   = ExpiryCalculator.calculateByName(purchase, "milk")
        assertEquals(ExpiryCalculator.ExpiryStatus.CRITICAL, result.status)
    }

    @Test
    fun `scenario - bread bought 1 day ago is WATCH`() {
        // bread shelf = 5 days, bought 1 day ago → 4 days left → WATCH
        val purchase = LocalDate.now().minusDays(1)
        val result   = ExpiryCalculator.calculateByName(purchase, "bread")
        assertEquals(ExpiryCalculator.ExpiryStatus.WATCH, result.status)
    }

    @Test
    fun `scenario - rice bought today is FRESH`() {
        val result = ExpiryCalculator.calculateByName(LocalDate.now(), "rice")
        assertEquals(ExpiryCalculator.ExpiryStatus.FRESH, result.status)
    }

    @Test
    fun `scenario - chicken bought 5 days ago is EXPIRED`() {
        // chicken shelf = 3 days, bought 5 days ago → -2 days → EXPIRED
        val purchase = LocalDate.now().minusDays(5)
        val result   = ExpiryCalculator.calculateByName(purchase, "chicken")
        assertEquals(ExpiryCalculator.ExpiryStatus.EXPIRED, result.status)
    }

    // ═══════════════════════════════════════════════════════════════
    // 7. DISPLAY TEXT + URGENCY LABEL
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `display - 0 days says 'expires today'`() {
        assertTrue(ExpiryCalculator.expiryDisplayText(0).contains("today", ignoreCase = true))
    }

    @Test
    fun `display - 1 day says 'tomorrow'`() {
        assertTrue(ExpiryCalculator.expiryDisplayText(1).contains("tomorrow", ignoreCase = true))
    }

    @Test
    fun `display - negative days says 'ago'`() {
        assertTrue(ExpiryCalculator.expiryDisplayText(-3).contains("ago", ignoreCase = true))
    }

    @Test
    fun `urgency label - 10 days is Fresh`() {
        assertEquals("Fresh", ExpiryCalculator.urgencyLabel(10))
    }

    @Test
    fun `urgency label - 5 days is Watch`() {
        assertEquals("Watch", ExpiryCalculator.urgencyLabel(5))
    }

    @Test
    fun `urgency label - 2 days is Critical`() {
        assertEquals("Critical", ExpiryCalculator.urgencyLabel(2))
    }

    @Test
    fun `urgency label - 0 days is Expired`() {
        assertEquals("Expired", ExpiryCalculator.urgencyLabel(0))
    }
}