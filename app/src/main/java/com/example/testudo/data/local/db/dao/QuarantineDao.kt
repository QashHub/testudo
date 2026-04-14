package com.example.testudo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testudo.data.local.db.entity.QuarantineRecordEntity

@Dao
interface QuarantineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuarantineRecord(record: QuarantineRecordEntity): Long

    @Query("SELECT * FROM quarantine_records ORDER BY quarantinedAt DESC")
    suspend fun getAllQuarantineRecords(): List<QuarantineRecordEntity>

    @Query("SELECT * FROM quarantine_records WHERE packageName = :packageName ORDER BY quarantinedAt DESC")
    suspend fun getQuarantineRecordsForPackage(packageName: String): List<QuarantineRecordEntity>

    @Query("DELETE FROM quarantine_records")
    suspend fun clearAll()
}