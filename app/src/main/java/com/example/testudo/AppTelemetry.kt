package com.example.testudo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.io.File
import java.util.zip.ZipFile

/**
 * AppTelemetry — Extracts DREBIN-215 features from installed APKs
 * Features are binary (0/1) indicating presence of API calls and permissions
 * These 50 features match exactly what the DREBIN-trained ML model expects
 */
object AppTelemetry {

    data class AppFeatures(
        val appName: String,
        val packageName: String,
        val features: FloatArray
    )

    // 50 DREBIN features in exact order matching feature_columns_drebin.pkl
    val FEATURE_ORDER = listOf(
        "transact",
        "onServiceConnected",
        "bindService",
        "attachInterface",
        "ServiceConnection",
        "android.os.Binder",
        "SEND_SMS",
        "Ljava.lang.Class.getCanonicalName",
        "Ljava.lang.Class.getMethods",
        "Ljava.lang.Class.cast",
        "Ljava.net.URLDecoder",
        "android.content.pm.Signature",
        "android.telephony.SmsManager",
        "READ_PHONE_STATE",
        "getBinder",
        "ClassLoader",
        "Landroid.content.Context.registerReceiver",
        "Ljava.lang.Class.getField",
        "Landroid.content.Context.unregisterReceiver",
        "GET_ACCOUNTS",
        "RECEIVE_SMS",
        "Ljava.lang.Class.getDeclaredField",
        "READ_SMS",
        "getCallingUid",
        "Ljavax.crypto.spec.SecretKeySpec",
        "android.intent.action.BOOT_COMPLETED",
        "USE_CREDENTIALS",
        "MANAGE_ACCOUNTS",
        "android.content.pm.PackageInfo",
        "KeySpec",
        "TelephonyManager.getLine1Number",
        "DexClassLoader",
        "HttpGet.init",
        "SecretKey",
        "Ljava.lang.Class.getMethod",
        "System.loadLibrary",
        "android.intent.action.SEND",
        "Ljavax.crypto.Cipher",
        "WRITE_SMS",
        "READ_SYNC_SETTINGS",
        "android.telephony.gsm.SmsManager",
        "WRITE_HISTORY_BOOKMARKS",
        "TelephonyManager.getSubscriberId",
        "mount",
        "INSTALL_PACKAGES",
        "Runtime.getRuntime",
        "Ljava.lang.Object.getClass",
        "READ_HISTORY_BOOKMARKS",
        "Ljava.lang.Class.forName",
        "Binder"
    )

    // Permissions in the feature list — checked via PackageManager
    private val PERMISSION_FEATURES = setOf(
        "SEND_SMS", "READ_PHONE_STATE", "GET_ACCOUNTS", "RECEIVE_SMS",
        "READ_SMS", "USE_CREDENTIALS", "MANAGE_ACCOUNTS", "WRITE_SMS",
        "READ_SYNC_SETTINGS", "WRITE_HISTORY_BOOKMARKS", "INSTALL_PACKAGES",
        "READ_HISTORY_BOOKMARKS", "mount"
    )

    // Intent actions — checked via manifest
    private val INTENT_FEATURES = setOf(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.SEND"
    )

    /**
     * Extract DREBIN features from an installed app
     * Returns binary (0/1) feature vector of length 50
     */
    fun collectFeatures(context: Context, appInfo: ApplicationInfo): AppFeatures {
        val pm = context.packageManager
        val appName = pm.getApplicationLabel(appInfo).toString()
        val packageName = appInfo.packageName
        val apkPath = appInfo.sourceDir

        // Get permissions
        val permissions = try {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions?.toSet() ?: emptySet()
        } catch (e: Exception) { emptySet<String>() }

        // Scan DEX content for API calls
        val dexContent = extractDexContent(apkPath)

        // Build feature vector
        val features = FloatArray(FEATURE_ORDER.size) { i ->
            val feature = FEATURE_ORDER[i]
            when {
                // Permission features — check manifest permissions
                PERMISSION_FEATURES.contains(feature) -> {
                    if (permissions.any { it.contains(feature) }) 1f else 0f
                }
                // Intent features — check in DEX or manifest
                INTENT_FEATURES.contains(feature) -> {
                    if (dexContent.contains(feature)) 1f else 0f
                }
                // API call features — check in DEX bytecode
                else -> {
                    if (dexContent.contains(feature)) 1f else 0f
                }
            }
        }

        return AppFeatures(appName, packageName, features)
    }

    /**
     * Extract readable strings from APK DEX files
     * Looks for API call signatures and class names
     */
    private fun extractDexContent(apkPath: String): String {
        return try {
            val sb = StringBuilder()
            ZipFile(apkPath).use { zip ->
                val dexEntries = zip.entries().asSequence()
                    .filter { it.name.endsWith(".dex") }
                    .take(3) // scan first 3 dex files for speed

                for (entry in dexEntries) {
                    val bytes = zip.getInputStream(entry).readBytes()
                    // Extract ASCII strings from DEX
                    sb.append(String(bytes, Charsets.ISO_8859_1))
                }
            }
            sb.toString()
        } catch (e: Exception) { "" }
    }

    /**
     * Get all user-installed apps for scanning
     */
    fun getUserApps(context: Context): List<ApplicationInfo> {
        val pm = context.packageManager
        val all = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != "com.example.testudo" }

        // User-installed apps first, then system apps
        val userApps = all.filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        }
        val systemApps = all.filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
        }

        return (userApps + systemApps).take(30)
    }
}