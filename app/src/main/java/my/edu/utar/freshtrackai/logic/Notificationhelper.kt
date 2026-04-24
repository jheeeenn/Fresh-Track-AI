package my.edu.utar.freshtrackai.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    const val CHANNEL_ID_EXPIRY = "expiry_reminder_channel"
    const val CHANNEL_ID_EXPIRED = "expired_items_channel"

    private const val CHANNEL_NAME_EXPIRY = "Expiry Reminders"
    private const val CHANNEL_NAME_EXPIRED = "Expired Items"
    private const val CHANNEL_DESC_EXPIRY = "Alerts for food items nearing their expiry date"
    private const val CHANNEL_DESC_EXPIRED = "Alerts for food items that have already expired"
    private const val GROUP_KEY_EXPIRY = "my.edu.utar.freshtrackai.EXPIRY_ALERTS"

    private const val NOTIF_ID_CRITICAL = 1001
    private const val NOTIF_ID_WATCH = 1002
    private const val NOTIF_ID_EXPIRED = 1003
    private const val NOTIF_ID_TEST = 1099

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val expiryChannel = NotificationChannel(
                CHANNEL_ID_EXPIRY,
                CHANNEL_NAME_EXPIRY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_EXPIRY
                enableVibration(true)
            }

            val expiredChannel = NotificationChannel(
                CHANNEL_ID_EXPIRED,
                CHANNEL_NAME_EXPIRED,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_EXPIRED
            }

            manager.createNotificationChannel(expiryChannel)
            manager.createNotificationChannel(expiredChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun sendCriticalExpiryNotification(context: Context, itemNames: List<String>) {
        if (itemNames.isEmpty()) return

        val title = "Food Expiring Soon"
        val body = if (itemNames.size == 1) {
            "${itemNames[0]} expires within 3 days. Use it now!"
        } else {
            "${itemNames.size} items expiring within 3 days: ${itemNames.joinToString(", ")}"
        }

        sendNotification(
            context = context,
            notifId = NOTIF_ID_CRITICAL,
            channelId = CHANNEL_ID_EXPIRY,
            title = title,
            body = body,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    fun sendWatchExpiryNotification(context: Context, itemNames: List<String>) {
        if (itemNames.isEmpty()) return

        val title = "Use These Items Soon"
        val body = if (itemNames.size == 1) {
            "${itemNames[0]} expires within a week."
        } else {
            "${itemNames.size} items expiring this week: ${itemNames.joinToString(", ")}"
        }

        sendNotification(
            context = context,
            notifId = NOTIF_ID_WATCH,
            channelId = CHANNEL_ID_EXPIRY,
            title = title,
            body = body,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun sendExpiredNotification(context: Context, itemNames: List<String>) {
        if (itemNames.isEmpty()) return

        val title = "Expired Items in Your Inventory"
        val body = if (itemNames.size == 1) {
            "${itemNames[0]} has expired. Please remove it."
        } else {
            "${itemNames.size} items have expired: ${itemNames.joinToString(", ")}"
        }

        sendNotification(
            context = context,
            notifId = NOTIF_ID_EXPIRED,
            channelId = CHANNEL_ID_EXPIRED,
            title = title,
            body = body,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun sendTestNotification(context: Context) {
        sendNotification(
            context = context,
            notifId = NOTIF_ID_TEST,
            channelId = CHANNEL_ID_EXPIRY,
            title = "Fresh Track Test Notification",
            body = "Notifications are configured and working on this device.",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun sendNotification(
        context: Context,
        notifId: Int,
        channelId: String,
        title: String,
        body: String,
        priority: Int
    ) {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setGroup(GROUP_KEY_EXPIRY)
            .setAutoCancel(true)
            .apply { pendingIntent?.let { setContentIntent(it) } }
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
        }
    }
}
