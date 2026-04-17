package my.edu.utar.freshtrackai

import my.edu.utar.freshtrackai.logic.ExpiryCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * ExpiryCalculatorTest.kt
 * Member 3 — Unit Tests
 *
 * Run these with the green play button in Android Studio (no device needed).
 * Tests cover: status logic, date estimation, shelf-life rules, display text.
 */
class ExpiryCalculatorTest {

    // ─────────────────────────────────────────────────────────────
    // ExpiryStatus Tests
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `item with more than 7 days should be FRESH`() {
        val status = ExpiryCalculator.getExpiryStatus(10)
        assertEquals(ExpiryCalculator.ExpiryStatus.FRESH, status)
    }

    @Test
    fun `item with exactly 7 days should be WATCH`() {
        val status = ExpiryCalculator.getExpiryStatus(7)
        assertEquals(ExpiryCalculator.ExpiryStatus.WATCH, status)
    }

    @Test
    fun `item with 4 days should be WATCH`() {
        val status = ExpiryCalculator.getExpiryStatus(4)
        assertEquals(ExpiryCalculator.ExpiryStatus.WATCH, status)
    }

    @Test
    fun `item with 3 days should be CRITICAL`() {
        val status = ExpiryCalculator.getExpiryStatus(3)
        assertEquals(ExpiryCalculator.ExpiryStatus.CRITICAL, status)
    }

    @Test
    fun `item with 1 day should be CRITICAL`() {
        val status = ExpiryCalculator.getExpiryStatus(1)
        assertEquals(ExpiryCalculator.ExpiryStatus.CRITICAL, status)
    }

    @Test
    fun `item with 0 days should be EXPIRED`() {
        val status = ExpiryCalculator.getExpiryStatus(0)
        assertEquals(ExpiryCalculator.ExpiryStatus.EXPIRED, status)
    }

    @Test
    fun `item with negative days should be EXPIRED`() {
        val status = ExpiryCalculator.getExpiryStatus(-5)
        assertEquals(ExpiryCalculator.ExpiryStatus.EXPIRED, status)
    }

    // ─────────────────────────────────────────────────────────────
    // Shelf Life Estimation Tests
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `milk added today should expire in 7 days`() {
        val today      = LocalDate.now()
        val expiryDate = ExpiryCalculator.estimateExpiryDateByName(today, "milk")
        val expected   = today.plusDays(7)
        assertEquals(expected, expiryDate)
    }

    @Test
    fun `eggs added today should expire in 21 days`() {
        val today      = LocalDate.now()
        val expiryDate = ExpiryCalculator.estimateExpiryDateByName(today, "eggs")
        val expected   = today.plusDays(21)
        assertEquals(expected, expiryDate)
    }

    @Test
    fun `chicken added today should expire in 3 days`() {
        val today      = LocalDate.now()
        val expiryDate = ExpiryCalculator.estimateExpiryDateByName(today, "chicken")
        val expected   = today.plusDays(3)
        assertEquals(expected, expiryDate)
    }

    @Test
    fun `rice added today should expire in 365 days`() {
        val today      = LocalDate.now()
        val expiryDate = ExpiryCalculator.estimateExpiryDateByName(today, "rice")
        val expected   = today.plusDays(365)
        assertEquals(expected, expiryDate)
    }

    @Test
    fun `unknown item should fall back to 14 days`() {
        val today      = LocalDate.now()
        val expiryDate = ExpiryCalculator.estimateExpiryDateByName(today, "mystery item xyz")
        val expected   = today.plusDays(14)
        assertEquals(expected, expiryDate)
    }

    // ─────────────────────────────────────────────────────────────
    // Days Until Expiry Tests
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `expiry date 5 days from now should return 5`() {
        val expiryDate = LocalDate.now().plusDays(5)
        val days       = ExpiryCalculator.daysUntilExpiry(expiryDate)
        assertEquals(5L, days)
    }

    @Test
    fun `expiry date already passed should return negative value`() {
        val expiryDate = LocalDate.now().minusDays(3)
        val days       = ExpiryCalculator.daysUntilExpiry(expiryDate)
        assertTrue("Expected negative days", days < 0)
    }

    // ─────────────────────────────────────────────────────────────
    // Full ExpiryResult Tests
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `milk bought 5 days ago should be CRITICAL`() {
        // milk shelf life = 7 days, bought 5 days ago → 2 days left → CRITICAL
        val purchaseDate = LocalDate.now().minusDays(5)
        val result       = ExpiryCalculator.calculateByName(purchaseDate, "milk")
        assertEquals(ExpiryCalculator.ExpiryStatus.CRITICAL, result.status)
    }

    @Test
    fun `bread bought 1 day ago should be WATCH`() {
        // bread shelf life = 5 days, bought 1 day ago → 4 days left → WATCH
        val purchaseDate = LocalDate.now().minusDays(1)
        val result       = ExpiryCalculator.calculateByName(purchaseDate, "bread")
        assertEquals(ExpiryCalculator.ExpiryStatus.WATCH, result.status)
    }

    @Test
    fun `rice bought today should be FRESH`() {
        val result = ExpiryCalculator.calculateByName(LocalDate.now(), "rice")
        assertEquals(ExpiryCalculator.ExpiryStatus.FRESH, result.status)
    }

    // ─────────────────────────────────────────────────────────────
    // Display Text Tests
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `display text for 0 days should say expires today`() {
        val text = ExpiryCalculator.expiryDisplayText(0)
        assertTrue(text.contains("today", ignoreCase = true))
    }

    @Test
    fun `display text for 1 day should say tomorrow`() {
        val text = ExpiryCalculator.expiryDisplayText(1)
        assertTrue(text.contains("tomorrow", ignoreCase = true))
    }

    @Test
    fun `display text for negative days should say expired ago`() {
        val text = ExpiryCalculator.expiryDisplayText(-3)
        assertTrue(text.contains("ago", ignoreCase = true))
    }

    // ─────────────────────────────────────────────────────────────
    // Date String Parsing Tests (Ian's estimateExpiresInDays replacement)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `parse yyyy-MM-dd format correctly`() {
        val futureDate = LocalDate.now().plusDays(10).toString() // "2025-07-15"
        val days       = ExpiryCalculator.estimateExpiresInDays(futureDate)
        assertEquals(10L, days)
    }

    @Test
    fun `null date string should return fallback 14 days`() {
        val days = ExpiryCalculator.estimateExpiresInDays(null)
        assertEquals(14L, days)
    }

    @Test
    fun `empty date string should return fallback 14 days`() {
        val days = ExpiryCalculator.estimateExpiresInDays("")
        assertEquals(14L, days)
    }

    // ─────────────────────────────────────────────────────────────
    // Urgency Label Tests (Ian's urgencyForDays replacement)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `urgencyLabel for 10 days should be Fresh`() {
        assertEquals("Fresh", ExpiryCalculator.urgencyLabel(10))
    }

    @Test
    fun `urgencyLabel for 5 days should be Watch`() {
        assertEquals("Watch", ExpiryCalculator.urgencyLabel(5))
    }

    @Test
    fun `urgencyLabel for 2 days should be Critical`() {
        assertEquals("Critical", ExpiryCalculator.urgencyLabel(2))
    }

    @Test
    fun `urgencyLabel for -1 days should be Expired`() {
        assertEquals("Expired", ExpiryCalculator.urgencyLabel(-1))
    }
}
