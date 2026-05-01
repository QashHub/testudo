package com.example.testudo.utils

import android.content.Context


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