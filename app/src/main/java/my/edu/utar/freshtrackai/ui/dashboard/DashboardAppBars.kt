package my.edu.utar.freshtrackai.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import my.edu.utar.freshtrackai.R

@Composable
internal fun DashboardTopBar(
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var gemmaConnected by rememberSaveable { mutableStateOf(true) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var unitsMetric by rememberSaveable { mutableStateOf(true) }
    var avoidSpicy by rememberSaveable { mutableStateOf(false) }
    var avoidOnion by rememberSaveable { mutableStateOf(false) }
    var avoidCoriander by rememberSaveable { mutableStateOf(false) }

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
            IconButton(onClick = { showSettingsSheet = true }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile and settings", tint = Slate900)
            }
        }
        HorizontalDivider(color = Gray200)
    }

    if (showSettingsSheet) {
        ProfileQuickSettingsSheet(
            gemmaConnected = gemmaConnected,
            notificationsEnabled = notificationsEnabled,
            unitsMetric = unitsMetric,
            avoidSpicy = avoidSpicy,
            avoidOnion = avoidOnion,
            avoidCoriander = avoidCoriander,
            onDismiss = { showSettingsSheet = false },
            onGemmaConnectedChange = { gemmaConnected = it },
            onNotificationsChange = { notificationsEnabled = it },
            onUnitsMetricChange = { unitsMetric = it },
            onAvoidSpicyChange = { avoidSpicy = it },
            onAvoidOnionChange = { avoidOnion = it },
            onAvoidCorianderChange = { avoidCoriander = it }
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
    gemmaConnected: Boolean,
    notificationsEnabled: Boolean,
    unitsMetric: Boolean,
    avoidSpicy: Boolean,
    avoidOnion: Boolean,
    avoidCoriander: Boolean,
    onDismiss: () -> Unit,
    onGemmaConnectedChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onUnitsMetricChange: (Boolean) -> Unit,
    onAvoidSpicyChange: (Boolean) -> Unit,
    onAvoidOnionChange: (Boolean) -> Unit,
    onAvoidCorianderChange: (Boolean) -> Unit
) {
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
                "Session-level frontend settings for Fresh Track.",
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
                                tint = if (gemmaConnected) Emerald else RoseRed,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                if (gemmaConnected) "Connected" else "Offline",
                                color = if (gemmaConnected) Emerald else RoseRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Button(
                        onClick = { onGemmaConnectedChange(!gemmaConnected) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (gemmaConnected) "Set Offline (Mock)" else "Reconnect (Mock)")
                    }
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
                    Text("Dietary Defaults", color = Slate900, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = avoidSpicy,
                            onClick = { onAvoidSpicyChange(!avoidSpicy) },
                            label = { Text("Spicy") }
                        )
                        FilterChip(
                            selected = avoidOnion,
                            onClick = { onAvoidOnionChange(!avoidOnion) },
                            label = { Text("Onion") }
                        )
                        FilterChip(
                            selected = avoidCoriander,
                            onClick = { onAvoidCorianderChange(!avoidCoriander) },
                            label = { Text("Coriander") }
                        )
                    }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifications", color = Slate900, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationsChange
                        )
                    }

                    Text("Units", color = Slate900, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onUnitsMetricChange(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (unitsMetric) Emerald else White,
                                contentColor = if (unitsMetric) White else Slate900
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Metric")
                        }
                        OutlinedButton(
                            onClick = { onUnitsMetricChange(false) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Imperial", color = Slate900)
                        }
                    }
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Account", color = Slate900, fontWeight = FontWeight.SemiBold)
                    Text("Ian Ho (Mock)", color = Slate900, fontWeight = FontWeight.Bold)
                    Text("Plan: Gemma 4 Integrated", color = Slate600)
                }
            }
        }
    }
}

