package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nip: String = "",
    val name: String,
    val whatsappNumber: String,
    val position: String = "Guru",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
