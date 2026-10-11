package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.scheduler.ScheduleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AbsenApplication : Application() {

    companion object {
        const val CHANNEL_ID = "absen_reminder_channel"
        const val CHANNEL_NAME = "Pengingat Absen Guru"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Initialize DB and schedule reminders on app launch
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)
            // Ensure initial seeding if empty
            if (db.teacherDao().getTeacherCount() == 0) {
                AppDatabase.seedInitialData(db)
            }
            ScheduleManager.scheduleAllActiveReminders(applicationContext)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi status pengiriman pengingat absen pulang guru"
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
