package com.example.testudo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.viewmodel.AiRiskReportViewModel

@Composable
fun AiRiskReportScreen(
    navController: NavHostController,
    scanResults: List<Triple<String, String, Int>>,
    vm: AiRiskReportViewModel = viewModel()
) {
    val state by vm.uiState

    val appRisksData = scanResults
    val selectedFilter = state.selectedFilter
    val filters = state.filters
    val expandedItem = state.expandedItem

    val animatedScore by animateIntAsState(
        targetValue = if (state.scoreVisible) 18 else 0,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )

    LaunchedEffect(Unit) {
        vm.showScore()
    }

    val filteredRisks =
        if (selectedFilter == "All") {
            appRisksData
        } else {
            appRisksData.filter { it.second == selectedFilter }
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
            text = "AI Risk Report",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

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
                        text = "Most of your apps are safe but we found 3 suspicious apps and 1 malicious app.",
                        fontSize = 14.sp,
                        color = Color(0xFFCDD9E5)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

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
                        .clickable {
                            vm.selectFilter(filter)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color =
                        if (selectedFilter == filter) Color.White
                        else Color(0xFFCDD9E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

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
                            .background(Color(0xFFE8E1C8))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No $selectedFilter apps found",
                            color = Color(0xFF5A3E2B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                items(filteredRisks) { (name, status, score) ->
                    val rowColor = when (status) {
                        "Safe" -> Color(0xFF00FF87)
                        "Suspicious" -> Color(0xFFFFC107)
                        "Malicious" -> Color(0xFF1E90FF)
                        else -> Color(0xFF1E3A5F)
                    }

                    val isExpanded = expandedItem == name

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C2B3A))
                            .clickable {
                                vm.toggleExpandedItem(name)
                            }
                    ) {
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
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFCDD9E5)
                                    )

                                    Text(
                                        text = status,
                                        fontSize = 13.sp,
                                        color = rowColor
                                    )
                                }
                            }

                            Text(
                                text = score.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFFCDD9E5)
                            )
                        }

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
                                        text = "Risk Score: $score / 100",
                                        fontWeight = FontWeight.Bold,
                                        color = rowColor,
                                        fontSize = 13.sp
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = when (status) {
                                            "Safe" ->
                                                "This app has no known threats. It behaves normally and requests only standard permissions."

                                            "Suspicious" ->
                                                "This app shows unusual behaviour. It may request excessive permissions or communicate with unknown servers."

                                            "Malicious" ->
                                                "This app has been identified as malicious. It is strongly recommended to uninstall it immediately."

                                            else ->
                                                "No additional information available."
                                        },
                                        color = Color(0xFFCDD9E5),
                                        fontSize = 13.sp
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2E7D32))
                                                .clickable {
                                                    vm.markSafe(name)
                                                }
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

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFB22222))
                                                .clickable {
                                                    vm.markMalicious(name)
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✗ Mark Malicious",
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