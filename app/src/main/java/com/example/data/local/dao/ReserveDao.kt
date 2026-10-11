package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedMealTypeEntity
import com.example.data.local.entity.CachedReserveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReserveDao {
    @Query("SELECT * FROM cached_reserves ORDER BY date ASC, mealTypeId ASC")
    fun getAllReserves(): Flow<List<CachedReserveEntity>>

    @Query("SELECT * FROM cached_reserves")
    suspend fun getAllReservesDirect(): List<CachedReserveEntity>

    @Query("SELECT * FROM cached_reserves WHERE date = :date ORDER BY mealTypeId ASC")
    fun getReservesForDate(date: String): Flow<List<CachedReserveEntity>>

    @Query("SELECT * FROM cached_reserves WHERE date = :date AND mealTypeId = :mealTypeId LIMIT 1")
    suspend fun getReserve(date: String, mealTypeId: Int): CachedReserveEntity?

    @Query("SELECT * FROM cached_reserves WHERE reserveId = :reserveId LIMIT 1")
    suspend fun getReserveById(reserveId: Long): CachedReserveEntity?

    @Query("SELECT * FROM cached_reserves WHERE date = :date ORDER BY mealTypeId ASC")
    suspend fun getReservesForDateDirect(date: String): List<CachedReserveEntity>

    @Query("SELECT * FROM cached_reserves WHERE date >= :todayDate AND consumed = 0 ORDER BY date ASC, mealTypeId ASC")
    suspend fun getUpcomingUnconsumedReservesDirect(todayDate: String): List<CachedReserveEntity>

    @Query("SELECT * FROM cached_reserves WHERE date >= :todayDate ORDER BY date ASC, mealTypeId ASC LIMIT 1")
    suspend fun getNextUpcomingReserve(todayDate: String): CachedReserveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserves(reserves: List<CachedReserveEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserve(reserve: CachedReserveEntity)

    @Query("DELETE FROM cached_reserves WHERE reserveId = :reserveId")
    suspend fun deleteReserve(reserveId: Long)

    @Query("UPDATE cached_reserves SET forgotCardCode = :code, codeValid = :valid WHERE reserveId = :reserveId")
    suspend fun updateCode(reserveId: Long, code: String, valid: Boolean)

    @Query("UPDATE cached_reserves SET consumed = :consumed WHERE reserveId = :reserveId")
    suspend fun updateConsumedStatus(reserveId: Long, consumed: Boolean)

    @Query("SELECT * FROM cached_reserves WHERE consumed = 1 ORDER BY date DESC, mealTypeId ASC")
    fun getArchivedReserves(): Flow<List<CachedReserveEntity>>

    @Query("SELECT * FROM cached_reserves WHERE consumed = 0 ORDER BY date ASC, mealTypeId ASC")
    fun getActiveReserves(): Flow<List<CachedReserveEntity>>

    @Query("DELETE FROM cached_reserves")
    suspend fun clearReserves()

    @Query("DELETE FROM cached_reserves WHERE isGuest = 0")
    suspend fun clearNonGuestReserves()

    @Query("SELECT * FROM cached_meal_types ORDER BY disPriority ASC")
    fun getAllMealTypes(): Flow<List<CachedMealTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealTypes(mealTypes: List<CachedMealTypeEntity>)

    @Query("DELETE FROM cached_meal_types")
    suspend fun clearMealTypes()
}
