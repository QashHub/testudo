package com.example.testudo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.testudo.data.local.db.dao.QuarantineDao
import com.example.testudo.data.local.db.dao.ScanHistoryDao
import com.example.testudo.data.local.db.dao.ThreatLogDao
import com.example.testudo.data.local.db.dao.UserProfileDao
import com.example.testudo.data.local.db.dao.VirusSignatureDao
import com.example.testudo.data.local.db.entity.QuarantineRecordEntity
import com.example.testudo.data.local.db.entity.ScanHistoryEntity
import com.example.testudo.data.local.db.entity.ThreatLogEntity
import com.example.testudo.data.local.db.entity.UserProfileEntity
import com.example.testudo.data.local.db.entity.VirusSignatureEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ScanHistoryEntity::class,
        ThreatLogEntity::class,
        QuarantineRecordEntity::class,
        VirusSignatureEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun threatLogDao(): ThreatLogDao
    abstract fun quarantineDao(): QuarantineDao
    abstract fun virusSignatureDao(): VirusSignatureDao
}