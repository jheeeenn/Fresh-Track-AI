package my.edu.utar.freshtrackai.logic

import android.content.Context
import androidx.work.*
import kotlin.math.ceil
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.flow.firstOrNull
import my.edu.utar.freshtrackai.data.local.AppDatabase
import my.edu.utar.freshtrackai.logic.ExpiryCalculator.ExpiryStatus
import java.util.concurrent.TimeUnit

/**
 * ExpiryCheckWorker.kt
 * Member 3 — WorkManager Background Task
 *
 * Runs daily in the background (even when the app is closed) to:
 * 1. Load all inventory items from Member 2's real Room database
 * 2. Run ExpiryCalculator on each item using its stored expiryDate
 * 3. Group items by status (CRITICAL / WATCH / EXPIRED)
 * 4. Fire push notifications via NotificationHelper
 */
class ExpiryCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // ── Step 1: Load inventory from Member 2's real Room database ─
            val dao = AppDatabase.getDatabase(context).inventoryDao()
            // firstOrNull() collects the Flow once and returns the list
            val items = dao.getAllItems().firstOrNull() ?: emptyList()

            // ── Step 2: Run expiry calculation on each item ────────────────
            val criticalItems = mutableListOf<String>()
            val watchItems    = mutableListOf<String>()
            val expiredItems  = mutableListOf<String>()

            val currentTime = System.currentTimeMillis()

            for (item in items) {
                // item.expiryDate is stored as epoch millis (Long) in InventoryItem entity
                val daysRemaining = ceil(
                    (item.expiryDate - currentTime).toDouble() / (1000.0 * 60 * 60 * 24)
                ).toInt()

                when (ExpiryCalculator.getExpiryStatus(daysRemaining)) {
                    ExpiryStatus.CRITICAL -> criticalItems.add(item.name)
                    ExpiryStatus.WATCH    -> watchItems.add(item.name)
                    ExpiryStatus.EXPIRED  -> expiredItems.add(item.name)
                    ExpiryStatus.FRESH    -> { /* no notification needed */ }
                }
            }

            // ── Step 3: Send notifications ─────────────────────────────────
            NotificationHelper.sendCriticalExpiryNotification(context, criticalItems)
            NotificationHelper.sendWatchExpiryNotification(context, watchItems)
            NotificationHelper.sendExpiredNotification(context, expiredItems)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {

        private const val WORK_NAME = "ExpiryCheckWorker_Daily"

        /**
         * Schedules a true 24-hour periodic background check.
         * Call once from MainActivity or Application.onCreate().
         */
        fun scheduleDailyCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .setInitialDelay(1, TimeUnit.HOURS) // wait 1 hour after first install
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }

        /**
         * Cancels the scheduled daily check.
         * Call this if the user disables notifications in settings.
         */
        fun cancelDailyCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Runs the expiry check IMMEDIATELY as a one-time job.
         * Useful for the dev test panel and for testing without waiting 24 hours.
         */
        fun runNow(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<ExpiryCheckWorker>().build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}