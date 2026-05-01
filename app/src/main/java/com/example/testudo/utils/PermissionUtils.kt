package com.example.testudo.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.testudo.navigation.TestudoApp
import com.example.testudo.ui.screens.SplashScreenStandalone
import kotlinx.coroutines.delay


fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )

    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageStatsPermission(activity: Activity) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    activity.startActivity(intent)
}



@Composable
fun PermissionGate() {

    val context = LocalContext.current
    val activity = context as Activity

    var hasPermission by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    fun refreshPermission() {
        hasPermission = hasUsageStatsPermission(context)
    }

    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
        refreshPermission()

        if (!hasPermission) {
            requestUsageStatsPermission(activity)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            refreshPermission()
        }
    }

    when {
        showSplash -> SplashScreenStandalone()

        hasPermission -> TestudoApp()

        else -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Enable Usage Access in Settings")
        }
    }
}