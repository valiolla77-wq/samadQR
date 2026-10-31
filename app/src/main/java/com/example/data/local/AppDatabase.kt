package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.PendingReserveDao
import com.example.data.local.dao.ReserveDao
import com.example.data.local.entity.CachedMealTypeEntity
import com.example.data.local.entity.CachedReserveEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.PendingReserveEntity

@Database(
    entities = [
        CachedReserveEntity::class,
        CachedMealTypeEntity::class,
        ChatMessageEntity::class,
        PendingReserveEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reserveDao(): ReserveDao
    abstract fun chatDao(): ChatDao
    abstract fun pendingReserveDao(): PendingReserveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "samad_food.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
