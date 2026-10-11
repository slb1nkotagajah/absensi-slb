package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.AbsenApplication
import com.example.data.local.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.scheduler.ScheduleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_SCHEDULE = "com.example.action.TRIGGER_SCHEDULE"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_SCHEDULE_TITLE = "extra_schedule_title"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val scheduleTitle = intent.getStringExtra(EXTRA_SCHEDULE_TITLE) ?: "Pengingat Absen Pulang"

        if (scheduleId == -1L) {
            Log.w(TAG, "Received trigger with invalid scheduleId")
            return
        }

        Log.d(TAG, "Triggering background schedule: $scheduleId ($scheduleTitle)")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = AttendanceRepository(context)
                val results = repo.executeSchedule(scheduleId)

                // Show Notification
                val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                val notif = NotificationCompat.Builder(context, AbsenApplication.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("AbsenWA Guru: Pengingat Terkirim")
                    .setContentText("$scheduleTitle selesai dijalankan (${results.size} penerima).")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("$scheduleTitle selesai dijalankan:\n" + results.take(3).joinToString("\n"))
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notifManager?.notify(scheduleId.toInt(), notif)

                // Reschedule for next occurrence
                val schedule = AppDatabase.getInstance(context).scheduleDao().getScheduleById(scheduleId)
                if (schedule != null && schedule.isEnabled) {
                    ScheduleManager.scheduleReminder(context, schedule)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing background schedule", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
