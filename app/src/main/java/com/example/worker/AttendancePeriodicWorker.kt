package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.AttendanceRepository
import java.util.Calendar

class AttendancePeriodicWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "AttendanceWorker"
        const val WORK_NAME = "periodic_attendance_check"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Periodic attendance check running in background...")
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val isEnabled = (db.settingDao().getSettingValue(AppDatabase.KEY_SCHEDULER_ENABLED) ?: "true").toBoolean()
            if (!isEnabled) {
                return Result.success()
            }

            val now = com.example.util.DateTimeUtils.getJakartaCalendar()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)

            val daysMap = mapOf(
                Calendar.SUNDAY to "minggu",
                Calendar.MONDAY to "senin",
                Calendar.TUESDAY to "selasa",
                Calendar.WEDNESDAY to "rabu",
                Calendar.THURSDAY to "kamis",
                Calendar.FRIDAY to "jumat",
                Calendar.SATURDAY to "sabtu"
            )
            val todayName = daysMap[dayOfWeek] ?: ""

            val enabledSchedules = db.scheduleDao().getEnabledSchedules()
            val repo = AttendanceRepository(applicationContext)

            for (schedule in enabledSchedules) {
                val isTodayActive = schedule.daysOfWeek.split(",")
                    .map { it.trim().lowercase() }
                    .contains(todayName)

                if (isTodayActive) {
                    // Check if schedule time matches current window (within 15 minutes)
                    val scheduleTotalMinutes = schedule.hour * 60 + schedule.minute
                    val currentTotalMinutes = currentHour * 60 + currentMinute
                    val diff = currentTotalMinutes - scheduleTotalMinutes

                    // If scheduled time is right now (within 0..14 minutes window)
                    if (diff in 0..14) {
                        Log.d(TAG, "Periodic worker executing schedule ${schedule.id} (${schedule.title})")
                        repo.executeSchedule(schedule.id)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in periodic attendance check", e)
            Result.retry()
        }
    }
}
