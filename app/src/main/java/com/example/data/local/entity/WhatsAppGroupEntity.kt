package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_groups")
data class WhatsAppGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val groupId: String, // Format: 1203630248849@g.us atau ID grup Fonnte
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
