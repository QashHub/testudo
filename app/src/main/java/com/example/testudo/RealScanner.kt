package com.example.testudo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * RealScanner — On-device malware detection
 * Combines: blacklist, whitelist, permission analysis, install source, ML score
 */
object RealScanner {

    data class ScanResult(
        val appName: String,
        val packageName: String,
        val riskScore: Int,
        val label: String,
        val reasons: List<String>,
        val isSideloaded: Boolean,
        val isBlacklisted: Boolean,
        val isWhitelisted: Boolean,
        val mlScore: Float
    )

    private val MALWARE_BLACKLIST = setOf(
        "com.imagecompress.android",
        "com.contact.withme.texts",
        "com.cheery.message.sendsms",
        "com.bypass.memoryleak",
        "com.tencent.mm.b",
        "com.android.service.request",
        "com.nearly.app",
        "com.nanomark.cleaner",
        "com.android.settings.backup.helper",
        "com.android.phone.service",
        "com.android.update.service"
    )

    private val DANGEROUS_COMBOS = listOf(
        Triple(listOf("READ_SMS","SEND_SMS","RECEIVE_SMS"),          35, "Reads and sends SMS — common in SMS fraud malware"),
        Triple(listOf("RECORD_AUDIO","ACCESS_FINE_LOCATION"),        30, "Records audio and tracks location — stalkerware pattern"),
        Triple(listOf("READ_CONTACTS","INTERNET","READ_CALL_LOG"),   25, "Reads contacts and call logs with internet — possible data theft"),
        Triple(listOf("CAMERA","RECORD_AUDIO","ACCESS_FINE_LOCATION"),40,"Camera, mic and location — high risk spyware pattern"),
        Triple(listOf("BIND_ACCESSIBILITY_SERVICE","INTERNET"),      30, "Accessibility service with internet — possible credential theft"),
        Triple(listOf("INSTALL_PACKAGES","INTERNET"),                35, "Can install apps silently from internet — dropper pattern"),
        Triple(listOf("WRITE_EXTERNAL_STORAGE","READ_EXTERNAL_STORAGE","INTERNET"), 20, "Full storage access with internet — possible ransomware"),
    )

    private val HIGH_RISK_SINGLE = mapOf(
        "INSTALL_PACKAGES"            to Pair(25, "Can install other apps silently"),
        "DELETE_PACKAGES"             to Pair(20, "Can uninstall apps"),
        "BIND_DEVICE_ADMIN"           to Pair(20, "Has device administrator privileges"),
        "BIND_ACCESSIBILITY_SERVICE"  to Pair(15, "Uses accessibility service"),
        "READ_SMS"                    to Pair(15, "Can read your SMS messages"),
        "PROCESS_OUTGOING_CALLS"      to Pair(10, "Can intercept phone calls"),
    )

    fun scanApp(context: Context, appInfo: ApplicationInfo, mlEngine: MLEngine): ScanResult {
        val pm = context.packageManager
        val appName = pm.getApplicationLabel(appInfo).toString()
        val packageName = appInfo.packageName
        val reasons = mutableListOf<String>()
        var riskScore = 0

        // ── User whitelist — always safe ──────────────────────────────
        val isWhitelisted = UserListManager.isWhitelisted(context, packageName)
        if (isWhitelisted) {
            return ScanResult(
                appName = appName, packageName = packageName,
                riskScore = 0, label = "Safe",
                reasons = listOf("You marked this app as safe"),
                isSideloaded = false, isBlacklisted = false,
                isWhitelisted = true, mlScore = 0f
            )
        }

        // ── User blacklist — always malicious ─────────────────────────
        val isUserBlacklisted = UserListManager.isBlacklisted(context, packageName)
        if (isUserBlacklisted) {
            return ScanResult(
                appName = appName, packageName = packageName,
                riskScore = 100, label = "Malicious",
                reasons = listOf("You marked this app as malicious"),
                isSideloaded = false, isBlacklisted = true,
                isWhitelisted = false, mlScore = 100f
            )
        }

        // ── Built-in malware blacklist ────────────────────────────────
        val isBlacklisted = packageName in MALWARE_BLACKLIST
        if (isBlacklisted) {
            riskScore += 80
            reasons.add("Matches known malware package name database")
        }

        // ── Install source ────────────────────────────────────────────
        val isSideloaded = isSideloaded(context, packageName)
        if (isSideloaded) {
            riskScore += 15
            reasons.add("Not installed from Google Play Store")
        }

        // ── Permission analysis ───────────────────────────────────────
        val permissions = try {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions?.toList() ?: emptyList()
        } catch (e: Exception) { emptyList() }

        var permScore = 0
        for ((combo, score, reason) in DANGEROUS_COMBOS) {
            if (combo.all { perm -> permissions.any { it.contains(perm) } }) {
                permScore += score
                reasons.add(reason)
            }
        }
        for ((perm, scoreReason) in HIGH_RISK_SINGLE) {
            if (permissions.any { it.contains(perm) }) {
                permScore += scoreReason.first
                reasons.add(scoreReason.second)
            }
        }
        riskScore += permScore.coerceAtMost(40)

        // ── ML behavioral score ───────────────────────────────────────
        val telemetry = AppTelemetry.collectFeatures(context, appInfo)
        val mlResult = mlEngine.predict(telemetry.features)
        val mlScore = mlResult.riskScore
        riskScore += (mlScore / 100f * 25f).toInt()
        if (mlScore > 60) reasons.add("Behavioral analysis flagged suspicious activity")

        // ── App age check ─────────────────────────────────────────────
        try {
            val pkgInfo = pm.getPackageInfo(packageName, 0)
            val daysSinceInstall = (System.currentTimeMillis() -
                    pkgInfo.firstInstallTime) / (1000 * 60 * 60 * 24)
            if (daysSinceInstall < 7 && permScore > 20) {
                riskScore += 10
                reasons.add("Recently installed with elevated permissions")
            }
        } catch (e: Exception) { }

        val finalScore = riskScore.coerceIn(0, 100)
        val label = when {
            finalScore >= 60 -> "Malicious"
            finalScore >= 30 -> "Suspicious"
            else             -> "Safe"
        }

        if (reasons.isEmpty()) reasons.add("No threats detected — app appears safe")

        return ScanResult(
            appName = appName, packageName = packageName,
            riskScore = finalScore, label = label,
            reasons = reasons, isSideloaded = isSideloaded,
            isBlacklisted = isBlacklisted, isWhitelisted = false,
            mlScore = mlScore
        )
    }

    fun scanAllApps(context: Context, mlEngine: MLEngine): List<ScanResult> {
        return AppTelemetry.getUserApps(context)
            .filter { it.packageName != "com.example.testudo" }
            .map { scanApp(context, it, mlEngine) }
            .sortedByDescending { it.riskScore }
    }

    private fun isSideloaded(context: Context, packageName: String): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName)
            }
            installer == null || installer !in listOf(
                "com.android.vending",
                "com.google.android.feedback",
                "com.amazon.venezia",
                "com.sec.android.app.samsungapps"
            )
        } catch (e: Exception) { false }
    }
}
