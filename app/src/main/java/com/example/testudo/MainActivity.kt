package com.example.testudo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.NavController
import com.example.testudo.ui.theme.TestudoTheme
//A
//Initial UI Development Made by Andres any questions please ask.

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TestudoTheme {
                PermissionGate()
            }
        }
    }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Alerts : Screen("alerts")
    object User : Screen("user")
    object Cache : Screen("cache")
    object Settings : Screen("settings")
    object AIRiskReport : Screen("ai_risk_report")
}

@Composable
fun PermissionGate() {

    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)

        if (!hasPermission) {
            requestUsageStatsPermission(context)
        }
    }

    if (hasPermission) {
        TestudoApp()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD8CFAE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Please enable Usage Access for Testudo",
                color = Color(0xFF5A3E2B),
                fontWeight = FontWeight.Bold
            )
        }
    }
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

            composable(Screen.Settings.route) {
                SettingsScreen(navController)

            }
            
            composable(Screen.AIRiskReport.route){
                AiRiskReportScreen(navController)
            }

        }
    }
}
//a
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
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF8B1A1A))
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
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                navController.navigate(Screen.Settings.route){
                    popUpTo(navController.graph.startDestinationId) {saveState = true}
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings")}

        )
    }
}

@Composable
fun CacheScreen() {

    val context = LocalContext.current
    var cacheList by remember { mutableStateOf<List<AppCacheInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true

        val result = withContext(Dispatchers.IO) {
            getAppsSortedByCache(context).take(20)
        }

        cacheList = result
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE))
    ) {

        Spacer(Modifier.height(16.dp))
        TitleSection()
        Spacer(Modifier.height(16.dp))

        UsageCard()

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(cacheList) { app ->
                    CacheItem(
                        appName = app.appName,
                        size = formatBytes(app.cacheSizeBytes)
                    )
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
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
            .height(80.dp)
    ) {


        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(Color(0xFF8B1A1A))
                .padding(start = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = appName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = size,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }


        Box(
            modifier = Modifier
                .width(130.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFFB8860B))
                .clickable { },
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
            .clip(RoundedCornerShape(28.dp))
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

            Spacer(Modifier.height(4.dp))

            Text(
                "810Mb/1080Mb",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFB8860B))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFB8860B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "75%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A3E2B)
                )
            }
        }
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

@Composable
fun SettingsScreen(navController: NavHostController) {
    val settingsItem = listOf(
        "Notifacation Reminder",
        "Charging Optimization",
        "Auto update virus database",
        "Real-time protection",
        "Privacy Policy"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE))
    ) {
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TitleSection()
        }

        Spacer(Modifier.height(8.dp))

        // Back button
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF8B1A1A))
                .clickable { navController.popBackStack() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "< Settings",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Settings items list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )    {
            settingsItem.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF8B1A1A))
                        .clickable { }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = item,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
         }
    }
}

@Composable
fun AiRiskReportScreen(navController: NavHostController) {

    val appRisks = listOf(
        Triple("WhatsApp", "Safe", 18),
        Triple("Suspicious", "Suspicious", 51),
        Triple("Torjan.Dropper", "Malicious", 92)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8CFAE))
    ) {
        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TitleSection()
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "AI Risk Report",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A3E2B),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF8B1A1A))
                .clickable { navController.popBackStack() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "< Back",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8E1C8))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB8860B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "18",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A3E2B)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Risk Level",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF5A3E2B)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Most of your apps are safe but we found 2 suspicious apps and 1 malicious app.",
                        fontSize = 14.sp,
                        color = Color(0xFF5A3E2B)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            appRisks.forEach { (name, status, score) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8E1C8))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF8B1A1A)
                        )
                        Text(
                            text = status,
                            fontSize = 13.sp,
                            color = Color(0xFF8B1A1A)
                        )
                    }
                    Text(
                        text = score.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF5A3E2B)
                    )
                }
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
        AiRiskReportScreen(navController)
    }
}