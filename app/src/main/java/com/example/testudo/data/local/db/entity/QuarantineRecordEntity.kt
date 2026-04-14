package com.example.testudo.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quarantine_records",
    foreignKeys = [
        ForeignKey(
            entity = ThreatLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["threatLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["threatLogId"]),
        Index(value = ["packageName"]),
        Index(value = ["quarantinedAt"])
    ]
)
data class QuarantineRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val threatLogId: Long,

    val packageName: String,
    val appName: String,

    val quarantinedAt: Long,
    val quarantineReason: String,

    val actionStatus: String,
    val evidenceSnapshotPath: String? = null,

    val permissionsRevoked: Boolean = false,
    val backgroundExecutionBlocked: Boolean = false,
    val isRestored: Boolean = false
)