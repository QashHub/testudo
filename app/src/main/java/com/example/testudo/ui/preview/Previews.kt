package com.example.testudo.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.testudo.navigation.TestudoApp
import com.example.testudo.ui.screens.AiRiskReportScreen
import com.example.testudo.ui.screens.AlertsScreen
import com.example.testudo.ui.components.BottomNavBar
import com.example.testudo.ui.screens.CacheScreen
import com.example.testudo.ui.components.FeatureButton
import com.example.testudo.ui.components.ScanButton
import com.example.testudo.ui.screens.SettingsScreen
import com.example.testudo.ui.screens.SplashScreenStandalone
import com.example.testudo.ui.screens.UserScreen
import com.example.testudo.ui.theme.TestudoTheme

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview(){
    TestudoTheme {
        TestudoTheme {
            val navController = rememberNavController()
            SplashScreenStandalone()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AlertsPreview() {
    TestudoTheme {
        AlertsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TestudoAppPreview() {
    TestudoTheme {
        TestudoApp()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "User Screen Preview"
)
@Composable
fun UserScreenPreview() {
    TestudoTheme {
        UserScreen()

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CacheScreenPreview() {
    TestudoTheme {
        CacheScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureButtonPreview() {
    TestudoTheme {
        FeatureButton("Alerts")
    }
}

@Preview(showBackground = true)
@Composable
fun ScanButtonPreview() {
    TestudoTheme {
        ScanButton()
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavPreview() {
    TestudoTheme {
        val navController = rememberNavController()
        BottomNavBar(navController)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    TestudoTheme {
        val navController = rememberNavController()
        SettingsScreen(navController)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiRiskReportPreview() {
    TestudoTheme {
        val navController = rememberNavController()
        AiRiskReportScreen(navController, emptyList())
    }
}