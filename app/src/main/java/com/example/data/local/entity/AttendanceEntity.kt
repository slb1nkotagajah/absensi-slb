package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: Long,
    val teacherName: String,
    val date: String,             // Format: yyyy-MM-dd
    val clockInTime: String = "",  // e.g. "07:15"
    val clockOutTime: String = "", // e.g. "15:05"
    val status: String = "BELUM_PULANG", // SUDAH_PULANG, BELUM_PULANG, IZIN, SAKIT
    val notes: String = ""
)
