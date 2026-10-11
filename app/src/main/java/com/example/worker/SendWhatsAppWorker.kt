package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.AttendanceRepository

class SendWhatsAppWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getLong("schedule_id", -1L)
        if (scheduleId == -1L) return Result.failure()

        return try {
            val repo = AttendanceRepository(applicationContext)
            repo.executeSchedule(scheduleId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
