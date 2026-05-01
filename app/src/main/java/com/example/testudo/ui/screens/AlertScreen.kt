package com.example.testudo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.testudo.ui.components.TitleSection
import com.example.testudo.ui.components.AlertItem

@Composable
fun AlertsScreen() {

    data class AlertData(val leftText: String, val rightText: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

    var alerts by remember {
        mutableStateOf(
            listOf(
                AlertData("Poor network connection — AI processing may take longer than usual.", "Check your internet connection.", Icons.Default.WifiOff),
                AlertData("Free up space to save AI results and continue using the app.","Storage space full", Icons.Default.Storage)
            )
        )

    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .background(Color(0xFF0D1B2A))
    ) {
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TitleSection()
        }

        Spacer(Modifier.height(16.dp))


        // Header bar

        Text(
            "Alerts",
            color = Color(0xFFCDD9E5),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)

        )


        Spacer(Modifier.height(16.dp))

        // No alerts card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1C2B3A))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No alerts available",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCDD9E5),
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "You'll see important notifications here when they arrive.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFFCDD9E5),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "✓  You're all caught up!",
                    color = Color(0xFF1E3A5F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Previous alerts section title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Previous Alerts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCDD9E5)
            )
            if (alerts.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E3A5F))
                        .clickable { alerts = emptyList() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Clear All",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Alert items
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C2B3A))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No previous alerts",
                    color = Color(0xFFCDD9E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                alerts.forEach { alert ->
                    AlertItem(
                        leftText = alert.leftText,
                        rightText = alert.rightText,
                        icon = alert.icon,
                        onDismiss = {
                            alerts = alerts.filter { it.leftText != alert.leftText }
                        }
                    )
                }
            }
        }
    }

}



