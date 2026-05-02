package com.example.testudo.navigation


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Alerts : Screen("alerts")
    object Status : Screen("status")
    object User : Screen("user")
    object Cache : Screen("cache")
    object Settings : Screen("settings")
    object AIRiskReport : Screen("ai_risk_report")
}