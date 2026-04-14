package com.example.testudo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testudo.data.local.db.entity.ScanHistoryEntity

@Dao
interface ScanHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(scanHistory: ScanHistoryEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY scannedAt DESC")
    suspend fun getAllScanHistory(): List<ScanHistoryEntity>

    @Query("SELECT * FROM scan_history WHERE packageName = :packageName ORDER BY scannedAt DESC")
    suspend fun getScanHistoryForPackage(packageName: String): List<ScanHistoryEntity>

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}