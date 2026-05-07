package com.example.testudo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * InstallMonitor — BroadcastReceiver that fires when any app is installed
 * Immediately scans the new app and notifies the user if a threat is found
 *
 * Register in AndroidManifest.xml:
 *
 * <receiver android:name=".InstallMonitor" android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.PACKAGE_ADDED"/>
 *         <action android:name="android.intent.action.PACKAGE_REPLACED"/>
 *         <data android:scheme="package"/>
 *     </intent-filter>
 * </receiver>
 */
class InstallMonitor : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return

        // Skip our own app
        if (packageName == context.packageName) return

        // Skip system updates
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false) &&
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            // Only scan replacements if the package was previously flagged
            if (!UserListManager.isBlacklisted(context, packageName)) return
        }

        // Run scan in background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ml = MLEngine(context)
                val scanResult = ApkScanner.scanInstalledApp(context, packageName)
                ml.close()

                // Notify if threats found
                if (scanResult.riskScore >= 30) {
                    sendInstallAlert(context, scanResult)
                }

                // Auto-blacklist if very high risk
                if (scanResult.riskScore >= 80) {
                    UserListManager.addToBlacklist(context, packageName)
                }

            } catch (e: Exception) {
                // Silent fail — don't crash on install events
            }
        }
    }

    private fun sendInstallAlert(context: Context, result: ApkScanner.ApkScanResult) {
        val channelId = "testudo_install_alerts"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New App Scan Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a newly installed app is flagged"
            }
            nm.createNotificationChannel(channel)
        }

        val severity = when {
            result.riskScore >= 60 -> "⛔ MALICIOUS"
            result.riskScore >= 30 -> "⚠️ SUSPICIOUS"
            else                   -> "✓ Safe"
        }

        val topThreat = result.threats.maxByOrNull {
            when (it.severity) {
                ApkScanner.Severity.CRITICAL -> 4
                ApkScanner.Severity.HIGH     -> 3
                ApkScanner.Severity.MEDIUM   -> 2
                ApkScanner.Severity.LOW      -> 1
                ApkScanner.Severity.INFO     -> 0
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("scan_package", result.packageName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, result.packageName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$severity — ${result.appName}")
            .setContentText(topThreat?.title ?: "Risk score: ${result.riskScore}/100")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${topThreat?.description ?: ""}\n\nRisk Score: ${result.riskScore}/100. Tap to view full report."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(result.packageName.hashCode(), notification)
    }
}
