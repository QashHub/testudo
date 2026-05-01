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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.testudo.AppTelemetry
import com.example.testudo.MLEngine
import com.example.testudo.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.testudo.ui.components.TitleSection
import com.example.testudo.ui.components.ScanButton
import com.example.testudo.ui.components.SurroundingButtons

@Composable
fun MainScreen(
    navController: NavHostController,
    scanResults: List<Triple<String, String, Int>>,
    onScanComplete: (List<Triple<String, String, Int>>) -> Unit
) {
    val context = LocalContext.current
    var isSafe by remember { mutableStateOf(true) }
    var isScanning by remember { mutableStateOf(false) }
    var mlResults by remember { mutableStateOf(scanResults) }
    var scanStatus by remember { mutableStateOf("Scanning...") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TitleSection()
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Hello John!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCDD9E5)
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isScanning) {
                CircularProgressIndicator(color = Color(0xFF8B1A1A))
                Spacer(modifier = Modifier.height(8.dp))
                Text(scanStatus, color = Color(0xFF5A3E2B), fontSize = 14.sp)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    SurroundingButtons(navController, alertCount = 2)
                    ScanButton(
                        modifier = Modifier.align(Alignment.Center),
                        isSafe = isSafe,
                        onClick = {
                            isScanning = true
                        }
                    )
                }
            }

            LaunchedEffect(isScanning) {
                if (isScanning) {
                    val results = withContext(Dispatchers.IO) {
                        val ml = MLEngine(context)
                        val apps = AppTelemetry.getUserApps(context)
                        apps.mapIndexed { index, appInfo ->
                            withContext(Dispatchers.Main) {
                                scanStatus = "Scanning ${index + 1}/${apps.size}..."
                            }
                            val telemetry = AppTelemetry.collectFeatures(context, appInfo)
                            val result = ml.predict(telemetry.features)
                            Triple(telemetry.appName, result.label, result.riskScore.toInt())
                        }.also { ml.close() }
                    }
                    mlResults = results
                    onScanComplete(results)
                    isSafe = results.none { it.second == "Malicious" || it.second == "Suspicious" }
                    isScanning = false
                    scanStatus = "Scanning..."
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E3A5F))
                    .clickable { navController.navigate(Screen.AIRiskReport.route) }
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