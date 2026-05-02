package com.example.testudo.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testudo.ui.components.StatusAppItem
import com.example.testudo.ui.components.StatusStatCard
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.viewmodel.StatusViewModel

@Composable
fun StatusScreen(
    vm: StatusViewModel = viewModel()
) {
    val state by vm.uiState

    val animatedSuspicious by animateIntAsState(
        targetValue = state.suspiciousCount,
        animationSpec = tween(1000),
        label = "suspicious"
    )

    val animatedVirus by animateIntAsState(
        targetValue = state.virusCount,
        animationSpec = tween(1000),
        label = "virus"
    )

    val animatedBlocked by animateIntAsState(
        targetValue = state.blockedCount,
        animationSpec = tween(1000),
        label = "blocked"
    )

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

    val circleColor = if (state.isSafe) Color(0xFF00897B) else Color(0xFFB22222)
    val statusText = if (state.isSafe) "SAFE!" else "THREAT!"
    val statusMessage =
        if (state.isSafe) "No Virus' detected"
        else "Threats found"

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
        ) {
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
            items(state.apps) { packageName ->
                StatusAppItem(packageName)
            }
        }
    }
}