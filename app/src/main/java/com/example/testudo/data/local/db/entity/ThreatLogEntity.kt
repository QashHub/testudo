package com.example.testudo.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "threat_logs",
    foreignKeys = [
        ForeignKey(
            entity = ScanHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanHistoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scanHistoryId"]),
        Index(value = ["packageName"]),
        Index(value = ["detectedAt"])
    ]
)
data class ThreatLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val scanHistoryId: Long,

    val packageName: String,
    val appName: String,

    val threatType: String,
    val severity: String,

    val detectionReason: String,
    val detectedAt: Long,

    val confidenceScore: Float? = null,
    val recommendedAction: String? = null
)