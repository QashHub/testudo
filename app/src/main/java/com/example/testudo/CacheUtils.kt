package com.example.testudo

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.storage.StorageManager
import android.os.Build
import android.os.UserHandle
import java.util.*

fun getAppsSortedByCache(context: Context): List<AppCacheInfo> {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return emptyList()
    }

    val pm = context.packageManager
    val storageStatsManager =
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

    val storageManager =
        context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

    val uuid = storageManager.primaryStorageVolume.uuid?.let {
        UUID.fromString(it)
    } ?: StorageManager.UUID_DEFAULT

    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    val cacheList = mutableListOf<AppCacheInfo>()

    for (app in apps) {
        try {
            val stats = storageStatsManager.queryStatsForPackage(
                uuid,
                app.packageName,
                UserHandle.getUserHandleForUid(app.uid)
            )

            val cacheSize = stats.cacheBytes

            if (cacheSize > 0) {
                cacheList.add(
                    AppCacheInfo(
                        appName = pm.getApplicationLabel(app).toString(),
                        packageName = app.packageName,
                        cacheSizeBytes = cacheSize
                    )
                )
            }

        } catch (e: Exception) {
            // for apps that cant be queried
        }
    }

    return cacheList.sortedByDescending { it.cacheSizeBytes }
}