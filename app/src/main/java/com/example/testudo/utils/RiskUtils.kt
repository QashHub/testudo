package com.example.testudo.utils

import android.content.Context
import com.example.testudo.AppRisk


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


fun scanInstalledApps(context: Context): List<AppRisk> {

    val pm = context.packageManager
    val apps = pm.getInstalledApplications(0)

    val suspiciousPermissions = listOf(
        "android.permission.SEND_SMS",
        "android.permission.READ_SMS",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_CONTACTS"
    )

    return apps.map { app ->

        val packageInfo = pm.getPackageInfo(
            app.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS
        )

        val permissions = packageInfo.requestedPermissions ?: emptyArray()

        var riskScore = 0

        permissions.forEach {
            if (suspiciousPermissions.contains(it)) {
                riskScore += 20
            }
        }

        if (app.packageName.contains("hack")) riskScore += 60
        if (app.packageName.contains("spy")) riskScore += 50
        if (app.packageName.contains("test")) riskScore += 20

        riskScore = riskScore.coerceAtMost(100)

        AppRisk(
            name = pm.getApplicationLabel(app).toString(),
            packageName = app.packageName,
            riskScore = riskScore
        )
    }
}




