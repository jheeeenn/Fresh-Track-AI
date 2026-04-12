package my.edu.utar.freshtrackai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import my.edu.utar.freshtrackai.ui.dashboard.FreshTrackDashboardScreen
import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshTrackAITheme(darkTheme = false, dynamicColor = false) {
                FreshTrackDashboardScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
