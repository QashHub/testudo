package com.example.testudo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.testudo.QuarantineManager
import com.example.testudo.RealScanner
import com.example.testudo.navigation.Screen
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.viewmodel.AiRiskReportViewModel

// ── Shared data model ─────────────────────────────────────────────────────────
data class ScanResultItem(
    val appName: String,
    val packageName: String,
    val status: String,
    val riskScore: Int
)

@Composable
fun AiRiskReportScreen(
    navController: NavHostController,
    scanResults: List<ScanResultItem>,          // ← updated type
    vm: AiRiskReportViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by vm.uiState
    var expandedItem by remember { mutableStateOf<String?>(null) }

    // Track which packages are currently quarantined so the UI updates instantly
    var quarantinedPackages by remember {
        mutableStateOf(
            QuarantineManager.getAll(context).map { it.packageName }.toSet()
        )
    }

    val selectedFilter = state.selectedFilter
    val filters = state.filters

    val avgScore = if (scanResults.isEmpty()) 0
    else scanResults.map { it.riskScore }.average().toInt()

    val animatedScore by animateIntAsState(
        targetValue = if (state.scoreVisible) avgScore else 0,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )

    LaunchedEffect(Unit) { vm.showScore() }

    val filteredRisks = if (selectedFilter == "All") scanResults
    else scanResults.filter { it.status == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TitleSection()
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "AI Risk Report",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // ── Summary card ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1C2B3A))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00897B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = animatedScore.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCDD9E5)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Risk Level",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFCDD9E5)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Scanned ${scanResults.size} apps. " +
                                "${scanResults.count { it.status == "Safe" }} safe, " +
                                "${scanResults.count { it.status == "Suspicious" }} suspicious, " +
                                "${scanResults.count { it.status == "Malicious" }} malicious.",
                        fontSize = 14.sp,
                        color = Color(0xFFCDD9E5)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Filter tabs ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedFilter == filter) Color(0xFF1E3A5F)
                            else Color(0xFF1C2B3A)
                        )
                        .clickable { vm.selectFilter(filter) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (selectedFilter == filter) Color.White else Color(0xFFCDD9E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── App list ──────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (filteredRisks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C2B3A))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No $selectedFilter apps found",
                            color = Color(0xFFCDD9E5),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                items(filteredRisks, key = { it.packageName }) { item ->
                    val rowColor = when (item.status) {
                        "Safe"       -> Color(0xFF00FF87)
                        "Suspicious" -> Color(0xFFFFC107)
                        "Malicious"  -> Color(0xFFFF4444)   // fixed: was blue
                        else         -> Color(0xFF1E3A5F)
                    }
                    val isExpanded    = expandedItem == item.packageName
                    val isQuarantined = item.packageName in quarantinedPackages

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C2B3A))
                            .clickable {
                                if (item.packageName.isNotBlank()) {
                                    navController.navigate(
                                        Screen.ThreatDetail.createRoute(item.packageName)
                                    )
                                } else {
                                    expandedItem = if (isExpanded) null else item.packageName
                                }
                            }
                    ) {
                        // ── Row header ────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(rowColor)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.appName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFCDD9E5)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.status,
                                            fontSize = 13.sp,
                                            color = rowColor
                                        )
                                        // Quarantine badge
                                        if (isQuarantined) {
                                            Text(
                                                text = "🔒 Quarantined",
                                                fontSize = 11.sp,
                                                color = Color(0xFFFF9800)
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = item.riskScore.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFFCDD9E5)
                            )
                        }

                        // ── Expanded panel ────────────────────────────
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Risk Score: ${item.riskScore} / 100",
                                        fontWeight = FontWeight.Bold,
                                        color = rowColor,
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = when (item.status) {
                                            "Safe"       -> "No known threats. Behaves normally and requests only standard permissions."
                                            "Suspicious" -> "Unusual behaviour detected. May request excessive permissions or contact unknown servers."
                                            "Malicious"  -> "Identified as malicious. Strongly recommended to quarantine or uninstall immediately."
                                            else         -> "No additional information available."
                                        },
                                        color = Color(0xFFCDD9E5),
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    // ── Action buttons ────────────────
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Mark Safe
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2E7D32))
                                                .clickable { vm.markSafe(item.appName) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✓ Mark Safe",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Quarantine / Restore toggle
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isQuarantined) Color(0xFF5D4037)
                                                    else Color(0xFFB22222)
                                                )
                                                .clickable {
                                                    if (isQuarantined) {
                                                        // Restore
                                                        QuarantineManager.restore(
                                                            context, item.packageName
                                                        )
                                                        quarantinedPackages =
                                                            quarantinedPackages - item.packageName
                                                    } else {
                                                        // Build a minimal ScanResult to pass in
                                                        val fakeResult = RealScanner.ScanResult(
                                                            appName     = item.appName,
                                                            packageName = item.packageName,
                                                            riskScore   = item.riskScore,
                                                            label       = item.status,
                                                            reasons     = listOf("Manually quarantined from Risk Report"),
                                                            isSideloaded  = false,
                                                            isBlacklisted = false,
                                                            isWhitelisted = false,
                                                            mlScore       = item.riskScore.toFloat()
                                                        )
                                                        QuarantineManager.quarantine(context, fakeResult)
                                                        quarantinedPackages =
                                                            quarantinedPackages + item.packageName
                                                        vm.markMalicious(item.appName, item.riskScore)
                                                    }
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isQuarantined) "↩ Restore App"
                                                else "🔒 Quarantine",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}