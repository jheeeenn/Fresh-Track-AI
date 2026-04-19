package my.edu.utar.freshtrackai.logic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- I ONLY FIXED THESE 5 LINES ---
import my.edu.utar.freshtrackai.ui.dashboard.DashboardTopBar
// ----------------------------------
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * NotificationTestScreen.kt
 * Member 3 — Developer Test Panel
 *
 * A clean, professional in-app panel to test all Member 3 features.
 * Replaces the previous red debug box.
 *
 * HOW TO USE:
 * Add a "🧪 Dev Tools" tab or button somewhere in the app (e.g. Settings screen)
 * that navigates to this Composable. Remove before production release.
 */

// ─── Colour tokens (reuse Ian's tokens or define locally) ────────────────────
private val BgPage       = Color(0xFFF8FAFC)
private val BgCard       = Color.White
private val BorderCard   = Color(0xFFE2E8F0)
private val TextPrimary  = Color(0xFF0F172A)
private val TextSecondary= Color(0xFF64748B)
private val GreenBadge   = Color(0xFF14532D)
private val GreenBg      = Color(0xFFDCFCE7)
private val AmberBadge   = Color(0xFF92400E)
private val AmberBg      = Color(0xFFFEF3C7)
private val RedBadge     = Color(0xFF991B1B)
private val RedBg        = Color(0xFFFFE4E6)
private val BlueBadge    = Color(0xFF1E3A8A)
private val BlueBg       = Color(0xFFDBEAFE)

@Composable
fun NotificationTestScreen() {
    val context = LocalContext.current
    var lastAction by remember { mutableStateOf("Tap a button to test.") }
    var logLines   by remember { mutableStateOf(listOf<Pair<String, Color>>()) }

    fun log(msg: String, color: Color = TextPrimary) {
        logLines = (listOf(msg to color) + logLines).take(8)
        lastAction = msg
    }

    // FIX: Using produceState to handle the AI suspend functions without freezing the UI!
    val milkTest by produceState<Pair<String, Boolean>?>(initialValue = null) {
        val r = ExpiryCalculator.calculateByName(LocalDate.now().minusDays(5), "milk")
        value = "${r.daysRemaining} days → ${r.status}" to (r.status == ExpiryCalculator.ExpiryStatus.CRITICAL)
    }

    val breadTest by produceState<Pair<String, Boolean>?>(initialValue = null) {
        val r = ExpiryCalculator.calculateByName(LocalDate.now().minusDays(1), "bread")
        value = "${r.daysRemaining} days → ${r.status}" to (r.status == ExpiryCalculator.ExpiryStatus.WATCH)
    }

    val riceTest by produceState<Pair<String, Boolean>?>(initialValue = null) {
        val r = ExpiryCalculator.calculateByName(LocalDate.now(), "rice")
        value = "${r.daysRemaining} days → ${r.status}" to (r.status == ExpiryCalculator.ExpiryStatus.FRESH)
    }

    val chickenTest by produceState<Pair<String, Boolean>?>(initialValue = null) {
        val r = ExpiryCalculator.calculateByName(LocalDate.now().minusDays(5), "chicken")
        value = "${r.daysRemaining} days → ${r.status}" to (r.status == ExpiryCalculator.ExpiryStatus.EXPIRED)
    }

    // FIX: Handling the ShelfLifeRules AI suspend functions safely
    var shelfLifeTests by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(Unit) {
        val results = listOf("milk", "eggs", "chicken", "rice", "salmon", "bread").map { food ->
            val days = ShelfLifeRules.getShelfLifeByNameAI(food)
            val cat  = ShelfLifeRules.detectCategoryWithAI(food)
            food to "$days days ($cat)"
        }
        shelfLifeTests = results
    }

    Scaffold(
        topBar = { DashboardTopBar() },
        containerColor = BgPage
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ────────────────────────────────────────────
            Text("🧪 Dev Test Panel", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Member 3 — Expiry Logic & Notifications", color = TextSecondary, fontSize = 14.sp)

            HorizontalDivider(color = BorderCard)

            // ── Section 1: Expiry Logic ───────────────────────────
            TestSectionHeader("1. Expiry Status Logic (AI Powered)")

            TestResultCard(
                title  = "Milk (added 5 days ago)",
                result = milkTest?.first ?: "Loading AI...",
                expected = "~2 days → CRITICAL",
                passed = milkTest?.second ?: false
            )

            TestResultCard(
                title  = "Bread (added 1 day ago)",
                result = breadTest?.first ?: "Loading AI...",
                expected = "~4 days → WATCH",
                passed = breadTest?.second ?: false
            )

            TestResultCard(
                title  = "Rice (added today)",
                result = riceTest?.first ?: "Loading AI...",
                expected = "365 days → FRESH",
                passed = riceTest?.second ?: false
            )

            TestResultCard(
                title  = "Chicken (added 5 days ago)",
                result = chickenTest?.first ?: "Loading AI...",
                expected = "-2 days → EXPIRED",
                passed = chickenTest?.second ?: false
            )

            // ── Section 2: Date Parsing Bug Fix ──────────────────
            TestSectionHeader("2. Date Format Parsing (Bug Fix)")

            val today14Days = LocalDate.now().plusDays(14)
            val uiFormatDate = today14Days.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

            TestResultCard(
                title    = "UI format: \"$uiFormatDate\"",
                result   = "${ExpiryCalculator.estimateExpiresInDays(uiFormatDate)} days",
                expected = "14 days",
                passed   = ExpiryCalculator.estimateExpiresInDays(uiFormatDate) == 14L
            )

            TestResultCard(
                title    = "Invalid string: \"garbage\"",
                result   = "${ExpiryCalculator.estimateExpiresInDays("garbage")} days",
                expected = "14 days (fallback)",
                passed   = ExpiryCalculator.estimateExpiresInDays("garbage") == 14L
            )

            // ── Section 3: Shelf-life Rules ───────────────────────
            TestSectionHeader("3. Shelf-Life Rule Mapping")

            if (shelfLifeTests.isEmpty()) {
                TestInfoRow("Loading...", "AI is processing...")
            } else {
                shelfLifeTests.forEach { (food, info) ->
                    TestInfoRow(food, info)
                }
            }

            // ── Section 4: Push Notifications ────────────────────
            TestSectionHeader("4. Push Notifications")

            TestActionButton(
                label    = "🔴  Send CRITICAL Alert",
                subtitle = "Items expiring in 1-3 days",
                color    = Color(0xFFDC2626)
            ) {
                NotificationHelper.sendCriticalExpiryNotification(
                    context,
                    listOf("Whole Milk", "Chicken Breast")
                )
                log("✅ CRITICAL notification sent", Color(0xFF14532D))
            }

            TestActionButton(
                label    = "🟡  Send WATCH Alert",
                subtitle = "Items expiring in 4-7 days",
                color    = Color(0xFFD97706)
            ) {
                NotificationHelper.sendWatchExpiryNotification(
                    context,
                    listOf("Baby Spinach", "Greek Yogurt")
                )
                log("✅ WATCH notification sent", Color(0xFF92400E))
            }

            TestActionButton(
                label    = "⚫  Send EXPIRED Alert",
                subtitle = "Items that have already expired",
                color    = Color(0xFF475569)
            ) {
                NotificationHelper.sendExpiredNotification(
                    context,
                    listOf("Old Bread")
                )
                log("✅ EXPIRED notification sent", TextSecondary)
            }

            // ── Section 5: WorkManager ────────────────────────────
            TestSectionHeader("5. WorkManager Background Task")

            TestActionButton(
                label    = "▶️  Run Daily Check NOW",
                subtitle = "Triggers ExpiryCheckWorker immediately (don't wait 24h)",
                color    = Color(0xFF0EA5E9)
            ) {
                ExpiryCheckWorker.runNow(context)
                log("✅ WorkManager job enqueued — check logcat for ExpiryCheckWorker", Color(0xFF1E3A8A))
            }

            TestActionButton(
                label    = "📅  Schedule Daily Check",
                subtitle = "Sets up the recurring 24h background job",
                color    = Color(0xFF8B5CF6)
            ) {
                ExpiryCheckWorker.scheduleDailyCheck(context)
                log("✅ Daily check scheduled", Color(0xFF5B21B6))
            }

            TestActionButton(
                label    = "🛑  Cancel Daily Check",
                subtitle = "Cancels the recurring job",
                color    = Color(0xFF6B7280)
            ) {
                ExpiryCheckWorker.cancelDailyCheck(context)
                log("⛔ Daily check cancelled", Color(0xFF6B7280))
            }

            // ── Log output ────────────────────────────────────────
            if (logLines.isNotEmpty()) {
                HorizontalDivider(color = BorderCard)
                TestSectionHeader("Log Output")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape  = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        logLines.forEach { (line, color) ->
                            Text("> $line", color = color.copy(alpha = 0.9f), fontSize = 13.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun TestSectionHeader(title: String) {
    Text(
        title,
        color      = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize   = 15.sp,
        modifier   = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun TestResultCard(title: String, result: String, expected: String, passed: Boolean) {
    val bg     = if (passed) GreenBg  else RedBg
    val badge  = if (passed) GreenBadge else RedBadge
    val label  = if (passed) "PASS" else "FAIL"

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape  = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Got: $result", color = TextSecondary, fontSize = 13.sp)
                Text("Expected: $expected", color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(badge, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TestInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(value, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun TestActionButton(
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape  = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = onClick,
                colors  = ButtonDefaults.buttonColors(containerColor = color),
                shape   = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Run", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}