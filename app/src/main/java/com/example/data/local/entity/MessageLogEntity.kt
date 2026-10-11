package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_logs")
data class MessageLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleTitle: String = "",
    val recipient: String,         // Phone number or Group ID
    val recipientName: String = "",
    val messageText: String,
    val status: String,            // SUCCESS, FAILED, PENDING
    val responseRaw: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
