package com.example.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ScheduleEntity
import com.example.receiver.AlarmReceiver
import com.example.worker.AttendancePeriodicWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ScheduleManager {
    private const val TAG = "ScheduleManager"

    suspend fun scheduleAllActiveReminders(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val isEnabled = (db.settingDao().getSettingValue(AppDatabase.KEY_SCHEDULER_ENABLED) ?: "true").toBoolean()
            if (!isEnabled) {
                Log.d(TAG, "Scheduler is disabled in settings. Skipping.")
                return@withContext
            }

            // 1. Schedule exact alarms for each enabled schedule
            val schedules = db.scheduleDao().getEnabledSchedules()
            for (schedule in schedules) {
                scheduleReminder(context, schedule)
            }

            // 2. Start WorkManager periodic safety net (every 15 minutes)
            try {
                val periodicWork = PeriodicWorkRequestBuilder<AttendancePeriodicWorker>(15, TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    AttendancePeriodicWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
                )
                Log.d(TAG, "Enqueued periodic WorkManager safety net.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enqueue periodic WorkManager", e)
            }
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun scheduleReminder(context: Context, schedule: ScheduleEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextTriggerTime = calculateNextTriggerTime(schedule) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_SCHEDULE
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_TITLE, schedule.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // AlarmClockInfo is the highest priority exact alarm on Android, waking up CPU from Doze mode
            val alarmClockInfo = AlarmManager.AlarmClockInfo(nextTriggerTime, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled alarm clock for '${schedule.title}' at: ${Date(nextTriggerTime)}")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException for setAlarmClock, falling back to setExactAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback inexact alarm", e2)
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        }
    }

    fun scheduleTestInSeconds(context: Context, schedule: ScheduleEntity, seconds: Int = 30) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (seconds * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_SCHEDULE
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_TITLE, "[UJI COBA] ${schedule.title}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (schedule.id + 9999).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled immediate test in $seconds seconds")
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    fun cancelReminder(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_SCHEDULE
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, scheduleId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Calculates the nearest upcoming trigger time matching the schedule's active days and time.
     */
    fun calculateNextTriggerTime(schedule: ScheduleEntity): Long? {
        val activeDaysMap = mapOf(
            Calendar.SUNDAY to "minggu",
            Calendar.MONDAY to "senin",
            Calendar.TUESDAY to "selasa",
            Calendar.WEDNESDAY to "rabu",
            Calendar.THURSDAY to "kamis",
            Calendar.FRIDAY to "jumat",
            Calendar.SATURDAY to "sabtu"
        )

        val scheduledDays = schedule.daysOfWeek.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        if (scheduledDays.isEmpty()) return null

        val now = com.example.util.DateTimeUtils.getJakartaCalendar()
        val candidate = com.example.util.DateTimeUtils.getJakartaCalendar().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Search through up to 8 days to find the closest active day in Asia/Jakarta timezone
        for (dayOffset in 0..7) {
            val testCal = (candidate.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            // If it's today, it must be in the future (at least 20 seconds from now)
            if (dayOffset == 0 && testCal.timeInMillis <= now.timeInMillis + 20_000) {
                continue
            }

            val dayOfWeek = testCal.get(Calendar.DAY_OF_WEEK)
            val dayName = activeDaysMap[dayOfWeek] ?: ""

            if (scheduledDays.contains(dayName)) {
                return testCal.timeInMillis
            }
        }

        return null
    }

    fun getNextExecutionSummary(schedule: ScheduleEntity): String {
        val nextTime = calculateNextTriggerTime(schedule) ?: return "Tidak ada hari aktif yang cocok"
        val now = System.currentTimeMillis()
        val diffMs = nextTime - now

        val dateStr = com.example.util.DateTimeUtils.formatJakarta("EEEE, dd MMM yyyy", nextTime)
        val timeStr = com.example.util.DateTimeUtils.formatJakarta("HH:mm", nextTime)

        val diffMinutes = diffMs / (1000 * 60)
        val diffHours = diffMinutes / 60
        val remainingMin = diffMinutes % 60

        val countdown = when {
            diffMinutes <= 0 -> "sebentar lagi"
            diffHours == 0L -> "$diffMinutes menit lagi"
            else -> "$diffHours jam $remainingMin menit lagi"
        }

        return "$dateStr pukul $timeStr WIB ($countdown)"
    }
}
