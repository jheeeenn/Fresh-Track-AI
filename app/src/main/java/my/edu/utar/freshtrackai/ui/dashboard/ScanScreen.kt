package my.edu.utar.freshtrackai.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import android.content.Context
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SmartScanScreen(
    onDone: (ScanMode, List<ScanCapture>) -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    val maxPhotos = 4
    var scanMode by rememberSaveable { mutableStateOf(ScanMode.Food) }
    var gemmaConnected by rememberSaveable { mutableStateOf(true) }
    var flashOn by rememberSaveable { mutableStateOf(false) }
    var helperText by rememberSaveable { mutableStateOf("Align food items within the frame") }

    val captures = remember { mutableStateListOf<ScanCapture>() }
    val pendingQueue = remember { mutableStateListOf<ScanCapture>() }
    var reviewCapture by remember { mutableStateOf<ScanCapture?>(null) }
    var showDonePrompt by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFile = File.createTempFile(
            "freshtrack_${timeStamp}_",
            ".jpg",
            context.cacheDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    fun slotUsedCount(): Int = captures.size + pendingQueue.size + if (reviewCapture != null) 1 else 0
    fun maxReached(): Boolean = slotUsedCount() >= maxPhotos
    fun popNextReview() {
        reviewCapture = if (pendingQueue.isNotEmpty()) pendingQueue.removeAt(0) else null
    }
    fun enqueueForReview(newCaptures: List<ScanCapture>) {
        if (newCaptures.isEmpty()) return
        pendingQueue.addAll(newCaptures)
        if (reviewCapture == null) {
            popNextReview()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val remaining = maxPhotos - slotUsedCount()
        if (remaining <= 0) {
            helperText = "Maximum 4 photos reached."
            return@rememberLauncherForActivityResult
        }
        val selected = uris.take(remaining).map { uri ->
            ScanCapture.Gallery(id = UUID.randomUUID().toString(), uri = uri)
        }
        if (uris.size > remaining) {
            helperText = "Only $remaining photo(s) added. Max is 4."
        } else if (selected.isNotEmpty()) {
            helperText = "Photo selected. Review before continuing."
        }
        enqueueForReview(selected)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCameraUri
        if (!success || uri == null) {
            helperText = "Camera capture canceled."
            pendingCameraUri = null
            return@rememberLauncherForActivityResult
        }
        if (maxReached()) {
            helperText = "Maximum 4 photos reached."
            pendingCameraUri = null
            return@rememberLauncherForActivityResult
        }
        enqueueForReview(
            listOf(
                ScanCapture.Camera(
                    id = UUID.randomUUID().toString(),
                    uri = uri
                )
            )
        )
        helperText = "Photo captured. Review before continuing."
        pendingCameraUri = null
    }

    val lensLabel = when {
        !gemmaConnected -> "GEMMA 4 OFFLINE"
        scanMode == ScanMode.Food -> "AI LENS ACTIVE"
        else -> "RECEIPT AI ACTIVE"
    }

    Scaffold(topBar = { DashboardTopBar() }, bottomBar = { BottomNav(RootTab.Scan, onTabSelected) }) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Gray100)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { scanMode = ScanMode.Food },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMode == ScanMode.Food) Emerald else Color.Transparent,
                            contentColor = if (scanMode == ScanMode.Food) White else Slate900
                        )
                    ) { Text("Scan Food", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { scanMode = ScanMode.Receipt },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMode == ScanMode.Receipt) Emerald else Color.Transparent,
                            contentColor = if (scanMode == ScanMode.Receipt) White else Slate900
                        )
                    ) { Text("Scan Receipt", fontWeight = FontWeight.Bold) }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.72f)
                            .background(
                                if (scanMode == ScanMode.Food) {
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color(0xFF295F2D), Color(0xFF1E3A2E), Color(0xFF0F172A))
                                    )
                                } else {
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                                    )
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0x99000000))
                                    .clickable { gemmaConnected = !gemmaConnected }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    lensLabel,
                                    color = if (gemmaConnected) White else RoseRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { flashOn = !flashOn },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = if (flashOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                contentDescription = "Flash",
                                tint = White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.82f)
                                .height(240.dp)
                                .border(2.dp, Emerald, RoundedCornerShape(8.dp))
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = 80.dp)
                                .size(72.dp)
                                .border(3.dp, Emerald, CircleShape)
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 84.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedIconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                enabled = !maxReached(),
                                modifier = Modifier.size(52.dp),
                                border = BorderStroke(1.dp, Color(0x66FFFFFF))
                            ) {
                                Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Gallery", tint = White)
                            }

                            Button(
                                onClick = {
                                    val uri = createTempImageUri(context)
                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                enabled = !maxReached(),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Emerald),
                                modifier = Modifier
                                    .size(92.dp)
                                    .border(5.dp, Emerald, CircleShape)
                            ) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = "Capture", modifier = Modifier.size(36.dp))
                            }

                            OutlinedIconButton(
                                onClick = {
                                    if (captures.isNotEmpty()) {
                                        captures.removeAt(captures.lastIndex)
                                        helperText = "Last photo removed."
                                    } else {
                                        helperText = "No previous capture."
                                    }
                                },
                                modifier = Modifier.size(52.dp),
                                border = BorderStroke(1.dp, Color(0x66FFFFFF))
                            ) {
                                Icon(Icons.Outlined.RestartAlt, contentDescription = "Undo", tint = White)
                            }
                        }

                        Text(
                            text = helperText.uppercase(),
                            color = White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 26.dp)
                        )
                    }
                }
            }

            item {
                SmartTipCard(
                    title = "SMART TIP",
                    message = "Scanning multiple items? Tap capture for each item. Fresh Track groups them in one inventory batch automatically."
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, Gray200)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Captured ${captures.size}/$maxPhotos • target for Gemma 4: 1536px long edge (frontend target)",
                            color = Slate600,
                            fontSize = 12.sp
                        )
                        if (captures.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(captures, key = { it.id }) { cap ->
                                    CapturePreview(cap, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)))
                                }
                            }
                            Button(
                                onClick = { onDone(scanMode, captures.toList()) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done Scanning")
                            }
                        }
                    }
                }
            }
        }
    }

    if (reviewCapture != null) {
        val current = reviewCapture!!
        AlertDialog(
            onDismissRequest = {
                popNextReview()
            },
            title = { Text("Review Capture", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CapturePreview(
                        capture = current,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Text("Photo ${captures.size + 1} of $maxPhotos. Keep this image?", color = Slate600)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    captures.add(current)
                    popNextReview()
                    showDonePrompt = true
                    helperText = "Captured ${captures.size}/$maxPhotos"
                }) { Text("Use Photo") }
            },
            dismissButton = {
                TextButton(onClick = {
                    popNextReview()
                    helperText = "Photo discarded."
                }) { Text("Discard") }
            }
        )
    }

    if (showDonePrompt) {
        AlertDialog(
            onDismissRequest = { showDonePrompt = false },
            title = { Text("Capture complete", fontWeight = FontWeight.Bold) },
            text = { Text("Done scanning, or take another photo?") },
            confirmButton = {
                TextButton(onClick = {
                    showDonePrompt = false
                    onDone(scanMode, captures.toList())
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDonePrompt = false }) { Text("Take Another") }
            }
        )
    }
}

@Composable
private fun CapturePreview(capture: ScanCapture, modifier: Modifier = Modifier) {
    val model = when (capture) {
        is ScanCapture.Camera -> capture.uri
        is ScanCapture.Gallery -> capture.uri
    }

    AsyncImage(
        model = model,
        contentDescription = "Captured image",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}