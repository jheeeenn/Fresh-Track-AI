package my.edu.utar.freshtrackai.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
internal fun freshOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Slate900,
    unfocusedTextColor = Slate900,
    disabledTextColor = Slate600,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    disabledContainerColor = Gray50,
    focusedBorderColor = Emerald,
    unfocusedBorderColor = Gray200,
    disabledBorderColor = Gray200,
    cursorColor = Emerald,
    focusedLabelColor = Slate600,
    unfocusedLabelColor = Slate600,
    focusedPlaceholderColor = Slate600.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = Slate600.copy(alpha = 0.7f),
    focusedLeadingIconColor = Slate600,
    unfocusedLeadingIconColor = Slate600,
    disabledLeadingIconColor = Slate600.copy(alpha = 0.6f)
)

@Composable
internal fun Badge(label: String, textColor: Color, bg: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
internal fun DraggableAddFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val fabSize = 56.dp
    val sideMargin = 16.dp
    val bottomOffset = 98.dp
    var dragX by rememberSaveable { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = bottomOffset)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

            // FIX IS HERE: Use maxWidth.toPx() instead of constraints.maxWidth
            val totalWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

            val fabSizePx = with(LocalDensity.current) { fabSize.toPx() }
            val sideMarginPx = with(LocalDensity.current) { sideMargin.toPx() }
            val minX = sideMarginPx
            val maxX = (totalWidthPx - fabSizePx - sideMarginPx).coerceAtLeast(minX)

            if (dragX == null) {
                dragX = maxX
            }

            FloatingActionButton(
                onClick = onClick,
                containerColor = Emerald,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier
                    .offset { IntOffset((dragX ?: maxX).roundToInt(), 0) }
                    .pointerInput(totalWidthPx) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val next = (dragX ?: maxX) + dragAmount.x
                                dragX = next.coerceIn(minX, maxX)
                            },
                            onDragEnd = {
                                val current = dragX ?: maxX
                                val midpoint = (minX + maxX) / 2f
                                dragX = if (current < midpoint) minX else maxX
                            }
                        )
                    }
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add item")
            }
        }
    }
}

@Composable
internal fun SmartTipCard(
    title: String,
    message: String,
    actionText: String? = null,
    actionLoading: Boolean = false,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray100),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Gray200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2FBEA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title.uppercase(), color = Emerald, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp)
                    if (onDismiss != null) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Dismiss guide",
                            tint = Slate600,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(onClick = onDismiss)
                        )
                    }
                }
                Text(message, color = Slate600)
                if (actionText != null && onAction != null) {
                    Button(
                        onClick = onAction,
                        enabled = !actionLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (actionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = White
                            )
                        } else {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(actionText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AiProcessingOverlay(
    state: DashboardAiTaskState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.46f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = Emerald)
                Text(
                    text = state.title,
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = state.detail,
                    color = Slate600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun RecipeProcessingBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray100),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Emerald
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Recipe Generation In Progress",
                    color = Slate900,
                    fontWeight = FontWeight.Bold
                )
                Text(message, color = Slate600)
                Text(
                    "You can stay here, or leave this tab and come back later.",
                    color = Slate600,
                    fontSize = 12.sp
                )
            }
        }
    }
}
