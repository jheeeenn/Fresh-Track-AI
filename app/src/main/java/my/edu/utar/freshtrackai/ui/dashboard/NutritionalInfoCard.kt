package my.edu.utar.freshtrackai.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.logic.NutritionAiHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun NutritionalInfoCard(
    draft: AddItemFormDraft,
    onDraftChange: (AddItemFormDraft) -> Unit,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val dashboardController = LocalDashboardTopBarController.current
    var scanLoading by remember { mutableStateOf(false) }
    var aiLoading by remember { mutableStateOf(false) }
    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (!success || uri == null) {
            scanLoading = false
            pendingCameraUri = null
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            scanLoading = true
            dashboardController?.setAiTask(
                DashboardAiTaskState(
                    title = "Scanning nutrition label locally...",
                    detail = "Please stay on this page while AI finishes."
                )
            )
            errorMessage = null

            when (val result = NutritionAiHelper.getNutritionFromLabelUri(context, uri)) {
                is NutritionAiHelper.NutritionResult.Success ->
                    onDraftChange(draft.copy(nutritionNotes = result.text))

                is NutritionAiHelper.NutritionResult.Failure ->
                    errorMessage = result.reason
            }

            scanLoading = false
            pendingCameraUri = null
            dashboardController?.setAiTask(null)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            scanLoading = false
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            scanLoading = true
            dashboardController?.setAiTask(
                DashboardAiTaskState(
                    title = "Scanning nutrition label locally...",
                    detail = "Please stay on this page while AI finishes."
                )
            )
            errorMessage = null

            when (val result = NutritionAiHelper.getNutritionFromLabelUri(context, uri)) {
                is NutritionAiHelper.NutritionResult.Success ->
                    onDraftChange(draft.copy(nutritionNotes = result.text))

                is NutritionAiHelper.NutritionResult.Failure ->
                    errorMessage = result.reason
            }

            scanLoading = false
            dashboardController?.setAiTask(null)
        }
    }

    fun createTempUri(): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File.createTempFile("nutrition_label_${timestamp}_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Nutritional Info",
                color = Slate900,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = draft.nutritionNotes,
                onValueChange = { onDraftChange(draft.copy(nutritionNotes = it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 23 kcal / 100g, high fiber, low sodium") },
                colors = freshOutlinedTextFieldColors(),
                minLines = 3
            )

            if (errorMessage != null) {
                Text(
                    text = "Warning: $errorMessage",
                    color = RoseRed,
                    fontSize = 12.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { sourceMenuExpanded = true },
                        enabled = !scanLoading && !aiLoading,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Gray200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (scanLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Emerald
                            )
                        } else {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (scanLoading) "Scanning..." else "Scan Label",
                            color = Slate900,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    DropdownMenu(
                        expanded = sourceMenuExpanded,
                        onDismissRequest = { sourceMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Camera") },
                            onClick = {
                                sourceMenuExpanded = false
                                val uri = createTempUri()
                                pendingCameraUri = uri
                                scanLoading = true
                                cameraLauncher.launch(uri)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gallery") },
                            onClick = {
                                sourceMenuExpanded = false
                                scanLoading = true
                                galleryLauncher.launch("image/*")
                            }
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            aiLoading = true
                            dashboardController?.setAiTask(
                                DashboardAiTaskState(
                                    title = "Estimating nutrition with local Gemma...",
                                    detail = "Please stay on this page while AI finishes."
                                )
                            )
                            errorMessage = null

                            when (
                                val result = NutritionAiHelper.estimateNutritionByName(
                                    itemName = draft.name.ifBlank { "Unknown item" },
                                    categoryName = draft.category.label
                                )
                            ) {
                                is NutritionAiHelper.NutritionResult.Success ->
                                    onDraftChange(draft.copy(nutritionNotes = result.text))

                                is NutritionAiHelper.NutritionResult.Failure ->
                                    errorMessage = result.reason
                            }

                            aiLoading = false
                            dashboardController?.setAiTask(null)
                        }
                    },
                    enabled = !scanLoading && !aiLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gray200),
                    modifier = Modifier.weight(1f)
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Emerald
                        )
                    } else {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Emerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (aiLoading) "AI..." else "AI Fill",
                        color = Slate900,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                "Scan the label for exact values, or let local AI estimate from the item name.",
                color = Slate600,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
