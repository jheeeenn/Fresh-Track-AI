// ─────────────────────────────────────────────────────────────────────────────
// DROP-IN REPLACEMENT for the Nutritional Info Card section inside
// AddMissingItemScreen (ItemReviewScreens.kt).
//
// WHAT CHANGED:
//   1. "Quick Scan Label" button now launches the camera/gallery and calls
//      NutritionAiHelper.getNutritionFromLabel() to OCR the label.
//   2. A new "AI Auto-fill" button calls NutritionAiHelper.estimateNutritionByName()
//      when no image is available.
//   3. Both show a CircularProgressIndicator while loading.
//   4. On save (onSubmit), if nutritionNotes is still blank, auto-fill fires.
//
// HOW TO INTEGRATE:
//   1. Add `NutritionAiHelper.kt` to app/src/main/java/.../logic/
//   2. In AddMissingItemScreen, replace the Nutritional Info Card `item { }` block
//      with the NutritionalInfoCard composable below.
//   3. Pass `draft`, `onDraftChange`, and `coroutineScope` as shown.
// ─────────────────────────────────────────────────────────────────────────────

package my.edu.utar.freshtrackai.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.*

/**
 * Nutritional Info card for AddMissingItemScreen.
 *
 * Features:
 *  • Editable multiline text field for manual entry.
 *  • "Quick Scan Label" – camera capture → Gemini Vision OCR.
 *  • "AI Auto-fill"     – name-based nutrition estimate via Gemini.
 *  • Loading states for both buttons.
 *  • Toast-style error display on failure.
 */
@Composable
internal fun NutritionalInfoCard(
    draft: AddItemFormDraft,
    onDraftChange: (AddItemFormDraft) -> Unit,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    var scanLoading by remember { mutableStateOf(false) }
    var aiLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Camera URI holder ──────────────────────────────────────────────────
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // ── Camera launcher ───────────────────────────────────────────────────
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
            errorMessage = null
            val result = NutritionAiHelper.getNutritionFromLabelUri(context, uri)
            when (result) {
                is NutritionAiHelper.NutritionResult.Success ->
                    onDraftChange(draft.copy(nutritionNotes = result.text))
                is NutritionAiHelper.NutritionResult.Failure ->
                    errorMessage = result.reason
            }
            scanLoading = false
            pendingCameraUri = null
        }
    }

    // ── Gallery launcher ──────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            scanLoading = false
            return@rememberLauncherForActivityResult
        }
        coroutineScope.launch {
            scanLoading = true
            errorMessage = null
            val result = NutritionAiHelper.getNutritionFromLabelUri(context, uri)
            when (result) {
                is NutritionAiHelper.NutritionResult.Success ->
                    onDraftChange(draft.copy(nutritionNotes = result.text))
                is NutritionAiHelper.NutritionResult.Failure ->
                    errorMessage = result.reason
            }
            scanLoading = false
        }
    }

    // ── Helper: create a temp file URI for camera capture ─────────────────
    fun createTempUri(): Uri {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File.createTempFile("nutrition_label_${ts}_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // ── UI ─────────────────────────────────────────────────────────────────
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
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🍎", fontSize = 18.sp)
                Text("Nutritional Info", color = Slate900, fontWeight = FontWeight.Bold)
            }

            // Text field
            OutlinedTextField(
                value = draft.nutritionNotes,
                onValueChange = { onDraftChange(draft.copy(nutritionNotes = it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 23 kcal / 100g, high fiber, low sodium") },
                colors = freshOutlinedTextFieldColors(),
                minLines = 3
            )

            // Error message
            if (errorMessage != null) {
                Text(
                    text = "⚠ $errorMessage",
                    color = RoseRed,
                    fontSize = 12.sp
                )
            }

            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ── Quick Scan Label button (camera + gallery) ─────────────
                OutlinedButton(
                    onClick = {
                        // Show a simple choice: Camera or Gallery.
                        // For simplicity we launch camera first; to add gallery
                        // you can use a DropdownMenu or AlertDialog (see comment below).
                        val uri = createTempUri()
                        pendingCameraUri = uri
                        scanLoading = true
                        cameraLauncher.launch(uri)
                    },
                    enabled = !scanLoading && !aiLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gray200),
                    modifier = Modifier.weight(1f)
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
                        if (scanLoading) "Scanning…" else "Scan Label",
                        color = Slate900,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // ── AI Auto-fill button ────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            aiLoading = true
                            errorMessage = null
                            val result = NutritionAiHelper.estimateNutritionByName(
                                itemName = draft.name.ifBlank { "Unknown item" },
                                categoryName = draft.category.label
                            )
                            when (result) {
                                is NutritionAiHelper.NutritionResult.Success ->
                                    onDraftChange(draft.copy(nutritionNotes = result.text))
                                is NutritionAiHelper.NutritionResult.Failure ->
                                    errorMessage = result.reason
                            }
                            aiLoading = false
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
                        if (aiLoading) "AI…" else "AI Fill",
                        color = Slate900,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Hint text
            Text(
                "Scan the label for exact values, or let AI estimate from the item name.",
                color = Slate600,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HOW TO USE NutritionalInfoCard in AddMissingItemScreen
// ─────────────────────────────────────────────────────────────────────────────
//
// 1. Add a coroutineScope reference at the top of AddMissingItemScreen:
//      val coroutineScope = rememberCoroutineScope()
//
// 2. Replace the existing Nutritional Info `item { Card { ... } }` block with:
//      item {
//          NutritionalInfoCard(
//              draft = draft,
//              onDraftChange = onDraftChange,
//              coroutineScope = coroutineScope
//          )
//      }
//
// 3. In the onSubmit block (DashboardRoute.kt), add auto-fill BEFORE saving:
//
//      onSubmit = {
//          coroutineScope.launch {
//              // Auto-fill nutrition if blank
//              if (NutritionAiHelper.shouldAutoFill(addFormDraft.nutritionNotes)) {
//                  val result = NutritionAiHelper.estimateNutritionByName(
//                      itemName = addFormDraft.name,
//                      categoryName = addFormDraft.category.label
//                  )
//                  if (result is NutritionAiHelper.NutritionResult.Success) {
//                      addFormDraft = addFormDraft.copy(nutritionNotes = result.text)
//                  }
//              }
//              // ... rest of your existing submit logic
//          }
//      }
// ─────────────────────────────────────────────────────────────────────────────