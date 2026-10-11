package com.example.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.SamadRepository
import com.example.data.security.SecurePrefs

class SyncReservesWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = SecurePrefs(context)
        if (!prefs.autoSyncDaily) {
            return Result.success()
        }

        val repository = SamadRepository(context, prefs)
        val result = repository.fetchAndCacheReserves()
        return when (result) {
            is com.example.data.repository.SyncResult.Success -> Result.success()
            is com.example.data.repository.SyncResult.NoReservesFound -> Result.success()
            is com.example.data.repository.SyncResult.Error -> {
                if (result.isAuthError) Result.failure() else Result.retry()
            }
        }
    }
}
