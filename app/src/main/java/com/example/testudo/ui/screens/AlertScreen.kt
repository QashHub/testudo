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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testudo.ui.components.AlertItem
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.viewmodel.AlertsViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.testudo.data.local.db.DatabaseProvider
import com.example.testudo.data.local.db.entity.ScanHistoryEntity

@Composable
fun AlertsScreen(
    vm: AlertsViewModel = viewModel()
) {
    val state by vm.uiState
    val alerts = state.alerts
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scanHistoryDao = remember { db.scanHistoryDao() }

    var scanHistory by remember { mutableStateOf<List<ScanHistoryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        scanHistory = scanHistoryDao.getAllScanHistory()
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

        Text(
            "Alerts",
            color = Color(0xFFCDD9E5),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        CurrentAlertsCard()

        Spacer(Modifier.height(24.dp))

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
                        .clickable { vm.clearAllAlerts() }
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

        if (scanHistory.isEmpty()) {
            NoPreviousAlertsCard()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scanHistory) { scan ->
                    ScanHistoryAlertItem(scan)
                }
            }
        }
    }
}
@Composable
private fun CurrentAlertsCard() {
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
                "all caught up!",
                color = Color(0xFF1E3A5F),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun NoPreviousAlertsCard() {
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
}

@Composable
private fun ScanHistoryAlertItem(scan: ScanHistoryEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C2B3A))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = scan.appName,
                color = Color(0xFFCDD9E5),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Risk: ${scan.riskLevel} (${scan.riskScore})",
                color = Color(0xFFCDD9E5),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Action: ${scan.actionTaken}",
                color = Color(0xFFCDD9E5),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = scan.behaviorSummary,
                color = Color(0xFF8FA3B8),
                fontSize = 13.sp
            )
        }
    }
}