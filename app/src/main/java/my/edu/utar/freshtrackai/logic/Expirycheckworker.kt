package my.edu.utar.freshtrackai.logic

import android.content.Context
import androidx.work.*
// --- I ONLY FIXED THESE 3 LINES ---
import my.edu.utar.freshtrackai.logic.`ExpiryCalculator`.ExpiryStatus
// ----------------------------------
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * ExpiryCheckWorker.kt
 * Member 3 — WorkManager Background Task
 *
 * Runs daily in the background (even when the app is closed) to:
 * 1. Load all inventory items from the database (Member 2's DAO)
 * 2. Run ExpiryCalculator on each item
 * 3. Group items by status (CRITICAL / WATCH / EXPIRED)
 * 4. Fire push notifications via NotificationHelper
 *
 * HOW TO INTEGRATE WITH MEMBER 2 (DATABASE):
 * - In doWork(), replace the TODO block with a call to your DAO
 * - e.g. val items = AppDatabase.getInstance(applicationContext).inventoryDao().getAllItems()
 */
class ExpiryCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            // ── Step 1: Load inventory from database ──────────────────
            // TODO (Member 2 integration): Replace this with a real DB call.
            // Example:
            //   val db    = AppDatabase.getInstance(context)
            //   val items = db.inventoryDao().getAllItemsSync()
            //
            // For now, we use a placeholder data class to demonstrate the logic.
            val items = loadInventoryItems()

            // ── Step 2: Run expiry calculation on each item ────────────
            val criticalItems = mutableListOf<String>()
            val watchItems    = mutableListOf<String>()
            val expiredItems  = mutableListOf<String>()

            for (item in items) {
                val expiryDate = item.expiryDate
                    ?: ExpiryCalculator.estimateExpiryDateByName(item.dateAdded, item.name)

                val result = ExpiryCalculator.calculate(expiryDate)

                when (result.status) {
                    ExpiryStatus.CRITICAL -> criticalItems.add(item.name)
                    ExpiryStatus.WATCH    -> watchItems.add(item.name)
                    ExpiryStatus.EXPIRED  -> expiredItems.add(item.name)
                    ExpiryStatus.FRESH    -> { /* no notification needed */ }
                }
            }

            // ── Step 3: Send notifications ─────────────────────────────
            NotificationHelper.sendCriticalExpiryNotification(context, criticalItems)
            NotificationHelper.sendWatchExpiryNotification(context, watchItems)
            NotificationHelper.sendExpiredNotification(context, expiredItems)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // ENHANCEMENT: Professional retry logic. Don't retry infinitely and drain battery.
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Placeholder model — replace with Member 2's InventoryItem entity
    // ─────────────────────────────────────────────────────────────

    /**
     * Temporary data class mirroring what Member 2's database entity will provide.
     * Once Member 2's InventoryItem is ready, delete this and use theirs directly.
     */
    data class InventoryItemPlaceholder(
        val id: Int,
        val name: String,
        val category: String,
        val dateAdded: LocalDate,
        val expiryDate: LocalDate?   // null if no manufacturer date — we'll estimate
    )

    /**
     * TODO: Replace this entire function with a real DB call (Member 2).
     * This is just a stub so the Worker compiles and can be tested now.
     */
    private fun loadInventoryItems(): List<InventoryItemPlaceholder> {
        // Stub — returns empty list until DB is connected
        return listOf(
            // Expired item (Will trigger EXPIRED notification)
            InventoryItemPlaceholder(1, "Old Bread", "Bakery", LocalDate.now().minusDays(10), LocalDate.now().minusDays(2)),

            // Critical item (Will trigger CRITICAL notification)
            InventoryItemPlaceholder(2, "Fresh Milk", "Dairy", LocalDate.now(), LocalDate.now().plusDays(2)),

            // Watch item (Will trigger WATCH notification)
            InventoryItemPlaceholder(3, "Yogurt", "Dairy", LocalDate.now(), LocalDate.now().plusDays(6)),

            // Fresh item (Will NOT trigger any notification)
            InventoryItemPlaceholder(4, "Canned Beans", "Pantry", LocalDate.now(), LocalDate.now().plusDays(30))
        )
    } // <-- Removed the extra '}' that was here!

    // ─────────────────────────────────────────────────────────────
    // Static scheduler — call this from Application or MainActivity
    // ─────────────────────────────────────────────────────────────

    companion object {

        private const val WORK_NAME = "ExpiryCheckWorker_Daily"

        /**
         * Schedules the daily background check.
         * Call this once in your Application.onCreate() or MainActivity.
         *
         * Usage:
         * ExpiryCheckWorker.scheduleDailyCheck(applicationContext)
         */
        fun scheduleDailyCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true) // ENHANCEMENT: Avoid crashing phones with full storage
                .build()

            // CHANGED: 15 Minutes is the absolute lowest Android allows
            val dailyWorkRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                // ENHANCEMENT: Smart backoff if it fails (wait 10 mins before retrying)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                // CHANGED: Removed the Initial Delay so the first one happens quickly
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }
        /*
        fun scheduleDailyCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true) // don't run on critically low battery
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS) // wait 1 hour after first install
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // don't restart if already scheduled
                dailyWorkRequest
            )
        }
        */
        /**
         * Cancels the scheduled background check.
         * Call this if the user disables notifications in settings.
         */
        fun cancelDailyCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Runs the check IMMEDIATELY (one-time).
         * Useful for testing without waiting 24 hours.
         *
         * Usage:
         * ExpiryCheckWorker.runNow(applicationContext)
         */
        fun runNow(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<ExpiryCheckWorker>().build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}