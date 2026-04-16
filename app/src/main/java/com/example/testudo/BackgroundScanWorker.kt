package com.example.testudo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * BackgroundScanWorker — runs automatic scans every 6 hours
 * Uses WorkManager so it survives app restarts
 * Sends a notification if malicious/suspicious apps are found
 */
class BackgroundScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val ml = MLEngine(applicationContext)
            val results = RealScanner.scanAllApps(applicationContext, ml)
            ml.close()

            val threats = results.filter {
                it.label == "Malicious" || it.label == "Suspicious"
            }

            if (threats.isNotEmpty()) {
                sendThreatNotification(threats)
            }

            // Save last scan time
            applicationContext.getSharedPreferences("testudo_scan", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_scan_time", System.currentTimeMillis())
                .putInt("last_threat_count", threats.size)
                .apply()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendThreatNotification(threats: List<RealScanner.ScanResult>) {
        val channelId = "testudo_threats"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create notification channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Testudo Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when malicious apps are detected"
            }
            nm.createNotificationChannel(channel)
        }

        // Build notification
        val malicious   = threats.count { it.label == "Malicious" }
        val suspicious  = threats.count { it.label == "Suspicious" }
        val title = when {
            malicious > 0  -> "⚠️ Malicious app detected!"
            suspicious > 0 -> "Suspicious apps found"
            else           -> "Scan complete"
        }
        val message = buildString {
            if (malicious > 0)  append("$malicious malicious app(s). ")
            if (suspicious > 0) append("$suspicious suspicious app(s). ")
            append("Tap to view details.")
        }

        // Intent to open app when notification tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notification)
    }

    companion object {
        private const val WORK_NAME = "testudo_background_scan"

        /**
         * Start periodic background scanning every 6 hours
         * Call this from MainActivity.onCreate()
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)  // don't scan when battery is low
                .build()

            val request = PeriodicWorkRequestBuilder<BackgroundScanWorker>(
                6, TimeUnit.HOURS  // scan every 6 hours
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // don't replace if already scheduled
                request
            )
        }

        /**
         * Cancel background scanning
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Get time of last scan
         */
        fun getLastScanTime(context: Context): Long {
            return context.getSharedPreferences("testudo_scan", Context.MODE_PRIVATE)
                .getLong("last_scan_time", 0L)
        }

        /**
         * Get threat count from last scan
         */
        fun getLastThreatCount(context: Context): Int {
            return context.getSharedPreferences("testudo_scan", Context.MODE_PRIVATE)
                .getInt("last_threat_count", 0)
        }
    }
}
