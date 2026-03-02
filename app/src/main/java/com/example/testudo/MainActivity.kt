package com.example.testudo

import android.os.Bundle
import android.util.Log
import androidx.compose.ui.zIndex
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testudo.ui.theme.TestudoTheme

//Initial UI Development Made by Andres any questions please ask.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("APP_START_CHECK", "THIS VERSION OF MAINACTIVITY IS RUNNING")

        enableEdgeToEdge()

        setContent {
            TestudoTheme {
                TestudoApp()
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Alerts : Screen("alerts")
    object User : Screen("user")
    object Cache : Screen("cache")
}

@Composable
fun TestudoApp() {

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NAV_DEBUG", "Now at route: ${destination.route}")
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Home.route) {
                MainScreen(navController)
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
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            TitleSection()

            Spacer(modifier = Modifier.height(40.dp))

            Box(contentAlignment = Alignment.Center) {

                SurroundingButtons(navController)

                ScanButton(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun UserScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE)),
        horizontalAlignment = Alignment.Start
    ) {

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TitleSection()
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A3E2B),
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
        )

        // My Account
        ProfileCard(
            title = "My Account",
            subtitle = "example@gmail.com"
        )

        // Plan
        ProfileCard(
            title = "Free Plan",
            subtitle = "Subscription: Active"
        )

        // About
        ProfileCard(
            title = "About",
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "i",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        )
    }
}

@Composable
fun TitleSection() {
    Text(
        text = "Testudo",
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFB22222)
    )
}

@Composable
fun SurroundingButtons(navController: NavHostController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(80.dp)) {

            FeatureButton(
                "Alerts",
                onClick = {

                    Log.d("NAV_DEBUG", "Alerts button pressed")
                    Log.d("NAV_DEBUG", "Navigating to route: ${Screen.Alerts.route}")

                    navController.navigate(Screen.Alerts.route) {
                        launchSingleTop = true
                    }
                }
            )

            FeatureButton("Status")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(80.dp)) {
            FeatureButton("AI Assist")
            FeatureButton(
                "Clean Cache",
                onClick = {
                    navController.navigate(Screen.Cache.route)
                }
            )
        }
    }
}

@Composable
fun FeatureButton(
    text: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8B1A1A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScanButton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(Color(0xFFB8860B))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SCAN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A3E2B)
        )
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFC9C2A6)
    ) {

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.User.route,
            onClick = {
                navController.navigate(Screen.User.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "User") },
            label = { Text("User") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

@Composable
fun CacheScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(24.dp))

        TitleSection()

        Spacer(Modifier.height(12.dp))

        UsageCard()

        Spacer(Modifier.height(12.dp))

        CacheItem("WhatsApp", "128Mb")
        CacheItem("Instagram", "64Mb")
        CacheItem("Chrome", "32Mb")
        CacheItem("Toggl", "18Mb")
    }
}

@Composable
fun CacheItem(
    appName: String,
    size: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF8B1A1A))
                .padding(20.dp)
        ) {
            Text(
                "$appName\n$size",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .background(Color(0xFFB8860B))
                .padding(horizontal = 20.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Clean Cache",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UsageCard() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF8B1A1A))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(
                "Total Usage",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "810Mb/1080Mb",
                color = Color.White,
                fontSize = 18.sp
            )
        }

        UsageCircle("75%")
    }
}

@Composable
fun UsageCircle(percent: String) {

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color(0xFFB8860B)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            percent,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A3E2B)
        )
    }
}

@Composable
fun AlertsScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(24.dp))

        TitleSection()

        AlertsHeader()

        NoAlertsSection()

        PreviousAlertsSection()
    }
}

@Composable
fun AlertsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF8B1A1A))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Alerts",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun NoAlertsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8E1C8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "No alerts available",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 18.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "You'll see important notifications here\nwhen they arrive.",
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "You're all caught up!",
            color = Color.Black
        )
    }
}


@Composable
fun PreviousAlertsSection() {

    Text(
        "Previous Alerts",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF5A3E2B),
        modifier = Modifier.padding(12.dp)
    )

    AlertItem(
        leftText = "Poor network connection\nAI processing may take longer than usual.",
        rightText = "Check your internet connection."
    )

    AlertItem(
        leftText = "Free up space to save AI results and continue using the app.",
        rightText = "Storage space full"
    )
}


@Composable
fun AlertItem(
    leftText: String,
    rightText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFE6D9A8), RoundedCornerShape(topEnd = 40.dp))
                .padding(12.dp)
        ) {
            Text(
                leftText,
                color = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF8B1A1A))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                rightText,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileCard(
    title: String,
    subtitle: String? = null,
    leadingIcon: (@Composable (() -> Unit))? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8B1A1A))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        leadingIcon?.let {
            Box(modifier = Modifier.padding(end = 12.dp)) {
                it()
            }
        }

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
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

