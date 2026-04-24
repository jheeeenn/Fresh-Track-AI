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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.R
import my.edu.utar.freshtrackai.ai.GeminiApiKeyValidationResult
import my.edu.utar.freshtrackai.ai.GemmaModelDownloadManager
import my.edu.utar.freshtrackai.ai.GemmaModelDownloadStatus
import my.edu.utar.freshtrackai.ai.GemmaModelStatus
import my.edu.utar.freshtrackai.ai.GemmaModelStore
import my.edu.utar.freshtrackai.ai.gemmaDownloadStatusMessage
import my.edu.utar.freshtrackai.ai.validateGeminiApiKey
import my.edu.utar.freshtrackai.logic.NotificationHelper

internal enum class ApiKeyStatusTone {
    Neutral,
    Success,
    Error
}

internal data class ApiKeyStatusUi(
    val message: String,
    val tone: ApiKeyStatusTone
)

internal fun apiKeyStatusForValidationResult(
    result: GeminiApiKeyValidationResult
): ApiKeyStatusUi {
    return when (result) {
        GeminiApiKeyValidationResult.NotSet ->
            ApiKeyStatusUi("No API key entered.", ApiKeyStatusTone.Neutral)
        GeminiApiKeyValidationResult.Valid ->
            ApiKeyStatusUi("API key is valid.", ApiKeyStatusTone.Success)
        GeminiApiKeyValidationResult.InvalidKey ->
            ApiKeyStatusUi("API key is invalid.", ApiKeyStatusTone.Error)
        GeminiApiKeyValidationResult.QuotaExhausted ->
            ApiKeyStatusUi("API key works, but quota/credits are exhausted.", ApiKeyStatusTone.Error)
        GeminiApiKeyValidationResult.RequestFailed ->
            ApiKeyStatusUi("Request failed. Check connection or try again.", ApiKeyStatusTone.Error)
    }
}

@Composable
internal fun DashboardTopBar(
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val controller = LocalDashboardTopBarController.current
    val coroutineScope = rememberCoroutineScope()
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var gemmaStatus by rememberSaveable { mutableStateOf(GemmaModelStore.getModelStatus(context).name) }
    var gemmaDownloadStatus by rememberSaveable { mutableStateOf("") }
    var apiKeyDraft by rememberSaveable { mutableStateOf("") }
    var apiKeyStatusMessage by rememberSaveable { mutableStateOf("No API key saved.") }
    var apiKeyStatusTone by rememberSaveable { mutableStateOf(ApiKeyStatusTone.Neutral.name) }

    val gemmaModelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            controller?.setAiTask(
                DashboardAiTaskState(
                    title = "Preparing Gemma 4 Model…",
                    detail = "Please stay on this page while the model file is copied."
                )
            )

            val result = withContext(Dispatchers.IO) {
                GemmaModelStore.importModelFromUri(context, uri)
            }

            controller?.setAiTask(null)
            gemmaStatus = GemmaModelStore.getModelStatus(context).name

            result.exceptionOrNull()?.let {
                Toast.makeText(
                    context,
                    it.message ?: "Failed to import Gemma model.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(showSettingsSheet) {
        if (showSettingsSheet) {
            gemmaStatus = GemmaModelStore.getModelStatus(context).name
            val savedDownloadId = DashboardPreferencesStore.loadGemmaDownloadId(context)
            gemmaDownloadStatus = savedDownloadId?.let { downloadId ->
                gemmaDownloadStatusMessage(
                    GemmaModelDownloadManager.queryDownloadStatus(context, downloadId)
                )
            }.orEmpty()
            val savedKey = DashboardPreferencesStore.loadGeminiApiKey(context)
            apiKeyDraft = savedKey
            if (savedKey.isBlank()) {
                apiKeyStatusMessage = "No API key saved."
                apiKeyStatusTone = ApiKeyStatusTone.Neutral.name
            } else {
                apiKeyStatusMessage = "API key saved."
                apiKeyStatusTone = ApiKeyStatusTone.Success.name
            }
        }
    }

    LaunchedEffect(showSettingsSheet) {
        if (!showSettingsSheet) return@LaunchedEffect

        while (showSettingsSheet) {
            val downloadId = DashboardPreferencesStore.loadGemmaDownloadId(context)
            if (downloadId == null) {
                delay(1000)
                continue
            }

            when (val status = GemmaModelDownloadManager.queryDownloadStatus(context, downloadId)) {
                GemmaModelDownloadStatus.Successful -> {
                    gemmaDownloadStatus = gemmaDownloadStatusMessage(status)
                    DashboardPreferencesStore.clearGemmaDownloadId(context)
                    return@LaunchedEffect
                }
                is GemmaModelDownloadStatus.Failed,
                GemmaModelDownloadStatus.NotFound -> {
                    gemmaDownloadStatus = gemmaDownloadStatusMessage(status)
                    DashboardPreferencesStore.clearGemmaDownloadId(context)
                    return@LaunchedEffect
                }
                else -> {
                    gemmaDownloadStatus = gemmaDownloadStatusMessage(status)
                }
            }

            delay(1000)
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
            apiKeyValue = apiKeyDraft,
            apiKeyStatusMessage = apiKeyStatusMessage,
            apiKeyStatusTone = ApiKeyStatusTone.valueOf(apiKeyStatusTone),
            gemmaDownloadStatus = gemmaDownloadStatus,
            onDismiss = { showSettingsSheet = false },
            onDownloadGemmaModel = {
                runCatching { GemmaModelDownloadManager.enqueueDownload(context) }
                    .onSuccess { downloadId ->
                        DashboardPreferencesStore.saveGemmaDownloadId(context, downloadId)
                        gemmaDownloadStatus =
                            gemmaDownloadStatusMessage(GemmaModelDownloadStatus.Pending)
                        Toast.makeText(
                            context,
                            "Download started.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onFailure {
                        Toast.makeText(
                            context,
                            it.message ?: "Failed to start model download.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            },
            onChooseGemmaModel = { gemmaModelLauncher.launch(arrayOf("*/*")) },
            onOpenNotificationSettings = { NotificationHelper.openNotificationSettings(context) },
            onApiKeyChange = { apiKeyDraft = it },
            onSaveApiKey = {
                val normalized = apiKeyDraft.trim()
                if (normalized.isBlank()) {
                    apiKeyStatusMessage = "Enter an API key first."
                    apiKeyStatusTone = ApiKeyStatusTone.Error.name
                } else {
                    DashboardPreferencesStore.saveGeminiApiKey(context, normalized)
                    apiKeyDraft = normalized
                    apiKeyStatusMessage = "API key saved."
                    apiKeyStatusTone = ApiKeyStatusTone.Success.name
                }
            },
            onClearApiKey = {
                DashboardPreferencesStore.clearGeminiApiKey(context)
                apiKeyDraft = ""
                apiKeyStatusMessage = "API key cleared."
                apiKeyStatusTone = ApiKeyStatusTone.Neutral.name
            },
            onTestApiKey = {
                coroutineScope.launch {
                    controller?.setAiTask(
                        DashboardAiTaskState(
                            title = "Testing API key…",
                            detail = "Checking the external Gemini service."
                        )
                    )
                    val result = withContext(Dispatchers.IO) {
                        validateGeminiApiKey(apiKeyDraft)
                    }
                    controller?.setAiTask(null)
                    val status = apiKeyStatusForValidationResult(result)
                    apiKeyStatusMessage = status.message
                    apiKeyStatusTone = status.tone.name
                }
            }
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
    apiKeyValue: String,
    apiKeyStatusMessage: String,
    apiKeyStatusTone: ApiKeyStatusTone,
    gemmaDownloadStatus: String,
    onDismiss: () -> Unit,
    onDownloadGemmaModel: () -> Unit,
    onChooseGemmaModel: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onTestApiKey: () -> Unit
) {
    val gemmaStatusLabel = when (gemmaStatus) {
        GemmaModelStatus.Configured -> "Configured"
        GemmaModelStatus.MissingFile -> "Missing file"
        GemmaModelStatus.NotSet -> "Not set"
    }
    val gemmaStatusColor = when (gemmaStatus) {
        GemmaModelStatus.Configured -> Emerald
        GemmaModelStatus.MissingFile -> RoseRed
        GemmaModelStatus.NotSet -> Slate600
    }
    val apiKeyStatusColor = when (apiKeyStatusTone) {
        ApiKeyStatusTone.Neutral -> Slate600
        ApiKeyStatusTone.Success -> Emerald
        ApiKeyStatusTone.Error -> RoseRed
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
                    Text(
                        "Download the Gemma 4 file to Downloads, then tap Choose Model to import it into the app.",
                        color = Slate600,
                        fontSize = 12.sp
                    )
                    if (gemmaDownloadStatus.isNotBlank()) {
                        Text(
                            gemmaDownloadStatus,
                            color = Slate600,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedButton(
                        onClick = onDownloadGemmaModel,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Download Gemma 4", color = Slate900)
                    }
                    Button(
                        onClick = onChooseGemmaModel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Choose Model"
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
                    Text("External API Key", color = Slate900, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Enter your Gemini API key for external AI features.",
                        color = Slate600
                    )
                    OutlinedTextField(
                        value = apiKeyValue,
                        onValueChange = onApiKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Gemini API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        placeholder = { Text("Paste your API key") },
                        colors = freshOutlinedTextFieldColors()
                    )
                    Text(
                        apiKeyStatusMessage,
                        color = apiKeyStatusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSaveApiKey,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = onClearApiKey,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear", color = Slate900)
                        }
                    }
                    Button(
                        onClick = onTestApiKey,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test API Key")
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
                    Text("Notifications", color = Slate900, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (notificationsGranted) "Permission granted" else "Permission required",
                        color = Slate600
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenNotificationSettings,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Settings", color = Slate900)
                        }
                    }
                }
            }
        }
    }
}
