package my.edu.utar.freshtrackai.ui.dashboard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.R
import my.edu.utar.freshtrackai.ai.GemmaModelStatus
import my.edu.utar.freshtrackai.ai.GemmaModelStore
import my.edu.utar.freshtrackai.logic.NotificationHelper

@Composable
internal fun DashboardTopBar(
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val controller = LocalDashboardTopBarController.current
    val coroutineScope = rememberCoroutineScope()
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var gemmaStatus by rememberSaveable {
        mutableStateOf(GemmaModelStore.getModelStatus(context).name)
    }
    LaunchedEffect(showSettingsSheet) {
        if (showSettingsSheet) {
            gemmaStatus = GemmaModelStore.getModelStatus(context).name
        }
    }


    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(if (showBack) 40.dp else 0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (showBack && onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                }
            }
            Image(
                painter = painterResource(id = R.drawable.ic_freshtrack_logo_focus),
                contentDescription = "Fresh Track logo",
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Fresh Track AI", color = Slate900, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            if (controller != null) {
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile and settings", tint = Slate900)
                }
            }
        }
        HorizontalDivider(color = Gray200)
    }

    if (showSettingsSheet && controller != null) {
        ProfileQuickSettingsSheet(
            gemmaStatus = GemmaModelStatus.valueOf(gemmaStatus),
            notificationsGranted = NotificationHelper.hasNotificationPermission(context),
            onDismiss = { showSettingsSheet = false },
            onOpenNotificationSettings = { NotificationHelper.openNotificationSettings(context) },
            onSendTestNotification = { NotificationHelper.sendTestNotification(context) }
        )
    }
}

@Composable
internal fun BottomNav(active: RootTab, onTabSelected: (RootTab) -> Unit) {
    Column {
        HorizontalDivider(color = Gray200)
        NavigationBar(containerColor = White, modifier = Modifier.navigationBarsPadding()) {
            RootTab.entries.forEach { tab ->
                val activeColor = if (tab == active) Emerald else Slate600
                NavigationBarItem(
                    selected = tab == active,
                    onClick = { onTabSelected(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.label, tint = activeColor) },
                    label = { Text(tab.label, color = activeColor) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileQuickSettingsSheet(
    gemmaStatus: GemmaModelStatus,
    notificationsGranted: Boolean,
    onDismiss: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onSendTestNotification: () -> Unit
) {
    val gemmaStatusLabel = when (gemmaStatus) {
        GemmaModelStatus.Ready -> "Ready"
        GemmaModelStatus.Missing -> "Missing"
    }

    val gemmaStatusColor = when (gemmaStatus) {
        GemmaModelStatus.Ready -> Emerald
        GemmaModelStatus.Missing -> RoseRed
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Profile & Quick Settings", color = Slate900, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Text(
                "Real device and app status controls for Fresh Track.",
                color = Slate600
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Gray100)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Memory, contentDescription = null, tint = Emerald)
                            Text("Gemma 4 Status", color = Slate900, fontWeight = FontWeight.SemiBold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = gemmaStatusColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                gemmaStatusLabel,
                                color = gemmaStatusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    /*Button(
                        onClick = onChooseGemmaModel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (gemmaStatus == GemmaModelStatus.NotSet) "Choose Model" else "Change Model"
                        )
                    }*/
                    Text(
                        text = if (gemmaStatus == GemmaModelStatus.Ready) {
                            "Bundled Gemma model is available for local food and receipt scanning."
                        } else {
                            "Bundled Gemma model is missing. Please reinstall the app or check app assets."
                        },
                        color = Slate600
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Gray100)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Notifications", color = Slate900, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (notificationsGranted) "Permission granted" else "Permission required",
                        color = Slate600
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenNotificationSettings,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Open Settings", color = Slate900)
                        }
                        Button(
                            onClick = onSendTestNotification,
                            enabled = notificationsGranted,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send Test")
                        }
                    }
                }
            }
        }
    }
}
