package com.example.testudo.navigation

import com.example.testudo.ui.screens.ThreatDetailScreen
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testudo.viewmodel.HomeViewModel

@Composable
fun TestudoApp() {

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NAV_DEBUG", "Now at route: ${destination.route}")
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val homeViewModel: HomeViewModel = viewModel()
    val homeState = homeViewModel.uiState.value

    val scanResults = homeState.scanResults
    val alertCount = scanResults.count {
        it.status == "Malicious" || it.status == "Suspicious"
    }

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
                MainScreen(
                    navController = navController,
                    vm = homeViewModel
                )
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

            composable(Screen.AIRiskReport.route) {
                AiRiskReportScreen(
                    navController = navController,
                    scanResults = scanResults
                )
            }

            composable("threat_detail/{packageName}") { backStackEntry ->
                val pkg = backStackEntry.arguments?.getString("packageName") ?: ""
                ThreatDetailScreen(navController, pkg)
            }

            composable(Screen.Status.route) {
                StatusScreen()
            }

        }
    }
}