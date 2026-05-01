package com.example.testudo.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testudo.ui.components.StatusAppItem
import com.example.testudo.ui.components.StatusStatCard
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.utils.getMostUsedApps

@Composable
fun StatusScreen() {

    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<String>>(emptyList()) }
    val isSafe = true
    var suspiciousCount by remember { mutableStateOf(0) }
    var virusCount by remember { mutableStateOf(0) }
    var blockedCount by remember { mutableStateOf(0) }

    val animatedSuspicious by animateIntAsState(
        targetValue = suspiciousCount,
        animationSpec = tween(1000),
        label = "suspicious"
    )

    val animatedVirus by animateIntAsState(
        targetValue = virusCount,
        animationSpec = tween(1000),
        label = "virus"
    )

    val animatedBlocked by animateIntAsState(
        targetValue = blockedCount,
        animationSpec = tween(1000),
        label = "blocked"
    )

    LaunchedEffect(Unit) {
        apps = getMostUsedApps(context)
        suspiciousCount = 0
        virusCount = 0
        blockedCount = 0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "safePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val circleColor = if (isSafe) Color(0xFF00897B) else Color(0xFFB22222)
    val statusText = if (isSafe) "SAFE!" else "THREAT!"
    val statusMessage = if (isSafe) "No Virus has been detected" else "Threats found on your device"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        )
        {
            TitleSection()
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Device Status",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = statusMessage,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))


        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusStatCard(
                label = "Suspicious Activities",
                value = animatedSuspicious,
                color = Color(0xFFFFC107)
            )
            StatusStatCard(
                label = "Virus Detection",
                value = animatedVirus,
                color = Color(0xFFFF3B3B)
            )
            StatusStatCard(
                label = "Virus Blocked",
                value = animatedBlocked,
                color = Color(0xFF00897B)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Most used apps
        Text(
            text = "Most Used Apps (Last 24h)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.fillMaxWidth()
        )



        Spacer(Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps) { packageName ->
                StatusAppItem(packageName)

            }

        }
    }
}