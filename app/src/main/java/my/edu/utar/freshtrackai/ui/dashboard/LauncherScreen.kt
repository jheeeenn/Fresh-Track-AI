package my.edu.utar.freshtrackai.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.edu.utar.freshtrackai.R

@Composable
internal fun AppLauncherScreen(onLaunch: () -> Unit) {
    Surface(color = Gray50, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp)) {
            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(108.dp).clip(RoundedCornerShape(22.dp)).background(White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_freshtrack_logo_focus),
                        contentDescription = "Fresh Track logo",
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(Modifier.height(28.dp))
                Text("Fresh Track AI", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                Spacer(Modifier.height(14.dp))
                Text(
                    "Precision food tracking for high-performance living.",
                    textAlign = TextAlign.Center,
                    color = Slate600
                )
                Spacer(Modifier.height(30.dp))
                Button(
                    onClick = onLaunch,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Launch App", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
            Text(
                "GEMMA 4 INTEGRATED",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            )
        }
    }
}


