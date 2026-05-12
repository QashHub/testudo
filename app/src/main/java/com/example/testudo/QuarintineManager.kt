// QuarantineManager.kt
package com.example.testudo

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

object QuarantineManager {

    private const val PREFS = "testudo_quarantine"
    private const val KEY   = "quarantined_packages"

    data class QuarantineEntry(
        val packageName: String,
        val appName: String,
        val riskScore: Int,
        val reason: String,
        val timestamp: Long
    )

    // ── Admin helpers ─────────────────────────────────────────────────

    private fun getDpm(context: Context) =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private fun getAdmin(context: Context) =
        ComponentName(context, AdminReceiver::class.java)

    fun isAdminActive(context: Context): Boolean =
        getDpm(context).isAdminActive(getAdmin(context))

    /**
     * Launch the system screen asking the user to grant Device Admin.
     * Call this from your onboarding or settings screen.
     */
    fun requestAdminPrivileges(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdmin(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Testudo needs administrator access to disable malicious apps " +
                        "without uninstalling them, so they can be restored if needed."
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ── Core quarantine actions ───────────────────────────────────────

    /**
     * Quarantine an app:
     * 1. Records it in the local registry
     * 2. Hides/disables it via Device Admin (if active)
     * 3. Falls back to uninstall prompt if admin not granted
     *
     * Returns true if the app was successfully hidden (admin path),
     * false if fallback uninstall prompt was used instead.
     */
    fun quarantine(context: Context, result: RealScanner.ScanResult): Boolean {
        // Always record in registry regardless of method used
        addToRegistry(context, result)

        return if (isAdminActive(context)) {
            disableApp(context, result.packageName)
            true
        } else {
            // Admin not granted — prompt uninstall as fallback
            promptUninstall(context, result.packageName)
            false
        }
    }

    /**
     * Restore a quarantined app — makes it visible and runnable again.
     */
    fun restore(context: Context, packageName: String): Boolean {
        removeFromRegistry(context, packageName)

        return if (isAdminActive(context)) {
            getDpm(context).setApplicationHidden(getAdmin(context), packageName, false)
            Toast.makeText(context, "App restored", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(
                context,
                "Admin access needed to restore — app may already be uninstalled",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    // ── Registry (persisted list of quarantined apps) ─────────────────

    fun isQuarantined(context: Context, packageName: String): Boolean =
        getAll(context).any { it.packageName == packageName }

    fun getAll(context: Context): List<QuarantineEntry> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                QuarantineEntry(
                    packageName = o.getString("packageName"),
                    appName     = o.getString("appName"),
                    riskScore   = o.getInt("riskScore"),
                    reason      = o.getString("reason"),
                    timestamp   = o.getLong("timestamp")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Private helpers ───────────────────────────────────────────────

    private fun disableApp(context: Context, packageName: String) {
        try {
            getDpm(context).setApplicationHidden(getAdmin(context), packageName, true)
            Toast.makeText(context, "App quarantined (disabled)", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            // Can't hide system apps — fall back to warning only
            Toast.makeText(
                context,
                "Cannot disable system app — recorded as threat",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun promptUninstall(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun addToRegistry(context: Context, result: RealScanner.ScanResult) {
        val entries = getAll(context).toMutableList()
        entries.removeAll { it.packageName == result.packageName }
        entries.add(
            QuarantineEntry(
                packageName = result.packageName,
                appName     = result.appName,
                riskScore   = result.riskScore,
                reason      = result.reasons.firstOrNull() ?: "Flagged by scan",
                timestamp   = System.currentTimeMillis()
            )
        )
        save(context, entries)
    }

    private fun removeFromRegistry(context: Context, packageName: String) {
        val entries = getAll(context).toMutableList()
        entries.removeAll { it.packageName == packageName }
        save(context, entries)
    }

    private fun save(context: Context, entries: List<QuarantineEntry>) {
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(JSONObject().apply {
                put("packageName", e.packageName)
                put("appName",     e.appName)
                put("riskScore",   e.riskScore)
                put("reason",      e.reason)
                put("timestamp",   e.timestamp)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }
}