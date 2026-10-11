package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WhatsAppGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppGroupDao {
    @Query("SELECT * FROM whatsapp_groups ORDER BY name ASC")
    fun getAllGroupsFlow(): Flow<List<WhatsAppGroupEntity>>

    @Query("SELECT * FROM whatsapp_groups ORDER BY name ASC")
    suspend fun getAllGroups(): List<WhatsAppGroupEntity>

    @Query("SELECT * FROM whatsapp_groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: Long): WhatsAppGroupEntity?

    @Query("SELECT COUNT(*) FROM whatsapp_groups")
    suspend fun getGroupCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: WhatsAppGroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<WhatsAppGroupEntity>)

    @Update
    suspend fun updateGroup(group: WhatsAppGroupEntity)

    @Delete
    suspend fun deleteGroup(group: WhatsAppGroupEntity)

    @Query("DELETE FROM whatsapp_groups")
    suspend fun deleteAllGroups()
}
