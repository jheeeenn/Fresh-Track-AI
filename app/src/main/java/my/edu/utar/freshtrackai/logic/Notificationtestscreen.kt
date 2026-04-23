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
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.data.local.AppDatabase
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.DashboardTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Colour tokens ────────────────────────────────────────────────────────────
private val BgPage        = Color(0xFFF8FAFC)
private val BgCard        = Color.White
private val BorderCard    = Color(0xFFE2E8F0)
private val TextPrimary   = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val GreenBadge    = Color(0xFF14532D)
private val GreenBg       = Color(0xFFDCFCE7)
private val RedBadge      = Color(0xFF991B1B)
private val RedBg         = Color(0xFFFFE4E6)

/**
 * NotificationTestScreen.kt
 * Member 3 — Developer Test Panel
 *
 * Reads live data from Member 2's Room database and uses it for:
 *  - Expiry status logic tests
 *  - Push notification tests (sends real device notifications)
 *  - WorkManager background job controls
 */
@Composable
fun NotificationTestScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ── Live DB items via Flow ────────────────────────────────────────────────
    val dao = remember { AppDatabase.getDatabase(context).inventoryDao() }
    val dbItems by dao.getAllItems().collectAsState(initial = emptyList())

    // ── Log state ─────────────────────────────────────────────────────────────
    var logLines by remember { mutableStateOf(listOf<Pair<String, Color>>()) }
    fun log(msg: String, color: Color = TextPrimary) {
        logLines = (listOf(msg to color) + logLines).take(10)
    }

    // ── Pre-computed expiry groups from real DB ───────────────────────────────
    val currentTime = System.currentTimeMillis()

    val expiredItems  = remember(dbItems) {
        dbItems.filter { ExpiryCalculator.getExpiryStatus(daysRemaining(it, currentTime)) == ExpiryCalculator.ExpiryStatus.EXPIRED }
    }
    val criticalItems = remember(dbItems) {
        dbItems.filter { ExpiryCalculator.getExpiryStatus(daysRemaining(it, currentTime)) == ExpiryCalculator.ExpiryStatus.CRITICAL }
    }
    val watchItems    = remember(dbItems) {
        dbItems.filter { ExpiryCalculator.getExpiryStatus(daysRemaining(it, currentTime)) == ExpiryCalculator.ExpiryStatus.WATCH }
    }
    val freshItems    = remember(dbItems) {
        dbItems.filter { ExpiryCalculator.getExpiryStatus(daysRemaining(it, currentTime)) == ExpiryCalculator.ExpiryStatus.FRESH }
    }

    // ── Expiry logic tests (AI-backed, run once on composition) ──────────────
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

    // ── Shelf-life rule tests ─────────────────────────────────────────────────
    var shelfLifeTests by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(Unit) {
        shelfLifeTests = listOf("milk", "eggs", "chicken", "rice", "salmon", "bread").map { food ->
            val days = ShelfLifeRules.getShelfLifeByNameAI(food)
            val cat  = ShelfLifeRules.detectCategoryWithAI(food)
            food to "$days days ($cat)"
        }
    }

    // ── Date parsing tests ────────────────────────────────────────────────────
    val today14Days    = LocalDate.now().plusDays(14)
    val uiFormatDate   = today14Days.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

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

            // ── Header ────────────────────────────────────────────────────────
            Text("🧪 Dev Test Panel", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Member 3 — Expiry Logic, DB & Notifications", color = TextSecondary, fontSize = 14.sp)

            HorizontalDivider(color = BorderCard)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 0: Live Database Summary
            // ══════════════════════════════════════════════════════════════════
            TestSectionHeader("0. Live Inventory Database (${dbItems.size} items)")

            if (dbItems.isEmpty()) {
                InfoCard("No items in the database yet. Add items via the app first.")
            } else {
                // Summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DbSummaryBadge("🟢 Fresh",    freshItems.size,    GreenBadge, GreenBg,            Modifier.weight(1f))
                    DbSummaryBadge("🟡 Watch",     watchItems.size,    Color(0xFF92400E), Color(0xFFFEF3C7), Modifier.weight(1f))
                    DbSummaryBadge("🔴 Critical",  criticalItems.size, RedBadge,   RedBg,              Modifier.weight(1f))
                    DbSummaryBadge("⚫ Expired",   expiredItems.size,  Color(0xFF3F3F46), Color(0xFFF4F4F5), Modifier.weight(1f))
                }

                // List each item with its computed status
                dbItems.forEach { item ->
                    val days   = daysRemaining(item, currentTime)
                    val status = ExpiryCalculator.getExpiryStatus(days)
                    DbItemRow(item = item, daysRemaining = days, status = status)
                }
            }

            HorizontalDivider(color = BorderCard)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 1: Expiry Logic Unit Tests
            // ══════════════════════════════════════════════════════════════════
            TestSectionHeader("1. Expiry Status Logic (AI Powered)")

            TestResultCard("Milk (added 5 days ago)",    milkTest?.first    ?: "Loading AI…", "~2 days → CRITICAL", milkTest?.second    ?: false)
            TestResultCard("Bread (added 1 day ago)",    breadTest?.first   ?: "Loading AI…", "~4 days → WATCH",    breadTest?.second   ?: false)
            TestResultCard("Rice (added today)",         riceTest?.first    ?: "Loading AI…", "365 days → FRESH",   riceTest?.second    ?: false)
            TestResultCard("Chicken (added 5 days ago)", chickenTest?.first ?: "Loading AI…", "-2 days → EXPIRED",  chickenTest?.second ?: false)

            HorizontalDivider(color = BorderCard)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 2: Date Parsing Bug Fix
            // ══════════════════════════════════════════════════════════════════
            TestSectionHeader("2. Date Format Parsing (Bug Fix)")

            TestResultCard(
                title    = "UI format: \"$uiFormatDate\"",
                result   = "${ExpiryCalculator.estimateExpiresInDays(uiFormatDate)} days",
                expected = "14 days",
                passed   = ExpiryCalculator.estimateExpiresInDays(uiFormatDate) == 14
            )
            TestResultCard(
                title    = "Invalid string: \"garbage\"",
                result   = "${ExpiryCalculator.estimateExpiresInDays("garbage")} days",
                expected = "14 days (fallback)",
                passed   = ExpiryCalculator.estimateExpiresInDays("garbage") == 14
            )

            HorizontalDivider(color = BorderCard)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 3: Shelf-Life Rules
            // ══════════════════════════════════════════════════════════════════
            TestSectionHeader("3. Shelf-Life Rule Mapping")

            if (shelfLifeTests.isEmpty()) {
                TestInfoRow("Loading…", "AI is processing…")
            } else {
                shelfLifeTests.forEach { (food, info) -> TestInfoRow(food, info) }
            }

            HorizontalDivider(color = BorderCard)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 4: Push Notifications using REAL database items
            // ══════════════════════════════════════════════════════════════════
            TestSectionHeader("4. Push Notifications (Live DB Data)")

            // Critical — uses actual critical items from DB, falls back to sample if empty
            TestActionButton(
                label    = "🔴  Send CRITICAL Alert",
                subtitle = if (criticalItems.isEmpty())
                    "No critical items in DB — will send sample data"
                else
                    "${criticalItems.size} critical item(s) from DB: ${criticalItems.joinToString { it.name }}",
                color    = Color(0xFFDC2626)
            ) {
                val names = criticalItems.map { it.name }.ifEmpty {
                    listOf("Whole Milk (sample)", "Chicken Breast (sample)")
                }
                NotificationHelper.sendCriticalExpiryNotification(context, names)
                log("✅ CRITICAL sent: ${names.joinToString()}", Color(0xFF14532D))
            }

            // Watch — uses actual watch items from DB
            TestActionButton(
                label    = "🟡  Send WATCH Alert",
                subtitle = if (watchItems.isEmpty())
                    "No watch items in DB — will send sample data"
                else
                    "${watchItems.size} watch item(s) from DB: ${watchItems.joinToString { it.name }}",
                color    = Color(0xFFD97706)
            ) {
                val names = watchItems.map { it.name }.ifEmpty {
                    listOf("Baby Spinach (sample)", "Greek Yogurt (sample)")
                }
                NotificationHelper.sendWatchExpiryNotification(context, names)
                log("✅ WATCH sent: ${names.joinToString()}", Color(0xFF92400E))
            }

            // Expired — uses actual expired items from DB
            TestActionButton(
                label    = "⚫  Send EXPIRED Alert",
                subtitle = if (expiredItems.isEmpty())
                    "No expired items in DB — will send sample data"
                else
                    "${expiredItems.size} expired item(s) from DB: ${expiredItems.joinToString { it.name }}",
                color    = Color(0xFF475569)
            ) {
                val names = expiredItems.map { it.name }.ifEmpty {
                    listOf("Old Bread (sample)")
                }
                NotificationHelper.sendExpiredNotification(context, names)
                log("✅ EXPIRED sent: ${names.joinToString()}", TextSecondary)
            }

            // Send all at once — full scan using real DB
            TestActionButton(
                label    = "📣  Send ALL Alerts (Full Scan)",
                subtitle = "Scans all ${dbItems.size} DB item(s) and fires relevant notifications",
                color    = Color(0xFF7C3AED)
            ) {
                coroutineScope.launch {
                    if (dbItems.isEmpty()) {
                        log("⚠️ Database is empty — add items first", Color(0xFFDC2626))
                        return@launch
                    }
                    val critNames = criticalItems.map { it.name }
                    val watchNames = watchItems.map { it.name }
                    val expNames = expiredItems.map { it.name }
                    NotificationHelper.sendCriticalExpiryNotification(context, critNames)
                    NotificationHelper.sendWatchExpiryNotification(context, watchNames)
                    NotificationHelper.sendExpiredNotification(context, expNames)
                    log(
                        "✅ All alerts fired — " +
                                "Critical: ${critNames.size}, Watch: ${watchNames.size}, Expired: ${expNames.size}",
                        Color(0xFF5B21B6)
                    )
                }
            }

            HorizontalDivider(color = BorderCard)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 5: WorkManager Background Task
            // ══════════════════════════════════════════════════════════════════
            TestSectionHeader("5. WorkManager Background Task (24h)")

            InfoCard("The periodic job is now set to 24 hours. Use 'Run NOW' below to test immediately without waiting.")

            TestActionButton(
                label    = "▶️  Run Daily Check NOW",
                subtitle = "Triggers ExpiryCheckWorker immediately using real DB data",
                color    = Color(0xFF0EA5E9)
            ) {
                ExpiryCheckWorker.runNow(context)
                log("✅ WorkManager one-time job enqueued", Color(0xFF1E3A8A))
            }

            TestActionButton(
                label    = "📅  Schedule 24h Daily Check",
                subtitle = "Enqueues the recurring 24-hour background job",
                color    = Color(0xFF8B5CF6)
            ) {
                ExpiryCheckWorker.scheduleDailyCheck(context)
                log("✅ 24h daily check scheduled", Color(0xFF5B21B6))
            }

            TestActionButton(
                label    = "🛑  Cancel Daily Check",
                subtitle = "Cancels the recurring background job",
                color    = Color(0xFF6B7280)
            ) {
                ExpiryCheckWorker.cancelDailyCheck(context)
                log("⛔ Daily check cancelled", Color(0xFF6B7280))
            }

            // ── Log output ────────────────────────────────────────────────────
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
                            Text(
                                "> $line",
                                color      = color.copy(alpha = 0.9f),
                                fontSize   = 13.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Helper: compute days remaining from InventoryItem (epoch millis) ─────────
private fun daysRemaining(item: InventoryItem, currentTimeMillis: Long): Int =
    ((item.expiryDate - currentTimeMillis) / (1000L * 60 * 60 * 24)).toInt()

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun TestSectionHeader(title: String) {
    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun InfoCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        shape  = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Text(
            message,
            color    = Color(0xFF0369A1),
            fontSize = 13.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun DbSummaryBadge(
    label: String,
    count: Int,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DbItemRow(item: InventoryItem, daysRemaining: Int, status: ExpiryCalculator.ExpiryStatus) {
    val (bgColor, textColor) = when (status) {
        ExpiryCalculator.ExpiryStatus.EXPIRED  -> Color(0xFFFFE4E6) to Color(0xFF991B1B)
        ExpiryCalculator.ExpiryStatus.CRITICAL -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        ExpiryCalculator.ExpiryStatus.WATCH    -> Color(0xFFFFFBEB) to Color(0xFFB45309)
        ExpiryCalculator.ExpiryStatus.FRESH    -> Color(0xFFDCFCE7) to Color(0xFF14532D)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(item.category, color = TextSecondary, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (daysRemaining <= 0) "Expired ${-daysRemaining}d ago"
                else "Expires in ${daysRemaining}d",
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(status.name, color = textColor, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TestResultCard(title: String, result: String, expected: String, passed: Boolean) {
    val bg    = if (passed) GreenBg  else RedBg
    val badge = if (passed) GreenBadge else RedBadge
    val label = if (passed) "PASS" else "FAIL"
    Card(colors = CardDefaults.cardColors(containerColor = bg), shape = RoundedCornerShape(10.dp)) {
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
            Box(modifier = Modifier.background(badge, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TestInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(BgCard, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(value, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun TestActionButton(label: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape  = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewNotificationTest() {
    my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme {
        NotificationTestScreen()
    }
}