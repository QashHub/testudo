package com.example.testudo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.testudo.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.testudo.ui.components.TitleSection
import com.example.testudo.ui.components.ScanButton
import com.example.testudo.ui.components.SurroundingButtons
import com.example.testudo.viewmodel.HomeViewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))
            TitleSection()

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Hello John!",
                color = Color.White
            )

            Spacer(Modifier.height(32.dp))

            if (state.isScanning) {

                CircularProgressIndicator()

                Text(state.scanStatus)

            } else {

                Box(contentAlignment = Alignment.Center) {

                    SurroundingButtons(
                        navController,
                        alertCount = state.scanResults.count {
                            it.second == "Malicious" ||
                                    it.second == "Suspicious"
                        }
                    )

                    ScanButton(
                        isSafe = state.isSafe,
                        onClick = {
                            vm.startScan()
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E3A5F))
                    .clickable {
                        navController.navigate(Screen.AIRiskReport.route)
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI Risk Report",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}