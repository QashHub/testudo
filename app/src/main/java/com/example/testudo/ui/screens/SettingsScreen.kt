package com.example.testudo.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.testudo.AppSettings
import com.example.testudo.ui.components.TitleSection
import com.example.testudo.ui.components.SettingsToggleItem
import com.example.testudo.ui.components.SettingsSectionHeader

@Composable
fun SettingsScreen(navController: NavHostController) {

    var settings by remember { mutableStateOf(AppSettings()) }

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
            text = "Settings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            item{
                SettingsSectionHeader(title = "Notifications")
            }

            item{
                SettingsToggleItem(
                    title = "Notification Reminder",
                    subtitle = "Get reminders to scan your device",
                    expandedDetail = "Sends a daily reminder to run a security scan. Recommended to keep your device safe.",
                    icon = Icons.Default.Notifications,
                    checked = settings.notificationEnabled,
                    onCheckedChange = { settings = settings.copy(notificationEnabled = it) }
                )
            }

            item{
                Spacer(Modifier.height(4.dp))
                SettingsSectionHeader(title = "Performance")
            }

            item{
                SettingsToggleItem(
                    title = "Charging Optimization",
                    subtitle = "Optimize performance while charging",
                    expandedDetail = "Runs heavy tasks only when your device is plugged-in to save battery life.",
                    icon = Icons.Default.BatteryChargingFull,
                    checked = settings.chargingOptEnabled,
                    onCheckedChange = { settings = settings.copy(chargingOptEnabled = it) }
                )

            }

            item{
                SettingsToggleItem(
                    title = "Auto Update Virus Database",
                    subtitle = "Keep virus definitions up to date",
                    expandedDetail = "Automatically downloads the latest virus definitions in the background so scans are always accurate.",
                    icon = Icons.Default.Autorenew,
                    checked = settings.autoUpdateEnabled,
                    onCheckedChange = { settings = settings.copy(autoUpdateEnabled = it) }
                )

            }

            item{
                Spacer(Modifier.height(4.dp))
                SettingsSectionHeader(title = "Privacy")
            }

            item{
                SettingsToggleItem(
                    title = "Real-time Protection",
                    subtitle = "Monitor threats in the background",
                    expandedDetail = "Continuously monitors installed apps and file activity for suspicious behavior in real time.",
                    icon = Icons.Default.Security,
                    checked = settings.realtimeProtEnabled,
                    onCheckedChange = { settings = settings.copy(realtimeProtEnabled = it) }
                )

            }

            item{
                SettingsToggleItem(
                    title = "Privacy Policy",
                    subtitle = "Share anonymous usage data",
                    expandedDetail = "Allows Testudo to collect anonymous usage statistics to help inprove the app. No personal data is shared",
                    icon = Icons.Default.Policy,
                    checked = settings.privacyPolicyEnabled,
                    onCheckedChange = { settings = settings.copy(privacyPolicyEnabled = it) }
                )
            }
        }
    }
}