package my.edu.utar.freshtrackai.ui.dashboard

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
internal fun SmartScanScreen(
    onDone: (ScanMode, ScanCapture) -> Unit,
    onTabSelected: (RootTab) -> Unit,
    showGuide: Boolean,
    onDismissGuide: () -> Unit
) {
    var scanMode by rememberSaveable { mutableStateOf(ScanMode.Food) }
    var helperText by rememberSaveable { mutableStateOf("Open camera or gallery to start scanning.") }
    var selectedCapture by remember { mutableStateOf<ScanCapture?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

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

    fun updateSelection(capture: ScanCapture, message: String) {
        selectedCapture = capture
        helperText = message
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            helperText = "Gallery selection canceled."
            return@rememberLauncherForActivityResult
        }

        updateSelection(
            capture = ScanCapture.Gallery(
                id = UUID.randomUUID().toString(),
                uri = uri
            ),
            message = if (scanMode == ScanMode.Food) {
                "Food image selected. Use this photo when it looks clear."
            } else {
                "Receipt image selected. Use this photo when the text is readable."
            }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null

        if (!success || uri == null) {
            helperText = "Camera capture canceled."
            return@rememberLauncherForActivityResult
        }

        updateSelection(
            capture = ScanCapture.Camera(
                id = UUID.randomUUID().toString(),
                uri = uri
            ),
            message = if (scanMode == ScanMode.Food) {
                "Food photo captured. Check framing before continuing."
            } else {
                "Receipt photo captured. Check that the receipt text is visible."
            }
        )
    }

    val modeTitle = if (scanMode == ScanMode.Food) "Scan Food" else "Scan Receipt"
    val modeBadge = if (scanMode == ScanMode.Food) "FOOD DETECTION" else "RECEIPT OCR"
    val modeDescription = if (scanMode == ScanMode.Food) {
        "Use one clear photo of the food item. Keep the item centered and avoid dark shadows."
    } else {
        "Use one clear photo of the receipt. Make sure the receipt is flat and the printed text is readable."
    }
    val frameHint = if (scanMode == ScanMode.Food) {
        "Align the food item inside the frame."
    } else {
        "Align the full receipt inside the frame."
    }

    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = { BottomNav(RootTab.Scan, onTabSelected) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                        onClick = {
                            scanMode = ScanMode.Food
                            helperText = "Open camera or gallery to start scanning."
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMode == ScanMode.Food) Emerald else Color.Transparent,
                            contentColor = if (scanMode == ScanMode.Food) White else Slate900
                        )
                    ) {
                        Text("Scan Food", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            scanMode = ScanMode.Receipt
                            helperText = "Open camera or gallery to start scanning."
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMode == ScanMode.Receipt) Emerald else Color.Transparent,
                            contentColor = if (scanMode == ScanMode.Receipt) White else Slate900
                        )
                    ) {
                        Text("Scan Receipt", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (scanMode == ScanMode.Food) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF285F2E), Color(0xFF183B2B), Color(0xFF0F172A))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                                    )
                                }
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0x33000000))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    modeBadge,
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            if (selectedCapture != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF86EFAC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Photo ready",
                                        color = Color(0xFFDCFCE7),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = modeTitle,
                                color = White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                            Text(
                                text = modeDescription,
                                color = Color(0xFFD1D5DB),
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.82f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x14000000))
                                .border(
                                    border = BorderStroke(2.dp, Emerald),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedCapture == null) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(88.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x22FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (scanMode == ScanMode.Food) {
                                                Icons.Outlined.CameraAlt
                                            } else {
                                                Icons.Outlined.PhotoLibrary
                                            },
                                            contentDescription = null,
                                            tint = Emerald,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                    Text(
                                        text = frameHint,
                                        color = White,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = helperText,
                                        color = Color(0xFFD1D5DB),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x16000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = when (val capture = selectedCapture) {
                                            is ScanCapture.Camera -> capture.uri
                                            is ScanCapture.Gallery -> capture.uri
                                            null -> null
                                        },
                                        contentDescription = "Selected scan image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        Text(
                            text = helperText,
                            color = White,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Open Gallery")
                            }
                            Button(
                                onClick = {
                                    val uri = createTempImageUri(context)
                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Emerald),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = "Open Camera")
                            }
                        }

                        if (selectedCapture != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedCapture = null
                                        helperText = "Open camera or gallery to start scanning."
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp),
                                    border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Outlined.RestartAlt, contentDescription = "Retake")
                                }
                                Button(
                                    onClick = {
                                        selectedCapture?.let { onDone(scanMode, it) }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Use Photo")
                                }
                            }
                        }
                    }
                }
            }

            if (showGuide) {
                item {
                    SmartTipCard(
                        title = "SCAN GUIDE",
                        message = if (scanMode == ScanMode.Food) {
                            "Use Camera or Gallery, make sure the item is clear, then confirm the photo before AI starts."
                        } else {
                            "Use one flat receipt photo, keep the full receipt visible, then confirm the photo before OCR starts."
                        },
                        onDismiss = onDismissGuide
                    )
                }
            }
        }
    }
}
