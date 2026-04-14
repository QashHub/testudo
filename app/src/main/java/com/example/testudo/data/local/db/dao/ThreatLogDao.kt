package com.example.testudo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testudo.data.local.db.entity.ThreatLogEntity

@Dao
interface ThreatLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatLog(threatLog: ThreatLogEntity): Long

    @Query("SELECT * FROM threat_logs ORDER BY detectedAt DESC")
    suspend fun getAllThreatLogs(): List<ThreatLogEntity>

    @Query("SELECT * FROM threat_logs WHERE scanHistoryId = :scanHistoryId ORDER BY detectedAt DESC")
    suspend fun getThreatLogsForScan(scanHistoryId: Long): List<ThreatLogEntity>

    @Query("DELETE FROM threat_logs")
    suspend fun clearAll()
}