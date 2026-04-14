package com.example.testudo.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "virus_signatures",
    indices = [
        Index(value = ["signatureHash"], unique = true),
        Index(value = ["virusName"])
    ]
)
data class VirusSignatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val signatureHash: String,
    val virusName: String,

    val severity: String,
    val description: String? = null,

    val recommendedAction: String? = null,
    val definitionVersion: String? = null,

    val createdAt: Long,
    val updatedAt: Long? = null
)