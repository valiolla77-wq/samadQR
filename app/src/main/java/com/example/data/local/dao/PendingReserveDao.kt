package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PendingReserveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingReserveDao {

    @Query("SELECT * FROM pending_reserves ORDER BY createdAt DESC")
    fun getAllPendingReserves(): Flow<List<PendingReserveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(reserve: PendingReserveEntity)

    @Query("DELETE FROM pending_reserves WHERE programId = :programId")
    suspend fun deletePending(programId: Long)

    @Query("DELETE FROM pending_reserves")
    suspend fun clearAll()
}
