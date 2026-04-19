package my.edu.utar.freshtrackai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

// FIX: Removed duplicate imports so the file compiles cleanly
import my.edu.utar.freshtrackai.logic.ExpiryCheckWorker
import my.edu.utar.freshtrackai.logic.NotificationHelper
import my.edu.utar.freshtrackai.ui.dashboard.FreshTrackDashboardScreen
import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Member 3: Create notification channels and schedule worker
        NotificationHelper.createNotificationChannels(this)

        // Starts the 24-hour background checking loop
        ExpiryCheckWorker.scheduleDailyCheck(applicationContext)

        enableEdgeToEdge()
        setContent {
            FreshTrackAITheme(darkTheme = false, dynamicColor = false) {
                FreshTrackDashboardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}