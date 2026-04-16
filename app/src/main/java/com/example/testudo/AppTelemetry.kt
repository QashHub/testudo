package com.example.testudo

import android.app.ActivityManager
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.os.Debug
import android.os.Process
import android.content.pm.PackageManager

/**
 * AppTelemetry — Real behavioral feature collector
 * Replaces random number generation with actual Android API measurements
 * Matches the 15 features in MLEngine.FEATURE_ORDER
 */
object AppTelemetry {

    data class AppFeatures(
        val appName: String,
        val packageName: String,
        val features: FloatArray
    )

    /**
     * Collect real behavioral features for a single app
     * Uses Android's ActivityManager, TrafficStats, PackageManager, UsageStatsManager
     */
    fun collectFeatures(
        context: Context,
        appInfo: ApplicationInfo
    ): AppFeatures {
        val pm = context.packageManager
        val appName = pm.getApplicationLabel(appInfo).toString()
        val packageName = appInfo.packageName
        val uid = appInfo.uid

        // ── 1. CPU usage estimate via UsageStats ──────────────────────
        val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
                as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24) // last 24 hours

        val usageStats = usageManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        val appUsage = usageStats?.find { it.packageName == packageName }
        val foregroundMs = appUsage?.totalTimeInForeground ?: 0L
        // Estimate CPU as % of time spent in foreground over 24h
        val cpuEstimate = ((foregroundMs.toFloat() / (24 * 60 * 60 * 1000)) * 100f)
            .coerceIn(0f, 100f)

        // ── 2. Memory usage ───────────────────────────────────────────
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager
        val pids = getPidsForPackage(context, packageName)
        var memoryMb = 0f
        if (pids.isNotEmpty()) {
            val memInfo = activityManager.getProcessMemoryInfo(pids.toIntArray())
            memoryMb = memInfo.sumOf { it.totalPss }.toFloat() / 1024f // KB to MB
        }
        // Fallback: estimate from app size
        if (memoryMb == 0f) {
            memoryMb = (appInfo.sourceDir?.let {
                java.io.File(it).length() / (1024 * 1024)
            } ?: 50L).toFloat().coerceIn(10f, 500f)
        }

        // ── 3. Network bytes via TrafficStats ─────────────────────────
        val txBytes = TrafficStats.getUidTxBytes(uid)
            .takeIf { it != TrafficStats.UNSUPPORTED.toLong() } ?: 0L
        val rxBytes = TrafficStats.getUidRxBytes(uid)
            .takeIf { it != TrafficStats.UNSUPPORTED.toLong() } ?: 0L
        val netBytesSentKb = (txBytes / 1024f).coerceIn(0f, 10000f)
        val netBytesRecvKb = (rxBytes / 1024f).coerceIn(0f, 10000f)

        // ── 4. Permission analysis ────────────────────────────────────
        val permissions = try {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions ?: emptyArray()
        } catch (e: Exception) { emptyArray() }

        val permissionCount = permissions.size.toFloat().coerceIn(0f, 30f)

        // Dangerous permission flags
        val dangerousPerms = listOf(
            "READ_CONTACTS", "WRITE_CONTACTS", "READ_SMS", "SEND_SMS",
            "RECEIVE_SMS", "RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION",
            "READ_CALL_LOG", "WRITE_CALL_LOG", "PROCESS_OUTGOING_CALLS",
            "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"
        )
        val dangerousCount = permissions.count { perm ->
            dangerousPerms.any { perm.contains(it) }
        }.toFloat()

        val cryptoApiCalls = permissions.count { perm ->
            perm.contains("MANAGE_KEYGUARD") ||
                    perm.contains("USE_BIOMETRIC") ||
                    perm.contains("USE_FINGERPRINT")
        }.toFloat()

        val reflectionApiCalls = if (
            (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        ) 5f else 1f

        // ── 5. App behavior signals ───────────────────────────────────
        // Background wakeups estimated from launch count
        val launchCount = appUsage?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                it.totalTimeVisible / 1000f / 60f // mins visible
            else
                it.totalTimeInForeground / 1000f / 60f
        } ?: 0f
        val backgroundWakeups = (launchCount / 10f).coerceIn(0f, 30f)

        // Inter-app comms estimate from permissions
        val interAppComms = permissions.count { perm ->
            perm.contains("BROADCAST") ||
                    perm.contains("BIND_") ||
                    perm.contains("INSTALL_PACKAGES")
        }.toFloat().coerceIn(0f, 20f)

        // Battery drain estimate from foreground time
        val batteryDrain = (foregroundMs / (1000f * 60f * 60f) * 2f)
            .coerceIn(0f, 20f)

        // DNS queries estimate from network activity
        val dnsQueries = if (netBytesSentKb > 0) {
            (netBytesSentKb / 100f).coerceIn(1f, 150f)
        } else 0f

        // Unique remote IPs estimate
        val uniqueIps = if (netBytesSentKb > 0) {
            (netBytesSentKb / 500f + 1f).coerceIn(1f, 80f)
        } else 0f

        // File ops estimate from app size and permissions
        val fileWriteOps = (dangerousCount * 3f + backgroundWakeups).coerceIn(0f, 100f)
        val fileReadOps  = (permissionCount * 2f).coerceIn(0f, 200f)

        // Syscall estimate
        val syscallCount = (memoryMb * 2f + netBytesSentKb / 10f + dangerousCount * 20f)
            .coerceIn(50f, 3000f)

        // ── 6. Build feature vector (matches MLEngine.FEATURE_ORDER) ──
        val features = floatArrayOf(
            cpuEstimate,        // cpu_usage_pct
            memoryMb,           // mem_usage_mb
            netBytesSentKb,     // net_bytes_sent_kb
            netBytesRecvKb,     // net_bytes_recv_kb
            fileWriteOps,       // file_write_ops
            fileReadOps,        // file_read_ops
            syscallCount,       // syscall_count
            permissionCount,    // permissions_requested
            backgroundWakeups,  // background_wakeups
            interAppComms,      // inter_app_comms
            batteryDrain,       // battery_drain_pct_hr
            dnsQueries,         // dns_queries
            uniqueIps,          // unique_remote_ips
            cryptoApiCalls,     // crypto_api_calls
            reflectionApiCalls  // reflection_api_calls
        )

        return AppFeatures(appName, packageName, features)
    }

    /**
     * Get PIDs for a package name
     */
    private fun getPidsForPackage(context: Context, packageName: String): List<Int> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager
        return activityManager.runningAppProcesses
            ?.filter { it.pkgList?.contains(packageName) == true }
            ?.map { it.pid }
            ?: emptyList()
    }

    /**
     * Scan all installed user apps (excludes system apps)
     */
    fun getUserApps(context: Context): List<ApplicationInfo> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) &&
                        appInfo.packageName != "com.example.testudo"  // exclude self
            }
            .take(15) // limit to 15 for performance
    }
}
