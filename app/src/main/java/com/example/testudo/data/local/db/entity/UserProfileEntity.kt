package com.example.testudo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val email: String,
    val phone: String,
    val paymentDetails: String,
    val isPremium: Boolean
)