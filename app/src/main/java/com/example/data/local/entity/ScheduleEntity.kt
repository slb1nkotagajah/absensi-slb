package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetType: String = "ALL_TEACHERS", // ALL_TEACHERS, GROUP_WA, SPECIFIC_CONTACTS
    val targetValue: String = "",           // Group ID (e.g. 120363xxx@g.us) or custom phones
    val daysOfWeek: String = "Senin,Selasa,Rabu,Kamis,Jumat",
    val hour: Int = 15,                     // 15 = 15:00
    val minute: Int = 0,
    val messageTemplate: String = "🔔 *PENGINGAT ABSEN PULANG*\n\nYth. Bapak/Ibu {nama_guru},\nMohon segera melakukan presensi absen pulang hari ini tanggal {tanggal} sebelum pukul {jam}.\n\nLink: {link_absen}\n\nTerima kasih atas dedikasi hari ini. Selamat beristirahat! 🙏",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isDayActive(dayName: String): Boolean {
        val days = daysOfWeek.split(",").map { it.trim().lowercase() }
        return days.contains(dayName.trim().lowercase())
    }
}
