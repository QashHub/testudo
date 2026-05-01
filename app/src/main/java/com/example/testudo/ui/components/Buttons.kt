package com.example.testudo.ui.components

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.navigation.NavHostController
import com.example.testudo.navigation.Screen

@Composable
fun ScanButton(
    modifier: Modifier = Modifier,
    isSafe: Boolean = true,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringColor = if (isSafe) Color(0xFF00FF87) else Color(0xFF1E90FF)

    Box(
        modifier = modifier
            .size(190.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        // Status ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(ringColor)
        )

        // Inner scan button
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFF00897B))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SCAN",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCDD9E5)
            )
        }
    }
}


@Composable
fun SurroundingButtons(navController: NavHostController, alertCount: Int = 2) {
    Column(
        verticalArrangement = Arrangement.spacedBy(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(100.dp)) {

            BadgedBox(
                badge = {
                    if (alertCount > 0) {
                        Badge(containerColor = Color(0xFF1E90FF)) {
                            Text(
                                text = alertCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            ) {
                FeatureButton(
                    "Alerts",
                    onClick = {
                        Log.d("NAV_DEBUG", "Alerts button pressed")
                        Log.d("NAV_DEBUG", "Navigating to route: ${Screen.Alerts.route}")
                        navController.navigate(Screen.Alerts.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }


            FeatureButton(
                "Status",
                onClick = {
                    navController.navigate(Screen.Status.route)
                }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(100.dp)) {
            FeatureButton("AI Assist")
            FeatureButton(
                "Clean Cache",
                onClick = {
                    navController.navigate(Screen.Cache.route)
                }
            )
        }
    }
}

@Composable
fun FeatureButton(
    text: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E3A5F))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}