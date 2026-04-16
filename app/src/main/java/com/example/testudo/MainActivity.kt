package com.example.testudo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import android.app.AppOpsManager
import android.content.Context
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.animation.core.animateFloat
import android.os.Process
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.navigation.NavController
import com.example.testudo.ui.theme.TestudoTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Policy
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import com.valentinilk.shimmer . shimmer


//AA
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
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Alerts : Screen("alerts")
    object Status : Screen("status")
    object User : Screen("user")
    object Cache : Screen("cache")
    object Settings : Screen("settings")
    object AIRiskReport : Screen("ai_risk_report")
}

fun generateAlerts(risks: List<AppRisk>): List<String> {

    val alerts = mutableListOf<String>()

    risks.forEach {

        if (it.riskScore > 80) {
            alerts.add("${it.name} is potentially malicious")
        }

        if (it.riskScore > 50) {
            alerts.add("${it.name} is suspicious")
        }
    }

    return alerts
}

fun getMostUsedApps(context: Context): List<String> {

    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager

    val end = System.currentTimeMillis()
    val start = end - (1000 * 60 * 60 * 24)

    val stats = usageStatsManager.queryUsageStats(
        android.app.usage.UsageStatsManager.INTERVAL_DAILY,
        start,
        end
    )

    return stats.sortedByDescending { it.totalTimeInForeground }
        .take(5)
        .map { it.packageName }
}

@Composable
fun PermissionGate() {

    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
        hasPermission = hasUsageStatsPermission(context)

        if (!hasPermission) {
            requestUsageStatsPermission(context)
        }
    }

    if (showSplash) {
        SplashScreenStandalone()
    } else if (hasPermission) {
        TestudoApp()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B2A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Please enable Usage Access for Testudo",
                color = Color(0xFFCDD9E5),
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

    val alertCount = 2

    var user by remember {
        mutableStateOf(
            User(
                name = "John Doe",
                email = "john@example.com",
                phone = "+44 7123456789",
                paymentDetails = "Visa •••• 1234",
                isPremium = false
            )
        )
    }

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
                MainScreen(navController, userName = user.name)
            }

            composable(Screen.Alerts.route) {
                AlertsScreen()
            }

            composable(Screen.User.route) {
                UserScreen(user = user, onUserChange = { user = it })
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

            composable(Screen.Status.route) {
                StatusScreen()
            }

        }
    }
}
//a
@Composable
fun MainScreen(navController: NavHostController, userName: String = "John") {
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
                text = "Hello ${userName.split(" ").first()}!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCDD9E5)
            )

            Spacer(modifier = Modifier.height(32.dp))


            Box(contentAlignment = Alignment.Center) {

                SurroundingButtons(navController, alertCount = 2)

                ScanButton(
                    modifier = Modifier.align(Alignment.Center),
                    isSafe = true
                )
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

@Composable
fun UserScreen(user: User, onUserChange: (User) -> Unit) {



    var editMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TitleSection()
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "User Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )

        Spacer(Modifier.height(16.dp))

// Profile Initials
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A5F)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name
                            .split(" ")
                            .take(2)
                            .joinToString("") { it.first().uppercase() },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF87)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCDD9E5)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(20.dp))

        EditableField("Name", user.name, editMode) {
            onUserChange(user.copy(name = it))
        }

        EditableField(
            label = "Email",
            value = user.email,
            editable = editMode,
            validate = { it.contains("@") && it.contains(".") },
            errorMessage = "Please enter a valid email address"
        ) {
            onUserChange(user.copy(email = it))
        }

        EditableField(
            label = "Phone",
            value = user.phone,
            editable = editMode,
            validate = { it.startsWith("+") && it.length >= 10 },
            errorMessage = "Please enter a valid phone number"
        ) {
            onUserChange(user.copy(phone = it))
        }


        EditableField("Payment Details", user.paymentDetails, editMode) {
            onUserChange(user.copy(paymentDetails = it))
        }

        Spacer(Modifier.height(16.dp))

        PremiumToggle(user.isPremium) {
            onUserChange(user.copy(isPremium = it))
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Button(
                onClick = { editMode = !editMode },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(if (editMode) "Cancel" else "Edit", color = Color.White)
            }

            if (editMode) {
                Button(
                    onClick = { editMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PremiumToggle(
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1C2B3A))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            "Premium Account",
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )

        Switch(
            checked = isPremium,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    editable: Boolean,
    validate: ((String) -> Boolean)? = null,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit
) {

    Column(modifier = Modifier.padding(bottom = 12.dp)) {

        Text(
            label,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )

        if (editable) {

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFFCDD9E5)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFCDD9E5),
                    unfocusedTextColor = Color(0xFFCDD9E5),
                    focusedBorderColor = Color(0xFF1E3A5F),
                    unfocusedBorderColor = Color(0xFF1E3A5F),
                    cursorColor = Color(0xFF1E3A5F)
                )
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1C2B3A))
                    .padding(14.dp)
            ) {
                Text(
                    value,
                    color = Color(0xFFCDD9E5),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun TitleSection() {
    Text(
        text = "Testudo",
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E90FF),

    )
}

@Composable
fun SurroundingButtons(navController: NavHostController, alertCount: Int = 2) {
    Column(
        verticalArrangement = Arrangement.spacedBy(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(100.dp)) {

            BadgedBox(
                badge = {
                    if (alertCount > 0) {
                        Badge(containerColor = Color(0xFF1E90FF)) {
                            Text(
                                text = alertCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            ) {
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
            }


            FeatureButton(
                "Status",
                onClick = {
                    navController.navigate(Screen.Status.route)
                }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(100.dp)) {
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
            .background(Color(0xFF1E3A5F))
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
    modifier: Modifier = Modifier,
    isSafe: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringColor = if (isSafe) Color(0xFF00FF87) else Color(0xFF1E90FF)

    Box(
        modifier = modifier
            .size(190.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        // Status ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(ringColor)
        )

        // Inner scan button
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFF00897B))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SCAN",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCDD9E5)
            )
        }
    }
}
@Composable
fun BottomNavBar(navController: NavHostController, alertCount: Int = 0) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF0D1B2A)
    ) {

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.User.route,
            onClick = {
                navController.navigate(Screen.User.route) {
                    popUpTo(Screen.Home.route) {
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
                navController.navigate(Screen.Settings.route) {
                    popUpTo(Screen.Home.route) {
                        saveState = true
                    }
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
            .background(Color(0xFF0D1B2A))
    ) {

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TitleSection()
        }
        Spacer(Modifier.height(16.dp))
        Text(text = "Clean Cache",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        UsageCard()

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .shimmer()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C2B3A))

                    )
                }
            }

        }
        else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(cacheList) { app ->
                    CacheItem(
                        appName = app.appName,
                        size = formatBytes(app.cacheSizeBytes),
                        packageName = app.packageName
                    )
                }
            }
        }
    }
}

fun openAppCacheSettings(context: Context, packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.parse("package:$packageName")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
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

fun scanInstalledApps(context: Context): List<AppRisk> {

    val pm = context.packageManager
    val apps = pm.getInstalledApplications(0)

    return apps.map {

        val risk = when {
            it.packageName.contains("test") -> 70
            it.packageName.contains("hack") -> 90
            else -> (5..40).random()
        }

        AppRisk(
            name = pm.getApplicationLabel(it).toString(),
            riskScore = risk
        )
    }
}

@Composable
fun CacheItem(
    appName: String,
    size: String,
    packageName: String
) {

    val context = LocalContext.current

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
                .background(Color(0xFF1E3A5F))
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
                .background(Color(0xFF00897B))
                .clickable {
                    openAppCacheSettings(context, packageName)
                },
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
            .background(Color(0xFF1E3A5F))
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
                .background(Color(0xFF00897B))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF00897B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "75%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCDD9E5)
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
            .background(Color(0xFF00897B)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            percent,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5)
        )
    }
}

@Composable
fun AlertsScreen() {

    data class AlertData(val leftText: String, val rightText: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

    var alerts by remember {
        mutableStateOf(
            listOf(
                AlertData("Poor network connection — AI processing may take longer than usual.", "Check your internet connection.", Icons.Default.WifiOff),
                AlertData("Free up space to save AI results and continue using the app.","Storage space full", Icons.Default.Storage)
                )
            )

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


        // Header bar

            Text(
                "Alerts",
                color = Color(0xFFCDD9E5),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp)

            )


        Spacer(Modifier.height(16.dp))

        // No alerts card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1C2B3A))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No alerts available",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCDD9E5),
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "You'll see important notifications here when they arrive.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFFCDD9E5),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "✓  You're all caught up!",
                    color = Color(0xFF1E3A5F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Previous alerts section title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Previous Alerts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCDD9E5)
            )
            if (alerts.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E3A5F))
                        .clickable { alerts = emptyList() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Clear All",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Alert items
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C2B3A))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No previous alerts",
                    color = Color(0xFFCDD9E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                alerts.forEach { alert ->
                    AlertItem(
                        leftText = alert.leftText,
                        rightText = alert.rightText,
                        icon = alert.icon,
                        onDismiss = {
                            alerts = alerts.filter { it.leftText != alert.leftText }
                        }
                    )
                }
            }
        }
    }

}

@Composable
fun AlertsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E3A5F))
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
            .background(Color(0xFF1C2B3A))
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertItem(
    leftText: String,
    rightText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDismiss: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E90FF))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1C2B3A))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF1E3A5F),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(leftText, color = Color(0xFFCDD9E5), fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E3A5F))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rightText,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
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
            .background(Color(0xFF1E3A5F))
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


@Composable
fun SettingsSectionHeader(title: String){
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFCDD9E5),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}
//Functionality
@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    expandedDetail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF1E3A5F) else Color(0xFF2A3F55),
        animationSpec = tween(durationMillis = 400),
        label = "bgColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.97f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFCDD9E5),
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 4.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFFCDD9E5),
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00FF87),
                    uncheckedThumbColor = Color(0xFF8899AA),
                    uncheckedTrackColor = Color(0xFF2A3F55)
                )
            )
        }

        // Expanded detail
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33000000))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = expandedDetail,
                    color = Color(0xFFCDD9E5),
                    fontSize = 13.sp
                )
            }
        }
    }
}
@Composable
fun AiRiskReportScreen(navController: NavHostController) {

    val appRisks = remember {
        listOf(
            Triple("WhatsApp", "Safe", 18),
            Triple("Suspicious", "Suspicious", 51),
            Triple("Torjan.Dropper", "Malicious", 92)
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Safe", "Suspicious", "Malicious")
    var expandedItem by remember { mutableStateOf<String?>(null) }

    // Animated risk score
    var scoreVisible by remember { mutableStateOf(false) }
    val animatedScore by animateIntAsState(
        targetValue = if (scoreVisible) 18 else 0,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )

    LaunchedEffect(Unit) {
        scoreVisible = true
    }

    val filteredRisks = if (selectedFilter == "All") appRisks
    else appRisks.filter { it.second == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                        text = "Most of your apps are safe but we found 2 suspicious apps and 1 malicious app.",
                        fontSize = 14.sp,
                        color = Color(0xFFCDD9E5)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Filter buttons
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
                        .clickable { selectedFilter = filter }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (selectedFilter == filter) Color.White else Color(0xFFCDD9E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // App risk list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredRisks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C2B3A))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No $selectedFilter apps found",
                        color = Color(0xFFCDD9E5),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                filteredRisks.forEach { (name, status, score) ->

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
                                expandedItem = if (isExpanded) null else name
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

                        // Expanded detail
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
                                            "Safe" -> "This app has no known threats. It behaves normally and requests only standard permissions."
                                            "Suspicious" -> "This app shows unusual behaviour. It may request excessive permissions or communicate with unknown servers."
                                            "Malicious" -> "This app has been identified as malicious. It is strongly recommended to uninstall it immediately."
                                            else -> "No additional information available."
                                        },
                                        color = Color(0xFFCDD9E5),
                                        fontSize = 13.sp
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

@Composable
fun SplashScreenStandalone() {
    var visible by remember { mutableStateOf(false) }

    val splashAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { alpha = splashAlpha }
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 60.dp,
                            topEnd = 60.dp,
                            bottomStart = 40.dp,
                            bottomEnd = 40.dp
                        )
                    )
                    .background(Color(0xFF1E3A5F)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF87)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Testudo",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E90FF)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your Device Security Guard",
                fontSize = 14.sp,
                color = Color(0xFFCDD9E5),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatusScreen() {

    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<String>>(emptyList()) }
    val isSafe = true
    var suspiciousCount by remember { mutableStateOf(0)}
    var virusCount by remember { mutableStateOf(0) }
    var blockedCount by remember { mutableStateOf(0) }

    val animatedSuspicious by animateIntAsState(
        targetValue = suspiciousCount,
        animationSpec = tween(1000),
        label = "suspicious"
    )

    val animatedVirus by animateIntAsState(
        targetValue = virusCount,
        animationSpec = tween(1000),
        label = "virus"
    )

    val animatedBlocked by animateIntAsState(
        targetValue = blockedCount,
        animationSpec = tween(1000),
        label = "blocked"
    )

    LaunchedEffect(Unit) {
        apps = getMostUsedApps(context)
        suspiciousCount = 0
        virusCount = 0
        blockedCount = 0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "safePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val circleColor = if (isSafe) Color(0xFF00897B) else Color(0xFFB22222)
    val statusText = if (isSafe) "SAFE!" else "THREAT!"
    val statusMessage = if (isSafe) "No Virus has been detected" else "Threats found on your device"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        )
        {
            TitleSection()
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Device Status",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = statusMessage,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))


        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusStatCard(
                label = "Suspicious Activities",
                value = animatedSuspicious,
                color = Color(0xFFFFC107)
            )
            StatusStatCard(
                label = "Virus Detection",
                value = animatedVirus,
                color = Color(0xFFFF3B3B)
            )
            StatusStatCard(
                label = "Virus Blocked",
                value = animatedBlocked,
                color = Color(0xFF00897B)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Most used apps
        Text(
            text = "Most Used Apps (Last 24h)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCDD9E5),
            modifier = Modifier.fillMaxWidth()
        )



        Spacer(Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps) { packageName ->
                StatusAppItem(packageName)

            }

        }
    }
}
@Composable
fun StatusStatCard(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C2B3A))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCDD9E5)
            )
        }
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
@Composable
fun StatusAppItem(packageName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C2B3A))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = packageName,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCDD9E5),
                fontSize = 14.sp
            )
            Text(
                text = "Active in last 24h",
                fontSize = 12.sp,
                color = Color(0xFF8899AA)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E3A5F))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Safe",
                color = Color(0xFF00FF87),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


//PREVIEWS!!!
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
        UserScreen(
            user = User(
                name = "John Doe",
                email = "john@example.com",
                phone = "+44 7123456789",
                paymentDetails = "Visa •••• 1234",
                isPremium = false
            ),
            onUserChange = {}
        )
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
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatusScreenPreview() {
    TestudoTheme {
        StatusScreen()
    }
}