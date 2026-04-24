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
import my.edu.utar.freshtrackai.ai.GemmaModelStatus
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

        val pickGemmaModelLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri == null) {
                Log.d("GEMMA_PICKER", "No model selected")
                return@registerForActivityResult
            }

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val savedPath = GemmaModelStore.importModelFromUri(this, uri)
                    .getOrElse { throw it }

                Log.d("GEMMA_PICKER", "Model saved to: $savedPath")
                maybeRequestNotificationPermission()
            } catch (e: Exception) {
                Log.e("GEMMA_PICKER", "Failed to save selected model", e)
            }
        }

        val gemmaStatus = GemmaModelStore.getModelStatus(this)
        if (shouldRequestGemmaModel(gemmaStatus)) {
            if (gemmaStatus == GemmaModelStatus.MissingFile) {
                Toast.makeText(
                    this,
                    "Selected Gemma model file is missing. Please choose it again.",
                    Toast.LENGTH_LONG
                ).show()
            }
            pickGemmaModelLauncher.launch(arrayOf("*/*"))
        } else {
            maybeRequestNotificationPermission()
        }

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

internal fun shouldRequestGemmaModel(status: GemmaModelStatus): Boolean {
    return status != GemmaModelStatus.Configured
}
