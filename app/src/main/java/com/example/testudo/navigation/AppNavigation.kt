package com.example.testudo.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testudo.ui.components.BottomNavBar
import com.example.testudo.ui.screens.AiRiskReportScreen
import com.example.testudo.ui.screens.AlertsScreen
import com.example.testudo.ui.screens.CacheScreen
import com.example.testudo.ui.screens.MainScreen
import com.example.testudo.ui.screens.SettingsScreen
import com.example.testudo.ui.screens.SplashScreenStandalone
import com.example.testudo.ui.screens.StatusScreen
import com.example.testudo.ui.screens.UserScreen

@Composable
fun TestudoApp() {

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NAV_DEBUG", "Now at route: ${destination.route}")
        }
    }

    var scanResults by remember { mutableStateOf<List<Triple<String, String, Int>>>(emptyList()) }
    val alertCount = scanResults.count { it.second == "Malicious" || it.second == "Suspicious" }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Splash.route) {
                BottomNavBar(navController, alertCount)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(
                if (currentRoute == Screen.Splash.route) PaddingValues(0.dp)
                else innerPadding
            )
        ) {
            composable(Screen.Splash.route){
                SplashScreenStandalone()
            }

            composable(Screen.Home.route) {
                MainScreen(navController, scanResults) { scanResults = it }
            }

            composable(Screen.Alerts.route) {
                AlertsScreen()
            }

            composable(Screen.User.route) {
                UserScreen()
            }

            composable(Screen.Cache.route) {
                CacheScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(navController)

            }

            composable(Screen.AIRiskReport.route){
                AiRiskReportScreen(navController, scanResults)
            }

            composable(Screen.Status.route) {
                StatusScreen()
            }

        }
    }
}