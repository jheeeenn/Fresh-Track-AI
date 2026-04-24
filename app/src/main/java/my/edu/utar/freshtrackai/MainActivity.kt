package my.edu.utar.freshtrackai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import my.edu.utar.freshtrackai.ai.AppContextProvider

import my.edu.utar.freshtrackai.ai.GemmaModelStore
import my.edu.utar.freshtrackai.logic.ExpiryCheckWorker
import my.edu.utar.freshtrackai.logic.NotificationHelper
import my.edu.utar.freshtrackai.ui.dashboard.FreshTrackDashboardScreen
import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

class MainActivity : ComponentActivity() {
    private val prefs by lazy {
        getSharedPreferences("freshtrack_prefs", MODE_PRIVATE)
    }

    private var notificationPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContextProvider.initialize(applicationContext)

        NotificationHelper.createNotificationChannels(this)
        ExpiryCheckWorker.scheduleDailyCheck(applicationContext)

        enableEdgeToEdge()

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            prefs.edit()
                .putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
                .apply()
            Log.d("NOTIF_PERMISSION", "Notification permission granted = $granted")
        }

        val bundledModelResult = GemmaModelStore.ensureBundledModelReady(this)
        bundledModelResult
            .onSuccess { savedPath ->
                Log.d("GEMMA_MODEL", "Bundled Gemma model ready at: $savedPath")
            }
            .onFailure { error ->
                Log.e("GEMMA_MODEL", "Failed to prepare bundled Gemma model", error)
            }

        maybeRequestNotificationPermission()



        setContent {
            FreshTrackAITheme(darkTheme = false, dynamicColor = false) {
                FreshTrackDashboardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val wasRequested = prefs.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false)
        val canReRequest = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

        if (wasRequested && !canReRequest) {
            Log.d("NOTIF_PERMISSION", "Notification permission denied permanently or already handled.")
            return
        }

        notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
    }
}
