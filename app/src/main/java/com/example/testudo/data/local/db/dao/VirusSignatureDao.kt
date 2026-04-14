package com.example.testudo.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testudo.data.local.db.entity.VirusSignatureEntity

@Dao
interface VirusSignatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirusSignature(signature: VirusSignatureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirusSignatures(signatures: List<VirusSignatureEntity>)

    @Query("SELECT * FROM virus_signatures ORDER BY virusName ASC")
    suspend fun getAllVirusSignatures(): List<VirusSignatureEntity>

    @Query("SELECT * FROM virus_signatures WHERE signatureHash = :signatureHash LIMIT 1")
    suspend fun getSignatureByHash(signatureHash: String): VirusSignatureEntity?

    @Query("DELETE FROM virus_signatures")
    suspend fun clearAll()
}