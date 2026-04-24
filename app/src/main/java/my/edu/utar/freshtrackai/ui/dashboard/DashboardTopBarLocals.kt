package my.edu.utar.freshtrackai.ui.dashboard

import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf

internal data class DashboardTopBarController(
    val currentAiTask: DashboardAiTaskState?,
    val setAiTask: (DashboardAiTaskState?) -> Unit
)

internal data class DashboardAiTaskState(
    val title: String,
    val detail: String,
    val allowNavigationAway: Boolean = false
)

internal val LocalDashboardTopBarController =
    staticCompositionLocalOf<DashboardTopBarController?> { null }
