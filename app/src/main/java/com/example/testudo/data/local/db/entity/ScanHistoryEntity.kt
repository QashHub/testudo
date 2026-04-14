package com.example.testudo.data.local.db.entity


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_history",
    indices = [
        Index(value = ["scannedAt"]),
        Index(value = ["packageName"])
    ]
)
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val packageName: String,
    val appName: String,

    val scannedAt: Long,

    val riskScore: Int,
    val riskLevel: String,

    val behaviorSummary: String,
    val actionTaken: String,

    val isManualScan: Boolean = false,
    val modelVersion: String? = null
)